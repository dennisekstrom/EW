package client;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import trading.Strategy;

import forex.ForexConstants;
import forex.ForexException;

/**
 * Position class. TODO ändra så att den bara behöver ta typ en trade som
 * inparameter.
 * 
 * @author Tobias
 * 
 */

public class Position {

	private static Calendar cal = new GregorianCalendar(ForexConstants.GMT);

	private double profit;
	private final double openRate;
	private Date openTime;
	private long openTimeMillis;
	private long closeTime;
	private Order order;
	private final String tradeID;
	private Strategy strategy;

	boolean isClosed;

	public Position(Order order, double openRate, Date openTime) {
		if (!order.getOrderCommand().equals(OrderCommand.CLOSE)) {
			this.order = order;
			if (order.isCloseTradeOrder()) {
				this.tradeID = order.getClosedTrade().getTradeID();
				this.closeTime = order.getClosedTrade().getCloseTime()
						.getTimeInMillis();
			} else
				this.tradeID = order.getTrade().getTradeID();
		} else {
			throw new ForexException(
					"A close order can't be added as a position");
		}

		this.openRate = openRate;
		this.openTime = openTime;
		profit = 0;

		isClosed = false;
	}

	/**
	 * Constructor to be used by strategies
	 * 
	 * @param order
	 * @param openRate
	 * @param openTime
	 * @param strategyID
	 */
	public Position(Order order, double openRate, Date openTime, Strategy strat) {
		this.setStrategy(strat);
		if (!order.getOrderCommand().equals(OrderCommand.CLOSE)) {
			this.order = order;
			if (order.isCloseTradeOrder()) {
				this.tradeID = order.getClosedTrade().getTradeID();
				this.closeTime = order.getClosedTrade().getCloseTime()
						.getTimeInMillis();
			} else
				this.tradeID = order.getTrade().getTradeID();
		} else {
			throw new ForexException(
					"A close order can't be added as a position");
		}

		this.openRate = openRate;
		this.openTime = openTime;
		profit = 0;

		isClosed = false;
	}

	public Position(Order order, double openRate, long openTimeMillis) {
		if (!order.getOrderCommand().equals(OrderCommand.CLOSE)) {
			this.order = order;
			this.tradeID = order.getTrade().getTradeID();
		} else {
			throw new ForexException(
					"A close order can't be added as a position");
		}

		this.openRate = openRate;
		this.openTimeMillis = openTimeMillis;
		profit = 0;

		isClosed = false;
	}

	/**
	 * Closes this position
	 * 
	 * @param position
	 *            close rate
	 * @param position
	 *            close time
	 */
	public void close(Calendar cal) {
		closeTime = cal.getTimeInMillis();
		isClosed = true;
	}

	public double getOpenRate() {
		return openRate;
	}

	/**
	 * @return position close rate
	 */
	public Double getCloseRate() {
		if (isClosed)
			return openRate + profit;
		else
			return null;
	}

	public Date getOpenTime() {
		return openTime;
	}

	public long getOpenTimeInMillis() {
		return openTime.getTime();
	}

	public long getCloseTime() {
		return closeTime;
	}

	public String getStartTimeStringRepresentation() {
		return String.format("%1$tY/%1$tm/%1$td %1$tT", cal);
	}

	/**
	 * @return the order amount of this position
	 */
	public double getAmount() {
		return order.getAmount();
	}

	/**
	 * @return the order for this position
	 */
	public Order getOrder() {
		return order;
	}

	public String getBuySell() {
		if (order.isCloseTradeOrder())
			return order.getClosedTrade().getBuySell();
		else
			return order.getTrade().getBuySell();
	}

	/**
	 * @return the profit of this position
	 */
	public double getProfit() {
		if (order.isCloseTradeOrder())
			return order.getClosedTrade().getGrossPL();
		else
			return order.getTrade().getGrossPL();
	}

	/**
	 * Updates this position's profit according to current tick
	 * 
	 * @param current
	 *            tick
	 */
	// public void adjustProfit(ITick tick) {
	// if (order.getOrderCommand().equals(OrderCommand.BUY))
	// this.profit = tick.getAsk() - openRate;
	// else if (order.getOrderCommand().equals(OrderCommand.SELL))
	// this.profit = openRate - tick.getBid();
	//
	// profit *= order.getAmount() * TempConstants.LEVERAGE; // TODO temp lot
	// // size = 1000
	// }

	/**
	 * Set spread to position
	 * 
	 * @param spread
	 */
	public void setSpreadLoss(double spread) {
		profit -= spread * TempConstants.LEVERAGE;
	}

	@Override
	public String toString() {
		// @formatter:off
		if (order.isCloseTradeOrder())
			return String.format(
					"<html>%s to %s<br>%s  %-4s  %s P/L: %.2fEUR</html>",
					String.format("%1$tY/%1$tm/%1$td %1$tT", getOpenTime()),
					String.format("%1$tY/%1$tm/%1$td %1$tT", order
							.getClosedTrade().getCloseTime().getTime()), order
							.getInstrument().toString(), getBuySell(),
					getAmount(), order.getClosedTrade().getGrossPL());
		else if (strategy != null) {
			return String
					.format("<html>%s %s<br>%s  %-4s  %s P/L: %.2fEUR  %.2f Pips</html>",
							String.format("%1$tY/%1$tm/%1$td %1$tT",
									getOpenTime()), strategy.getStrategyName(),
							order.getInstrument().toString(), getBuySell(),
							getAmount(), order.getTrade().getGrossPL(), order
									.getTrade().getPL());

		} else
			return String.format(
					"<html>%s <br>%s  %-4s  %s P/L: %.2fEUR  %.2f Pips</html>",
					String.format("%1$tY/%1$tm/%1$td %1$tT", getOpenTime()),
					order.getInstrument().toString(), getBuySell(),
					getAmount(), order.getTrade().getGrossPL(), order
							.getTrade().getPL());
		// @formatter:on
	}

	public String getInstrument() {
		return order.getInstrument();
	}

	public String getTradeID() {
		return tradeID;
	}

	public long getOpenTimeMillis() {
		return openTimeMillis;
	}

	public Strategy getStrategy() {
		return strategy;
	}

	public void setStrategy(Strategy strategy) {
		this.strategy = strategy;
	}

}
