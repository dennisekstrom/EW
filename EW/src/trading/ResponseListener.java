package trading;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import client.Order;
import client.OrderCommand;
import client.PositionController;
import com.fxcore2.*;

/**
 * ResponseListener listens to changes in different tables. For example, it
 * listens to the trading table so an update is made in the client when a trade
 * is made in the fxcm trading station.
 * 
 * @author Tobias W
 * 
 */

public class ResponseListener implements IO2GResponseListener {

	private final O2GSession mSession;
	private final Semaphore mSemaphore;
	private O2GResponse mResponse = null;
	// private boolean mError = false;
	private String mRequestID;
	private List<O2GTradeRow> mTrades;
	private OrderMonitor mOrderMonitor;
	private final TradeController tradeController;
	private Strategy activeStrategy = null;

	// The strategy that made the incoming trade
	private Strategy strategy = null;

	public ResponseListener(O2GSession session, TradeController tradeController) {
		mRequestID = "";
		mOrderMonitor = null;
		mTrades = new ArrayList<O2GTradeRow>();
		mSemaphore = new Semaphore(0);
		mSession = session;
		this.tradeController = tradeController;
		// mInstrument = instrument;
	}

	// Gets Response
	public O2GResponse getResponse() {
		return mResponse;
	}

	public void setRequestID(String requestID) {
		mRequestID = requestID;
	}

	public void waitEvents() throws InterruptedException {
		mSemaphore.acquire(1);
	}

	public List<O2GTradeRow> getTrades() {
		return mTrades;
	}

	// Implementation of IO2GResponseListener interface public method
	// onRequestCompleted
	@Override
	public void onRequestCompleted(String requestID, O2GResponse response) {
		mResponse = response;
		System.out.println("Request " + requestID + " completed");
		// mError = false;
		// if (response.getType() == O2GResponseType.GET_OFFERS) {
		// mResponse = response;
		// mRequest = "getoffers";
		// }
		// printOffers(mSession, mResponse, mInstrument);
	}

	// Implementation of IO2GResponseListener interface public method
	// onRequestFailed
	@Override
	public void onRequestFailed(String requestID, String error) {
		if (mRequestID.equals(requestID)) {
			System.out.println("Request failed: " + error);
			mSemaphore.release();
		}
		// this.mError = true;
	}

	public void setActiveStrategy(Strategy strat) {
		activeStrategy = strat;
	}

	// Implementation of IO2GResponseListener interface public method
	// onTablesUpdates
	@Override
	public void onTablesUpdates(O2GResponse response) {
		// Live data feed
		// if (response.getType() == O2GResponseType.TABLES_UPDATES) {
		// mResponse = response;
		// mRequest = "tablesupdates";
		// printOffers(mSession, mResponse, mInstrument);
		// }

		O2GResponseReaderFactory factory = mSession.getResponseReaderFactory();
		if (factory != null) {
			O2GTablesUpdatesReader reader = factory
					.createTablesUpdatesReader(response);
			for (int i = 0; i < reader.size(); i++) {
				O2GOrderRow orderRow;
				switch (reader.getUpdateTable(i)) {
				case ACCOUNTS:
					O2GAccountRow account = reader.getAccountRow(i);
					// Show balance updates
					System.out.format("Balance: %.2f%n", account.getBalance());
					tradeController.updateAccount(account);
					break;
				case ORDERS:
					orderRow = reader.getOrderRow(i);
					// if stop order
					if (orderRow.getType().trim().equals("S")) {
						activeStrategy.stopLossOrderID = orderRow.getOrderID();
					}
					// if limit order
					if (orderRow.getType().trim().equals("L")) {
						activeStrategy.limitOrderID = orderRow.getOrderID();
					}

					if (mRequestID.equals(orderRow.getRequestID())) {
						switch (reader.getUpdateType(i)) {
						case INSERT:
							if ((OrderMonitor.isClosingOrder(orderRow) || OrderMonitor
									.isOpeningOrder(orderRow))
									&& mOrderMonitor == null) {
								System.out
										.println("The order has been added. Order ID: "
												+ orderRow.getOrderID()
												+ " Rate: "
												+ orderRow.getRate()
												+ " Time In Force: "
												+ orderRow.getTimeInForce());
								mOrderMonitor = new OrderMonitor(orderRow);
							}
							break;
						case DELETE:
							if (mOrderMonitor != null) {
								System.out
										.println("The order has been deleted. Order ID: "
												+ orderRow.getOrderID());
								mOrderMonitor.onOrderDeleted(orderRow);
								if (mOrderMonitor.isOrderCompleted()) {
									printResult();
									mSemaphore.release();
								}
							}
							break;
						default:
							break;
						}
					}
					break;
				case TRADES:
					O2GTradeRow trade = reader.getTradeRow(i);
					PositionController positionController = null;
					if (reader.getUpdateType(i) == O2GTableUpdateType.INSERT) {
						if (mOrderMonitor != null) {
							mOrderMonitor.onTradeAdded(trade);
							if (mOrderMonitor.isOrderCompleted()) {
								mTrades = mOrderMonitor.getTrades();
								printResult();
								mSemaphore.release();

							}
						}

						// Notify strategies that a position has been opened
						tradeController.getStrategyController()
								.notifyOpenedTrade(trade);

						// Add position to the client.
						positionController = tradeController
								.getPositionController();
						O2GTradesTable tradesTable = tradeController
								.getTradesTable();
						String instrument = tradeController
								.getInstrumentName(trade);

						// check if trade comes from a strategy or not
						if (strategy == null) {
							if (trade.getBuySell().equals("B")) {
								positionController.handleOrder(new Order(
										instrument, OrderCommand.BUY, trade
												.getAmount(), tradesTable
												.getRow(0)));
							} else if (trade.getBuySell().equals("S")) {

								positionController.handleOrder(new Order(
										instrument, OrderCommand.SELL, trade
												.getAmount(), tradesTable
												.getRow(0)));
							}
						} else {
							if (trade.getBuySell().equals("B")) {
								positionController.handleOrder(new Order(
										instrument, OrderCommand.BUY, trade
												.getAmount(), tradesTable
												.getRow(0), strategy));
							} else if (trade.getBuySell().equals("S")) {

								positionController.handleOrder(new Order(
										instrument, OrderCommand.SELL, trade
												.getAmount(), tradesTable
												.getRow(0), strategy));
							}
						}

						// reset if trade came from strategy
						strategy = null;

					}
					break;
				case CLOSED_TRADES:
					O2GClosedTradeRow closedTrade = reader.getClosedTradeRow(i);
					if (reader.getUpdateType(i) == O2GTableUpdateType.INSERT) {
						if (mOrderMonitor != null) {
							mOrderMonitor.onClosedTradeAdded(closedTrade);
							if (mOrderMonitor.isOrderCompleted()) {
								printResult();
								mSemaphore.release();
							}
						}

						positionController = tradeController
								.getPositionController();

						positionController.closePosition(closedTrade);
						tradeController.getStrategyController()
								.notifyClosedTrade(closedTrade);
						// tradeController.getStrategyController()
						// .getActiveStrategiesPanel().setStrategies();
						// positionController.updatePositions(tradeController);
					}
					break;
				case MESSAGES:
					O2GMessageRow message = reader.getMessageRow(i);
					if (reader.getUpdateType(i) == O2GTableUpdateType.INSERT) {
						if (mOrderMonitor != null) {
							mOrderMonitor.onMessageAdded(message);
							if (mOrderMonitor.isOrderCompleted()) {
								printResult();
								mSemaphore.release();
							}
						}
					}
					break;
				default:
					break;
				}
			}
		}
	}

	private void printResult() {
		if (mOrderMonitor != null) {
			OrderMonitor.ExecutionResult result = mOrderMonitor.getResult();
			List<O2GTradeRow> trades;
			List<O2GClosedTradeRow> closedTrades;
			O2GOrderRow order = mOrderMonitor.getOrder();
			String orderID = order.getOrderID();
			trades = mOrderMonitor.getTrades();
			closedTrades = mOrderMonitor.getClosedTrades();
			switch (result) {
			case Canceled:
				if (trades.size() > 0) {
					printTrades(trades, orderID);
					printClosedTrades(closedTrades, orderID);
					System.out
							.println("A part of the order has been canceled. Amount = "
									+ mOrderMonitor.getRejectAmount());
				} else {
					System.out.println("The order: OrderID = " + orderID
							+ " has been canceled.");
					System.out.println("The cancel amount = "
							+ mOrderMonitor.getRejectAmount() + ".");
				}
				break;
			case FullyRejected:
				System.out.println("The order has been rejected. OrderID = "
						+ orderID);
				System.out.println("The rejected amount = "
						+ mOrderMonitor.getRejectAmount());
				System.out.println("Rejection cause: "
						+ mOrderMonitor.getRejectMessage());
				break;
			case PartialRejected:
				printTrades(trades, orderID);
				printClosedTrades(closedTrades, orderID);
				System.out
						.println("A part of the order has been rejected. Amount = "
								+ mOrderMonitor.getRejectAmount());
				System.out.println("Rejection cause: "
						+ mOrderMonitor.getRejectMessage());
				break;
			case Executed:
				printTrades(trades, orderID);
				printClosedTrades(closedTrades, orderID);
				break;
			default:
				break;
			}
		}
	}

	private void printTrades(List<O2GTradeRow> trades, String orderID) {
		if (trades.size() == 0) {
			return;
		}
		System.out.println("For the order: OrderID = " + orderID
				+ " the following positions have been opened: ");
		for (int i = 0; i < trades.size(); i++) {
			O2GTradeRow trade = trades.get(i);
			String tradeID = trade.getTradeID();
			int amount = trade.getAmount();
			double rate = trade.getOpenRate();
			System.out.println("Trade ID: " + tradeID + "; Amount: " + amount
					+ "; Rate: " + rate);
		}
	}

	private void printClosedTrades(List<O2GClosedTradeRow> closedTrades,
			String orderID) {
		if (closedTrades.size() == 0) {
			return;
		}
		System.out.println("For the order: OrderID = " + orderID
				+ " the following positions have been closed: ");
		for (int i = 0; i < closedTrades.size(); i++) {
			O2GClosedTradeRow closedTrade = closedTrades.get(i);
			String tradeID = closedTrade.getTradeID();
			int amount = closedTrade.getAmount();
			double rate = closedTrade.getCloseRate();
			System.out.println("Closed Trade ID: " + tradeID + "; Amount: "
					+ amount + "; Closed Rate: " + rate);
		}
	}

	public Strategy getStrategy() {
		return strategy;
	}

	public void setStrategyTrade(Strategy strat) {
		strategy = strat;

	}
}
