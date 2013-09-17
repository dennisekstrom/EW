package trading.util;

import java.util.concurrent.Semaphore;
import com.fxcore2.*;

/**
 * Listens to session status - Written completely by FXCM
 * 
 * @author Tobias W
 * 
 */
public class SessionStatusListener implements IO2GSessionStatus {

	// Connection, session and status variables
	private boolean mConnected = false;
	private boolean mDisconnected = false;
	private boolean mError = false;
	private String mSessionID = "";
	private String mPin = "";
	private final Semaphore mSemaphore;
	private O2GSession mSession = null;
	private O2GSessionStatusCode mStatus = null;

	// Constructor
	public SessionStatusListener(O2GSession session, String sessionID,
			String pin) {
		mSession = session;
		mSessionID = sessionID;
		mPin = pin;
		mSemaphore = new Semaphore(0);
	}

	public void waitEvents() throws InterruptedException {
		mSemaphore.acquire(1);
	}

	// Shows if session is connected
	public boolean isConnected() {
		return mConnected;
	}

	// Shows if session is disconnected
	public boolean isDisconnected() {
		return mDisconnected;
	}

	// Shows if there was an error during the login process
	public boolean hasError() {
		return mError;
	}

	// Returns current session status
	public O2GSessionStatusCode getStatus() {
		return mStatus;
	}

	// Implementation of IO2GSessionStatus interface public method
	// onSessionStatusChanged
	@Override
	public void onSessionStatusChanged(O2GSessionStatusCode status) {
		mStatus = status;
		System.out.println("Status: " + mStatus.toString());
		if (mStatus == O2GSessionStatusCode.CONNECTED) {
			mConnected = true;
		} else {
			mConnected = false;
		}
		if (status == O2GSessionStatusCode.DISCONNECTED) {
			mDisconnected = true;
		} else {
			mDisconnected = false;
		}
		if (mStatus == O2GSessionStatusCode.TRADING_SESSION_REQUESTED) {
			O2GSessionDescriptorCollection descs = mSession
					.getTradingSessionDescriptors();
			System.out.println("Session descriptors");
			System.out.println("id, name, description, requires pin");
			for (O2GSessionDescriptor desc : descs) {
				System.out.println(desc.getId() + " " + desc.getName() + " "
						+ desc.getDescription() + " " + desc.isPinRequired());
			}
			if (!mSessionID.isEmpty()) {
				mSession.setTradingSession(mSessionID, mPin);
			} else {
				System.out
						.println("Argument for trading session ID is missing");
			}
		}
		mSemaphore.release();
	}

	// Implementation of IO2GSessionStatus interface public method onLoginFailed
	@Override
	public void onLoginFailed(String error) {
		System.out.println("Login error: " + error);
		mError = true;
	}
}
