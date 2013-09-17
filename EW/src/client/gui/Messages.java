package client.gui;

import java.awt.Dimension;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EtchedBorder;

/**
 * Used for posting important messages to the user, for example if a strategy is
 * failing or if todays goal has been reached
 * 
 * @author Tobias
 * 
 */

@SuppressWarnings("serial")
public class Messages extends JScrollPane {

	// private static String[] positions;
	private final JList list;
	private static DefaultListModel listModel = new DefaultListModel();

	/**
	 * Create Message panel
	 */
	public Messages() {
		list = new JList();
		list.setModel(listModel);
		this.getViewport().setView(list);
		this.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		this.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

		// set border
		this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		this.setPreferredSize(new Dimension(390, 130));
	}

	public static void addMessage(String message) {
		Date date = new Date(System.currentTimeMillis());
		listModel.addElement(message + " " + date);
	}

	public static void removeTopMessage() {
		listModel.remove(0);
	}

}
