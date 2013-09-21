package trading;

import java.util.ArrayList;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import trading.util.Event;

import client.gui.ActiveStrategies;
import client.gui.ClientMain;
import client.gui.Messages;

import com.fxcore2.O2GClosedTradeRow;
import com.fxcore2.O2GTradeRow;

import forex.Offer;

/**
 * Class to handle all strategies.
 * 
 * Strategy controller keeps track of all strategies. As a default the listeners
 * are not updated, start the strategies by calling the start method. It updates
 * every strategy listener.
 * 
 * @author Tobias W
 * 
 */
public class StrategyController {

	// Strategy Listeners
	private final HashSet<IStrategyListener> strategyListeners;
	// A Hash to map listeners with strategies
	private final HashMap<IStrategyListener, Strategy> stratListenerMap;
	// Active strategies are strategies performing tasks every tick
	private final ArrayList<Strategy> activeStrategies;
	// Strategies added to the controller, but not necessarily active.
	private final ArrayList<Strategy> strategies;

	// private UpcomingEvents eventPanel;
	private ActiveStrategies activeStrategiesPanel;

	// boolean to control strategies on/off
	private boolean run = false;

	private boolean isEventLive = false;
	private long timeAtEventStart = 0;

	public StrategyController() {
		strategyListeners = new HashSet<IStrategyListener>();
		activeStrategies = new ArrayList<Strategy>();
		strategies = new ArrayList<Strategy>();
		stratListenerMap = new HashMap<IStrategyListener, Strategy>();
	}

	/**
	 * Pauses all strategies, but keeps them as strategies. The listeners will
	 * stop listening.
	 */
	public void pauseStrategies() {
		System.out.println("Pausing strategies");
		for (IStrategyListener listener : strategyListeners) {
			listener.onPause();
			Strategy strategy = stratListenerMap.get(listener);
			strategy.setIsPaused(true);
		}
		run = false;
	}

	/**
	 * Enables all active strategies. This method will NOT relaunch (call the
	 * onstart() again) strategies.
	 */
	public void enableStrategies() {
		for (IStrategyListener listener : strategyListeners) {
			Strategy strategy = stratListenerMap.get(listener);
			if (!strategy.hasLaunched()) {
				listener.onStart();
				strategy.setLaunched(true);
			} else if (strategy.isPaused()) {
				Messages.addMessage(strategy.getStrategyName()
						+ " just resumed");
				listener.onContinue();
				strategy.setIsPaused(false);
			}
		}
		run = true;
	}

	/**
	 * Starts a strategy. Can be used to reboot a strategy (onstart() will be
	 * called)
	 * 
	 * @param strategy
	 */
	public void launchStrategy(Strategy strategy) {
		if (strategies.contains(strategy)) {
			activateStrategy(strategy);

			Messages.addMessage(strategy.getStrategyName() + " just launched");
			strategy.onStart();
			strategy.setLaunched(true);

		} else {
			System.out.println("Strategy not added");
		}
	}

	/**
	 * Get active strategies
	 * 
	 * @return
	 */
	public ArrayList<Strategy> getActiveStrategies() {
		return activeStrategies;
	}

	/**
	 * Get strategies
	 */

	public ArrayList<Strategy> getStrategies() {
		return strategies;
	}

	/**
	 * Add strategy to be active.
	 * 
	 * @param strat
	 */
	public void activateStrategy(Strategy strat) {
		if (!activeStrategies.contains(strat)) {
			strat.setActive(true);
			activeStrategies.add(strat);
			if (!strategies.contains(strat))
				strategies.add(strat);
			addStrategyListener(strat);
		}
		activeStrategiesPanel.setStrategies();

	}

	/**
	 * Add strategy
	 */

	public void addStrategy(Strategy strat, String instrument) {
		strat.instrument = instrument;
		strategies.add(strat);
	}

	/**
	 * Remove active strategy
	 * 
	 * @param strat
	 */
	public void deactivateStrategy(Strategy strat) {
		strat.setActive(false);
		if (activeStrategies.contains(strat)) {
			activeStrategies.remove(strat);
			removeStrategyListener(strat);
			stratListenerMap.remove(strat);
			strat.setIsPaused(true);
			strat.onPause();
		}
		activeStrategiesPanel.setStrategies();
	}

	/**
	 * Remove strategy
	 * 
	 * @param strat
	 */
	public void removeStrategy(Strategy strat) {
		strat.setActive(false);
		strategies.remove(strat);
		deactivateStrategy(strat);
	}

	/**
	 * Removes all active strategies and returns them. Also clears the
	 * listeners.
	 * 
	 * @return
	 */
	public ArrayList<Strategy> removeAllActiveStrategies() {
		ArrayList<Strategy> removedStrategies = new ArrayList<Strategy>();
		Collections.copy(removedStrategies, activeStrategies);

		for (Strategy strat : activeStrategies)
			strat.setActive(false);
		strategyListeners.clear();
		activeStrategies.clear();
		stratListenerMap.clear();

		return removedStrategies;
	}

	/**
	 * Removes all strategies and returns them.
	 */

	public ArrayList<Strategy> removeAllStrategies() {
		ArrayList<Strategy> removedStrategies = new ArrayList<Strategy>();
		Collections.copy(removedStrategies, strategies);

		for (Strategy strat : strategies)
			strat.setActive(false);

		strategies.clear();

		return removedStrategies;
	}

	/**
	 * Adds an StrategyListener
	 * 
	 * @param listener
	 *            the StrategyListener to be added
	 */
	private void addStrategyListener(Strategy strat) {
		IStrategyListener listener = strat;
		if (listener == null)
			return;

		strategyListeners.add(listener);
		stratListenerMap.put(listener, strat);
	}

	/**
	 * Removes an StrategyListener
	 * 
	 * @param listener
	 *            the StrategyListener to be removed
	 */
	private void removeStrategyListener(IStrategyListener listener) {
		strategyListeners.remove(listener);
		stratListenerMap.remove(listener);
	}

	/**
	 * Called to notify strategies that an event is live
	 * 
	 * @param eventTitle
	 */
	public void onEventLive(ArrayList<Event> upcomingEvents) {
		for (Event event : upcomingEvents) {
			Messages.addMessage(event.getTitle() + " just went live");
		}

		isEventLive = true;
		timeAtEventStart = System.currentTimeMillis();
		if (run && ClientMain.isLive) {
			for (IStrategyListener listener : strategyListeners) {
				listener.onEventLive(upcomingEvents);
			}
		}
	}

	/**
	 * Notify that a trade has been closed
	 */

	public void notifyClosedTrade(O2GClosedTradeRow trade) {
		ArrayList<Strategy> activeStrategiesTemp = new ArrayList<Strategy>();
		for (int i = 0; i < activeStrategies.size(); i++) {
			activeStrategiesTemp.add(null);
		}
		Collections.copy(activeStrategiesTemp, activeStrategies);

		if (run && ClientMain.isLive) {

			for (Strategy strat : activeStrategiesTemp) {

				if (strat.currentTrade != null
						&& trade.getTradeID().equals(
								strat.currentTrade.getTradeID())) {
					strat.currentTrade = null;
					strat.notifyClosedTrade(trade);
					System.out.println("Closed Trade from: "
							+ strat.getStrategyName());
				}
			}

		}
	}

	/**
	 * Notify that a trade has been opened
	 */

	public void notifyOpenedTrade(O2GTradeRow trade) {
		if (run && ClientMain.isLive) {
			for (IStrategyListener listener : strategyListeners) {
				listener.notifyOpenedTrade(trade);
			}
		}
	}

	private void strategyAlgorithm(Offer row) {

		if (run && ClientMain.isLive) {

			ArrayList<Strategy> activeStrategiesTemp = new ArrayList<Strategy>();
			for (int i = 0; i < activeStrategies.size(); i++) {
				activeStrategiesTemp.add(null);
			}
			Collections.copy(activeStrategiesTemp, activeStrategies);

			// Update algorithms for strategies, but only if it's not already
			// updating
			for (Strategy strat : activeStrategiesTemp) {
				if (!strat.isAlgorithmActive
						&& strat.instrument.equals(row.getInstrument())) {
					strat.isAlgorithmActive = true;
					strat.strategyAlgorithm(row);
					strat.isAlgorithmActive = false;

				}
			}

			if (isEventLive)
				checkEventDuration();

		}
	}

	/**
	 * Is called whenever a new tick is received.
	 */
	public void onTick(Offer data) {

		strategyAlgorithm(data);

		if (run) {
			for (IStrategyListener listener : strategyListeners)
				listener.onTick(data);

			// Update historic data
			for (Strategy strat : activeStrategies) {
				if (strat.instrument.equals(data.getInstrument()))
					strat.addRowToHistoricData(data, strat);
			}

			if (isEventLive)
				checkEventDuration();

		}

	}

	/**
	 * Set upcoming events
	 */

	public void setUpcomingEvents(ArrayList<Event> upcoming) {
		for (Strategy strat : activeStrategies) {
			strat.upcomingEvents = upcoming;
			break;
		}
	}

	/**
	 * Makes sure it only tries to fetch an result for 3 minutes max
	 */
	private void checkEventDuration() {
		// wait 5 minutes for event results
		if (System.currentTimeMillis() - timeAtEventStart > 1000 * 60 * 3) {
			for (IStrategyListener listener : strategyListeners) {
				listener.onEventPassed();
			}
			isEventLive = false;
			// eventPanel.updateEvents(true);
		}
	}

	// public void addEventPanel(UpcomingEvents eventPanel) {
	// this.eventPanel = eventPanel;
	// }

	public void addActiveStrategiesPanel(ActiveStrategies activeStrategiesPanel) {
		this.activeStrategiesPanel = activeStrategiesPanel;
	}

	public void closeTrade(O2GClosedTradeRow trade) {
		ArrayList<Strategy> activeStrategiesTemp = new ArrayList<Strategy>();
		for (int i = 0; i < activeStrategies.size(); i++) {
			activeStrategiesTemp.add(null);
		}
		Collections.copy(activeStrategiesTemp, activeStrategies);

		for (Strategy strat : activeStrategiesTemp) {
			if (trade.getTradeID().equals(strat.currentTrade.getTradeID())) {
				strat.currentTrade = null;
				System.out.println("Closed Trade from: "
						+ strat.getStrategyName());
			}
		}
	}

	public ActiveStrategies getActiveStrategiesPanel() {
		return activeStrategiesPanel;
	}

}
