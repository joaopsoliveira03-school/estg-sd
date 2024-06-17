package client.gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import client.Client;
import client.protocols.UserAuthHandler;

/**
 * The Login class represents a graphical user interface for the login functionality.
 * It extends the JFrame class and provides a login form with username and password fields.
 * Upon successful login, it initializes the authenticated client and closes the login window.
 */
public class Login extends JFrame {
  public Login() {
    setTitle("Forças Armadas Portuguesas");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(500, 300);
    setLocationRelativeTo(null);
    setLayout(new BorderLayout());

    JLabel titleLabel = new JLabel("Menu Login");
    titleLabel.setHorizontalAlignment(JLabel.CENTER);
    titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
    titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
    add(titleLabel, BorderLayout.PAGE_START);

    JLabel usernameLabel = new JLabel("Username:");
    usernameLabel.setHorizontalAlignment(JLabel.CENTER);
    JTextField usernameField = new JTextField(15);

    JLabel passwordLabel = new JLabel("Password:");
    passwordLabel.setHorizontalAlignment(JLabel.CENTER);
    JPasswordField passwordField = new JPasswordField(15);

    Panel panel = new Panel();
    panel.setLayout(new GridLayout(2, 2));

    panel.add(usernameLabel);
    panel.add(usernameField);
    panel.add(passwordLabel);
    panel.add(passwordField);

    add(panel, BorderLayout.CENTER);

    JButton loginButton = new JButton("Login");
    add(loginButton, BorderLayout.PAGE_END);

    loginButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          String result = UserAuthHandler.login(usernameField.getText(), new String(passwordField.getPassword()));
          if (!result.equals("OK")) {
            throw new Exception(result);
          }
          Client.initAuthenticated();
          dispose();
        } catch (Exception ex) {
          JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
      }
    });

    setVisible(true);
  }
}
