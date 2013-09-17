package trading.util;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import trading.StrategyController;

import client.PositionController;
import client.UIUpcomingEvents;

import feed.IOffersListener;
import feed.LiveOfferFeed;

/**
 * Offer rate listener. Prints out for every tick received. Uses an offer table
 * where each row represents a new tick. Updates what is necessary to update for
 * each received tick (Strategies, events, positions etc.)
 * 
 * TODO ska ???ndras s??? den implementar IOffersListener ist???llet och sen
 * registrera till OffersFeed.
 * 
 * @author Tobias W
 * 
 */

public class OffersListener implements IOffersListener {

	private String mInstrument = null;
	private final StrategyController strategyController;
	private final TradeController tradeController;
	private final int previousTradeTableSize = 0;

	private boolean hasInformedLiveEvent = false;
	private UIUpcomingEvents eventsPanel = null;

	Executor executor = Executors.newFixedThreadPool(2);

	public OffersListener(StrategyController strategy,
			TradeController tradeController) {
		this.strategyController = strategy;
		this.tradeController = tradeController;

	}

	public void SetInstrumentFilter(String instrument) {
		mInstrument = instrument;
	}

	@Override
	public void onOffer(Offer offer) {
		// inform strategy listeners
		strategyController.onTick(offer);
		if (offer.getInstrument().equals(mInstrument)) {
			// Update position controller
			updatePositionController(offer);

			// Update events
			updateEvents();
		}

	}

	public void addToFeed() {
		OfferFeed.getInstance().registerListener(this);
	}

	public void removeFromFeed() {
		OfferFeed.getInstance().unRegisterListener(this);
	}

	/**
	 * Handles the position controller updates TODO SwingWorker!!!!!!!!!
	 * 
	 * @param row
	 */
	private void updatePositionController(Offer row) {
		if (row == null)
			return;
		PositionController positionController = tradeController
				.getPositionController();
		// for (Position position : positionController.openPositions)
		// position.adjustProfit();

		// update rate label in client
		positionController
				.updateRateLabel(row.getAskClose(), row.getBidClose());
		// update balance label
		positionController.updateBalanceLabel(tradeController.getAccount()
				.getBalance());

		// update daily profit label
		positionController.updateDailyProfitLabel((tradeController.getAccount()
				.getBalance() - tradeController.getAccount().getM2MEquity()));

		positionController.updateGUI(tradeController, false);
		positionController.getHost().updateClock();
	}

	/**
	 * Handles the event updates and calls strategy listeners when necessary
	 */
	private void updateEvents() {
		eventsPanel = tradeController.getPositionController().getHost()
				.getEventsPanel();
		if ((tradeController.getPositionController().getHost().getEventsPanel()
				.getUpcomingEvent().getTime() - System.currentTimeMillis())
				/ (1000 * 60) < 0) {
			if (!hasInformedLiveEvent) {
				hasInformedLiveEvent = true;
				strategyController.addEventPanel(eventsPanel);
				ArrayList<Event> eventsToListeners = new ArrayList<Event>();
				for (Event e : eventsPanel.getUpcomingEvents())
					eventsToListeners.add(e);
				strategyController.onEventLive(eventsToListeners);
				//Orsaken till at den tar n???sta event:
				executor.execute(new UpdateEvents());

			}
			//eventsPanel.updateEvents(false);
		} else
			hasInformedLiveEvent = false;

		if (eventsPanel != null && eventsPanel.getListModel().isEmpty()) {
			System.out.println("Listmodel TOM! (uppdaterar)");

			eventsPanel.updateEvents(true);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		strategyController.setUpcomingEvents(eventsPanel.getUpcomingEvents());
	}

	private class UpdateEvents implements Runnable {
		//StatusWindow member variable and constructor
		@Override
		public void run() {
			//alot of code
			eventsPanel.updateEvents(false);
		}
	}

}
