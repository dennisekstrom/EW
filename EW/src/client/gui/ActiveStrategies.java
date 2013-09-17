package client.gui;

import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EtchedBorder;

import trading.Strategy;
import trading.TradeController;

/**
 * UI Panel for all active strategies.
 * 
 * @author Tobias W
 * 
 */

@SuppressWarnings("serial")
public class ActiveStrategies extends JScrollPane {

	// private static String[] positions;
	private final JList list;
	private final DefaultListModel listModel;
	private final TradeController tradeController;

	/**
	 * Create position panel
	 */
	public ActiveStrategies(TradeController tradeController) {
		this.tradeController = tradeController;
		list = new JList();
		listModel = new DefaultListModel();
		list.setModel(listModel);
		list.setFixedCellHeight(35);
		list.setFont(new Font("arial", Font.PLAIN, 15));
		list.setCellRenderer(new ListColorRenderer());

		list.setSelectionModel(new DefaultListSelectionModel() {
			@Override
			public void setSelectionInterval(int index0, int index1) {
				if (list.isSelectedIndex(index0)) {
					list.removeSelectionInterval(index0, index1);
				} else {
					list.addSelectionInterval(index0, index1);
				}
			}
		});
		this.getViewport().setView(list);
		this.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		// set border
		this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));

		tradeController.getStrategyController().addActiveStrategiesPanel(this);
	}

	/**
	 * @return strategy list
	 */
	public JList getList() {
		return list;
	}

	/**
	 * Update and add strategies to list
	 */
	public void setStrategies() {
		listModel.clear();

		for (Strategy strat : tradeController.getStrategyController()
				.getActiveStrategies()) {
			if (strat.getCurrentTrade() != null) {
				String gross = String.format("%.2f", strat.getTotalGross());
				listModel.addElement("<html>" + strat.getStrategyName()
						+ " Gross: " + gross + "EUR<br>"
						+ strat.getInstrument() + " "
						+ strat.getCurrentTrade().getBuySell() + " "
						+ strat.getCurrentTrade().getAmount() + " "
						+ strat.getTimeFrame() + "</html>");
			} else {
				String gross = String.format("%.2f", strat.getTotalGross());
				listModel.addElement("<html>" + strat.getStrategyName()
						+ " Gross: " + gross + "EUR<br>"
						+ strat.getInstrument() + " " + strat.getTimeFrame()
						+ " (No open positions yet) </html>");
			}

		}

	}
}
