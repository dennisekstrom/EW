package client.gui;

import javax.swing.*;

import client.User;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

/**
 * Password class. The login frame for the client. Must be executed and approved
 * before client starts.
 * 
 * @author Tobias
 * 
 */

@SuppressWarnings("serial")
public class PasswordFrame extends JFrame implements ActionListener {
	private final ArrayList<PropertyChangeListener> passwordListeners;

	private static String OK = "ok";
	private static String HELP = "help";

	// private JFrame controllingFrame;
	private final JLabel passwordLabel;
	private final JLabel idLabel;
	private final JPasswordField passwordField;
	private final JTextField idField;

	private User user;

	public PasswordFrame() {

		// initialize fields
		passwordListeners = new ArrayList<PropertyChangeListener>();

		// GUI
		this.setLayout(new FlowLayout());
		JPanel p = new JPanel(new GridLayout(2, 0));

		idField = new JTextField(10);

		passwordField = new JPasswordField(10);
		passwordField.setActionCommand(OK);
		passwordField.addActionListener(this);

		idLabel = new JLabel("Account: ");
		passwordLabel = new JLabel("Password:");

		idLabel.setLabelFor(idField);
		passwordLabel.setLabelFor(passwordField);

		idField.setText("D25203691001");
		passwordField.setText("6296");

		JComponent buttonPane = createButtonPanel();

		// Lay out everything.
		p.add(idLabel);
		p.add(idField);
		p.add(passwordLabel);
		p.add(passwordField);

		this.add(p);
		this.add(buttonPane);
		this.pack();
		this.setTitle("Login");

		resetFocus();
	}

	/**
	 * Add a listener to be notified when correct password is entered.
	 * 
	 * @param listener
	 *            listener to be added
	 */
	public void addPasswordListener(PropertyChangeListener listener) {
		if (listener == null)
			return;

		this.passwordListeners.add(listener);
	}

	private void notifyListeners() {
		for (PropertyChangeListener pcl : passwordListeners)
			pcl.propertyChange(new PropertyChangeEvent(this, "PasswordCorrect",
					false, true));
	}

	protected JComponent createButtonPanel() {
		JPanel p = new JPanel(new GridLayout(0, 1));
		JButton okButton = new JButton("OK");
		JButton helpButton = new JButton("Help");

		okButton.setActionCommand(OK);
		helpButton.setActionCommand(HELP);
		okButton.addActionListener(this);
		helpButton.addActionListener(this);

		p.add(okButton);
		p.add(helpButton);

		return p;
	}

	/**
	 * @return user
	 */
	public User getUser() {
		return user;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (OK.equals(cmd)) { // Process the password.
			char[] input = passwordField.getPassword();
			user = User.getUser(idField.getText());
			user.setId(idField.getText());
			user.setPassword(input);
			notifyListeners();

			passwordField.selectAll();
		}
		resetFocus();
	}

	protected void resetFocus() {
		passwordField.requestFocusInWindow();
	}

}