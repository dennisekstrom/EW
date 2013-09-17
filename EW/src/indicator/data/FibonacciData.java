package indicator.data;

public class FibonacciData {

	public enum Error {
		NO_ERROR, INTERVAL_TOO_LARGE, DID_NOT_FIND
	}

	public enum Reliability {
		NO_RELIABILITY, STRONG, MEDIUM, WEAK
	}

	public enum Trend {
		NO_TREND, UP, CONSOLIDATION, DOWN
	}

	//Custom fibonacci fields
	private double fibonacciHalfRate;
	private double fibonacciGoldenRateOver;
	private double fibonacciGoldenRateUnder;
	private double resistanceRate;
	private double supportRate;
	private Reliability reliability;
	private Trend trend;
	private Error error;

	public FibonacciData() {

	}

	public double getFibonacciHalfRate() {
		return fibonacciHalfRate;
	}

	public void setFibonacciHalfRate(double fibbonacciHalfRate) {
		this.fibonacciHalfRate = fibbonacciHalfRate;
	}

	public double getFibonacciGoldenRateUnder() {
		return fibonacciGoldenRateUnder;
	}

	public void setFibonacciGoldenRateUnder(double fibonacciGoldenRateUnder) {
		this.fibonacciGoldenRateUnder = fibonacciGoldenRateUnder;
	}

	public double getFibonacciGoldenRateOver() {
		return fibonacciGoldenRateOver;
	}

	public void setFibonacciGoldenRateOver(double fibonacciGoldenRateOver) {
		this.fibonacciGoldenRateOver = fibonacciGoldenRateOver;
	}

	public Reliability getReliability() {
		return reliability;
	}

	public void setReliability(Reliability reliability) {
		this.reliability = reliability;
	}

	public Error getError() {
		return error;
	}

	public void setError(Error error) {
		this.error = error;
	}

	public Trend getTrend() {
		return trend;
	}

	public void setTrend(Trend trend) {
		this.trend = trend;
	}

	public double getResistanceRate() {
		return resistanceRate;
	}

	public void setResistanceRate(double resistanceRate) {
		this.resistanceRate = resistanceRate;
	}

	public double getSupportRate() {
		return supportRate;
	}

	public void setSupportRate(double supportRate) {
		this.supportRate = supportRate;
	}

}
