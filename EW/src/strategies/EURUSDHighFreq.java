package strategies;

import indicator.data.TrendData;

import java.util.ArrayList;
import java.util.List;

import trading.util.Event;
import trading.util.Indicator;
import trading.util.OfferData;
import trading.Strategy;
import trading.StrategyController;
import trading.TradeController;

import com.fxcore2.O2GClosedTradeRow;
import com.fxcore2.O2GOfferTableRow;

/**
 * A Strategy sample. onTick is called for every received tick from fxcm.
 * 
 * Inherits variables such as historicData and stratControl
 * 
 * 
 * @author Tobias W
 * 
 */

public class EURUSDHighFreq extends Strategy {

	private static final String STRATEGY_NAME = "High frequency trade strategy";

	private static final int HISTORY_MINUTES_BACKTRACE = 60 * 24; //one day
	private static final int BACKTRACE_BAR_AMOUNT = 50;
	private static final int BACKTRACE_BAR_AMOUNT_PEAKS_VALLEYS = 100;
	private static final String TIME_FRAME = "m5";
	private static final String STRAT_INSTRUMENT = "EUR/USD";
	private static final String INSTRUMENT = "EUR/USD";

	//Total gross of closed trades with the strategy
	private long totalGross = 0;

	private int ticksBetweenCheck = 100;
	private final String closingMessage = null;

	//Handles all trading related calls
	private final TradeController tradeController;

	public EURUSDHighFreq(StrategyController strategy,
			TradeController tradeController) {
		super(strategy, tradeController, STRATEGY_NAME);
		this.tradeController = tradeController;
	}

	private int counter = 0;

	//	private final int count = 0;

	//Live feed
	@Override
	public void onTick(O2GOfferTableRow row) {
		//		strategyAlgoritm(row);
	}

	//Backtrace feed
	@Override
	public void onTick(OfferData data) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStart() {
		System.out.println("Started " + STRATEGY_NAME);

		historicData = getHistoricData(STRAT_INSTRUMENT, TIME_FRAME,
				HISTORY_MINUTES_BACKTRACE);

	}

	@Override
	public void onContinue() {
		System.out.println("Resumed " + STRATEGY_NAME);

	}

	@Override
	public void onPause() {
		System.out.println("Paused " + STRATEGY_NAME);

	}

	@Override
	public void onEventLive(ArrayList<Event> liveEvents) {
	}

	@Override
	public void onEventPassed() {

	}

	@Override
	public void notifyClosedTrade(O2GClosedTradeRow trade) {
		sendEmailNotification("tob.wikstrom@gmail.com", "Closed trade "
				+ STRATEGY_NAME, "BuySell: " + trade.getBuySell() + "\n"
				+ "EUR/USD" + "\n" + "Open time: "
				+ trade.getOpenTime().getTime() + "\n" + "Close Time: "
				+ trade.getCloseTime().getTime() + "P/L: " + trade.getGrossPL()
				+ "EUR\n" + closingMessage);
		counter = 1;

		totalGross += trade.getGrossPL();

	}

	@Override
	public void strategyAlgorithm(OfferData row) {
		//An order strategy		
		if (currentTrade == null && counter % ticksBetweenCheck == 0) {

			System.out.println();
			System.out.println("--");
			System.out.println(STRATEGY_NAME);
			System.out.println("--");

			TrendData trendData = Indicator.findLinearTrend(
					BACKTRACE_BAR_AMOUNT, historicData.size() - 1,
					historicData, null, null, null, null, null, null);

			if (trendData.getTrend() == TrendData.Trend.UP
					&& (trendData.getReliability() == TrendData.Reliability.MEDIUM || trendData
							.getReliability() == TrendData.Reliability.STRONG)) {
				List<OfferData> subList = historicData.subList(
						historicData.size()
								- BACKTRACE_BAR_AMOUNT_PEAKS_VALLEYS,
						historicData.size() - 1);

				double limit = Indicator.getHighestPeak(subList,
						Indicator.getMax(subList)).getBidHigh();
				List<OfferData> valleys = Indicator.getAllValleys(subList);
				double stopLoss = valleys.get(valleys.size() - 1).getBidLow();

				tradeController.openPosition("EUR/USD", "B", 20000, limit,
						stopLoss, this);
				sendEmailNotification("tob.wikstrom@gmail.com", "Long trade "
						+ STRATEGY_NAME, "Long position \n" + "EUR/USD Rate: "
						+ row.getBidClose() + "\n" + "Limit: " + limit
						+ " Stop: " + stopLoss + "\n" + "Time: "
						+ row.getTime().getTime());

			} else if (trendData.getTrend() == TrendData.Trend.DOWN
					&& (trendData.getReliability() == TrendData.Reliability.MEDIUM || trendData
							.getReliability() == TrendData.Reliability.STRONG)) {
				List<OfferData> subList = historicData.subList(
						historicData.size()
								- BACKTRACE_BAR_AMOUNT_PEAKS_VALLEYS,
						historicData.size() - 1);

				List<OfferData> peaks = Indicator.getAllPeaks(subList);

				double stopLoss = peaks.get(0).getBidHigh();
				double limit = Indicator.getLowestValley(subList,
						Indicator.getMin(subList)).getAskLow();

				System.out.println("Peak: " + stopLoss);
				tradeController.openPosition("EUR/USD", "S", 20000, limit,
						stopLoss, this);
				sendEmailNotification("tob.wikstrom@gmail.com", "Short trade "
						+ STRATEGY_NAME, "Short position \n" + "EUR/USD Rate: "
						+ row.getBidClose() + "\n" + "Limit: " + limit
						+ " Stop: " + stopLoss + "\n" + "Time: "
						+ row.getTime().getTime());

			}

			counter = 1;
		}
		counter++;

		//end of order strategy

	}

	@Override
	public String getTimeFrame() {
		return TIME_FRAME;
	}

	@Override
	public String getInstrument() {
		return INSTRUMENT;
	}

	@Override
	public double getTotalGross() {
		return totalGross;
	}

	@Override
	public strategies.Reliability getDefaultReliability() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public strategies.Reliability getFundamentalReliability() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public strategies.Reliability getConsolidationReliability() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public strategies.Reliability getHighVolatilityReliability() {
		// TODO Auto-generated method stub
		return null;
	}

}
