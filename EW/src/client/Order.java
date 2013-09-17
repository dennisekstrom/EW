package client;

import trading.Strategy;

import com.fxcore2.O2GClosedTradeRow;
import com.fxcore2.O2GTradeTableRow;

/**
 * Order class. An order is created when either buy, sell or close commands are
 * called.
 * 
 * @author Tobias
 * 
 */

public class Order {

	private final OrderCommand orderCommand;
	private final double amount;
	private final String instrument;
	private O2GTradeTableRow trade;
	private O2GClosedTradeRow closedTrade;
	private boolean isCloseTradeOrder = false;
	private Strategy strategy = null;

	/**
	 * Constructor for live trade order
	 * 
	 * @param instrument
	 * @param orderCommand
	 * @param amount
	 * @param trade
	 */
	public Order(String instrument, OrderCommand orderCommand, double amount,
			O2GTradeTableRow trade) {
		this.amount = amount;
		this.orderCommand = orderCommand;
		this.instrument = instrument;
		this.trade = trade;

	}

	/**
	 * Constructor for live trade order, strategy version
	 * 
	 * @param instrument
	 * @param orderCommand
	 * @param amount
	 * @param trade
	 */
	public Order(String instrument, OrderCommand orderCommand, double amount,
			O2GTradeTableRow trade, Strategy strat) {
		this.amount = amount;
		this.orderCommand = orderCommand;
		this.instrument = instrument;
		this.trade = trade;
		this.strategy = strat;

	}

	/**
	 * Constructor for close trade order
	 * 
	 * @param instrument
	 * @param orderCommand
	 * @param amount
	 * @param trade
	 */
	public Order(String instrument, OrderCommand orderCommand, double amount,
			O2GClosedTradeRow closedTrade) {
		this.amount = amount;
		this.orderCommand = orderCommand;
		this.instrument = instrument;
		this.setClosedTrade(closedTrade);
		isCloseTradeOrder = true;

	}

	/**
	 * Constructor for backtrace trade order.
	 * 
	 * @param instrument
	 * @param orderCommand
	 * @param amount
	 */
	public Order(String instrument, OrderCommand orderCommand, double amount) {
		this.amount = amount;
		this.orderCommand = orderCommand;
		this.instrument = instrument;
		this.trade = null;
	}

	public double getAmount() {
		return amount;
	}

	public OrderCommand getOrderCommand() {
		return orderCommand;
	}

	public String getInstrument() {
		return instrument;
	}

	public O2GTradeTableRow getTrade() {
		return trade;
	}

	public O2GClosedTradeRow getClosedTrade() {
		return closedTrade;
	}

	public void setClosedTrade(O2GClosedTradeRow closedTrade) {
		this.closedTrade = closedTrade;
	}

	public boolean isCloseTradeOrder() {
		return isCloseTradeOrder;
	}

	public Strategy getStrategy() {
		return strategy;
	}

}
