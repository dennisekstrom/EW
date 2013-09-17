package client.gui;

import javax.swing.JCheckBox;

import trading.Strategy;

/**
 * Basically a checkbox with a strategy attribute
 * 
 * @author tobbew92
 * 
 */
@SuppressWarnings("serial")
public class StrategyCheckBox extends JCheckBox{
	
	Strategy strategy;
	

	public StrategyCheckBox(String name, Strategy strategy) {
		super(name);
		this.strategy = strategy;
	}

	public Strategy getStrategy() {
		return strategy;
	}
	
}
