package client.gui;

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;


@SuppressWarnings("serial")
public class BottomPanel extends JPanel {
	
	public BottomPanel(int theme, int panelType) {
		// super(theme, panelType);
		
		this.setLayout(new GridLayout(2, 2));
		this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));

	}

}
