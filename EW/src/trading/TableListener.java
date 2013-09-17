package trading;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import com.fxcore2.*;

public class TableListener implements IO2GTableListener {

	private String mRequestID;
	private List<O2GTradeRow> mTrades;
	private OrderMonitor mOrderMonitor;
	private final Semaphore mSemaphore;

	public TableListener() {
		mRequestID = "";
		mOrderMonitor = null;
		mTrades = new ArrayList<O2GTradeRow>();
		mSemaphore = new Semaphore(0);
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

	public void onRequestFailed(String requestID, String error) {
		if (mRequestID.equals(requestID)) {
			System.out.println("Request failed, error: " + error);
			mSemaphore.release();
		}
	}

	public void subscribeTableListener(O2GTableManager manager) {
		O2GOrdersTable ordersTable = (O2GOrdersTable) manager
				.getTable(O2GTableType.ORDERS);
		O2GTradesTable tradesTable = (O2GTradesTable) manager
				.getTable(O2GTableType.TRADES);
		O2GMessagesTable messagesTable = (O2GMessagesTable) manager
				.getTable(O2GTableType.MESSAGES);
		O2GClosedTradesTable closedTradesTable = (O2GClosedTradesTable) manager
				.getTable(O2GTableType.CLOSED_TRADES);
		ordersTable.subscribeUpdate(O2GTableUpdateType.INSERT, this);
		ordersTable.subscribeUpdate(O2GTableUpdateType.DELETE, this);
		tradesTable.subscribeUpdate(O2GTableUpdateType.INSERT, this);
		closedTradesTable.subscribeUpdate(O2GTableUpdateType.INSERT, this);
		messagesTable.subscribeUpdate(O2GTableUpdateType.INSERT, this);
	}

	public void unsubscribeTableListener(O2GTableManager manager) {
		O2GOrdersTable ordersTable = (O2GOrdersTable) manager
				.getTable(O2GTableType.ORDERS);
		O2GTradesTable tradesTable = (O2GTradesTable) manager
				.getTable(O2GTableType.TRADES);
		O2GMessagesTable messagesTable = (O2GMessagesTable) manager
				.getTable(O2GTableType.MESSAGES);
		O2GClosedTradesTable closedTradesTable = (O2GClosedTradesTable) manager
				.getTable(O2GTableType.CLOSED_TRADES);
		ordersTable.unsubscribeUpdate(O2GTableUpdateType.INSERT, this);
		ordersTable.unsubscribeUpdate(O2GTableUpdateType.DELETE, this);
		tradesTable.unsubscribeUpdate(O2GTableUpdateType.INSERT, this);
		closedTradesTable.unsubscribeUpdate(O2GTableUpdateType.INSERT, this);
		messagesTable.unsubscribeUpdate(O2GTableUpdateType.INSERT, this);
	}

	// Implementation of IO2GTableListener interface public method onAdded
	@Override
	public void onAdded(String rowID, O2GRow rowData) {
		O2GTableType type = rowData.getTableType();
		switch (type) {
			case ORDERS:
				O2GOrderRow orderRow = (O2GOrderRow) rowData;

				if (mRequestID.equals(orderRow.getRequestID())) {
					if ((OrderMonitor.isClosingOrder(orderRow) || OrderMonitor
							.isOpeningOrder(orderRow)) && mOrderMonitor == null) {
						System.out
								.println("The order has been added. Order ID: "
										+ orderRow.getOrderID() + " Rate: "
										+ orderRow.getRate()
										+ " Time In Force: "
										+ orderRow.getTimeInForce());
						mOrderMonitor = new OrderMonitor(orderRow);
					}
				}
				break;
			case TRADES:
				O2GTradeRow tradeRow = (O2GTradeRow) rowData;
				if (mOrderMonitor != null) {
					mOrderMonitor.onTradeAdded(tradeRow);
					if (mOrderMonitor.isOrderCompleted()) {
						mTrades = mOrderMonitor.getTrades();
						printResult();
						mSemaphore.release();
					}
				}
				break;
			case CLOSED_TRADES:
				O2GClosedTradeRow closedTradeRow = (O2GClosedTradeRow) rowData;
				if (mOrderMonitor != null) {
					mOrderMonitor.onClosedTradeAdded(closedTradeRow);
					if (mOrderMonitor.isOrderCompleted()) {
						printResult();
						mSemaphore.release();
					}
				}
				break;
			case MESSAGES:
				O2GMessageRow messageRow = (O2GMessageRow) rowData;
				if (mOrderMonitor != null) {
					mOrderMonitor.onMessageAdded(messageRow);
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

	// Implementation of IO2GTableListener interface public method onChanged
	@Override
	public void onChanged(String rowID, O2GRow rowData) {
	}

	// Implementation of IO2GTableListener interface public method onDeleted
	@Override
	public void onDeleted(String rowID, O2GRow rowData) {
		if (rowData.getTableType() == O2GTableType.ORDERS) {
			O2GOrderRow orderRow = (O2GOrderRow) rowData;
			if (mRequestID.equals(orderRow.getRequestID())) {
				System.out.println("The order has been deleted. Order ID: "
						+ orderRow.getOrderID());
				mOrderMonitor.onOrderDeleted(orderRow);
				if (mOrderMonitor != null) {
					if (mOrderMonitor.isOrderCompleted()) {
						printResult();
						mSemaphore.release();
					}
				}
			}
		}
	}

	// Implementation of IO2GTableListener interface public method onStatus
	@Override
	public void onStatusChanged(O2GTableStatus status) {
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
					System.out
							.println("The order has been rejected. OrderID = "
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
}
