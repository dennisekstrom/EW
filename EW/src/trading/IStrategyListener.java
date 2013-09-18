package trading;

import java.util.ArrayList;

import trading.util.Event;

import com.fxcore2.O2GClosedTradeRow;
import com.fxcore2.O2GOfferTableRow;
import com.fxcore2.O2GTradeRow;

import forex.Offer;

/**
 * Interface to be implemented by all strategies.
 * 
 * @author Tobias W
 * 
 */

public interface IStrategyListener {

	/**
	 * Called for each arriving live tick.This includes all FXCM instruments.
	 * 
	 * @param instrument
	 *            the instrument of the tick
	 * @param tick
	 *            the tick
	 */
	public void onTick(O2GOfferTableRow row);

	/**
	 * Called for each historic/back trace tick
	 */

	public void onTick(Offer data);

	/**
	 * Called when strategy starts
	 */
	public void onStart();

	/**
	 * Called when strategy pauses
	 */
	public void onPause();

	/**
	 * Called when strategy continues from a pause
	 */

	public void onContinue();

	/**
	 * Called when event goes live.
	 * 
	 * @param eventTitle
	 */

	public void onEventLive(ArrayList<Event> liveEvents);

	/**
	 * Called when event has passed
	 */

	public void onEventPassed();

	/**
	 * Notify that a trade has been closed
	 */
	public void notifyClosedTrade(O2GClosedTradeRow trade);

	/**
	 * Notify that a trade has been closed
	 */
	public void notifyOpenedTrade(O2GTradeRow trade);

}
