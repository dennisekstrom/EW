package strategies;

import indicator.data.TrendData;

import java.util.ArrayList;

import com.fxcore2.O2GClosedTradeRow;
import com.fxcore2.O2GOfferTableRow;

import trading.util.Event;
import trading.util.Indicator;
import trading.util.OfferData;
import trading.Strategy;
import trading.StrategyController;
import trading.TradeController;
import trading.util.Event.EventImpact;
import trading.util.Event.EventResult;

public class EURUSDEvents extends Strategy {

	private static final String STRATEGY_NAME = "Event EUR/USD strategy";
	private static final int HISTORY_MINUTES_BACKTRACE = 60 * 24 * 5; //one day
	private static final String TIME_FRAME = "m5";
	private static final String STRAT_INSTRUMENT = "EUR/USD";
	private static final String INSTRUMENT = "EUR/USD";

	private static final int BACKTRACE_BAR_AMOUNT = 100;

	//Total gross of closed trades with the strategy
	private long totalGross = 0;

	private boolean awaitingEventResult = true;
	private boolean eventIsLive = false;
	private boolean fetchingEvent = false;
	private ArrayList<Event> liveEvents = new ArrayList<Event>();
	private int ticksBetweenCheck = 400;
	private final String closingMessage = null;

	//Handles all trading related calls
	private final TradeController tradeController;

	private int counter = 0;

	private TrendData.Trend currentTrend;

	public EURUSDEvents(StrategyController stratControl,
			TradeController tradeController) {
		super(stratControl, tradeController, STRATEGY_NAME);
		this.tradeController = tradeController;

	}

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
	public Reliability getDefaultReliability() {
		// TODO Auto-generated method stub
		return Reliability.INTERMEDIATE;
	}

	@Override
	public Reliability getFundamentalReliability() {
		// TODO Auto-generated method stub
		return Reliability.WEAK;
	}

	@Override
	public Reliability getConsolidationReliability() {
		// TODO Auto-generated method stub
		return Reliability.INTERMEDIATE;
	}

	@Override
	public Reliability getHighVolatilityReliability() {
		// TODO Auto-generated method stub
		return Reliability.INTERMEDIATE;
	}

	@Override
	public void onEventLive(ArrayList<Event> liveEvents) {
		this.liveEvents = liveEvents;
		eventIsLive = true;
		awaitingEventResult = true;
		System.out.println("EURUSDEvents received live event!");
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
		sendEmailNotification(
				"tob.wikstrom@gmail.com",
				"Closed trade",
				STRATEGY_NAME + "BuySell: " + trade.getBuySell() + "\n"
						+ "EUR/USD" + "\n" + "Open time: "
						+ trade.getOpenTime().getTime() + "\n" + "Close Time: "
						+ trade.getCloseTime().getTime() + "P/L: "
						+ trade.getGrossPL() + "EUR\n" + closingMessage);
		counter = 1;

		totalGross += trade.getGrossPL();

	}

	@Override
	public void strategyAlgorithm(OfferData row) {
		//An order strategy		

		handleLiveEvents(row);

		if (currentTrade == null && counter % ticksBetweenCheck == 0) {

			System.out.println();
			System.out.println("--");
			System.out.println(STRATEGY_NAME);
			System.out.println("--");

			TrendData trendData = Indicator.findLinearTrend(
					BACKTRACE_BAR_AMOUNT, historicData.size() - 1,
					historicData, null, null, null, null, null, null);

			if (trendData.getTrend() == TrendData.Trend.CONSOLIDATION)
				currentTrend = TrendData.Trend.CONSOLIDATION;
			else if (trendData.getTrend() == TrendData.Trend.UP)
				currentTrend = TrendData.Trend.DOWN;
			else if (trendData.getTrend() == TrendData.Trend.DOWN)
				currentTrend = TrendData.Trend.DOWN;
			else
				currentTrend = TrendData.Trend.NO_TREND;

			counter = 1;
			ticksBetweenCheck = 400;
		}

		counter++;

		//end of order strategy

	}

	private void handleLiveEvents(OfferData row) {
		//Handle live events
		if (eventIsLive && awaitingEventResult && !fetchingEvent) {
			boolean sentEmail = false;
			for (Event event : liveEvents) {
				fetchingEvent = true;
				//TODO göra i en annan tråd?
				System.out.println(STRATEGY_NAME + " is fetching event maybe "
						+ event.getInstrument());
				if (event.getInstrument() != "EUR"
						&& event.getInstrument() != "USD")
					continue;
				System.out.println(STRATEGY_NAME + " is fetching event!");
				Event eventResult = getEventResult(event);
				fetchingEvent = false;

				System.out.println("Strat två event: " + eventResult.getTitle()
						+ " --- " + eventResult.getInstrument());

				if (!sentEmail) {
					sendEmailNotification(
							"tob.wikstrom@gmail.com",
							"Live EURUSD event " + STRATEGY_NAME,
							eventResult.getTitle() + " --- "
									+ eventResult.getInstrument());
					sentEmail = true;

				}

				if (eventResult.getInstrument().equals("EUR")
						&& eventResult.getImpact() == EventImpact.HIGH) {
					if (eventResult.getResult() == EventResult.NONE) {
						System.out.println("Result: NOT FOUND YET");
						awaitingEventResult = true;
					} else if (eventResult.getResult() == EventResult.BETTER) {
						System.out.println("Result: BETTER");
						tradeController.openPosition("EUR/USD", "B", 20000,
								row.getBidClose() + 0.001,
								row.getBidClose() - 0.0005, this);
						sendEmailNotification(
								"tob.wikstrom@gmail.com",
								"Long trade - Event Strategy",
								"Long position \n" + "EUR/USD Rate: "
										+ row.getBidClose() + "\n" + "Event: "
										+ eventResult.getTitle() + " "
										+ eventResult.getInstrument() + " "
										+ eventResult.getResult() + "\n"
										+ "Time: " + row.getTime().getTime());
						awaitingEventResult = false;
					} else if (eventResult.getResult() == EventResult.WORSE) {
						//gör något om det är sämre då
						System.out.println("Result: WORSE");
						tradeController.openPosition("EUR/USD", "S", 20000,
								row.getBidClose() + 0.001,
								row.getBidClose() - 0.0005, this);
						sendEmailNotification(
								"tob.wikstrom@gmail.com",
								"Short trade - Event Strategy",
								"Short position \n" + "EUR/USD Rate: "
										+ row.getBidClose() + "\n" + "Event: "
										+ eventResult.getTitle() + " "
										+ eventResult.getInstrument() + " "
										+ eventResult.getResult() + "\n"
										+ "Time: " + row.getTime().getTime());
						awaitingEventResult = false;
					} else if (eventResult.getResult() == EventResult.UNCHANGED) {
						System.out.println("UNCHANGED");
						//awaitingEventResult = false;
					}
				} else if (eventResult.getInstrument().equals("USD")
						&& eventResult.getImpact() == EventImpact.HIGH) {
					if (eventResult.getResult() == EventResult.NONE) {
						System.out.println("Result: NOT FOUND YET");
						awaitingEventResult = true;
					} else if (eventResult.getResult() == EventResult.BETTER) {
						System.out.println("Result: BETTER");
						tradeController.openPosition("EUR/USD", "S", 20000,
								row.getBidClose() + 0.001,
								row.getBidClose() - 0.0005, this);
						sendEmailNotification(
								"tob.wikstrom@gmail.com",
								"Long trade - Event Strategy",
								"Long position \n" + "EUR/USD Rate: "
										+ row.getBidClose() + "\n" + "Event: "
										+ eventResult.getTitle() + " "
										+ eventResult.getInstrument() + " "
										+ eventResult.getResult() + "\n"
										+ "Time: " + row.getTime().getTime());
						awaitingEventResult = false;
					} else if (eventResult.getResult() == EventResult.WORSE) {
						//gör något om det är sämre då
						System.out.println("Result: WORSE");
						tradeController.openPosition("EUR/USD", "B", 20000,
								row.getBidClose() + 0.001,
								row.getBidClose() - 0.0005, this);
						sendEmailNotification(
								"tob.wikstrom@gmail.com",
								"Short trade - Event Strategy",
								"Short position \n" + "EUR/USD Rate: "
										+ row.getBidClose() + "\n" + "Event: "
										+ eventResult.getTitle() + " "
										+ eventResult.getInstrument() + " "
										+ eventResult.getResult() + "\n"
										+ "Time: " + row.getTime().getTime());
						awaitingEventResult = false;
					} else if (eventResult.getResult() == EventResult.UNCHANGED) {
						System.out.println("UNCHANGED");
						//awaitingEventResult = false;
					}

				}
			}

		}
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

}
