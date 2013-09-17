package strategies;

import java.util.ArrayList;

import com.fxcore2.O2GClosedTradeRow;
import com.fxcore2.O2GOfferTableRow;

import trading.Strategy;
import trading.StrategyController;
import trading.TradeController;
import trading.util.Event;
import trading.util.OfferData;
import trading.util.Event.EventImpact;
import trading.util.Event.EventResult;

public class AUDUSDEvents extends Strategy {

	private static final String STRATEGY_NAME = "Event AUD/USD strategy";
	// private static final String instrument = "AUD/USD";

	// Total gross of closed trades with the strategy
	private long totalGross = 0;

	private boolean awaitingEventResult = true;
	private boolean eventIsLive = false;
	private boolean fetchingEvent = false;
	private ArrayList<Event> liveEvents = new ArrayList<Event>();
	private final String closingMessage = null;

	// Handles all trading related calls
	private final TradeController tradeController;

	public AUDUSDEvents(StrategyController stratControl,
			TradeController tradeController) {
		super(stratControl, tradeController, STRATEGY_NAME);
		this.tradeController = tradeController;

	}

	// Live feed
	@Override
	public void onTick(O2GOfferTableRow row) {
	}

	// Backtrace feed
	@Override
	public void onTick(OfferData data) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStart() {
		System.out.println("Started " + STRATEGY_NAME);
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
	}

	@Override
	public void onEventPassed() {
		eventIsLive = false;
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyClosedTrade(O2GClosedTradeRow trade) {
		sendEmailNotification(
				"tob.wikstrom@gmail.com",
				"Closed trade " + STRATEGY_NAME,
				STRATEGY_NAME + "BuySell: " + trade.getBuySell() + "\n"
						+ this.instrument + "\n" + "Open time: "
						+ trade.getOpenTime().getTime() + "\n" + "Close Time: "
						+ trade.getCloseTime().getTime() + "P/L: "
						+ trade.getGrossPL() + "EUR\n" + closingMessage);

		totalGross += trade.getGrossPL();

	}

	private void handleLiveEvents() {
		// Handle live events
		if (eventIsLive && awaitingEventResult && !fetchingEvent) {
			boolean sentEmail = false;

			for (Event event : liveEvents) {
				fetchingEvent = true;
				// TODO g�ra i en annan tr�d?
				if (event.getInstrument() != "AUD"
						&& event.getInstrument() != "USD")
					continue;
				Event eventResult = getEventResult(event);
				fetchingEvent = false;

				System.out.println("--");
				System.out.println(STRATEGY_NAME);
				System.out.println("Strat fyra event: "
						+ eventResult.getTitle() + " --- "
						+ eventResult.getInstrument());
				if (!sentEmail) {
					sendEmailNotification(
							"tob.wikstrom@gmail.com",
							"Live AUDUSD event " + STRATEGY_NAME,
							eventResult.getTitle() + " --- "
									+ eventResult.getInstrument());
					sentEmail = true;

				}

				if (eventResult.getInstrument().equals("AUD")
						&& eventResult.getImpact() == EventImpact.HIGH) {
					if (eventResult.getResult() == EventResult.NONE) {
						System.out.println("Result: NOT FOUND YET");
						awaitingEventResult = true;
					} else if (eventResult.getResult() == EventResult.BETTER) {
						System.out.println("Result: BETTER");
						tradeController.openPosition(this.instrument, "B",
								20000, 0.0, 0.0, this);
						sendEmailNotification("tob.wikstrom@gmail.com",
								"Long trade - Event Strategy",
								"Long position \n" + this.instrument + "\n"
										+ "Event: " + eventResult.getTitle()
										+ " " + eventResult.getInstrument()
										+ " " + eventResult.getResult() + "\n");
						awaitingEventResult = false;
					} else if (eventResult.getResult() == EventResult.WORSE) {
						// g�r n�got om det �r s�mre d�
						System.out.println("Result: WORSE");
						tradeController.openPosition(this.instrument, "S",
								20000, 0.0, 0.0, this);
						sendEmailNotification("tob.wikstrom@gmail.com",
								"Short trade - Event Strategy",
								"Short position \n" + this.instrument + "\n"
										+ "Event: " + eventResult.getTitle()
										+ " " + eventResult.getInstrument()
										+ " " + eventResult.getResult() + "\n");
						awaitingEventResult = false;
					} else if (eventResult.getResult() == EventResult.UNCHANGED) {
						System.out.println("UNCHANGED");
						// awaitingEventResult = false;
					}
				} else if (eventResult.getInstrument().equals("USD")
						&& eventResult.getImpact() == EventImpact.HIGH) {
					if (eventResult.getResult() == EventResult.NONE) {
						System.out.println("Result: NOT FOUND YET");
						awaitingEventResult = true;
					} else if (eventResult.getResult() == EventResult.BETTER) {
						System.out.println("Result: BETTER");
						tradeController.openPosition(this.instrument, "S",
								20000, 0.0, 0.0, this);
						sendEmailNotification("tob.wikstrom@gmail.com",
								"Long trade - Event Strategy",
								"Long position \n" + this.instrument + "\n"
										+ "Event: " + eventResult.getTitle()
										+ " " + eventResult.getInstrument()
										+ " " + eventResult.getResult() + "\n");
						awaitingEventResult = false;
					} else if (eventResult.getResult() == EventResult.WORSE) {
						// g�r n�got om det �r s�mre d�
						System.out.println("Result: WORSE");
						tradeController.openPosition(this.instrument, "B",
								20000, 0.0, 0.0, this);
						sendEmailNotification("tob.wikstrom@gmail.com",
								"Short trade - Event Strategy",
								"Short position \n" + this.instrument + "\n"
										+ "Event: " + eventResult.getTitle()
										+ " " + eventResult.getInstrument()
										+ " " + eventResult.getResult() + "\n");
						awaitingEventResult = false;
					} else if (eventResult.getResult() == EventResult.UNCHANGED) {
						System.out.println("UNCHANGED");
						// awaitingEventResult = false;
					}

				}
			}

		}
	}

	@Override
	public String getTimeFrame() {
		return "m5";
	}

	@Override
	public String getInstrument() {
		return this.instrument;
	}

	@Override
	public double getTotalGross() {
		return totalGross;
	}

	@Override
	public void strategyAlgorithm(OfferData row) {
		handleLiveEvents();

	}

}
