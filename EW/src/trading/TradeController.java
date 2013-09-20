package trading;

import client.PositionController;

import com.fxcore2.*;

import strategies.EURUSDLowFreq;
import strategies.EURUSDEvents;
import strategies.EURUSDHighFreq;
import strategies.AUDUSDEvents;
import trading.util.StrategyOfferListener;
import trading.util.SessionStatusListener;
import trading.util.User;

import com.fxcore2.O2GOfferTableRow;

import feed.LiveOfferFeed;

/**
 * TradeController handles the ForexConnect API provided by FXCM. This class can
 * be used to both trade with a FXCM demo account or a real account.
 * 
 * Important: Add the library path as your working directory
 * 
 * @author Tobias W
 * 
 */

public class TradeController {

	private O2GSession session = null;
	private SessionStatusListener statusListener;
	private ResponseListener responseListener;
	private StrategyOfferListener offersListener;
	private OrderController orderController = null;
	private O2GTableManager tableManager;
	private TableListener tableListener;
	private O2GTradesTable tradesTable;
	private O2GClosedTradesTable closedTradesTable;
	private final User user;
	private PositionController positionController = null;
	O2GOffersTable offers;

	private final int orderCounter = 0;

	// Strategy controller, handles strategies
	private StrategyController stratControl;
	private String instrument;
	private O2GAccountRow account = null;

	public TradeController(String id, String password, String url,
			String connection, String instrument) {
		user = new User(id, password, url, connection);
		this.instrument = instrument;

	}

	/**
	 * Login to FXCM client
	 * 
	 * @param Account
	 *            info
	 */
	public void login() {
		// Create a session, subscribe to session listener, login, get accounts,
		// logout
		try {
			session = O2GTransport.createSession();
			stratControl = new StrategyController();
			setStatusListener(new SessionStatusListener(session, "", ""));
			setResponseListener(new ResponseListener(session, this));
			tableListener = new TableListener();
			session.subscribeSessionStatus(getStatusListener());
			// Use the table manager
			session.useTableManager(O2GTableManagerMode.YES, null);
			// Starts live update for chosen instrument
			session.subscribeResponse(getResponseListener());
			session.login(getUser().getId(), getUser().getPassword(), getUser()
					.getUrl(), getUser().getConnection());

			while (!getStatusListener().isConnected()
					&& !getStatusListener().hasError()) {
				Thread.sleep(50);
			}
			// create and prepare order controller
			orderController = new OrderController(this.getSession(),
					responseListener);
			orderController.prepareParamsFromLoginRules(this.getSession()
					.getLoginRules());

			// Get live feed
			O2GTableManager manager = session.getTableManager();
			if (manager == null)
				return;
			while (manager.getStatus() == O2GTableManagerStatus.TABLES_LOADING) {
				Thread.sleep(50);
			}

			offers = null;
			offersListener = null;
			if (manager.getStatus() == O2GTableManagerStatus.TABLES_LOADED) {
				offers = (O2GOffersTable) manager.getTable(O2GTableType.OFFERS);

				// Set EUR/USD offer listener
				offersListener = new StrategyOfferListener(stratControl, this);
				offersListener.SetInstrumentFilter(getInstrument());

				// TODO removed for testing
				// offers.subscribeUpdate(O2GTableUpdateType.UPDATE,
				// OffersFeed.getInstance());

				// Set up trade table
				tableManager = session.getTableManager();

				tradesTable = (O2GTradesTable) tableManager
						.getTable(O2GTableType.TRADES);
				closedTradesTable = (O2GClosedTradesTable) tableManager
						.getTable(O2GTableType.CLOSED_TRADES);
				// printTrades(tradesTable);
			}

			// ENDTEST

		} catch (Exception e) {
			System.out.println("Exception: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Unsubscribe to live feed update
	 */
	public void unsubsribeUpdate() {
		offers.subscribeUpdate(O2GTableUpdateType.UPDATE,
				LiveOfferFeed.getInstance());
		offersListener.addToFeed();
	}

	/**
	 * Subscribe to live feed update
	 */
	public void subscribeUpdate() {
		offers.subscribeUpdate(O2GTableUpdateType.UPDATE,
				LiveOfferFeed.getInstance());
		offersListener.removeFromFeed();
	}

	/**
	 * Logout from FXCM client. This method doesn't cancel orders/closes
	 * positions.
	 * 
	 * @param mSession
	 * @param statusListener
	 * @throws InterruptedException
	 */
	public void logout() throws InterruptedException {

		if (getStatusListener() == null || session == null) {
			throw new InterruptedException("You are not logged in");
		}

		if (!getStatusListener().hasError()) {
			getAccount();
			session.logout();
			while (!getStatusListener().isDisconnected()) {
				Thread.sleep(50);
			}
		}

		session.unsubscribeSessionStatus(getStatusListener());
		session.dispose();
		System.out.println("STATUS: Logged out");

	}

	/**
	 * Get account information
	 * 
	 * @param session
	 */
	public O2GAccountRow getAccount() {
		if (account == null) {
			O2GAccountRow acc = null;
			try {
				O2GLoginRules loginRules = session.getLoginRules();
				if (loginRules != null
						&& loginRules
								.isTableLoadedByDefault(O2GTableType.ACCOUNTS)) {
					O2GResponse accountsResponse = loginRules
							.getTableRefreshResponse(O2GTableType.ACCOUNTS);
					O2GResponseReaderFactory responseFactory = session
							.getResponseReaderFactory();
					O2GAccountsTableResponseReader accountsReader = responseFactory
							.createAccountsTableReader(accountsResponse);
					for (int i = 0; i < accountsReader.size(); i++) {
						acc = accountsReader.getRow(i);
						// System.out.println("AccountID = " +
						// account.getAccountID()
						// + " Balance = " + account.getBalance()
						// + " UsedMargin = " + account.getUsedMargin());
					}
				}
			} catch (Exception e) {
				System.out.println("Exception in getAccounts():\n\t "
						+ e.getMessage());
			}
			return acc;
		}
		return account;
	}

	/**
	 * Create a position with limit and stop attached
	 * 
	 * TODO fixa (fungerar inte)
	 */

	// public O2GTradeTableRow createELS(String instrument, String orderType,
	// double amount, double limitRate, double stopRate) {
	//
	//
	//
	// O2GRequestFactory requestFactory = session
	// .getRequestFactory();
	// if (requestFactory != null) {
	// O2GValueMap valueMap = requestFactory
	// .createValueMap();
	// valueMap.setString(O2GRequestParamsEnum.COMMAND,
	// "CreateOrder");
	// valueMap.setString(O2GRequestParamsEnum.ORDER_TYPE,
	// orderType);
	// valueMap.setString(O2GRequestParamsEnum.ACCOUNT_ID,
	// mAccountID);
	// valueMap.setString(O2GRequestParamsEnum.OFFER_ID,
	// mOfferID);
	// valueMap.setString(O2GRequestParamsEnum.BUY_SELL,
	// mBuySell);
	// valueMap.setDouble(O2GRequestParamsEnum.RATE, limitRate);
	// valueMap.setInt(O2GRequestParamsEnum.AMOUNT,
	// amount);
	// valueMap.setString(O2GRequestParamsEnum.CUSTOM_ID,
	// "EntryOrderWithStopLimit");
	// valueMap.setDouble(O2GRequestParamsEnum.RATE_LIMIT,
	// limitRate);
	// valueMap.setDouble(O2GRequestParamsEnum.RATE_STOP,
	// stopRate);
	// O2GRequest request = requestFactory
	// .createOrderRequest(valueMap);
	// O2GOrdersTable ordersTable = (O2GOrdersTable) tableManager
	// .getTable(O2GTableType.ORDERS);
	// TableListener tableListener = new TableListener();
	// ordersTable.subscribeUpdate(
	// O2GTableUpdateType.INSERT, tableListener);
	// session.sendRequest(request);
	// Thread.sleep(1000);
	// ordersTable.unsubscribeUpdate(
	// O2GTableUpdateType.INSERT, tableListener);
	//
	// // Edit secondary limit order (we will increase price by 50 pips)
	// mLimitOrderID = tableListener.getOrderID();
	// mRateLimit = mRateLimit + 50 * mPointSize;
	// O2GValueMap changeMap = requestFactory
	// .createValueMap();
	// changeMap.setString(O2GRequestParamsEnum.COMMAND,
	// "EditOrder");
	// changeMap.setString(O2GRequestParamsEnum.ORDER_ID,
	// mLimitOrderID);
	// changeMap
	// .setString(O2GRequestParamsEnum.ACCOUNT_ID,
	// mAccountID);
	// changeMap.setDouble(O2GRequestParamsEnum.RATE,
	// mRateLimit);
	//
	// // Send request using change ValueMap
	// O2GRequest changeRequest = requestFactory
	// .createOrderRequest(changeMap);
	// ordersTable.subscribeUpdate(
	// O2GTableUpdateType.UPDATE, tableListener);
	// session.sendRequest(changeRequest);
	// Thread.sleep(1000);
	// ordersTable.unsubscribeUpdate(
	// O2GTableUpdateType.UPDATE, tableListener);
	//
	//
	// // Remove secondary limit order
	// O2GValueMap removeMap = requestFactory
	// .createValueMap();
	// removeMap.setString(O2GRequestParamsEnum.COMMAND,
	// "DeleteOrder");
	// removeMap.setString(O2GRequestParamsEnum.ORDER_ID,
	// mLimitOrderID);
	//
	// // Send request using remove ValueMap
	// O2GRequest removeRequest = requestFactory
	// .createOrderRequest(removeMap);
	// ordersTable.subscribeUpdate(
	// O2GTableUpdateType.DELETE, tableListener);
	// session.sendRequest(removeRequest);
	// Thread.sleep(1000);
	// ordersTable.unsubscribeUpdate(
	// O2GTableUpdateType.DELETE, tableListener);
	// }
	// return tradesTable.getRow(0);
	//
	// }

	/**
	 * Open a position in client and FXCM, minimum amount is 1000. Set
	 * parameters to null if they are not used, except for instrument, buysell
	 * and amount which is obligatory.
	 * 
	 * @param mInstrument
	 * @param mBuySell
	 * @param amount
	 * @param limit
	 * @param stopLoss
	 * @param strat
	 * @return
	 */
	public boolean openPosition(String mInstrument, String mBuySell,
			int amount, Double limit, Double stopLoss, Strategy strat) {
		if (strat != null && strat.currentTrade != null)
			return false;

		// Set up strategy parameters for openings a position
		if (strat != null) {
			strat.setIncomingTrade(true);
			strat.setCurrentStrategy(strat);
			if (limit != null && stopLoss != null) {
				strat.setWithLimitAndStop(true);
				strat.setLimit(limit);
				strat.setStopLoss(stopLoss);
			}
		}

		String mAccountID = "";
		String mOfferID = "";

		// Create a session, subscribe to session listener, response listener,
		// login, open position, logout, unsubscribe
		try {
			O2GResponseReaderFactory readerFactory = session
					.getResponseReaderFactory();
			if (readerFactory != null) {
				O2GResponse response;
				O2GLoginRules loginRules = session.getLoginRules();
				response = loginRules
						.getTableRefreshResponse(O2GTableType.ACCOUNTS);
				O2GAccountsTableResponseReader accountsResponseReader = readerFactory
						.createAccountsTableReader(response);
				if (accountsResponseReader.size() > 0) {
					O2GAccountRow accountRow = accountsResponseReader.getRow(0); // The
																					// first
																					// account
					mAccountID = accountRow.getAccountID();
					response = loginRules
							.getTableRefreshResponse(O2GTableType.OFFERS);
					O2GOffersTableResponseReader offersResponseReader = readerFactory
							.createOffersTableReader(response);
					for (int i = 0; i < offersResponseReader.size(); i++) {
						O2GOfferRow offerRow = offersResponseReader.getRow(i);
						if (mInstrument.equals(offerRow.getInstrument())) {
							mOfferID = offerRow.getOfferID();
							break;
						}
					}
					if (!mOfferID.isEmpty()) {

						if (strat == null) {
							responseListener.setStrategyTrade(null);
							openPosition(session, mAccountID, mOfferID,
									mBuySell, amount, getResponseListener());
						} else {
							responseListener.setStrategyTrade(strat);
							openPosition(session, mAccountID, mOfferID,
									mBuySell, amount, getResponseListener());
						}

					} else {
						System.out
								.println("Cannot find offer for specified instrument: "
										+ mInstrument);
					}
				} else {
					System.out.println("Cannot find an account");
				}
			} else {
				System.out.println("Cannot create response reader factory");
			}
		} catch (Exception e) {
			System.out.println("Exception: " + e.getMessage());
			System.exit(1);
		}

		return true;
	}

	// Opens a position (only used by the openPosition method)
	private boolean openPosition(O2GSession session, String accountID,
			String offerID, String buySell, int amount,
			ResponseListener responseListener) throws InterruptedException {
		boolean result;
		O2GRequestFactory requestFactory = session.getRequestFactory();
		if (requestFactory == null) {
			System.out.println("Cannot create request factory");
			return false;
		}
		O2GValueMap valuemap = requestFactory.createValueMap();
		valuemap.setString(O2GRequestParamsEnum.COMMAND,
				Constants.Commands.CreateOrder);
		valuemap.setString(O2GRequestParamsEnum.ORDER_TYPE,
				Constants.Orders.TrueMarketOpen);
		valuemap.setString(O2GRequestParamsEnum.ACCOUNT_ID, accountID);
		valuemap.setString(O2GRequestParamsEnum.OFFER_ID, offerID);
		valuemap.setString(O2GRequestParamsEnum.BUY_SELL, buySell);
		valuemap.setInt(O2GRequestParamsEnum.AMOUNT, amount);
		valuemap.setString(O2GRequestParamsEnum.CUSTOM_ID, "OpenMarketOrder");
		O2GRequest request = requestFactory.createOrderRequest(valuemap);
		if (request != null) {
			responseListener.setRequestID(request.getRequestId());
			session.sendRequest(request);
			// responseListener.waitEvents();
			// Call to PositionController is done in ResponseListener

			// Thread.sleep(1000);
			// position opened successfully
			result = true;
		} else {
			System.out.println(requestFactory.getLastError());
			result = false;
		}

		return result;
	}

	/**
	 * Closes the specified trade
	 * 
	 * @param trade
	 * @throws InterruptedException
	 */
	public void closePosition(O2GTradeRow trade) {
		O2GOffersTable offersTable = (O2GOffersTable) tableManager
				.getTable(O2GTableType.OFFERS);
		O2GTableIterator iterator = new O2GTableIterator();
		O2GOfferTableRow offer = offersTable.getNextRowByColumnValue(
				"Instrument", instrument, iterator);
		if (offer != null) {
			if (trade == null) {
				O2GTradesTable tradesTable = (O2GTradesTable) tableManager
						.getTable(O2GTableType.TRADES);
				O2GTableIterator tradesIterator = new O2GTableIterator();
				trade = tradesTable.getNextRowByColumnValue("OfferID",
						offer.getOfferID(), tradesIterator);
			}
			if (trade != null) {
				tableListener.subscribeTableListener(tableManager);

				try {
					if (closePosition(session, trade, tableListener)) {
						// TODO BEH���������VS DENNA?
						// Thread.sleep(1000);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				tableListener.unsubscribeTableListener(tableManager);
			} else {
				System.out.println("There is no trade to close");
			}
		} else {
			System.out.println("Cannot find offer for specified instrument: "
					+ instrument);
		}
	}

	private boolean closePosition(O2GSession session, O2GTradeRow tradeRow,
			TableListener tableListener) throws InterruptedException {
		boolean result;
		O2GRequestFactory requestFactory = session.getRequestFactory();
		if (requestFactory == null) {
			System.out.println("Cannot create request factory");
			return false;
		}
		O2GLoginRules loginRules = session.getLoginRules();
		O2GPermissionChecker permissionChecker = loginRules
				.getPermissionChecker();
		O2GValueMap valuemap = requestFactory.createValueMap();
		valuemap.setString(O2GRequestParamsEnum.COMMAND, "CreateOrder");
		valuemap.setString(O2GRequestParamsEnum.ACCOUNT_ID,
				tradeRow.getAccountID());
		valuemap.setString(O2GRequestParamsEnum.OFFER_ID, tradeRow.getOfferID());
		valuemap.setString(O2GRequestParamsEnum.BUY_SELL, tradeRow.getBuySell()
				.equals(Constants.Buy) ? Constants.Sell : Constants.Buy);
		valuemap.setString(O2GRequestParamsEnum.CUSTOM_ID, "CloseMarketOrder");
		valuemap.setInt(O2GRequestParamsEnum.AMOUNT, tradeRow.getAmount());
		if (permissionChecker.canCreateMarketCloseOrder(instrument) != O2GPermissionStatus.PERMISSION_ENABLED) {
			valuemap.setString(O2GRequestParamsEnum.ORDER_TYPE,
					Constants.Orders.TrueMarketOpen); // in USA you need to use
														// "OM" to close a
														// position.
		} else {
			valuemap.setString(O2GRequestParamsEnum.ORDER_TYPE,
					Constants.Orders.TrueMarketClose);
			valuemap.setString(O2GRequestParamsEnum.TRADE_ID,
					tradeRow.getTradeID());
		}
		O2GRequest request = requestFactory.createOrderRequest(valuemap);
		if (request != null) {
			tableListener.setRequestID(request.getRequestId());
			session.sendRequest(request);
			tableListener.waitEvents();
			// close position was succcessful

			result = true;
		} else {
			System.out.println(requestFactory.getLastError());
			result = false;
		}
		return result;
	}

	/**
	 * Set up strategies
	 */
	public void setUpStrategies() {

		Strategy test = new EURUSDLowFreq(stratControl, this);
		stratControl.addStrategy(test, "EUR/USD");

		Strategy test2 = new EURUSDEvents(stratControl, this);
		stratControl.addStrategy(test2, "EUR/USD");

		Strategy test3 = new EURUSDHighFreq(stratControl, this);
		stratControl.addStrategy(test3, "EUR/USD");

		Strategy audUsdEvent = new AUDUSDEvents(stratControl, this);
		stratControl.addStrategy(audUsdEvent, "AUD/USD");

	}

	/**
	 * Attach a limit rate for a trade. Always use 5 decimals!
	 * 
	 * Returns true if successful
	 * 
	 * @param trade
	 */
	public void setLimit(O2GTradeRow trade, double limit, Strategy strat) {
		responseListener.setActiveStrategy(strat);
		if (trade.getBuySell().equals("B")) {
			if (trade.getOpenRate() > limit) {
				orderController
						.attachLimit(trade, trade.getOpenRate() + 0.0025);
				return;
			}
		} else if (trade.getBuySell().equals("S")) {
			if (trade.getOpenRate() < limit) {
				orderController
						.attachLimit(trade, trade.getOpenRate() - 0.0025);
				return;
			}
		}

		orderController.attachLimit(trade, limit);
	}

	/**
	 * Attach a stop rate for a trade. Always use 5 decimals!
	 * 
	 * Returns order ID if successful
	 * 
	 * @param trade
	 */
	public void setStopLoss(O2GTradeRow trade, double stop, Strategy strat) {
		responseListener.setActiveStrategy(strat);
		if (trade.getBuySell().equals("B")) {
			if (trade.getOpenRate() < stop + 0.0005) {
				orderController.attachStop(trade, trade.getOpenRate() - 0.0015);
				return;
			}
		} else if (trade.getBuySell().equals("S")) {
			if (trade.getOpenRate() > stop - 0.0005) {
				orderController.attachStop(trade, trade.getOpenRate() + 0.0015);
				return;
			}
		}
		orderController.attachStop(trade, stop);
	}

	public boolean editStopLoss(double stop, String orderID, Strategy strat) {
		if (orderID == "")
			return false;
		responseListener.setActiveStrategy(strat);
		orderController.editStop(strat.currentTrade, stop, orderID);
		return true;
	}

	public boolean editLimit(double limit, String orderID, Strategy strat) {
		if (orderID == "")
			return false;
		responseListener.setActiveStrategy(strat);
		orderController.editLimit(strat.currentTrade, limit, orderID);
		return true;
	}

	public String getInstrumentName(O2GTradeRow tradeRow) {
		O2GTableManager tableMgr = session.getTableManager();
		if (tableMgr == null)
			return "EUR/USD";
		O2GTableManagerStatus managerStatus = tableMgr.getStatus();
		while (managerStatus == O2GTableManagerStatus.TABLES_LOADING) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			managerStatus = tableMgr.getStatus();
		}
		if (managerStatus == O2GTableManagerStatus.TABLES_LOAD_FAILED)
			try {
				throw new Exception("Tables loading failed");
			} catch (Exception e) {
				e.printStackTrace();
			}

		O2GOffersTable offersTable = (O2GOffersTable) tableMgr
				.getTable(O2GTableType.OFFERS);
		O2GOfferTableRow offerRow = offersTable.findRow(tradeRow.getOfferID());
		return offerRow.getInstrument();
	}

	public String getInstrumentName(O2GClosedTradeRow tradeRow) {
		O2GTableManager tableMgr = session.getTableManager();
		O2GTableManagerStatus managerStatus = tableMgr.getStatus();
		while (managerStatus == O2GTableManagerStatus.TABLES_LOADING) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			managerStatus = tableMgr.getStatus();
		}
		if (managerStatus == O2GTableManagerStatus.TABLES_LOAD_FAILED)
			try {
				throw new Exception("Tables loading failed");
			} catch (Exception e) {
				e.printStackTrace();
			}

		O2GOffersTable offersTable = (O2GOffersTable) tableMgr
				.getTable(O2GTableType.OFFERS);
		O2GOfferTableRow offerRow = offersTable.findRow(tradeRow.getOfferID());
		return offerRow.getInstrument();
	}

	/**
	 * Print current offers from all availabe instruments.
	 * 
	 * @param offers
	 */
	public void printOffers(O2GOffersTable offers) {
		try {
			O2GOfferTableRow offer = null;
			O2GTableIterator iterator = new O2GTableIterator();
			offer = offers.getNextRow(iterator);
			while (offer != null) {
				System.out.println("Instrument = " + offer.getInstrument()
						+ " Bid Price = " + offer.getBid() + " Ask Price = "
						+ offer.getAsk() + " PipCost = " + offer.getPipCost());
				offer = offers.getNextRow(iterator);
			}
		} catch (Exception e) {
			System.out.println("Exception in getOffers().\n\t "
					+ e.getMessage());
		}
	}

	/**
	 * Get current session
	 * 
	 * @return
	 */
	public O2GSession getSession() {
		return session;
	}

	public String getInstrument() {
		return instrument;
	}

	public void setInstrument(String instrument) {
		this.instrument = instrument;
	}

	public SessionStatusListener getStatusListener() {
		return statusListener;
	}

	public void setStatusListener(SessionStatusListener statusListener) {
		this.statusListener = statusListener;
	}

	public ResponseListener getResponseListener() {
		return responseListener;
	}

	public void setResponseListener(ResponseListener responseListener) {
		this.responseListener = responseListener;
	}

	public User getUser() {
		return user;
	}

	public PositionController getPositionController() {
		return this.positionController;
	}

	public O2GTradesTable getTradesTable() {
		return tradesTable;
	}

	public O2GClosedTradesTable getClosedTradesTable() {
		return closedTradesTable;
	}

	/**
	 * Sets ut the position controller
	 */

	public void addPositionController(PositionController positionController) {
		this.positionController = positionController;

	}

	public int getOrderCounter() {
		return orderCounter;
	}

	public void changeOffersListenerInstrument(String instrument) {
		offersListener.SetInstrumentFilter(instrument);
	}

	public StrategyController getStrategyController() {
		return stratControl;
	}

	public void updateAccount(O2GAccountRow account) {
		this.account = account;
	}

	// Print Trades Table
	public static void printTrades(O2GTradesTable table) {
		try {
			TableListener tableListener = new TableListener();
			table.subscribeStatus(tableListener);
			System.out.println("\nPrinting Trades Table\n");
			for (int i = 0; i < table.size(); i++) {
				O2GTradeRow trade = table.getRow(i);
				System.out.println("TradeID = " + trade.getTradeID()
						+ " BuySell = " + trade.getBuySell() + " Amount = "
						+ trade.getAmount());
			}
			table.unsubscribeStatus(tableListener);
		} catch (Exception e) {
			System.out.println("Exception in getTrades().\n\t "
					+ e.getMessage());
		}
	}

}
