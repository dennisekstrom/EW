package trading.util;

import forex.Offer;
import indicator.data.FibonacciData;
import indicator.data.RecoilData;
import indicator.data.TrendData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.math3.stat.regression.SimpleRegression;

/**
 * Contains all indicator methods to use in a strategy. Examples are trend
 * finder and fibonacci backtrace.
 * 
 * TODO change hardcoded values to in parameter for methods Should be parameters
 * set in a strategy for maximized customizability
 * 
 * @author Tobias W
 * 
 */

public class Indicator {

	/**
	 * TODO work in progress.
	 * 
	 * Interval in data number.(e.g. if H1, then interval 50 means 50 hours back
	 * in time)
	 * 
	 * From is the index where the interval should start going back.
	 * 
	 * @param interval
	 * @param history
	 */
	public static FibonacciData fibonacci(int interval, int from,
			ArrayList<Offer> history) {
		System.out.println();
		System.out.println("Starting fibbonacci");

		if (from - interval <= 0) {
			System.out.println("Interval is too large");
			FibonacciData id = new FibonacciData();
			id.setError(FibonacciData.Error.INTERVAL_TOO_LARGE);
			return id;
		}

		double minVolatilityConstant = 0.001;
		// double strengthIndicatorOfExtremes = 0.1;
		double strengthResistanceAndSupport = 0.001;
		// Make a sublist of the specified interval
		List<Offer> subList = new ArrayList<Offer>();
		subList = new ArrayList<Offer>(history.subList(from - interval, from));

		// take max and min
		Offer globalMax = getMax(subList);
		Offer globalMin = getMin(subList);

		if (globalMax == null || globalMin == null) {
			System.out
					.println("Didn't find max/min pairs, please use a bigger interval");
			FibonacciData id = new FibonacciData();
			id.setError(FibonacciData.Error.DID_NOT_FIND);
			return id;
		}

		boolean foundLocals = false;
		// check if not consolidating
		Offer highestPeak;
		Offer lowestValley;
		if (globalMax.getBidClose() - globalMin.getBidClose() > minVolatilityConstant) {

			// test with timing TODO remove this
			long startTime = System.currentTimeMillis();

			highestPeak = getHighestPeak(subList, globalMax);

			long mediumTime = System.currentTimeMillis();
			System.out.println("Elapsed time of second peak: "
					+ (mediumTime - startTime));

			long secondTime = System.currentTimeMillis();
			lowestValley = getLowestValley(subList, globalMin);
			long mediumTime2 = System.currentTimeMillis();
			System.out.println("Elapsed time of second valley: "
					+ (mediumTime2 - secondTime));

			if (highestPeak != null && lowestValley == null)
				System.out.println("Highest peak: " + highestPeak.getBidHigh()
						+ " (" + highestPeak.getTime() + ")");
			else if (highestPeak == null && lowestValley != null)
				System.out.println("Lowest valley: " + lowestValley.getBidLow()
						+ " (" + lowestValley.getTime() + ")");
			else if (highestPeak == null || lowestValley == null) {
				System.out.println("No locals!");
			} else {
				foundLocals = true;

				System.out.println("Highest peak: " + highestPeak.getBidHigh()
						+ " (" + highestPeak.getTime() + ")");
				System.out.println("Lowest valley: " + lowestValley.getBidLow()
						+ " (" + lowestValley.getTime() + ")");
			}

		} else {
			System.out
					.println("Too much consolidation for accurate fibbonacci retracement");
			FibonacciData id = new FibonacciData();
			id.setError(FibonacciData.Error.NO_ERROR);
			id.setTrend(FibonacciData.Trend.CONSOLIDATION);
			id.setResistanceRate(globalMax.getBidClose());
			id.setSupportRate(globalMin.getBidClose());
			return id;
		}

		System.out.println("Global max: " + globalMax.getBidClose() + " ("
				+ globalMax.getTime() + ")");

		System.out.println("Global min: " + globalMin.getBidClose() + " ("
				+ globalMin.getTime() + ")");

		FibonacciData id = new FibonacciData();
		id.setError(FibonacciData.Error.NO_ERROR);
		id.setTrend(FibonacciData.Trend.NO_TREND);
		id.setResistanceRate(globalMax.getBidClose());
		id.setSupportRate(globalMin.getBidClose());
		id.setFibonacciHalfRate((globalMax.getBidClose() + globalMin
				.getBidClose()) / 2);
		double difference = globalMax.getBidClose() - globalMin.getBidClose();
		double ratio = difference * 0.618;
		id.setFibonacciGoldenRateOver(globalMin.getBidClose() + ratio);
		id.setFibonacciGoldenRateUnder(globalMax.getBidClose() - ratio);

		if (foundLocals) {
			// ber?kna reliability
			if (globalMax.getBidClose() - highestPeak.getBidClose() < strengthResistanceAndSupport) {
				id.setReliability(FibonacciData.Reliability.MEDIUM);
				if (lowestValley.getBidClose() - globalMin.getBidClose() < strengthResistanceAndSupport) {
					id.setReliability(FibonacciData.Reliability.STRONG);
					System.out.println("STRONG");
				} else
					System.out.println("MEDIUM");
			} else if (lowestValley.getBidClose() - globalMin.getBidClose() < strengthResistanceAndSupport) {
				id.setReliability(FibonacciData.Reliability.MEDIUM);
				System.out.println("MEDIUM");
			}

		} else {
			id.setReliability(FibonacciData.Reliability.WEAK);
			System.out.println("WEAK");
		}

		return id;
	}

	/**
	 * Finds and returns a trend. It can either be UP, DOWN, CONSOLIDATION or NO
	 * TREND. Always check the reliability of the trend before acting, since a
	 * UP trend can behave in many different ways.
	 * 
	 * Interval, from and history argument are mandatory. Set the other
	 * constants to null if default values should be used.
	 * 
	 * @param interval
	 *            how big of a interval to consider from the history array
	 * @param from
	 *            where from the historic array it should backtrace from (size -
	 *            1 would be from latest tick)
	 * @param history
	 *            historic array
	 * @param minVolatilityConstant
	 *            the minimum rate range for which the trend is considered to be
	 *            consolidating (max - min). (Default: 0.001)
	 * @param regressionMinPosSlope
	 *            Minimum regression slope value for trend to be considered
	 *            upwards (must be positive) (Default: 0.00001)
	 * @param regressionMaxNegSlope
	 *            Maximum regression slope value for trend to be considered
	 *            downwards (must be negative) (Default: -0.00001)
	 * @param maxErrorConstant
	 *            Maximum mean square error for trend to not be considered as
	 *            strong
	 * @param rSquaredStrongReliability
	 *            Minimum R^2 value for trend to be considered as strong
	 * @param rSquaredMinimum
	 *            Minimum R^2 value for trend to be considered as non existant
	 * @return
	 */
	public static TrendData findLinearTrend(int interval, int from,
			ArrayList<Offer> history, Double minVolatilityConstant,
			Double regressionMinPosSlope, Double regressionMaxNegSlope,
			Double maxErrorConstant, Double rSquaredStrongReliability,
			Double rSquaredMinimum) {

		// If set to null, use default values
		if (minVolatilityConstant == null || minVolatilityConstant < 0)
			minVolatilityConstant = 0.001;
		if (regressionMinPosSlope == null || regressionMinPosSlope <= 0)
			regressionMinPosSlope = 0.00001;
		if (regressionMaxNegSlope == null || regressionMaxNegSlope >= 0)
			regressionMaxNegSlope = -0.00001;
		if (maxErrorConstant == null || maxErrorConstant <= 0)
			maxErrorConstant = 0.0000005;
		if (rSquaredStrongReliability == null)
			rSquaredStrongReliability = 0.5;
		if (rSquaredMinimum == null)
			rSquaredMinimum = 0.1;

		System.out.println("---FINDING TREND---");
		if (from - interval <= 0) {
			System.out.println("Interval is too large");
			TrendData id = new TrendData();
			id.setError(TrendData.Error.INTERVAL_TOO_LARGE);
			return id;
		}

		// System.out.println("Finding a trend");
		TrendData id = new TrendData();

		// Make a sublist of the specified interval
		List<Offer> subList = new ArrayList<Offer>();
		subList = new ArrayList<Offer>(history.subList(from - interval, from));
		System.out.println("Last bar date: "
				+ subList.get(subList.size() - 1).getTime());
		System.out.println("First bar date: " + subList.get(0).getTime());

		// take max and min
		Offer globalMax = getMax(subList);
		Offer globalMin = getMin(subList);

		// Linear Least Squares method
		SimpleRegression regression = new SimpleRegression();

		int xValue = 0;
		for (int i = 0; i < subList.size(); i++) {
			regression.addData(i, subList.get(i).getBidClose());
			xValue = i;
		}

		System.out.println("Trend range\n" + history.get(from).getTime());
		System.out.println("to " + history.get(from - interval).getTime());
		System.out.println("-Linear regression-");
		System.out.println("Regression slope: " + regression.getSlope());
		System.out.println("Prediction: " + regression.predict(xValue + 20));
		System.out.println("Error (Mean Square): "
				+ regression.getMeanSquareError());
		System.out.println("Slope confidence interval: "
				+ regression.getSlopeConfidenceInterval());
		System.out.println("R square: " + regression.getRSquare());

		// check if regression correlation coefficient is trust worthy and if
		// error is OK.
		// continue if error is ok
		if (regression.getMeanSquareError() > maxErrorConstant) {

			if (regression.getRSquare() < rSquaredMinimum) {
				id.setError(TrendData.Error.DID_NOT_FIND);
				id.setTrend(TrendData.Trend.NO_TREND);
				System.out
						.println("Trend: No trend (R squared is too low to rely on and error to large");
				return id;
			}
			System.out.println("Error is large, but R Squared is OK.");
		}

		// check for consolidation
		if (globalMax.getBidClose() - globalMin.getBidClose() < minVolatilityConstant) {
			id.setError(TrendData.Error.NO_ERROR);
			id.setTrend(TrendData.Trend.CONSOLIDATION);
			System.out.println("Trend: Consolidation (Volatility Constant)");
			return id;
		} else if (0 < regression.getSlope()
				&& regression.getSlope() < regressionMinPosSlope) {
			id.setError(TrendData.Error.NO_ERROR);
			id.setTrend(TrendData.Trend.CONSOLIDATION);
			System.out
					.println("Trend: Consolidation (Minimum positive regression slope)");
			return id;
		} else if (regression.getSlope() > regressionMaxNegSlope
				&& regression.getSlope() < 0) {
			id.setError(TrendData.Error.NO_ERROR);
			id.setTrend(TrendData.Trend.CONSOLIDATION);
			System.out
					.println("Trend: Consolidation (Maximum negative regression slope)");
			return id;
		}

		// make sure the max and min are on the right "side" and that the slope
		// is in the correct angle
		if (regression.getSlope() > 0) {

			// Set reliability to medium if the error is large
			if (regression.getMeanSquareError() > maxErrorConstant) {
				System.out
						.println("Reliability trend: Medium (Error too large)");
				id.setReliability(TrendData.Reliability.MEDIUM);
			} else if (subList.indexOf(globalMax) > subList.indexOf(globalMin)
					&& regression.getRSquare() > rSquaredStrongReliability) {
				id.setReliability(TrendData.Reliability.STRONG);
				System.out.println("Reliability trend: Strong");
			} else if (subList.indexOf(globalMax) < subList.indexOf(globalMin)) {
				id.setReliability(TrendData.Reliability.WEAK);
				System.out.println("Reliability trend: Weak");
			} else {
				id.setReliability(TrendData.Reliability.MEDIUM);
				System.out
						.println("Reliability trend: Medium (R squared too small for total reliability)");
			}

			id.setError(TrendData.Error.NO_ERROR);
			id.setTrend(TrendData.Trend.UP);
			System.out.println("Trend: Up");
			return id;
		} else {

			// Set reliability to medium if the error is large
			if (regression.getMeanSquareError() > maxErrorConstant) {
				System.out
						.println("Reliability trend: Medium (Error too large)");
				id.setReliability(TrendData.Reliability.MEDIUM);
			} else if (subList.indexOf(globalMax) < subList.indexOf(globalMin)
					&& regression.getRSquare() > rSquaredStrongReliability) {
				id.setReliability(TrendData.Reliability.STRONG);
				System.out.println("Reliability trend: Strong");
			} else if (subList.indexOf(globalMax) > subList.indexOf(globalMin)) {
				id.setReliability(TrendData.Reliability.WEAK);
				System.out.println("Reliability trend: Weak");
			} else {
				System.out
						.println("Reliability trend: Medium (R squared too small for total reliability)");
				id.setReliability(TrendData.Reliability.MEDIUM);
			}

			id.setError(TrendData.Error.NO_ERROR);
			id.setTrend(TrendData.Trend.DOWN);
			System.out.println("Trend: Down");
			return id;
		}
	}

	/**
	 * Takes an interval (should be relative short) and determines if, at the
	 * 'from', there's a probability of a recoil.
	 * 
	 * TODO work in progress
	 * 
	 * @param interval
	 * @param history
	 * @param minVolume
	 *            the minimum volume of the bars in the downtrend/uptrend
	 * @return
	 */
	public static RecoilData recoilPotential(int interval,
			ArrayList<Offer> history, int minVolume) {

		RecoilData rd = new RecoilData();

		// Make a sublist of the specified interval
		List<Offer> subList = new ArrayList<Offer>();
		subList = new ArrayList<Offer>(history.subList(history.size()
				- interval, history.size() - 1));

		Offer globalMax = getMax(subList);
		Offer globalMin = getMin(subList);

		if (subList.indexOf(globalMin) + 4 >= subList.size() - 1) {
			// Potential recoil up
			rd.setRecoil(RecoilData.Recoil.UP);
			List<Offer> downFallList = subList.subList(subList.size() - 1
					- subList.indexOf(globalMax), subList.size() - 1);
			for (Offer bar : downFallList) {
				if (bar.getVolume() >= minVolume) {
					downFallList = downFallList.subList(
							downFallList.indexOf(bar), downFallList.size() - 1);
					break;
				}
				if (downFallList.indexOf(bar) == downFallList.size() - 1) {
					// No volumes is over the min volume, which means there's no
					// fall
					rd.setReliability(RecoilData.Reliability.NO_RELIABILITY);
					return rd;
				}

			}

			if (downFallList.size() > 15) {
				// Low recoil potential
			} else {
				boolean allApprovedVolumes = true;
				for (Offer bar : downFallList) {
					if (bar.getVolume() >= minVolume)
						continue;
					else {
						allApprovedVolumes = false;
						rd.setReliability(RecoilData.Reliability.WEAK);
						break;
					}
				}
				if (allApprovedVolumes)
					rd.setReliability(RecoilData.Reliability.MEDIUM);

			}

		} else if (subList.indexOf(globalMax) + 4 >= subList.size() - 1) {
			// Potential recoil down
			rd.setRecoil(RecoilData.Recoil.DOWN);
			// TODO
		}

		return null;

	}

	/**
	 * Get the highest peak in the list. A peak is defined inside the method.
	 * Basically it's what it sounds like, and this method returns the highest
	 * of those peaks. This method is often more useful than getMax() since max
	 * values often can be close to the end/beginning of the list.
	 * 
	 * @param list
	 * @param startGuess
	 *            the start point of the recursion. A good idea is to use the
	 *            max value for the list
	 * @return
	 */

	public static Offer getHighestPeak(List<Offer> list, Offer startGuess) {
		if (peakCondition(list, startGuess))
			return startGuess;
		else {
			int recursionCounter = 0;
			if (list.indexOf(startGuess) < 10
					|| list.indexOf(startGuess) > list.size() - 10)
				return getPeak(list, list.get(list.size() / 2),
						recursionCounter);
			return getPeak(list, startGuess, recursionCounter);
		}

	}

	private static Offer getPeak(List<Offer> list, Offer splitPoint,
			int recursionCounter) {
		if (recursionCounter > 100) {
			System.out.println("Couldn't find more peaks");
			return null;
		}
		recursionCounter++;
		Offer peak;
		// get sublists of each side of the max
		List<Offer> leftMaxList = new ArrayList<Offer>();

		if (list.indexOf(splitPoint) < 0) {
			System.out.println("Split point index is under 0");
			return null;
		}
		leftMaxList = new ArrayList<Offer>(list.subList(0,
				list.indexOf(splitPoint)));

		List<Offer> rightMaxList = new ArrayList<Offer>();
		rightMaxList = new ArrayList<Offer>(list.subList(
				list.indexOf(splitPoint), list.size() - 1));

		Offer leftMax = getMax(leftMaxList);
		Offer rightMax = getMax(rightMaxList);

		if ((leftMax == null && rightMax == null) || list.size() < 20) {
			System.out.println("Didn't find any peaks");
			return null;
		}

		if (leftMax == null && rightMax != null)
			peak = rightMax;
		else if (leftMax != null && rightMax == null)
			peak = leftMax;
		else if (leftMax.getBidHigh() > rightMax.getBidHigh())
			peak = leftMax;
		else
			peak = rightMax;

		// If peak conditions are fulfilled, return the guess.
		// Otherwise use recursion to test more possibilities
		int indexOfBottom = list.indexOf(peak);
		if (peakCondition(list, peak))
			return peak;
		else {
			List<Offer> newList = new ArrayList<Offer>();
			for (Offer d : list)
				newList.add(null);

			Collections.copy(newList, list);
			newList.remove(indexOfBottom);
			return getPeak(newList, splitPoint, recursionCounter);
		}

	}

	private static boolean peakCondition(List<Offer> list, Offer guess) {
		// Linear Least Squares method
		SimpleRegression regression = new SimpleRegression();

		int indexOfTop = list.indexOf(guess);
		double observationAtIndexOfTop = list.get(indexOfTop).getBidHigh();
		for (int i = 0; i < list.size(); i++) {
			regression.addData(i, list.get(i).getBidHigh());
		}

		// How much this calculated top differ from the regression line
		double estimateAtIndexTop = regression.getIntercept()
				+ regression.getSlope() * indexOfTop;
		double errorOfEstimate = Math.abs(observationAtIndexOfTop
				- estimateAtIndexTop);

		if (indexOfTop - 18 > 0 && indexOfTop + 18 < list.size() - 1) {

			// Check so that the top is surrounded by values which are lower
			for (int i = indexOfTop - 15; i < indexOfTop + 15; i++) {
				if (i == indexOfTop)
					continue;
				else {
					if (list.get(i).getBidHigh() > list.get(indexOfTop)
							.getBidHigh()) {
						// The top is really not a top
						return false;
					}

				}
			}
		} else {
			return false;
		}

		return true;
	}

	// private static int recursionCounterValley = 0;

	/**
	 * Get the lowest valley in the list. A valley is defined inside the method.
	 * Basically it's what it sounds like, and this method returns the lowest of
	 * those valleys. This method is often more useful than getMin() since min
	 * values often can be close to the end/beginning of the list.
	 * 
	 * @param list
	 * @param startGuess
	 *            the start point of the recursion. A good idea is to use the
	 *            min value for the list
	 * @return
	 */
	public static Offer getLowestValley(List<Offer> list, Offer startGuess) {
		if (valleyCondition(list, startGuess))
			return startGuess;
		else {
			int recursionCounter = 0;
			if (list.indexOf(startGuess) < 10
					|| list.indexOf(startGuess) > list.size() - 10)
				return getValley(list, list.get(list.size() / 2),
						recursionCounter);
			return getValley(list, startGuess, recursionCounter);
		}

	}

	private static Offer getValley(List<Offer> list, Offer splitPoint,
			int recursionCounter) {
		if (recursionCounter > 100) {
			System.out.println("Couldn't find more valleys");
			return null;
		}

		recursionCounter++;
		Offer valley;
		// get sublists of each side of the max
		List<Offer> leftMinList = new ArrayList<Offer>();
		int indexOfSplitPoint = list.indexOf(splitPoint);
		if (indexOfSplitPoint < 0) {
			System.out.println("Split point index is under 0");
			return null;
		}
		leftMinList = new ArrayList<Offer>(list.subList(0, indexOfSplitPoint));
		List<Offer> rightMinList = new ArrayList<Offer>();
		rightMinList = new ArrayList<Offer>(list.subList(indexOfSplitPoint,
				list.size() - 1));

		Offer leftMin = getMin(leftMinList);
		Offer rightMin = getMin(rightMinList);

		if ((leftMin == null && rightMin == null) || list.size() < 20) {
			System.out.println("Didn't find any more valleys");
			return null;
		}

		if (leftMin == null && rightMin != null)
			valley = rightMin;
		else if (leftMin != null && rightMin == null)
			valley = leftMin;
		else if (leftMin.getBidLow() > leftMin.getBidLow())
			valley = leftMin;
		else
			valley = rightMin;

		// If valley conditions are fulfilled, return the guess.
		// Otherwise use recursion to test more possibilities
		int indexOfBottom = list.indexOf(valley);
		if (valleyCondition(list, valley))
			return valley;
		else {
			List<Offer> newList = new ArrayList<Offer>();
			for (Offer d : list)
				newList.add(null);

			Collections.copy(newList, list);
			newList.remove(indexOfBottom);
			return getValley(newList, splitPoint, recursionCounter);
		}

	}

	// Conditions for a valley
	private static boolean valleyCondition(List<Offer> list, Offer guess) {
		// Linear Least Squares method
		SimpleRegression regression = new SimpleRegression();

		int indexOfBottom = list.indexOf(guess);
		double observationAtIndexOfBottom = list.get(indexOfBottom).getBidLow();
		for (int i = 0; i < list.size(); i++) {
			regression.addData(i, list.get(i).getBidLow());
		}

		// How much this calculated top differ from the regression line
		double estimateAtIndexBottom = regression.getIntercept()
				+ regression.getSlope() * indexOfBottom;
		double errorOfEstimate = Math.abs(observationAtIndexOfBottom
				- estimateAtIndexBottom);

		// Check so that the bottom is surrounded by values which are higher
		if (indexOfBottom - 18 > 0 && indexOfBottom + 18 < list.size() - 1) {
			for (int i = indexOfBottom - 15; i < indexOfBottom + 15; i++) {
				if (i == indexOfBottom)
					continue;
				else {
					if (list.get(i).getBidLow() < list.get(indexOfBottom)
							.getBidLow()) {
						// The bottom is really not a bottom
						return false;
					}

				}
			}
		} else {
			// The bottom is really not a bottom
			return false;
		}

		return true;
		// if (errorOfEstimate > 0.0006)
		// return true;
		// else
		// return false;
	}

	/**
	 * Returns all peaks from the given list. It's sorted with the lowest rate
	 * value first and the highest last. So if you want the lowest peak, use the
	 * index 0 element for example.
	 * 
	 * @param subList
	 * @return
	 */
	public static List<Offer> getAllPeaks(List<Offer> subList) {
		List<Offer> newList = new ArrayList<Offer>();
		for (Offer d : subList)
			newList.add(null);

		Collections.copy(newList, subList);

		List<Offer> peakList = new ArrayList<Offer>();
		List<Integer> peakIndexList = new ArrayList<Integer>();

		Offer peak;
		do {
			peak = getHighestPeak(newList, getMax(newList));
			if (newList.indexOf(peak) < 0)
				break;
			int indexOfPeak = newList.indexOf(peak);
			boolean isCloseToAnotherPeak = false;
			for (int index : peakIndexList) {
				if ((indexOfPeak < index && index - 15 < indexOfPeak)
						|| (indexOfPeak > index && index + 15 > indexOfPeak)
						|| (indexOfPeak == index)) {
					isCloseToAnotherPeak = true;
					break;
				}

			}
			if (!isCloseToAnotherPeak && peakCondition(subList, peak)) {
				peakIndexList.add(newList.indexOf(peak));
				peakList.add(peak);
			}
			newList.remove(peak);

		} while (peakList.size() < 50);

		Collections.sort(peakList, new Comparator<Offer>() {

			@Override
			public int compare(Offer a, Offer b) {
				if (a.getBidHigh() < b.getBidHigh())
					return 0;
				else
					return 1;
			}

		});

		return peakList;
	}

	/**
	 * Returns all valleys in the given list. It's sorted with the lowest rate
	 * value first and highest last. So if you want the lowest valley, use the
	 * index 0 element for example.
	 * 
	 * @param subList
	 * @return
	 */
	public static List<Offer> getAllValleys(List<Offer> subList) {
		List<Offer> newList = new ArrayList<Offer>();
		for (Offer d : subList)
			newList.add(null);

		Collections.copy(newList, subList);

		List<Offer> valleyList = new ArrayList<Offer>();
		List<Integer> valleyIndexList = new ArrayList<Integer>();

		Offer valley;
		do {
			valley = getLowestValley(newList, getMin(newList));
			if (newList.indexOf(valley) < 0)
				break;
			int indexOfValley = newList.indexOf(valley);
			boolean isCloseToAnotherValley = false;
			for (int index : valleyIndexList) {
				if ((indexOfValley < index && index - 15 < indexOfValley)
						|| (indexOfValley > index && index + 15 > indexOfValley)
						|| (indexOfValley == index)) {
					isCloseToAnotherValley = true;
					break;
				}

			}
			if (!isCloseToAnotherValley && valleyCondition(subList, valley)) {
				valleyIndexList.add(newList.indexOf(valley));
				valleyList.add(valley);
			}
			newList.remove(valley);

		} while (valleyList.size() < 50);

		Collections.sort(valleyList, new Comparator<Offer>() {

			@Override
			public int compare(Offer a, Offer b) {
				if (a.getBidHigh() < b.getBidHigh())
					return 0;
				else
					return 1;
			}

		});

		return valleyList;
	}

	/**
	 * Gets the max of the array, but ignores the rand extremes. Returns null of
	 * it didn't find any max.
	 * 
	 * @param list
	 * @return
	 */
	public static Offer getMax(List<Offer> list) {
		Double max = null;
		List<Double> maxList = new ArrayList<Double>();
		List<Double> tempMaxList = new ArrayList<Double>();

		for (int i = 0; i < list.size(); i++) {
			maxList.add(list.get(i).getBidHigh());
			tempMaxList.add(list.get(i).getBidHigh());
		}

		boolean maxFound = false;
		while (!maxFound) {
			if (tempMaxList.size() <= 1)
				return null;
			max = Collections.max(tempMaxList);
			// if the max is a rand, then don't count and continue searching
			if (tempMaxList.indexOf(max) == tempMaxList.size() - 1
					|| tempMaxList.indexOf(max) == 0) {
				maxFound = false;
				tempMaxList.remove(max);
			} else
				maxFound = true;

		}

		return list.get(maxList.indexOf(max));

	}

	/**
	 * Gets the min of the array, but ignores the rand extremes. Returns null of
	 * it didn't find any min.
	 * 
	 * @param list
	 * @return
	 */
	public static Offer getMin(List<Offer> list) {
		Double min = null;
		List<Double> minList = new ArrayList<Double>();
		List<Double> tempMinList = new ArrayList<Double>();

		for (int i = 0; i < list.size(); i++) {
			minList.add(list.get(i).getBidLow());
			tempMinList.add(list.get(i).getBidLow());
		}

		boolean minFound = false;
		while (!minFound) {
			if (tempMinList.size() <= 1)
				return null;
			min = Collections.min(tempMinList);
			// if the min is a rand, then don't count and continue searching
			if (tempMinList.indexOf(min) == tempMinList.size() - 1
					|| tempMinList.indexOf(min) == 0) {
				minFound = false;
				tempMinList.remove(min);
			} else
				minFound = true;

		}

		return list.get(minList.indexOf(min));

	}

	/**
	 * Get avarage double in a List
	 */

	private static double average(List<Offer> list) {
		if (list == null || list.isEmpty())
			return 0.0;

		double sum = 0;

		for (int i = 0; i < list.size(); i++)
			sum += list.get(i).getBidClose();

		return sum / list.size();

	}

}
