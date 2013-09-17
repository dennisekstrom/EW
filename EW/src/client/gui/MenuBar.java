package client.gui;

import io.StoreHistory;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.*;

import client.Client;

import trading.Strategy;

/**
 * Menubar for the client
 * 
 * @author Tobias
 * 
 */

@SuppressWarnings("serial")
public class MenuBar extends JMenuBar {

	private final ClientMain host;
	private Strategies strategyUI;

	/**
	 * Create Menu bar
	 * 
	 * @param host
	 */
	public MenuBar(ClientMain host) {
		this.host = host;

		this.setFont(new Font("helvetiva", Font.PLAIN, 13));
		// create menu
		JMenu systemMenu = new JMenu("System");
		JMenu chartsMenu = new JMenu("Charts");
		JMenu tradingMenu = new JMenu("Trading");
		JMenu helpMenu = new JMenu("Help");

		// add menu bars
		this.add(systemMenu);
		this.add(chartsMenu);
		this.add(tradingMenu);
		this.add(helpMenu);

		// create items
		JMenuItem loginItem = new JMenuItem("Login");
		JMenuItem exitItem = new JMenuItem("Exit");
		JMenuItem historyItem = new JMenuItem("History");
		JMenuItem comingSoonItem = new JMenuItem("Coming features");
		JMenuItem openChartViewItem = new JMenuItem("Open ChartView");
		JMenuItem detailsItem = new JMenuItem("Login Details");
		JMenuItem controlsItem = new JMenuItem("Controls");
		JMenuItem strategiesItem = new JMenuItem("Strategies");

		// system menu
		exitAction(exitItem);
		loginAction(loginItem);
		// charts menu
		chartAction(openChartViewItem);

		// help menu
		loginDetailsAction(detailsItem);
		controlsAction(controlsItem);
		comingSoonAction(comingSoonItem);

		// trading menu
		strategiesAction(strategiesItem);
		historyAction(historyItem);

		// add items
		systemMenu.add(loginItem);
		systemMenu.add(exitItem);

		tradingMenu.add(strategiesItem);
		tradingMenu.add(historyItem);

		chartsMenu.add(openChartViewItem);

		helpMenu.add(detailsItem);
		helpMenu.add(controlsItem);
		helpMenu.add(comingSoonItem);

	}

	/**
	 * 
	 * @param openChart
	 */
	private void chartAction(JMenuItem openChart) {
		openChart.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				host.showChartFrame();
			}
		});

	}

	/**
	 * Exit action
	 * 
	 * @param exit
	 */
	private void exitAction(JMenuItem exit) {
		exit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Client.stop();
			}
		});
	}

	/**
	 * Reboot program action
	 * 
	 * @param login
	 */
	private void loginAction(JMenuItem login) {
		login.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Client.reboot();
			}
		});
	}

	/**
	 * Open strategy UI
	 */

	private void strategiesAction(JMenuItem strategiesItem) {
		strategiesItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				ArrayList<Strategy> strategies = host.getTradeController()
						.getStrategyController().getStrategies();

				strategyUI = new Strategies(strategies, host
						.getTradeController().getStrategyController());
				

				strategyUI.setLocationRelativeTo(null);
				strategyUI.setVisible(true);
			}
		});
		
	}
	/**
	 * Open history text file action
	 * 
	 * @param history
	 */
	private void historyAction(JMenuItem history) {
		history.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				StoreHistory storer = new StoreHistory();

				String filePath = storer.getFile().getAbsolutePath();

				String nameOS = "os.name";
				Runtime load = Runtime.getRuntime();
				try {
					if (System.getProperty(nameOS).contains("Mac OS X")) {
						load.exec("/Applications/TextEdit.app/Contents/MacOS/TextEdit "
								+ filePath);
					} else {
						String newPath = filePath.replaceAll("\\\\", "/");
						load.exec("C:/Program Files/Windows NT/Accessories/wordpad.exe "
								+ newPath);
					}
				} catch (IOException io) {
					System.out.println("Couldn't open file" + " "
							+ io.getMessage());
				}

			}
		});
	}

	public void loginDetailsAction(JMenuItem detailsItem) {

		detailsItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				JOptionPane.showMessageDialog(new JFrame(), "Logged in as: "
						+ host.getLoggedInUser().getUsername() + " since "
						+ host.getLoggedInUser().getLoginTime());
			}
		});
	}

	public void controlsAction(JMenuItem controlsItem) {

		controlsItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				JOptionPane
						.showMessageDialog(new JFrame(),
								"Windows: Hold \"Shift\" while scrolling to scroll horinzontally.");
			}
		});
	}

	public void comingSoonAction(JMenuItem comingSoonItem) {

		comingSoonItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFrame frame = new JFrame("Coming soon - this is temp");
				frame.setLayout(new BorderLayout());

				DefaultListModel listModel = new DefaultListModel();

				frame.add(new JScrollPane(new JList(listModel)));
				frame.add(
						new JLabel(
								"Green = high priority, Red = low priority, Grey = normal priority"),
						BorderLayout.NORTH);

				String prio = new String(
						"<html><font color = GREEN>"
								+ "User ID storage in text file to recognize balance, username etc."
								+ "</font></html>");
				String lowPrio = new String("<html><font color = RED>"
						+ "Full database.db" + "</font></html>");

				listModel.addElement(prio);
				listModel
						.addElement("Better data storage of orders and positions");
				listModel
						.addElement("Add frame for user to choose starting time (like a calendar) after login");
				listModel
						.addElement("Add \"register\" to login window for first time users who then has to select default values");
				listModel
						.addElement("Draw arrows when added order (should change color depending on profit)");
				listModel.addElement("Strategies!");
				listModel
						.addElement("Strategy interaction with drawing objects");
				listModel.addElement("Correct leverages");
				listModel.addElement(lowPrio);

				frame.pack();
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
		});
	}

}
