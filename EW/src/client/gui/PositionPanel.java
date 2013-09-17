package client.gui;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EtchedBorder;

import client.Position;

/**
 * UI Panel for all open positions.
 * 
 * @author Tobias W
 * 
 */

@SuppressWarnings("serial")
public class PositionPanel extends JScrollPane {

	// private static String[] positions;
	private final JList list;
	private final DefaultListModel listModel;

	/**
	 * Create position panel
	 */
	public PositionPanel() {
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
	}

	public void deSelectAll() {
		//		list.removeSelectionInterval(0, listModel.size() - 1);
		for (int i = 0; i < listModel.size(); i++) {
			if (list.isSelectedIndex(i))
				list.removeSelectionInterval(i, i);
		}
	}

	/**
	 * @return position list
	 */
	public JList getList() {
		return list;
	}

	/**
	 * Update and add position to position list
	 */
	public void setPositions(ArrayList<Position> positions) {
		ArrayList<Position> tempPositions = new ArrayList<Position>();

		for (int i = 0; i < positions.size(); i++) {
			tempPositions.add(null);
		}

		Collections.copy(tempPositions, positions);

		if (list.isSelectionEmpty()) {
			listModel.clear();

			for (Position pos : tempPositions) {
				listModel.addElement(pos);
			}
		}

	}

}
