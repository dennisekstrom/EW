package client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JTabbedPane;

import client.Client;
import client.PositionController;
import client.User;

import swingjd.util.PanelType;
import swingjd.util.Theme;
import trading.TradeController;

import chart.ChartFrame;

import feed.RealTimeFeed;
import forex.ForexConstants;

/**
 * Main client frame. Functions as a host frame for other components. Other than
 * components it has a feed, a user and a position controller. When the
 * constructor is invoked the feed is started.
 * 
 * @author Tobias
 * 
 */
@SuppressWarnings("serial")
public class ClientMain extends JFrame {

	private static final String TITLE = "WEForex Alpha";
	private static final Dimension FRAME_SIZE = new Dimension(1024, 512);

	//private RealTimeFeed feed;

	private final User loggedInUser;
	private final TradeController tradeController;

	private PositionController positionController;

	// Strategies and real order handling
	// private Provider provider;

	// real time trading
	public static boolean isLive = false;

	// GUI
	private JMenuBar menuBar;
	private ToolBar toolbar;
	private final PositionPanel positionPanel;
	private final ActiveStrategies activeStrategiesPanel;
	private WestPanel entryPanel;
	private JTabbedPane positionTabbedPanel;
	private final ClosedPositionPanel closedPositionPanel;
	private final UpcomingEvents eventsPanel;
	private BottomPanel bottomPanel;

	// other frames
	//private ChartFrame chartFrame;

	// south panel
	public JLabel balanceLabel;
	public JLabel openProfitLabel;

	private JLabel clockLabel;
	private JLabel userNameLabel;

	private static Calendar time = new GregorianCalendar(ForexConstants.GMT);

	/**
	 * Create the main frame and start feed.
	 * 
	 * @param user
	 * @param start
	 *            time of feed
	 */
	public ClientMain(User user, long startTime,
			TradeController tradeController) {
		super(TITLE);

		this.loggedInUser = user;
		this.tradeController = tradeController;

		// TODO these parameters should be set in a login window
		// feed = new TimeRelativeFeed(TempConstants.defaultInstrument,
		// TempConstants.defaultTickInterval,
		// TempConstants.defaultTickBarSize, TempConstants.defaultSpeed,
		// startTime, TempConstants.defaultUpdateInterval);
//		feed = new RealTimeFeed(TempConstants.defaultInstrument,
//				TempConstants.defaultTickInterval,
//				TempConstants.defaultTickBarSize, TempConstants.defaultSpeed,
//				startTime, TempConstants.defaultUpdateInterval);

		closedPositionPanel = new ClosedPositionPanel();
		positionPanel = new PositionPanel();
		eventsPanel = new UpcomingEvents();
		activeStrategiesPanel = new ActiveStrategies(tradeController);

		initChart(startTime);

		this.positionController = new PositionController(this, feed,
				loggedInUser, positionPanel, closedPositionPanel);
		tradeController.addPositionController(positionController);
		positionController.updateGUI(tradeController, true);

		feed.addListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				time.setTimeInMillis((Long) evt.getNewValue());
				clockLabel.setText(String.format("%1$tY/%1$tm/%1$td %1$tR",
						time));
			}
		});
		// feed.addCurrentTimeListener(new PropertyChangeListener() {
		//
		// @Override
		// public void propertyChange(PropertyChangeEvent evt) {
		// time.setTimeInMillis((Long) evt.getNewValue());
		// clockLabel.setText(String.format("%1$tY/%1$tm/%1$td %1$tR",
		// time));
		// }
		// });

		// start feed
		feed.startFeed();

		// create components
		initGUI();

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				dispose();
				Client.stop();
			}
		});
	}

	private void initChart(long startTime) {
//		chartFrame = new ChartFrame(feed, TempConstants.defaultInstrument,
//				TempConstants.defaultTickBarSize, TempConstants.defaultPeriod,
//				TempConstants.defaultOfferSide, startTime, true);
//
//		chartFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//		chartFrame.setLocationRelativeTo(null);
	}

	/**
	 * Create main frame for client application
	 */
	private void initGUI() {
		// set layout
		this.setLayout(new BorderLayout(6, 6));
		this.setBackground(Color.LIGHT_GRAY);
		this.setMinimumSize(new Dimension(920, 520));

		// create components
		menuBar = new MenuBar(this);
		entryPanel = new WestPanel(tradeController);
		toolbar = new ToolBar(tradeController, positionController,
				positionPanel);
		positionTabbedPanel = new JTabbedPane();
		bottomPanel = new BottomPanel(Theme.GLOSSY_NAVYBLUE_THEME,
				PanelType.PANEL_RECTANGULAR);
		bottomPanel.setBackground(Color.WHITE);

		// create labels
		balanceLabel = new JLabel("" + loggedInUser.getBalance());
		openProfitLabel = new JLabel();
		String dateTime = User.getDateTime() + " ";
		clockLabel = new JLabel(dateTime.replaceAll(":[0-9][0-9] ", ""));
		userNameLabel = new JLabel(loggedInUser.getUsername());

		// position panel
		positionTabbedPanel.setFont(new Font("helvetica", Font.PLAIN, 14));
		positionTabbedPanel.addTab("Open Positions", null, positionPanel,
				"See open positions");
		positionTabbedPanel.addTab("Closed Positions", null,
				closedPositionPanel, "See Closed Positions");
		positionTabbedPanel.addTab("Upcoming Events", null, getEventsPanel());
		positionTabbedPanel.addTab("Active Strategies", null,
				activeStrategiesPanel);
		positionTabbedPanel.setBackground(Color.LIGHT_GRAY);

		// add to bottom panel
		bottomPanel.add(balanceLabel);
		bottomPanel.add(openProfitLabel);
		bottomPanel.add(clockLabel);
		bottomPanel.add(userNameLabel);

		// add panels and components
		this.setJMenuBar(menuBar);
		this.add(toolbar, BorderLayout.NORTH);
		this.add(entryPanel, BorderLayout.WEST);
		this.add(positionTabbedPanel, BorderLayout.CENTER);
		this.add(bottomPanel, BorderLayout.SOUTH);

		this.setSize(FRAME_SIZE);
	}

	public void setStartTime(long startTime) {
		chartFrame.setVisible(false);

		initChart(startTime);

		initGUI();

		this.positionController = new PositionController(this, feed,
				loggedInUser, positionPanel, closedPositionPanel);

		repaint();

		// TODO these parameters should be set in a separate window
		feed = new RealTimeFeed(TempConstants.defaultInstrument,
				TempConstants.defaultTickInterval,
				TempConstants.defaultTickBarSize, TempConstants.defaultSpeed,
				startTime, TempConstants.defaultUpdateInterval);

	}

	public WestPanel getEntryPanel() {
		return entryPanel;
	}

	public User getLoggedInUser() {
		return loggedInUser;
	}

	public void showChartFrame() {
		chartFrame.setLocationRelativeTo(null);
		chartFrame.setVisible(true);
	}

	public void setBalanceLabelText(String text) {
		balanceLabel.setText(text);
		balanceLabel.repaint();
	}

	public void setOpenProfitLabelText(String text) {
		openProfitLabel.setText(text);
		openProfitLabel.repaint();
	}

	//	public void repaintPositionPanel() {
	//		positionPanel.repaint();
	//	}
	//
	//	public void repaintClosedPositionPanel() {
	//		closedPositionPanel.repaint();
	//	}

	public void updateClock() {
		String dateTime = User.getDateTime() + " ";
		clockLabel.setText(dateTime.replaceAll(":[0-9][0-9] ", ""));
		clockLabel.repaint();
	}

	public ChartFrame getChartFrame() {
		return chartFrame;
	}

	public TradeController getTradeController() {
		return tradeController;
	}

	public UpcomingEvents getEventsPanel() {
		return eventsPanel;
	}

}
