package forex;

import util.Instrument;
import util.Period;

import com.fxcore2.O2GOfferTableRow;

/**
 * Datatype for a offer - a row.
 * 
 * 
 * @author Tobias W
 * 
 */

public class Offer implements Comparable<Offer> {

	private final String instrument;
	private final String timeFrame;
	private final double bidOpen;
	private final double bidHigh;
	private final double bidLow;
	private final double bidClose;
	private final double askOpen;
	private final double askHigh;
	private final double askLow;
	private final double askClose;

	private final int volume;

	private final long time;

	/**
	 * 
	 * @param bidOpen
	 * @param bidHigh
	 * @param bidLow
	 * @param bidClose
	 * @param askOpen
	 * @param askHigh
	 * @param askLow
	 * @param askClose
	 * @param time
	 * @param volume
	 */
	public Offer(String instrument, String timeFrame, double bidOpen,
			double bidHigh, double bidLow, double bidClose, double askOpen,
			double askHigh, double askLow, double askClose, long time,
			int volume) {
		this.instrument = instrument;
		this.timeFrame = timeFrame;
		this.bidOpen = bidOpen;
		this.bidHigh = bidHigh;
		this.bidLow = bidLow;
		this.bidClose = bidClose;
		this.askOpen = askOpen;
		this.askHigh = askHigh;
		this.askLow = askLow;
		this.askClose = askClose;
		this.time = time;
		this.volume = volume;
	}

	/**
	 * Converts a O2GOfferTableRow to a TradeData
	 * 
	 * @param row
	 * @return
	 */
	public static Offer convertRow(O2GOfferTableRow row, String timeFrame) {
		Offer data = new Offer(row.getInstrument(), timeFrame, row.getBid(),
				row.getHigh(), row.getLow(), row.getBid(), row.getAsk(),
				row.getHigh(), row.getLow(), row.getAsk(), row.getTime()
						.getTimeInMillis(), row.getVolume());

		return data;
	}

	public int getVolume() {
		return volume;
	}

	public double getAskClose() {
		return askClose;
	}

	public double getAskLow() {
		return askLow;
	}

	public double getAskHigh() {
		return askHigh;
	}

	public double getAskOpen() {
		return askOpen;
	}

	public double getBidClose() {
		return bidClose;
	}

	public double getBidHigh() {
		return bidHigh;
	}

	public double getBidLow() {
		return bidLow;
	}

	public double getBidOpen() {
		return bidOpen;
	}

	public long getTime() {
		return time;
	}

	public String getInstrument() {
		return instrument;
	}

	// TODO test
	public Instrument getInstrument2() {
		return Instrument.EURUSD;
	}

	public String getTimeFrame() {
		return timeFrame;
	}

	// TODO ändra Period-intervallen och sedan ha alla cases för denna
	public Period getPeriod() {
		if (timeFrame.equals("t1"))
			return Period.TICK;
		else if (timeFrame.equals("m5"))
			return Period.M5;
		else if (timeFrame.equals("h1"))
			return Period.H1;

		return null;
	}

	@Override
	public int compareTo(Offer o) {
		if (askHigh < o.getAskHigh())
			return -1;
		else if (askHigh > o.getAskHigh())
			return 1;
		else
			return 0;
	}
}
