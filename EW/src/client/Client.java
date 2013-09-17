package client;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import client.gui.ClientMain;
import client.gui.PasswordFrame;

import trading.TradeController;

/**
 * Main class for client
 * 
 * @author Tobias
 * 
 */

public class Client {
	private static PasswordFrame passwordFrame;
	private static ClientMain client;
	private static TradeController tradeController;

	private static long startTime;

	public static void main(String[] args) {
		Client.reboot();
	}

	/**
	 * Reboot client
	 */
	public static void reboot() {
		if (client != null)
			client.setVisible(false);
		if (tradeController != null) {
			try {
				tradeController.logout();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		client = null;
		requestPassword();
	}

	private static void requestPassword() {
		passwordFrame = new PasswordFrame();
		passwordFrame.addPasswordListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getSource() == passwordFrame) {
					User user = passwordFrame.getUser();
					passwordFrame.setVisible(false);
					passwordFrame = null;

					// startUp(user);
					startClient(user);
				}
			}
		});
		passwordFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		passwordFrame.setLocationRelativeTo(null);
		passwordFrame.setVisible(true);
	}

	public static void startClient(User user) {
		// Redirect all output to a PrintStream

		// SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd_HHmm");
		// try {
		// String time = sdf.format(Calendar.getInstance().getTime());
		// System.setOut(new PrintStream("logs/wefor_" + time + ".log"));
		// } catch (FileNotFoundException e) {
		// e.printStackTrace();
		// }

		// Demo for web platform: D171951730001 Pass: 691
		// Demo2 for trading station: D25203691001 Pass: 6296
		tradeController = new TradeController(user.getId(), new String(
				user.getPassword()), "http://www.fxcorporate.com/Hosts.jsp",
				"Demo", "EUR/USD");
		tradeController.login();

		if (tradeController.getAccount() == null) {
			JOptionPane
					.showMessageDialog(
							null,
							"Enter the correct password to login to the client.\n"
									+ "Password is only available for strategy testers.");
			Client.reboot();
			return;
		}

		tradeController.setUpStrategies();

		user.setUsername(tradeController.getAccount().getAccountID());

		client = new ClientMain(user, getStartTime(), tradeController);
		client.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		client.setLocationRelativeTo(null);
		client.setVisible(true);
	}

	public static void setStartTime(long time) {
		startTime = time;
	}

	public static void stop() {
		if (tradeController != null) {
			try {
				tradeController.logout();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.exit(0);
	}

	private static long getStartTime() {
		// return 1294009200000L;
		return startTime + 6000000;
	}
}
