package client.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import client.Position;
import client.PositionController;

import swingjd.custom.component.button.ButtonType;
import swingjd.custom.component.button.GradientButton;
import swingjd.util.Theme;
import trading.TradeController;

/**
 * User interface toolbar. Consists of user interactive buttons and a combo box
 * for adjusting the feed speed.
 * 
 * @author Tobias
 * 
 */
@SuppressWarnings("serial")
public class ToolBar extends JPanel {

	private final TradeController tradeController;
	private final PositionController positionController;
	private final PositionPanel positionPanel;

	private static final Color color = Color.WHITE;

	// GUI
	private final GradientButton updateButton;
	private final GradientButton closeButton;
	private final GradientButton closeAllButton;
	private final GradientButton pauseButton;
	private final GradientButton liveButton;
	//	private final JComboBox speedComboBox;

	//	private final JPanel speedPanel;

	private boolean isPaused;

	//	private final Double[] speeds = { 1D, 10D, 100D, 1000D, 3000D, 5000D,
	//			7000D, 10000D, 20000D, 50000D, 100000D, 1000000D };

	public ToolBar(TradeController tradeController,
			PositionController positionController, PositionPanel positionPanel) {
		//super(new FlowLayout(FlowLayout.LEFT, 4, -3));
		// super(Theme.STANDARD_BLACK_THEME, PanelType.PANEL_RECTANGULAR);
		this.setLayout(new FlowLayout(FlowLayout.LEFT, 4, -3));

		// initialize
		this.tradeController = tradeController;
		this.positionController = positionController;
		this.positionPanel = positionPanel;

		// set border
		this.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(EtchedBorder.RAISED),
				"Toolbar"));
		this.setBackground(color);

		//		speedPanel = new JPanel();
		//		speedPanel.setBackground(color);
		//		speedComboBox = new JComboBox(speeds);
		updateButton = new GradientButton("UPDATE",
				Theme.GRADIENT_BLUEGRAY_THEME,
				ButtonType.BUTTON_ROUNDED_RECTANGLUR);
		closeButton = new GradientButton("CLOSE",
				Theme.GRADIENT_BLUEGRAY_THEME,
				ButtonType.BUTTON_ROUNDED_RECTANGLUR);
		closeAllButton = new GradientButton("CLOSE ALL",
				Theme.GRADIENT_BLUEGRAY_THEME,
				ButtonType.BUTTON_ROUNDED_RECTANGLUR);
		pauseButton = new GradientButton("PAUSE",
				Theme.GRADIENT_BLUEGRAY_THEME,
				ButtonType.BUTTON_ROUNDED_RECTANGLUR);
		liveButton = new GradientButton("LIVE", Theme.GRADIENT_BLUEGRAY_THEME,
				ButtonType.BUTTON_ROUNDED_RECTANGLUR);

		Dimension buttonSize = new Dimension(100, 30);
		Dimension closeAllSize = new Dimension(120, 30);

		updateButton.setPreferredSize(new Dimension(buttonSize));
		closeButton.setPreferredSize(new Dimension(buttonSize));
		closeAllButton.setPreferredSize(closeAllSize);
		pauseButton.setPreferredSize(new Dimension(buttonSize));
		liveButton.setPreferredSize(new Dimension(buttonSize));

		updateButton.setFont(new Font("helvetiva", Font.PLAIN, 13));
		closeButton.setFont(new Font("helvetiva", Font.PLAIN, 13));
		closeAllButton.setFont(new Font("helvetiva", Font.PLAIN, 13));
		pauseButton.setFont(new Font("helvetiva", Font.PLAIN, 13));
		liveButton.setFont(new Font("helvetiva", Font.PLAIN, 13));

		//		speedComboBox.addActionListener(new ActionListener() {
		//
		//			@Override
		//			public void actionPerformed(ActionEvent e) {
		//				getPositionController().getFeed().setSpeed(
		//						(Double) speedComboBox.getSelectedItem());
		//			}
		//		});

		// make sure to display default speed at initalization
		//		speedComboBox.setSelectedItem(TempConstants.defaultSpeed);

		//		speedPanel.add(speedComboBox);

		// speedPanel.setBorder(BorderFactory.createTitledBorder(
		// BorderFactory.createEmptyBorder(), "Speeds"));

		//		speedPanel.setBorder(BorderFactory.createTitledBorder(null, "Speeds",
		//				TitledBorder.LEFT, TitledBorder.TOP, new Font("helvetica",
		//						Font.PLAIN, 13), Color.BLACK));

		this.add(updateButton);
		this.add(closeButton);
		this.add(closeAllButton);
		this.add(pauseButton);
		this.add(liveButton);

		this.setPreferredSize(new Dimension(600, 60));
		//		this.add(speedPanel);

		closeAction(closeButton);
		closeAllAction(closeAllButton);
		pauseAction(pauseButton);
		updateAction(updateButton);
		liveAction(liveButton);

		positionPanel.getList().addListSelectionListener(
				new ListSelectionListener() {

					@Override
					public void valueChanged(ListSelectionEvent e) {
						adjustCloseButton();
					}
				});

		adjustCloseButton();
	}

	private void adjustCloseButton() {
		if (positionPanel.getList().isSelectionEmpty())
			closeButton.setEnabled(false);
		else
			closeButton.setEnabled(true);
	}

	private PositionController getPositionController() {
		return positionController;
	}

	private void closeAction(JButton close) {
		close.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Object[] positions = positionPanel.getList()
						.getSelectedValues();

				for (Object pos : positions) {
					Position position = (Position) pos;
					tradeController.closePosition(position.getOrder()
							.getTrade());
					// positionController.closePosition(position,
					// tradeController);
				}
				positionPanel.deSelectAll();
				// positionController.updatePositions(tradeController);
			}
		});
	}

	private void closeAllAction(JButton closeAll) {
		closeAll.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// positionController.updatePositions(tradeController);
				positionController.closeAllPositions(tradeController);

				positionPanel.deSelectAll();
				//				positionPanel.setPositions(positionController
				//						.getOpenPositions());
			}
		});
	}

	private void updateAction(JButton updateButton) {
		updateButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				positionController.updateGUI(tradeController, true);
				positionController.getHost().getEventsPanel()
						.updateEvents(true);
				tradeController.getStrategyController()
						.getActiveStrategiesPanel().setStrategies();

			}
		});
	}

	private void pauseAction(JButton pause) {
		pause.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (isPaused) {
					// positionController.getFeed().start();
					pauseButton.setText("PAUSE");
					isPaused = false;
				} else {
					// positionController.getFeed().pause();
					pauseButton.setText("START");
					isPaused = true;
				}

				pauseButton.repaint();
			}
		});
	}

	private void liveAction(JButton live) {
		live.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!ClientMain.isLive) {
					// confirmation dialog
					if (JOptionPane.showConfirmDialog(
							positionController.getHost(),
							"This will engage live trading. \nAre you sure you want to continue?",
							"Live Trading", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						ClientMain.isLive = true;
						tradeController.unsubsribeUpdate();
						liveButton.setText("BACKTRACE");
					}
				} else {
					if (JOptionPane.showConfirmDialog(
							positionController.getHost(),
							"This will keep all open positions and leave your live trading session. \nAre you sure you want to continue?",
							"Local Trading", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						tradeController.subscribeUpdate();
						ClientMain.isLive = false;
						liveButton.setText("LIVE");
					}
				}

				liveButton.repaint();
			}
		});
	}
}
