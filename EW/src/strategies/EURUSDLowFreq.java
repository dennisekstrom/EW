package strategies;

import indicator.data.FibonacciData;
import indicator.data.TrendData;

import java.util.ArrayList;

import trading.util.Event;
import trading.util.Indicator;
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

public class EURUSDLowFreq extends Strategy {

	private static final String STRATEGY_NAME = "Low frequency trend strategy";

	private static final int HISTORY_MINUTES_BACKTRACE = 60 * 24 * 5; //one day
	private static final int BACKTRACE_BAR_AMOUNT = 200;
	private static final String TIME_FRAME = "m5";
	private static final String STRAT_INSTRUMENT = "EUR/USD";
	private static final String INSTRUMENT = "EUR/USD";

	//Total gross of closed trades with the strategy
	private long totalGross = 0;

	private boolean awaitingEventResult = true;
	private boolean eventIsLive = false;
	private boolean fetchingEvent = false;
	private boolean stopLossMovedToProfit = false;
	private ArrayList<Event> liveEvents = new ArrayList<Event>();
	private int ticksBetweenCheck = 400;
	private final String closingMessage = null;

	//Handles all trading related calls
	private final TradeController tradeController;

	private double fibResistanceRate;
	private double fibSupportRate;

	public EURUSDLowFreq(StrategyController strategy,
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
	public void onTick(Offer data) {
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
		this.liveEvents = liveEvents;
		eventIsLive = true;
		awaitingEventResult = true;
	}

	@Override
	public void onEventPassed() {
		eventIsLive = false;
		// TODO Auto-generated method stub

	}

	//	@Override
	//	public O2GTradeRow getCurrentTrade() {
	//		return currentTrade;
	//	}

	@Override
	public void notifyClosedTrade(O2GClosedTradeRow trade) {
		sendEmailNotification("tob.wikstrom@gmail.com", "Closed trade",
				"BuySell: " + trade.getBuySell() + "\n" + "EUR/USD" + "\n"
						+ "Open time: " + trade.getOpenTime().getTime() + "\n"
						+ "Close Time: " + trade.getCloseTime().getTime()
						+ "P/L: " + trade.getGrossPL() + "EUR\n"
						+ closingMessage);
		ticksBetweenCheck = 400 * 5;
		counter = 1;

		totalGross += trade.getGrossPL();

	}

	@Override
	public void strategyAlgorithm(OfferData row) {
		//Handle live events
		handleLiveEvents();

		//An order strategy		
		double resistanceDistanceFromPosition = 0.002;
		double supportDistanceFromPosition = 0.002;
		double limit = 0;
		double stopLoss = 0;

		if (currentTrade == null && counter % ticksBetweenCheck == 0) {

			System.out.println();
			System.out.println("--");
			System.out.println(STRATEGY_NAME);
			System.out.println("--");

			FibonacciData fibData = Indicator.fibonacci(BACKTRACE_BAR_AMOUNT,
					historicData.size() - 1, historicData);

			TrendData trendData = Indicator.findLinearTrend(
					BACKTRACE_BAR_AMOUNT, historicData.size() - 1,
					historicData, null, null, null, null, null, null);

			//criterias for opening position
			if (fibData.getTrend() != FibonacciData.Trend.CONSOLIDATION) {
				fibResistanceRate = fibData.getResistanceRate();
				fibSupportRate = fibData.getSupportRate();

				//Do something if the upcoming events are EUR/USD concerned
				//				for (Event event : upcomingEvents) {
				//					if (event.getInstrument().contains("EUR")
				//							|| event.getInstrument().contains("USD")) {
				//						//do something
				//					}
				//
				//				}

				//Only trade if fibonacci backtrace is reliable
				if (fibData.getReliability() == FibonacciData.Reliability.STRONG
						&& !eventIsLive) {

					//BUY CONDITIONS
					if (trendData.getTrend() == TrendData.Trend.UP
							&& trendData.getReliability() == TrendData.Reliability.STRONG) {
						System.out.println("UP TREND (STRONG)");
						limit = row.getBidClose()
								+ (row.getBidClose()
										- fibData.getFibonacciHalfRate() + 0.0021);
						stopLoss = (fibData.getFibonacciGoldenRateOver());
						tradeController.openPosition("EUR/USD", "B", 20000,
								limit, stopLoss, this);
						sendEmailNotification("tob.wikstrom@gmail.com",
								"Long trade " + STRATEGY_NAME,
								"Long position (Strong) \n" + "EUR/USD Rate: "
										+ row.getBidClose() + "\n" + "Limit: "
										+ limit + " Stop: " + stopLoss + "\n"
										+ "Time: " + row.getTime().getTime());

					}

					//SELL CONDITIONS
					if (trendData.getTrend() == TrendData.Trend.DOWN
							&& trendData.getReliability() == TrendData.Reliability.STRONG) {
						System.out.println("DOWN TREND (STRONG)");
						limit = row.getBidClose()
								- (fibData.getFibonacciHalfRate()
										- row.getBidClose() - 0.0021);
						stopLoss = (fibData.getFibonacciGoldenRateUnder());
						tradeController.openPosition("EUR/USD", "S", 20000,
								limit, stopLoss, this);
						sendEmailNotification("tob.wikstrom@gmail.com",
								"Short trade " + STRATEGY_NAME,
								"Short position (Strong) \n" + "EUR/USD Rate: "
										+ row.getBidClose() + "\n" + "Limit: "
										+ limit + " Stop: " + stopLoss + "\n"
										+ "Time: " + row.getTime().getTime());
					}

				} else if (fibData.getReliability() != FibonacciData.Reliability.STRONG
						&& !eventIsLive) {

					//BUY CONDITION
					if (trendData.getTrend() == TrendData.Trend.UP
							&& trendData.getReliability() == TrendData.Reliability.STRONG) {
						System.out.println("UP TREND (MEDIUM)");
						limit = row.getBidClose() + 0.0014;
						stopLoss = row.getAskClose() - 0.0009;
						tradeController.openPosition("EUR/USD", "B", 5000,
								limit, stopLoss, this);
						sendEmailNotification("tob.wikstrom@gmail.com",
								"Long trade " + STRATEGY_NAME,
								"Long position (Medium) \n" + "EUR/USD Rate: "
										+ row.getBidClose() + "\n" + "Limit: "
										+ limit + " Stop: " + stopLoss + "\n"
										+ "Time: " + row.getTime().getTime());
					}

					//SELL CONDITION
					if (trendData.getTrend() == TrendData.Trend.DOWN
							&& trendData.getReliability() == TrendData.Reliability.STRONG) {
						System.out.println("DOWN TREND (MEDIUM)");
						limit = row.getBidClose() - 0.0014;
						stopLoss = row.getAskClose() + 0.0009;
						tradeController.openPosition("EUR/USD", "S", 5000,
								limit, stopLoss, this);
						sendEmailNotification("tob.wikstrom@gmail.com",
								"Short trade " + STRATEGY_NAME,
								"Short position (Medium) \n" + "EUR/USD Rate: "
										+ row.getBidClose() + "\n" + "Limit: "
										+ limit + " Stop: " + stopLoss + "\n"
										+ "Time: " + row.getTime().getTime());
					}

				}
				//				} else
				//					System.out
				//							.println("Resistance or support (fibonacci) is too close to opening rate");
			}

			if (trendData.getTrend() == TrendData.Trend.CONSOLIDATION)
				System.out.println("CONSOLIDATION");
			counter = 1;
			ticksBetweenCheck = 400;
		}
		//		else if (currentTrade != null && counter % ticksBetweenCheck / 8 == 0) {
		//			IndicatorData trendData = Indicator.findTrend(BACKTRACE_BAR_AMOUNT,
		//					historicData.size() - 1, historicData);
		//
		//			if (trendData.getTrend() == IndicatorData.Trend.UP
		//					&& currentTrade.getBuySell().equals("S")) {
		//				tradeController.closePosition(currentTrade);
		//				closingMessage = "Up trend with short position, time to close";
		//			} else if (trendData.getTrend() == IndicatorData.Trend.DOWN
		//					&& currentTrade.getBuySell().equals("B")) {
		//				tradeController.closePosition(currentTrade);
		//				closingMessage = "Down trend with long position, time to close";
		//			}
		//
		//		}
		counter++;

		//If rate comes close to the limit, move the stop to avoid any unnecessary losses
		if (!stopLossMovedToProfit) {

			if (currentTrade != null && currentTrade.getBuySell().equals("B")) {
				if (row.getBidClose() >= currentTrade.getOpenRate() + 0.0006) {
					//move stop loss up
					System.out.println("Stop loss was moved up");
					tradeController.editStopLoss(
							currentTrade.getOpenRate() + 0.0003,
							stopLossOrderID, this);
					sendEmailNotification("tob.wikstrom@gmail.com",
							"Stop loss moved up " + STRATEGY_NAME, "Time: "
									+ row.getTime().getTime());
					stopLossMovedToProfit = true;
				}
			} else if (currentTrade != null
					&& currentTrade.getBuySell().equals("S")) {
				if (row.getBidClose() <= currentTrade.getOpenRate() - 0.0006) {
					//move stoploss down
					System.out.println("Stop loss was moved down");
					tradeController.editStopLoss(
							currentTrade.getOpenRate() - 0.0003,
							stopLossOrderID, this);
					sendEmailNotification("tob.wikstrom@gmail.com",
							"Stop loss moved down " + STRATEGY_NAME, "Time: "
									+ row.getTime().getTime());
					stopLossMovedToProfit = true;
				}
			}
		}

		//end of order strategy

	}

	private void handleLiveEvents() {
		//Handle live events
		//		if (eventIsLive && awaitingEventResult && !fetchingEvent) {
		//
		//			for (Event event : liveEvents) {
		//				fetchingEvent = true;
		//				//TODO göra i en annan tråd?
		//				Event eventResult = getEventResult(event);
		//				fetchingEvent = false;
		//
		//				System.out.println(eventResult.getTitle() + " --- "
		//						+ eventResult.getInstrument());
		//				//				System.out.println("Impact: " + eventResult.getImpact());
		//				//				System.out.println("Time left: " + eventResult.getTime());
		//				//				System.out
		//				//						.println("Instrument: " + eventResult.getInstrument());
		//
		//				if (eventResult.getResult() == EventResult.NONE) {
		//					System.out.println("Result: NOT FOUND YET");
		//					awaitingEventResult = true;
		//				} else if (eventResult.getResult() == EventResult.BETTER) {
		//					//gör något om det är bättre då
		//					System.out.println("Result: BETTER");
		//					awaitingEventResult = false;
		//				} else if (eventResult.getResult() == EventResult.WORSE) {
		//					//gör något om det är sämre då
		//					System.out.println("Result: WORSE");
		//					awaitingEventResult = false;
		//				} else if (eventResult.getResult() == EventResult.UNCHANGED) {
		//					System.out.println("UNCHANGED");
		//					//awaitingEventResult = false;
		//				}
		//			}
		//
		//		}
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
	public Reliability getDefaultReliability() {
		// TODO Auto-generated method stub
		return Reliability.INTERMEDIATE;
	}

	@Override
	public Reliability getFundamentalReliability() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Reliability getConsolidationReliability() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Reliability getHighVolatilityReliability() {
		// TODO Auto-generated method stub
		return null;
	}

}
