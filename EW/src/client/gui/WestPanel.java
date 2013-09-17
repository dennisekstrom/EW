package client.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import swingjd.custom.component.panel.GradientPanel;
import swingjd.util.PanelType;
import swingjd.util.Theme;
import trading.TradeController;

/**
 * Frame to display the static internal entry window.
 * 
 * 
 */

@SuppressWarnings("serial")
public class WestPanel extends GradientPanel {

	private static Font ratePanelFont = new Font("Rate panel font", 20, 20);

	private final String buyTitle = "BUY";
	private final String sellTitle = "SELL";

	private EntryButton buyBtn;
	private EntryButton sellBtn;

	// Combo boxes for choice of currency and amount
	private JComboBox currencyBox;
	private JComboBox amountBox;

	// currencies in use
	private final String[] currencies = { "EUR/USD", "EUR/SEK", "USD/JPY",
			"EUR/GBP", "USD/SEK" };
	private final Integer[] amount = { 1, 2, 3, 4, 5, 10, 20, 30, 40, 50, 100 };

	private static final Color backColor = Color.LIGHT_GRAY;

	// Trade controller to handle trades
	private final TradeController tradeController;

	/**
	 * Create UIEntry
	 * 
	 * @param positionController
	 * @param positionPanel
	 */
	public WestPanel(TradeController tradeController) {
		super(Theme.GRADIENT_BLACK_THEME, PanelType.PANEL_ROUNDED_RECTANGLUR);
		this.tradeController = tradeController;
		init();

	}

	private void init() {
		// create components
		currencyBox = new JComboBox(currencies);
		amountBox = new JComboBox(amount);
		buyBtn = new EntryButton(buyTitle);
		sellBtn = new EntryButton(sellTitle);

		JPanel topBorderPanel = new JPanel();

		currencyBox.setSelectedIndex(0);
		currencyBox.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEmptyBorder(), "Currency"));
		currencyBox.setPreferredSize(new Dimension(50, 50));
		amountBox.setSelectedIndex(0);
		amountBox.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEmptyBorder(), "Amount(K)"));
		amountBox.setPreferredSize(new Dimension(50, 50));

		// combo boxes
		JPanel comboPanel = new JPanel(new GridLayout(1, 3));
		comboPanel.add(currencyBox);
		comboPanel.add(amountBox);
		comboPanel.setFont(new Font("helvetiva", Font.PLAIN, 13));

		// Rate panel
		JPanel ratePanel = new JPanel(new GridLayout(2, 1));
		buyBtn.setFont(ratePanelFont);
		sellBtn.setFont(ratePanelFont);

		JPanel buttonsPanel = new JPanel(new GridLayout(1, 2));
		buttonsPanel.add(buyBtn);
		buttonsPanel.add(sellBtn);

		// Message panel
		JPanel bottomBorderPanel = new JPanel();
		bottomBorderPanel.setBorder(BorderFactory
				.createTitledBorder("Messages"));
		bottomBorderPanel.add(new Messages());

		// set sizes
		bottomBorderPanel.setPreferredSize(new Dimension(500, 300));
		comboPanel.setPreferredSize(new Dimension(300, 45));
		buttonsPanel.setPreferredSize(new Dimension(280, 110));

		// Add components
		topBorderPanel.add(comboPanel);
		topBorderPanel.add(buttonsPanel);

		ratePanel.add(topBorderPanel);
		ratePanel.add(bottomBorderPanel);

		this.setLayout(new BorderLayout());
		this.setBackground(backColor);
		// this.add(comboPanel, BorderLayout.NORTH);
		this.add(ratePanel, BorderLayout.CENTER);

		// set preferred size
		this.setPreferredSize(new Dimension(400, 300));

		// LISTENERS
		currencyBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				tradeController
						.changeOffersListenerInstrument((String) currencyBox
								.getSelectedItem());
			}
		});

		buyBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				tradeController.openPosition(
						(String) currencyBox.getSelectedItem(), "B",
						(Integer) amountBox.getSelectedItem() * 1000, null,
						null, null);

				tradeController.getPositionController().updateGUI(
						tradeController, false);

			}
		});

		sellBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				tradeController.openPosition(
						(String) currencyBox.getSelectedItem(), "S",
						(Integer) amountBox.getSelectedItem() * 1000, null,
						null, null);
				tradeController.getPositionController().updateGUI(
						tradeController, false);
			}
		});
	}

	/**
	 * Update ask rate interface in client
	 * 
	 * @param rate
	 * @param previousRate
	 */
	public void setAskRate(double rate, double rateDiff) {
		buyBtn.setRate(rate, rateDiff);
	}

	/**
	 * Update bid rate interface in client
	 * 
	 * @param rate
	 * @param previousRate
	 */
	public void setBidRate(double rate, double rateDiff) {
		sellBtn.setRate(rate, rateDiff);
	}

	public int getAmount() {
		return (Integer) amountBox.getSelectedItem();
	}
}
