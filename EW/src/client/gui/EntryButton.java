package client.gui;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

import swingjd.custom.component.button.ButtonType;
import swingjd.custom.component.button.GradientButton;
import swingjd.util.Theme;

/**
 * This class describes a button displaying a title, a rate and the difference
 * to previous rate. Its color is determined depending on the value of the rate
 * difference.
 * 
 * @author Dennis Ekstrom
 */
@SuppressWarnings("serial")
public class EntryButton extends GradientButton {

	private final String title;

	private final Border defaultBorder = BorderFactory
			.createRaisedBevelBorder();
	private final Border pressedBorder = BorderFactory
			.createLoweredBevelBorder();

	// private final Color positiveColor = new Color(102, 255, 102);
	// private final Color negativeColor = new Color(255, 102, 102);
	// private final Color neutralColor = Color.LIGHT_GRAY;

	private final MouseListener mouseListener = new MouseAdapter() {

		@Override
		public void mousePressed(final MouseEvent evt) {
			setBorder(pressedBorder);
		}

		@Override
		public void mouseReleased(final MouseEvent evt) {
			setBorder(defaultBorder);
		}

		@Override
		public void mouseExited(final MouseEvent evt) {
			setBorder(defaultBorder);
		}
	};

	EntryButton(String title) {
		super(title, Theme.GRADIENT_GRAY_THEME,
				ButtonType.BUTTON_ROUNDED_RECTANGLUR);
		this.title = title;

		this.addMouseListener(mouseListener);

		this.setSelectedTheme(Theme.GRADIENT_GOLD_THEME);
		this.setBorder(defaultBorder);
		this.setSize(new Dimension(200, 200));
		// this.setOpaque(true);
	}

	/**
	 * Sets parameters determining the view of the button.
	 * 
	 * @param rate
	 *            the current rate
	 * @param rateDiff
	 *            the difference between current rate and previous rate
	 */
	public void setRate(double rate, double rateDiff) {
		String sign;
		if (rateDiff > 0) {
			this.setButtonTheme(Theme.GRADIENT_GREEN_THEME);
			sign = "+";
		} else if (rateDiff < 0) {
			this.setButtonTheme(Theme.GRADIENT_RED_THEME);
			sign = "";
		} else {
			this.setButtonTheme(Theme.GRADIENT_BLACK_THEME);
			sign = "\u00B1";
		}

		String rateDiffString = new String(
				"<html><font color = GREY size = 3 font = helvetica>(" + sign
						+ String.format("%.5f", rateDiff) + ")</font></html>");

		// show rate
		this.setText("<html><center>" + title + "<br>"
				+ String.format("%.5f", rate) + "<br>" + rateDiffString
				+ "</center></html>");

	}

}