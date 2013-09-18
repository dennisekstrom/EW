package trading.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import trading.ResponseListener;

import com.fxcore2.O2GMarketDataSnapshotResponseReader;
import com.fxcore2.O2GRequest;
import com.fxcore2.O2GRequestFactory;
import com.fxcore2.O2GResponse;
import com.fxcore2.O2GResponseReaderFactory;
import com.fxcore2.O2GSession;
import com.fxcore2.O2GTimeConverter;
import com.fxcore2.O2GTimeConverterTimeZone;
import com.fxcore2.O2GTimeframe;
import com.fxcore2.O2GTimeframeCollection;

import forex.Offer;

/**
 * Handles historic data
 * 
 * @author Tobias W
 * 
 */
public class HistData {

	private O2GSession mSession = null;
	private final SessionStatusListener statusListener;
	private final ResponseListener responseListener;
	// private SimpleLog mSimpleLog;

	// Test
	private final ArrayList<Offer> historyArray = new ArrayList<Offer>();

	// private String mInstrument;

	public HistData(O2GSession session, SessionStatusListener statusListener,
			ResponseListener responseListener) {

		this.mSession = session;
		this.statusListener = statusListener;
		this.responseListener = responseListener;

	}

	/**
	 * Get hist stores historic data in a log file in your working directory. It
	 * stores blocks of 300 rows at a time. Returns an arraylist with TradeData.
	 * Most recent data is put at the back of the array (should this be changed
	 * for efficiency?)
	 * 
	 * @param mInstrument
	 * @param mTimeFrame
	 * @param mDateFrom
	 * @param mDateTo
	 */
	public ArrayList<Offer> getHist(String instrument, String mTimeFrame,
			String mDateFrom, String mDateTo) {

		System.out.println("History date to: " + mDateTo);
		System.out.println("History date from: " + mDateFrom);
		// Historic prices variables
		Calendar calFrom = null;
		Calendar calTo = null;
		Calendar calFirst = null;
		Calendar calDate = null;
		int mMaxBars = 300;

		// Market Data request variables
		O2GRequest request = null;
		boolean mContinue = true;
		int mReaderSize = 0;
		int mCounter = 0;
		int mBorder = 0;

		// Establish date format
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		// Get command line arguments
		mDateFrom = mDateFrom.trim();
		if ((!mDateFrom.equals("")) && (!mDateFrom.equals("{DATEFROM}"))) {
			try {
				Date dtFrom = df.parse(mDateFrom);
				calFrom = Calendar.getInstance();
				calFrom.setTime(dtFrom);
			} catch (Exception e) {
				System.out.println(" Date From format invalid.");
				System.exit(1);
			}
		}
		mDateTo = mDateTo.trim();
		if ((!mDateTo.equals("")) && (!mDateTo.equals("{DATETO}"))) {
			try {
				Date dtTo = df.parse(mDateTo);
				calTo = Calendar.getInstance();
				calTo.setTime(dtTo);
			} catch (Exception e) {
				System.out.println(" Date To format invalid.");
				System.exit(1);
			}
		}
		// Open log for writing info
		// try {
		// mSimpleLog = new SimpleLog(String.format("%s.log", mUserID));
		// } catch (FileNotFoundException e) {
		// e.printStackTrace();
		// }

		// Create a session, subscribe to session listener, login, get historic
		// prices, logout
		if (!statusListener.hasError()) {
			O2GRequestFactory requestFactory = mSession.getRequestFactory();
			O2GTimeframeCollection timeFrames = requestFactory
					.getTimeFrameCollection();
			O2GTimeframe timeFrame = timeFrames.get(mTimeFrame);
			if (timeFrame == null) {
				System.out.println("You specified an invalid time frame.");
				mContinue = false;
			}
			if (mContinue) {
				request = requestFactory
						.createMarketDataSnapshotRequestInstrument(instrument,
								timeFrame, mMaxBars);
				if (request == null) {
					System.out
							.println("Cannot create request for market data snapshot.");
					mContinue = false;
				}
			}
			if (mContinue) {
				do {
					requestFactory.fillMarketDataSnapshotRequestTime(request,
							calFrom, calTo, false);
					mSession.sendRequest(request);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					O2GResponse response = responseListener.getResponse();
					if (response != null) {
						O2GResponseReaderFactory responseFactory = mSession
								.getResponseReaderFactory();
						O2GMarketDataSnapshotResponseReader reader = responseFactory
								.createMarketDataSnapshotReader(response);
						mReaderSize = reader.size();
						// check if we need additional request
						if ((calFrom == null) || (calTo == null)
								|| (mReaderSize < mMaxBars)) {
							mBorder = 0;
						} else {
							if (mCounter > 0) {
								if (mTimeFrame.equals("m1")) {
									mBorder = 1;
								}
							}
						}
						O2GTimeConverter converter = mSession
								.getTimeConverter();
						for (int i = mReaderSize - 1; i >= 0; i--) {
							calDate = reader.getDate(i);
							calDate = converter.convert(calDate,
									O2GTimeConverterTimeZone.LOCAL);
							if (i <= mReaderSize - 1 - mBorder) {
								// TODO test (tog bort loggningen)
								// mSimpleLog
								// .log("Date = {0}, BidOpen = {1}, BidHigh = {2}, BidLow = {3}, BidClose = {4}, "
								// +
								// "AskOpen = {5}, AskHigh = {6}, AskLow = {7}, AskClose = {8}",
								// calDate.getTime(),
								// reader.getBidOpen(i),
								// reader.getBidHigh(i),
								// reader.getBidLow(i),
								// reader.getBidClose(i),
								// reader.getAskOpen(i),
								// reader.getAskHigh(i),
								// reader.getAskLow(i),
								// reader.getAskClose(i));

								// create a data object to send
								Offer data = new Offer(instrument,
										reader.getBidOpen(i),
										reader.getBidHigh(i),
										reader.getBidLow(i),
										reader.getBidClose(i),
										reader.getAskOpen(i),
										reader.getAskHigh(i),
										reader.getAskLow(i),
										reader.getAskClose(i), reader
												.getDate(i).getTimeInMillis(),
										reader.getVolume(i));
								// TODO ta bort 0an bara om ordningen ska ?ndras
								historyArray.add(0, data);

							}
							calFirst = reader.getDate(0);
						}
						System.out.println("Reader size: " + mReaderSize);
						mCounter++;
					}
					if (calFrom == null) {
						break;
					}
					if (mReaderSize < mMaxBars) {
						break;
					}
					if (calFrom.before(calFirst)) {
						calTo = calFirst;
					}
				} while (calFrom.before(calFirst));
			}
		}
		return historyArray;
	}
}
