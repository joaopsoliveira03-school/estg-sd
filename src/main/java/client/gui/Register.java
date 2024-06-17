package client.gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import client.Client;
import client.protocols.UserAuthHandler;
import shared.enumerations.Role;

/**
 * The Register class represents a JFrame window for user registration.
 * It allows the user to enter their username, password, name, and role.
 * Upon clicking the "Register" button, the user's information is validated
 * and sent to the UserAuthHandler for registration. If successful, the
 * Client is initialized and the window is closed.
 */
public class Register extends JFrame {
  private static final String WARNING_MESSAGE = "Role Selection";
  private static final String ERROR_MESSAGE = "Error";
  private static final String OK = "OK";

  public Register() {
    setTitle("Forças Armadas Portuguesas");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(500, 300);
    setLocationRelativeTo(null);
    setLayout(new BorderLayout());

    JLabel titleLabel = new JLabel("Register", JLabel.CENTER);
    titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
    titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
    add(titleLabel, BorderLayout.PAGE_START);

    JLabel usernameLabel = new JLabel("Username", JLabel.CENTER);
    JLabel passwordLabel = new JLabel("Password", JLabel.CENTER);
    JLabel nameLabel = new JLabel("Name", JLabel.CENTER);
    JLabel roleLabel = new JLabel("Role", JLabel.CENTER);

    JTextField usernameField = new JTextField();
    JPasswordField passwordField = new JPasswordField();
    JTextField nameField = new JTextField();

    JComboBox<String> roleComboBox = new JComboBox<>(Role.getValues());
    roleComboBox.setSelectedIndex(0);

    Panel panel = new Panel(new GridLayout(5, 2));
    panel.add(usernameLabel);
    panel.add(usernameField);
    panel.add(passwordLabel);
    panel.add(passwordField);
    panel.add(nameLabel);
    panel.add(nameField);
    panel.add(roleLabel);
    panel.add(roleComboBox);

    add(panel, BorderLayout.CENTER);

    JButton registerButton = new JButton("Register");
    add(registerButton, BorderLayout.PAGE_END);

    registerButton.addActionListener((ActionEvent e) -> {
      try {
        String selectedRole = Optional.ofNullable((String) roleComboBox.getSelectedItem()).orElse("");
        if (selectedRole.isEmpty()) {
          JOptionPane.showMessageDialog(null, "Please select a role.", WARNING_MESSAGE, JOptionPane.WARNING_MESSAGE);
        } else {
          String result = UserAuthHandler.register(usernameField.getText(),
              new String(passwordField.getPassword()), nameField.getText(),
              selectedRole);
          if (!result.equalsIgnoreCase(OK)) {
            throw new Exception(result);
          }
          Client.initAuthenticated();
          dispose();
        }
      } catch (Exception ex) {
        JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage(), ERROR_MESSAGE, JOptionPane.ERROR_MESSAGE);
      }
    });
    setVisible(true);
  }
}