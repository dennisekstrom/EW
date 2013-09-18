package client;

import io.StoreHistory;

import java.util.ArrayList;
import java.util.Collections;

import javax.swing.SwingWorker;

import trading.Strategy;
import trading.TradeController;

import client.gui.ClientMain;
import client.gui.ClosedPositionPanel;
import client.gui.PositionPanel;

import com.fxcore2.O2GClosedTradeRow;
import com.fxcore2.O2GClosedTradesTable;
import com.fxcore2.O2GTradeTableRow;
import com.fxcore2.O2GTradesTable;

/**
 * Handles orders and positions. Implements feed to update each positions
 * accordingly. Also updates the host (Client UI) to display the current values.
 * 
 * TODO needs to be adjusted to handle backtrace feed and not noly realtime
 * FXCM.
 * 
 * @author Tobias
 * 
 */
public class PositionController {

	private final ClientMain host;

	// stores closed positions
	public ArrayList<Position> closedPositions;
	// stores open positions
	public ArrayList<Position> openPositions;

	private final StoreHistory printRead;

	private double currentAskRate;
	private double currentBidRate;

	private Double previousAskRate;
	private Double previousBidRate;

	public double dailyProfit;
	// private double openProfit;

	private final User user;

	// components to be notified about changes
	private final ClosedPositionPanel closedPosPanel;
	private final PositionPanel positionPanel;

	private boolean updatingGUI = false;

	// private final DrawingPanel drawingHost;

	public PositionController(ClientMain host, User user,
			PositionPanel positionPanel, ClosedPositionPanel closedPosPanel) {
		openPositions = new ArrayList<Position>();
		closedPositions = new ArrayList<Position>();

		printRead = new StoreHistory();

		this.host = host;
		this.user = user;
		this.closedPosPanel = closedPosPanel;
		this.positionPanel = positionPanel;
		// drawingHost = host.getChartFrame().getChartPanel().getDrawingPanel();

	}

	public ArrayList<Position> getOpenPositions() {
		return openPositions;
	}

	/**
	 * Handles this order and stores it in text file.
	 * 
	 * @param order
	 */
	public void handleOrder(Order order) {
		Position pos = null;
		double spread = currentAskRate - currentBidRate;

		// Create and add positions
		if (ClientMain.isLive) {
			if (order.getStrategy() == null)
				pos = new Position(order, currentAskRate, order.getTrade()
						.getOpenTime().getTime());
			else
				pos = new Position(order, currentAskRate, order.getTrade()
						.getOpenTime().getTime(), order.getStrategy());

		}

		openPositions.add(pos);
		// pos.setSpreadLoss(spread);

		if (order.getOrderCommand().equals(OrderCommand.BUY)) {
			// drawingHost.draw(new UpSignalArrow(drawingHost, new
			// ChartPoint(
			// pos.getOpenTime(), pos.getOpenRate())));
		} else if (order.getOrderCommand().equals(OrderCommand.SELL)) {
			// drawingHost.draw(new DownSignalArrow(drawingHost,
			// new ChartPoint(pos.getOpenTime(), pos.getOpenRate())));
		}

		// write to file
		// printRead.writeToFile(order, feed.getCurrentTime());

		// update positionPanel
		// TODO beh���vs denna?
		updateGUI(host.getTradeController(), false);
	}

	// /**
	// * Closes this position.
	// *
	// * @param position
	// */
	// public void closePosition(Position position, TradeController
	// tradeController) {
	//
	// if (position.getOrder().getOrderCommand().equals(OrderCommand.BUY)) {
	// position.close(currentAskRate, feed.getCurrentTime());
	// // draw line from open to close
	// } else if (position.getOrder().getOrderCommand()
	// .equals(OrderCommand.SELL)) {
	// position.close(currentBidRate, feed.getCurrentTime());
	// // draw line from open to close
	// } else
	// throw new ForexException(
	// "position shouldn't have close order as order");
	//
	// // update stuff
	// openPositions.remove(position);
	// closedPosPanel.addClosedPosition(position);
	// positionPanel.setPositions(openPositions);
	// tradeController.closePosition(position.getOrder().getTrade());
	//
	// // Update balance and daily profit
	// dailyProfit += position.getProfit();
	// user.setBalance(user.getBalance() + position.getProfit()
	// * TempConstants.LEVERAGE);
	// }

	public void closePosition(O2GClosedTradeRow trade) {

		try {
			for (Position pos : openPositions) {
				if (pos.getTradeID().equals(trade.getTradeID())) {
					pos.close(trade.getCloseTime());
					openPositions.remove(pos);
					closedPosPanel.addClosedPosition(pos);
					// positionPanel.setPositions(openPositions);
					break;
				}
			}

			updateGUI(host.getTradeController(), true);

		} catch (Exception e) {
			System.out.println("hej");
		}
	}

	/**
	 * Close all open positions
	 */
	public void closeAllPositions(TradeController tradeController) {
		ArrayList<Position> positionsTemp = new ArrayList<Position>();

		for (int i = 0; i < openPositions.size(); i++)
			positionsTemp.add(null);

		Collections.copy(positionsTemp, openPositions);

		for (Position pos : positionsTemp)
			tradeController.closePosition(pos.getOrder().getTrade());

	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (Position position : openPositions) {
			sb.append("\n" + position.getOrder().getInstrument().toString()
					+ " " + position.getOrder().getOrderCommand().toString()
					+ " " + position.getOrder().getAmount() + " - "
					+ position.getOpenTime() + "\n " + position.getProfit());
		}

		return sb.toString();
	}

	public void updateDailyProfitLabel(double dailyProfit) {
		host.setOpenProfitLabelText(String.format("Daily profit: %.2f EUR",
				dailyProfit));
	}

	public void updateRateLabel(double askRate, double bidRate) {
		previousAskRate = currentAskRate;
		previousBidRate = currentBidRate;
		currentBidRate = bidRate;
		currentAskRate = askRate;
		if (previousAskRate == null || previousBidRate == null) {
			host.getEntryPanel().setAskRate(currentAskRate, 0D);
			host.getEntryPanel().setBidRate(currentBidRate, 0D);
		}
		host.getEntryPanel().setAskRate(currentAskRate,
				currentAskRate - previousAskRate);
		host.getEntryPanel().setBidRate(currentBidRate,
				currentBidRate - previousBidRate);
	}

	public void updateBalanceLabel(double balance) {
		user.setBalance(balance);
		host.setBalanceLabelText(String.format("Balance: %.2f EUR",
				user.getBalance()));
	}

	public ClientMain getHost() {
		return host;
	}

	// @Override
	// public void onTick(Instrument instrument, ITick tick) {

	// previousAskRate = currentAskRate;
	// previousBidRate = currentBidRate;
	//
	// // use closed bar rate as current rate
	// currentAskRate = tick.getAsk();
	// currentBidRate = tick.getBid();

	// for (Position position : openPositions)
	// position.adjustProfit(tick);

	// host.repaintPositionPanel();

	// update rate label in client
	// updateRateLabel();
	// update balance label
	// updateBalanceLabel();
	// update daily profit label
	// updateDynamicDailyProfit();
	// updateOpenProfitLabel();

	// }

	/**
	 * Update positions, closed positions and active stategies in the GUI.
	 * 
	 * @param tradeController
	 * @param updateClosedPositions
	 */
	public void updateGUI(TradeController tradeController,
			boolean updateClosedPositions) {
		if (!updatingGUI) {
			updatingGUI = true;
			new UpdateTask(tradeController, updateClosedPositions).execute();
		}

	}

	private class UpdateTask extends SwingWorker<Void, Integer> {

		private final TradeController tradeController;
		private final boolean updateClosedPositions;

		public UpdateTask(TradeController tradeController,
				boolean updateClosedPositions) {
			this.tradeController = tradeController;
			this.updateClosedPositions = updateClosedPositions;
		}

		@Override
		protected void done() {
			updatingGUI = false;
		}

		@Override
		protected Void doInBackground() throws Exception {

			O2GTradesTable tradesTable = tradeController.getTradesTable();
			O2GClosedTradesTable closedTradesTable = null;
			if (updateClosedPositions)
				closedTradesTable = tradeController.getClosedTradesTable();

			// update open positions
			openPositions.clear();
			for (int i = 0; i < tradesTable.size(); i++) {
				Position position;
				O2GTradeTableRow trade = tradesTable.getRow(i);
				// if (trade == null)
				// continue;
				String instrument = tradeController.getInstrumentName(trade);

				boolean isStrategyTrade = false;
				Strategy strategy = null;

				for (Strategy strat : host.getTradeController()
						.getStrategyController().getStrategies()) {
					if (strat.getCurrentTrade() == null)
						continue;
					if (strat.getCurrentTrade().getTradeID()
							.equals(trade.getTradeID())) {
						strategy = strat;
						isStrategyTrade = true;
						break;
					}
				}

				if (trade.getBuySell().equals("B")) {
					if (isStrategyTrade)
						position = new Position(new Order(instrument,
								OrderCommand.BUY, trade.getAmount(), trade),
								trade.getOpenRate(), trade.getOpenTime()
										.getTime(), strategy);
					else
						position = new Position(new Order(instrument,
								OrderCommand.BUY, trade.getAmount(), trade),
								trade.getOpenRate(), trade.getOpenTime()
										.getTime());

				} else {
					if (isStrategyTrade)
						position = new Position(new Order(instrument,
								OrderCommand.SELL, trade.getAmount(), trade),
								trade.getOpenRate(), trade.getOpenTime()
										.getTime(), strategy);
					else
						position = new Position(new Order(instrument,
								OrderCommand.SELL, trade.getAmount(), trade),
								trade.getOpenRate(), trade.getOpenTime()
										.getTime());
				}
				openPositions.add(position);
			}

			// update closed positions
			if (updateClosedPositions) {
				closedPositions.clear();
				if (closedTradesTable != null) {
					for (int i = 0; i < closedTradesTable.size(); i++) {
						Position position;
						O2GClosedTradeRow trade = closedTradesTable.getRow(i);
						String instrument = tradeController
								.getInstrumentName(trade);
						if (trade.getBuySell().equals("B")) {
							position = new Position(
									new Order(instrument, OrderCommand.BUY,
											trade.getAmount(), trade),
									trade.getOpenRate(), trade.getOpenTime()
											.getTime());
						} else {
							position = new Position(
									new Order(instrument, OrderCommand.SELL,
											trade.getAmount(), trade),
									trade.getOpenRate(), trade.getOpenTime()
											.getTime());
						}
						closedPositions.add(position);
					}

				}
			}

			// open positions
			positionPanel.setPositions(openPositions);

			// closed positions
			if (updateClosedPositions) {
				closedPosPanel.setClosedPositions(closedPositions);

				tradeController.getStrategyController()
						.getActiveStrategiesPanel().setStrategies();
			}

			positionPanel.repaint();
			closedPosPanel.repaint();
			return null;
		}
	}
}
