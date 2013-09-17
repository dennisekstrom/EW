package trading.util;

/**
 * Struct for an Event
 * 
 * @author Tobias W
 * 
 */

public class Event {

	public enum EventImpact {
		LOW, MEDIUM, HIGH, NONE
	}

	public enum EventResult {
		WORSE, BETTER, UNCHANGED, NONE
	}

	private String title = "";
	private EventResult result = EventResult.NONE;
	private long time = 0;
	private String instrument = "";
	private EventImpact impact = EventImpact.NONE;

	public Event(String eventTitle) {
		this.setTitle(eventTitle);

	}

	public Event(String eventTitle, EventResult result, String instrument,
			EventImpact impact, long time) {
		this.setTitle(eventTitle);
		this.result = result;
		this.time = time;
		this.instrument = instrument;
		this.impact = impact;

	}

	public EventResult getResult() {
		return result;
	}

	public void setResult(EventResult result) {
		this.result = result;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String eventTitle) {
		this.title = eventTitle;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long eventTime) {
		this.time = eventTime;
	}

	public String getInstrument() {
		return instrument;
	}

	public void setInstrument(String instrument) {
		this.instrument = instrument;
	}

	public EventImpact getImpact() {
		return impact;
	}

	public void setImpact(EventImpact impact) {
		this.impact = impact;
	}

}
