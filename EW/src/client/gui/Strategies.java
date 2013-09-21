package client.gui;

import java.awt.BorderLayout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import swingjd.custom.component.button.ButtonType;
import swingjd.custom.component.button.GradientButton;
import swingjd.util.Theme;
import trading.Strategy;
import trading.StrategyController;

/**
 * 
 * @author tobbew92
 * 
 */

@SuppressWarnings("serial")
public class Strategies extends JFrame implements ActionListener {

	public static int progress = 0;

	private static final String TITLE = "Strategies";
	private static final Dimension FRAME_SIZE = new Dimension(650, 350);

	private final CheckBoxList checkBoxList = new CheckBoxList();
	private final JPanel bottomPanel = new JPanel();
	private final JButton doneButton = new GradientButton(
			"Enable selected strategies", Theme.GRADIENT_BLUEGRAY_THEME,
			ButtonType.BUTTON_ROUNDED_RECTANGLUR);
	private final JButton launchButton = new GradientButton(
			"Launch selected strategies", Theme.GRADIENT_BLUEGRAY_THEME,
			ButtonType.BUTTON_ROUNDED_RECTANGLUR);
	private final JProgressBar progressBar = new JProgressBar(0, 100);

	private DefaultListModel listModel = new DefaultListModel();
	private ArrayList<Strategy> strategies = new ArrayList<Strategy>();
	private final StrategyController strategyController;

	public Strategies(ArrayList<Strategy> strategies,
			StrategyController strategyController) {
		super(TITLE);

		this.strategies = strategies;
		this.strategyController = strategyController;
		initGUI();

	}

	private void initGUI() {
		this.setLayout(new BorderLayout());

		// init checkbox list
		listModel = new DefaultListModel();

		checkBoxList.setModel(listModel);

		for (Strategy s : strategies)
			addStrategy(s);

		bottomPanel.setLayout(new GridLayout(1, 2));

		bottomPanel.add(launchButton);
		bottomPanel.add(doneButton);
		bottomPanel.add(progressBar);

		checkBoxList.setFont(new Font("helvetica", Font.PLAIN, 15));
		checkBoxList.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY,
				10));

		doneButton.setFont(new Font("helvetica", Font.PLAIN, 13));
		launchButton.setFont(new Font("helvetica", Font.PLAIN, 13));
		doneButton.setEnabled(false);

		for (int i = 0; i < checkBoxList.getModel().getSize(); i++) {
			StrategyCheckBox checkBox = (StrategyCheckBox) checkBoxList
					.getModel().getElementAt(i);
			if (checkBox.isSelected()) {
				doneButton.setEnabled(true);
				break;
			}
		}

		doneButton.addActionListener(this);
		launchButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				int activatingStrats = 0;
				progress = 0;
				progressBar.setValue(0);
				for (int i = 0; i < checkBoxList.getModel().getSize(); i++) {
					StrategyCheckBox checkBox = (StrategyCheckBox) checkBoxList
							.getModel().getElementAt(i);
					if (checkBox.isSelected())
						activatingStrats++;
				}

				EnableStratTask est = new EnableStratTask(activatingStrats);
				ProgressBarTask pbt = new ProgressBarTask(progressBar, est,
						activatingStrats);

				pbt.execute();
				est.execute();

				// strategyController.enableStrategies();
				// thisClass.setVisible(false);
				// initGUI();
			}
		});

		checkBoxList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				adjustEnableButton();
			}
		});

		adjustEnableButton();

		// add components
		this.add(checkBoxList);
		this.add(bottomPanel, BorderLayout.SOUTH);

		this.setSize(FRAME_SIZE);

	}

	private void adjustEnableButton() {
		boolean clear = true;
		for (int i = 0; i < checkBoxList.getModel().getSize(); i++) {
			StrategyCheckBox checkBox = (StrategyCheckBox) checkBoxList
					.getModel().getElementAt(i);
			if (checkBox.isSelected()) {
				if (checkBox.getStrategy().isPaused())
					doneButton.setEnabled(true);
				else if (!checkBox.getStrategy().isActive()) {
					doneButton.setEnabled(false);
					clear = false;
				}
			}
		}

		if (clear)
			doneButton.setEnabled(true);
	}

	public void addStrategy(Strategy strategy) {
		StrategyCheckBox checkBox;
		if (strategy.isPaused())
			checkBox = new StrategyCheckBox("(Paused) "
					+ strategy.getStrategyName(), strategy);
		else if (strategy.isActive())
			checkBox = new StrategyCheckBox("(Active) "
					+ strategy.getStrategyName(), strategy);
		else
			checkBox = new StrategyCheckBox(strategy.getStrategyName(),
					strategy);

		if (strategy.isActive())
			checkBox.setSelected(true);
		listModel.addElement(checkBox);
		this.repaint();
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {

		// progressBar.setVisible(true);

		for (int i = 0; i < checkBoxList.getModel().getSize(); i++) {
			StrategyCheckBox checkBox = (StrategyCheckBox) checkBoxList
					.getModel().getElementAt(i);
			Strategy strategy = checkBox.getStrategy();
			if (checkBox.isSelected()) {
				strategyController.activateStrategy(strategy);

			} else {
				strategyController.deactivateStrategy(strategy);
			}

		}

		strategyController.enableStrategies();
		// this.setVisible(false);
		initGUI();
	}

	private class ProgressBarTask extends SwingWorker<Void, Integer> {

		private final JProgressBar pb;
		private final EnableStratTask est;
		private final int activatingStrats;

		public ProgressBarTask(JProgressBar progressBar, EnableStratTask est,
				int activatingStrats) {
			this.activatingStrats = activatingStrats;
			this.est = est;
			pb = progressBar;
			pb.setVisible(true);
			pb.setStringPainted(true);
			pb.setValue(0);

		}

		@Override
		protected void done() {
			// pb.setValue(100);
		}

		@Override
		protected Void doInBackground() throws Exception {
			int defaultSleepTime = activatingStrats * 30;
			int sleepTime = defaultSleepTime;

			while (progress <= 100) {

				if (progress == 100) {
					pb.setValue(100);
					break;
				}

				if (est.isDone()) {
					progress = 100;
					pb.setValue(progress);
					break;
				}

				if (progress > 10 && !est.isDone())
					sleepTime = defaultSleepTime * 5;
				if (progress > 80 && !est.isDone())
					sleepTime = defaultSleepTime * 10;
				if (progress > 95 && !est.isDone())
					sleepTime = defaultSleepTime * 20;
				if (progress == 98 && !est.isDone())
					sleepTime = defaultSleepTime * 400;

				progress++;
				pb.setValue(progress);
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
			return null;
		}

	}

	private class EnableStratTask extends SwingWorker<Void, Integer> {

		private final int activatingStrats;

		public EnableStratTask(int activatingStrats) {
			this.activatingStrats = activatingStrats;

		}

		@Override
		protected void done() {
			initGUI();
		}

		@Override
		protected Void doInBackground() throws Exception {
			int counter = 1;
			for (int i = 0; i < checkBoxList.getModel().getSize(); i++) {
				StrategyCheckBox checkBox = (StrategyCheckBox) checkBoxList
						.getModel().getElementAt(i);
				Strategy strategy = checkBox.getStrategy();
				if (checkBox.isSelected()) {
					strategyController.launchStrategy(strategy);
					int percentage = (100 / activatingStrats) * counter;
					progress = percentage;
					counter++;
				}

			}
			strategyController.enableStrategies();
			return null;
		}

	}
}
