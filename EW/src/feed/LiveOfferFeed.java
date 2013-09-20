package feed;

import com.fxcore2.IO2GTableListener;
import com.fxcore2.O2GOfferTableRow;
import com.fxcore2.O2GRow;
import com.fxcore2.O2GTableStatus;

import forex.Offer;

/*
 * 
 * @author Tobias
 * 
 */

public final class LiveOfferFeed extends OfferFeed implements IO2GTableListener {

	private static LiveOfferFeed instance;

	static {
		instance = new LiveOfferFeed();
	}

	private LiveOfferFeed() {

	}

	public static LiveOfferFeed getInstance() {
		return instance;
	}

	@Override
	public void onAdded(String rowID, O2GRow row) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onChanged(String rowID, O2GRow row) {
		Offer offer = Offer.convertRow((O2GOfferTableRow) row, "t1");

		supplyOffer(offer);
	}

	@Override
	public void onDeleted(String rowID, O2GRow row) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(O2GTableStatus status) {
		// TODO Auto-generated method stub

	}

	@Override
	public long getCurrentTime() {
		return System.currentTimeMillis();
	}

}
