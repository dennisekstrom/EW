package indicator.data;

public class TrendData {

	public enum Error {
		NO_ERROR, INTERVAL_TOO_LARGE, DID_NOT_FIND
	}

	public enum Reliability {
		NO_RELIABILITY, STRONG, MEDIUM, WEAK
	}

	public enum Trend {
		NO_TREND, UP, CONSOLIDATION, DOWN
	}

	private Reliability reliability;
	private Trend trend;
	private Error error;

	public TrendData() {

	}

	public Reliability getReliability() {
		return reliability;
	}

	public void setReliability(Reliability reliability) {
		this.reliability = reliability;
	}

	public Trend getTrend() {
		return trend;
	}

	public void setTrend(Trend trend) {
		this.trend = trend;
	}

	public Error getError() {
		return error;
	}

	public void setError(Error error) {
		this.error = error;
	}

}
