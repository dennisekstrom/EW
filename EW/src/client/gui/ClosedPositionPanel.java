package client.gui;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EtchedBorder;

import client.Position;

/**
 * Closed positions frame. Displays all closed positions.
 * 
 * 
 * @author Tobias W
 * 
 */
@SuppressWarnings("serial")
public class ClosedPositionPanel extends JScrollPane {

	private final JList closedPositionList;
	private final DefaultListModel listModel;

	public ClosedPositionPanel() {

		listModel = new DefaultListModel();
		closedPositionList = new JList();
		closedPositionList.setModel(listModel);
		closedPositionList.setFixedCellHeight(35);
		closedPositionList.setFont(new Font("arial", Font.PLAIN, 15));
		closedPositionList.setCellRenderer(new ListColorRenderer());

		closedPositionList.setSelectionModel(new DefaultListSelectionModel() {
			@Override
			public void setSelectionInterval(int index0, int index1) {
				if (closedPositionList.isSelectedIndex(index0)) {
					closedPositionList.removeSelectionInterval(index0, index1);
				} else {
					closedPositionList.addSelectionInterval(index0, index1);
				}
			}
		});

		this.add(closedPositionList);
		this.getViewport().setView(closedPositionList);
		this.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		// set border
		this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
	}

	/**
	 * Add this closed position to list
	 * 
	 * @param position
	 */
	public void addClosedPosition(Position position) {
		listModel.addElement(position);
	}

	/**
	 * Update and add position to position list
	 */
	public void setClosedPositions(ArrayList<Position> positions) {
		listModel.clear();
		ArrayList<Position> tempPositions = new ArrayList<Position>();

		for (int i = 0; i < positions.size(); i++) {
			tempPositions.add(null);
		}

		Collections.copy(tempPositions, positions);

		Collections.sort(tempPositions, new Comparator<Position>() {

			@Override
			public int compare(Position a, Position b) {
				if (a.getCloseTime() - b.getCloseTime() > 0)
					return 0;
				else
					return 1;
			}

		});

		for (Position pos : tempPositions) {
			listModel.addElement(pos);
		}

	}
}
