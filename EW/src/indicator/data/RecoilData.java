package indicator.data;

public class RecoilData {

	public enum Error {
		NO_ERROR, INTERVAL_TOO_LARGE, DID_NOT_FIND
	}

	public enum Reliability {
		NO_RELIABILITY, STRONG, MEDIUM, WEAK
	}

	public enum Recoil {
		UP, DOWN
	}

	private Error error;
	private Reliability reliability;
	private Recoil recoil;

	public RecoilData() {

	}

	public Error getError() {
		return error;
	}

	public void setError(Error error) {
		this.error = error;
	}

	public Reliability getReliability() {
		return reliability;
	}

	public void setReliability(Reliability reliability) {
		this.reliability = reliability;
	}

	public Recoil getRecoil() {
		return recoil;
	}

	public void setRecoil(Recoil recoil) {
		this.recoil = recoil;
	}

}
