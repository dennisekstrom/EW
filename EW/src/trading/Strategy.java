package trading;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import trading.util.Event;
import trading.util.EventInterpreter;
import trading.util.HistData;

import com.fxcore2.O2GTradeRow;
import com.sun.mail.smtp.SMTPTransport;

import forex.Offer;

/**
 * Abstract class for a strategy. A strategy is designed to have a maximum of
 * one trade.
 * 
 * 
 * @author Tobias W
 * 
 */
public abstract class Strategy implements IStrategyListener {

	// Protected fields to be used in strategies

	protected final StrategyController stratControl;
	// Strategy instrument
	protected String instrument;
	// Historic data list, recommended to initialize in onStart(), with the
	// getHistoricData method
	protected ArrayList<Offer> historicData;
	// Current trade opened by strategy (a strategy has max one trade)
	protected O2GTradeRow currentTrade = null;
	// The order ID for the stop loss for the current trade ("" if it doesn't
	// exist)
	protected String stopLossOrderID = "";
	// The order ID for the limit for the current trade ("" if it doesn't exist)
	protected String limitOrderID = "";
	// List of upcoming events (events which occur on the next event scheduled
	// time)
	protected ArrayList<Event> upcomingEvents;
	// Used to check if algorithm is active by Strategy Controller
	protected boolean isAlgorithmActive = false;

	// Private fields to be used in this class
	// Trade controller
	private final TradeController tradeControl;
	// Historic Data object
	private final HistData histData;
	// Keeps track if a trade is being opened
	private boolean incomingTrade = false;

	// private int backTraceInMillis;
	private Offer previousRow;
	private boolean firstTick = true;
	private boolean isActive = false;
	private boolean hasLaunched = false;
	private boolean isPaused = false;
	private String strategyName;
	private Strategy currentStrategy;

	public Strategy(StrategyController stratControl,
			TradeController tradeController, String strategyName) {
		this.stratControl = stratControl;
		this.setStrategyName(strategyName);
		histData = new HistData(tradeController.getSession(),
				tradeController.getStatusListener(),
				tradeController.getResponseListener());
		tradeControl = tradeController;
	}

	/**
	 * Get historic data from the current time to the specified amount of
	 * minutes back in time. The method will not consider a weekend, so use it
	 * wisely.
	 * 
	 * Examples of time frames: t1=tick, m5=five minutes, H1=one hour, M1=one
	 * month
	 * 
	 * @param instrument
	 * @param timeFrame
	 * @param minutesBack
	 * @return historic data list, should be added to inherited 'historicData'
	 *         field
	 */
	public ArrayList<Offer> getHistoricData(String instrument,
			String timeFrame, int minutesBack) {

		// double backTraceInMillis = getBackTraceInMillis(timeFrame);

		Date date = new Date();

		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(date.getTime() - minutesBack * 60 * 1000);

		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		String fromTime = dateFormat.format(calendar.getTime());
		String toTime = dateFormat.format(date);

		System.out.println("From: " + fromTime + " To: " + toTime);

		ArrayList<Offer> history = histData.getHist(instrument, timeFrame,
				fromTime, toTime);

		return history;
	}

	/**
	 * Is called every correct strategy instrument tick to add data to historic
	 * array list. Will add according to the timeframe specified when historic
	 * data was collected (specified by the strategy).
	 * 
	 * @param row
	 * @param historicData
	 */
	public void addRowToHistoricData(Offer data, Strategy strat) {
		// add all incoming data to historic data
		if (firstTick) {
			previousRow = data;
			firstTick = false;
		}
		if (data.getTime() - previousRow.getTime() >= getBackTraceInMillis(strat
				.getTimeFrame())) {
			previousRow = data;
			strat.historicData.add(data);
			// this.historicData.add(OfferData.convertRow(row));
		}
	}

	/**
	 * Should be called frequently be a strategy to gather a result
	 */
	public Event getEventResult(Event ev) {
		EventInterpreter ei = new EventInterpreter();
		Event event = ei.interpret(ev);

		return event;
	}

	/**
	 * This strategy's reliability for default technical analysis
	 */
	public abstract strategies.Reliability getDefaultReliability();

	/**
	 * This strategy's reliability in fundamental times
	 */
	public abstract strategies.Reliability getFundamentalReliability();

	/**
	 * This strategy's reliability during consolidation
	 */
	public abstract strategies.Reliability getConsolidationReliability();

	/**
	 * This strategy's reliability during high volatility
	 */
	public abstract strategies.Reliability getHighVolatilityReliability();

	/**
	 * @return time frame for historic data (null if no historic data has been
	 *         fetched)
	 */
	public abstract String getTimeFrame();

	public abstract String getInstrument();

	public abstract double getTotalGross();

	/**
	 * This method should be used for strategy algorithms which can be a bit
	 * heavy on the CPU. When using Indicators, this is the safe place to have
	 * them. Also, this method is only called whenever a strategy instrument
	 * tick is received. The strategy instrument is defined when you add a
	 * strategy to the StrategyController.
	 */
	public abstract void strategyAlgorithm(Offer row);

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public String getStrategyName() {
		return strategyName;
	}

	public void setStrategyName(String strategyName) {
		this.strategyName = strategyName;
	}

	public boolean hasLaunched() {
		return hasLaunched;
	}

	public void setLaunched(boolean hasLaunched) {
		this.hasLaunched = hasLaunched;
	}

	public boolean isPaused() {
		return isPaused;
	}

	public void setIsPaused(boolean paused) {
		isPaused = paused;
	}

	public void setCurrentStrategy(Strategy strat) {
		currentStrategy = strat;
	}

	private double limit = 0;
	private double stopLoss = 0;
	private boolean withLimitAndStop = false;

	// /**
	// * Set limit to a position
	// */
	//
	// protected void setLimit(O2GTradeRow trade, double limit) {
	// tradeControl.setLimit(trade, limit);
	// }

	/**
	 * TODO var tvungen att g�ra s�h�r, eftersom tradeCOntroller.openPosition
	 * returnerade null av n�gon anledning n�r den kallades i onTick()-metoden
	 * (annars inte!).
	 */
	@Override
	public void notifyOpenedTrade(O2GTradeRow trade) {

		if (currentStrategy != null) {
			if (currentStrategy.isIncomingTrade()) {
				if (currentStrategy.currentTrade == null) {
					currentStrategy.currentTrade = trade;
					currentStrategy.setIncomingTrade(false);
				}
			}

			if (withLimitAndStop) {
				tradeControl.setLimit(trade, getLimit(), currentStrategy);
				tradeControl.setStopLoss(trade, getStopLoss(), currentStrategy);
			}
		}

		currentStrategy = null;
	}

	public O2GTradeRow getCurrentTrade() {
		return currentTrade;
	}

	/**
	 * Send an email via weforexstrategy@gmail.com to a recipient address
	 * 
	 * @param senderUsername
	 * @param senderPassword
	 * @param recipientAddress
	 * @param subject
	 * @param message
	 */
	protected void sendEmailNotification(String recipientAddress,
			String subject, String message) {

		final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

		// Get a Properties object
		Properties props = System.getProperties();
		props.setProperty("mail.smtps.host", "smtp.gmail.com");
		props.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
		props.setProperty("mail.smtp.socketFactory.fallback", "false");
		props.setProperty("mail.smtp.port", "465");
		props.setProperty("mail.smtp.socketFactory.port", "465");
		props.setProperty("mail.smtps.auth", "true");

		/*
		 * If set to false, the QUIT command is sent and the connection is
		 * immediately closed. If set to true (the default), causes the
		 * transport to wait for the response to the QUIT command. ref :
		 * http://java
		 * .sun.com/products/javamail/javadocs/com/sun/mail/smtp/package
		 * -summary.html http://forum.java.sun.com/thread.jspa?threadID=5205249
		 * smtpsend.java - demo program from javamail
		 */
		props.put("mail.smtps.quitwait", "false");

		Session session = Session.getInstance(props, null);

		// -- Create a new message --
		final MimeMessage msg = new MimeMessage(session);

		// -- Set the FROM and TO fields --
		try {
			msg.setFrom(new InternetAddress("weforexstrategy" + "@gmail.com"));
			msg.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(recipientAddress, false));
		} catch (AddressException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			msg.setSubject(subject);
			msg.setText(message, "utf-8");
			msg.setSentDate(new Date());
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			SMTPTransport t = (SMTPTransport) session.getTransport("smtps");

			t.connect("smtp.gmail.com", "weforexstrategy", "dennistobias");
			t.sendMessage(msg, msg.getAllRecipients());
			t.close();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SendFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private double getBackTraceInMillis(String timeFrame) {
		double backTraceInMillis = 0;
		if (timeFrame == "m5") {
			backTraceInMillis = 5 * 60 * 1000;
		} else if (timeFrame == "m1") {
			backTraceInMillis = 60 * 1000;
		} else if (timeFrame == "m15") {
			backTraceInMillis = 15 * 60 * 1000;
		} else if (timeFrame == "m30") {
			backTraceInMillis = 30 * 60 * 1000;
		} else if (timeFrame == "H1") {
			backTraceInMillis = 60 * 60 * 1000;
		} else if (timeFrame == "H2") {
			backTraceInMillis = 2 * 60 * 60 * 1000;
		} else if (timeFrame == "H3") {
			backTraceInMillis = 3 * 60 * 60 * 1000;
		} else if (timeFrame == "H4") {
			backTraceInMillis = 4 * 60 * 60 * 1000;
		} else if (timeFrame == "H6") {
			backTraceInMillis = 6 * 60 * 60 * 1000;
		} else if (timeFrame == "H8") {
			backTraceInMillis = 8 * 60 * 60 * 1000;
		} else if (timeFrame == "D1") {
			backTraceInMillis = 24 * 60 * 60 * 1000;
		} else if (timeFrame == "W1") {
			backTraceInMillis = 7 * 24 * 60 * 60 * 1000;
		} else if (timeFrame == "M1") {
			backTraceInMillis = 30 * 724 * 60 * 60 * 1000;
		}
		return backTraceInMillis;
	}

	public void setWithLimitAndStop(boolean withLimitAndStop) {
		this.withLimitAndStop = withLimitAndStop;
	}

	public double getStopLoss() {
		return stopLoss;
	}

	public void setStopLoss(double stopLoss) {
		this.stopLoss = stopLoss;
	}

	public double getLimit() {
		return limit;
	}

	public void setLimit(double limit) {
		this.limit = limit;
	}

	public boolean isIncomingTrade() {
		return incomingTrade;
	}

	public void setIncomingTrade(boolean incomingTrade) {
		this.incomingTrade = incomingTrade;
	}

	@Override
	public String toString() {
		return "Strategy";
	}
	// TODO add other characteristics
}
