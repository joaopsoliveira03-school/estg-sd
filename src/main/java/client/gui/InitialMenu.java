package client.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * The InitialMenu class represents the initial menu of the application.
 * It extends the JFrame class and provides functionality for user registration and login.
 */
public class InitialMenu extends JFrame {

  public InitialMenu() {
    setTitle("Forças Armadas Portuguesas");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);
    setLayout(new GridLayout(2, 1));
    setSize(500, 300);

    JButton registerButton = new JButton("Register");
    JButton loginButton = new JButton("Login");

    add(registerButton);
    add(loginButton);

    registerButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        SwingUtilities.invokeLater(() -> {
          new Register();
          dispose();
        });
      }
    });

    loginButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        SwingUtilities.invokeLater(() -> {
          new Login();
          dispose();
        });
      }
    });

    setVisible(true);
  }
}
