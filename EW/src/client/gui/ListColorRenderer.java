package client.gui;

import java.awt.Color;
import java.awt.Component;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import trading.Strategy;

/**
 * Custom list color renderer, used for coloring list elements.
 * 
 * @author tobbew92
 * 
 */
@SuppressWarnings("serial")
public class ListColorRenderer extends JLabel implements ListCellRenderer {

	public ListColorRenderer() {
		setOpaque(true);
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		// Assumes the stuff in the list has a pretty toString
		//If value is a strategy, use this custom texting
		if (value.toString().equals("Strategy")) {
			Strategy strat = (Strategy) value;
			if (strat.getCurrentTrade() != null)
				setText("<html>" + strat.getStrategyName() + " Gross: "
						+ strat.getTotalGross() + "EUR<br>"
						+ strat.getInstrument() + " "
						+ strat.getCurrentTrade().getBuySell() + " "
						+ strat.getCurrentTrade().getAmount() + " "
						+ strat.getTimeFrame() + "</html>");
			else
				setText("<html>" + strat.getStrategyName() + " Gross: "
						+ strat.getTotalGross() + "EUR<br>"
						+ strat.getInstrument() + " " + strat.getTimeFrame()
						+ " (No open positions yet) </html>");
		} else
			setText(value.toString());

		// based on the index you set the color. This produces the every other
		// effect.
		Color green = new Color(152, 251, 152);
		Color red = new Color(250, 128, 114);
		Color purple = new Color(221, 160, 221);
		Color orange = new Color(240, 165, 0);
		Color blue = new Color(0, 206, 209);
		String stringValue = value.toString();

		Pattern p = Pattern.compile("\\| High \\|");
		Matcher m = p.matcher(stringValue);

		Pattern p2 = Pattern.compile("\\| Medium \\|");
		Matcher m2 = p2.matcher(stringValue);

		Pattern p3 = Pattern.compile("\\| Low \\|");
		Matcher m3 = p3.matcher(stringValue);

		Pattern p4 = Pattern.compile("Holiday");
		Matcher m4 = p4.matcher(stringValue);

		Pattern p5 = Pattern.compile("P/L: (-|0\\.00)");
		Matcher m5 = p5.matcher(stringValue);

		Pattern p6 = Pattern.compile("P/L: [0-9]");
		Matcher m6 = p6.matcher(stringValue);

		Pattern p7 = Pattern.compile("Gross: [1-9]");
		Matcher m7 = p7.matcher(stringValue);

		Pattern p8 = Pattern.compile("Gross: -");
		Matcher m8 = p8.matcher(stringValue);

		if (m.find())
			setBackground(red);
		else if (m2.find())
			setBackground(orange);
		else if (m3.find())
			setBackground(green);
		else if (m4.find())
			setBackground(purple);
		else if (m5.find())
			setBackground(red);
		else if (m6.find())
			setBackground(green);
		else if (m7.find())
			setBackground(green);
		else if (m8.find())
			setBackground(red);
		else
			setBackground(Color.WHITE);

		if (isSelected)
			setBackground(blue);

		setBorder(BorderFactory.createRaisedBevelBorder());

		return this;
	}
}