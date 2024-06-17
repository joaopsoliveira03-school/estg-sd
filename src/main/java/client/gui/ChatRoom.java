package client.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.json.JSONException;
import org.json.JSONObject;

import client.Client;
import client.dataStructures.SharedObject;
import client.protocols.EventsHandler;

/**
 * The ChatRoom class is a class that represents the chat room of the client.
 * Here is where the clients receive and send all the events.
 */
public class ChatRoom extends JFrame {
  private static final Logger logger = Logger.getLogger(ChatRoom.class.getName());

  private JTextArea chatArea;
  private JScrollPane scrollPane;

  public ChatRoom() {
    setTitle("Forças Armadas Portuguesas");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(700, 700);
    setLocationRelativeTo(null);
    setLayout(new BorderLayout());

    JPanel chatPanel = new JPanel(new BorderLayout());

    JLabel chatTitleLabel = new JLabel("Chat of " + SharedObject.getUsername());
    chatTitleLabel.setHorizontalAlignment(JLabel.CENTER);
    chatTitleLabel.setFont(new Font("Arial", Font.BOLD, 18));
    chatPanel.add(chatTitleLabel, BorderLayout.PAGE_START);

    chatArea = new JTextArea(25, 50);
    chatArea.setEditable(false);
    scrollPane = new JScrollPane(chatArea);
    chatPanel.add(scrollPane, BorderLayout.CENTER);

    JPanel inputPanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    JLabel recipientTitleLabel = new JLabel("MESSAGE");
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 3;
    inputPanel.add(recipientTitleLabel, gbc);

    JLabel recipientLabel = new JLabel("Destination");
    JTextField recipientField = new JTextField(30);

    JLabel messageLabel = new JLabel("Message");
    JTextField messageField = new JTextField(30);

    JButton sendButtonChat = new JButton("Send Message");
    sendButtonChat.setToolTipText("Send Message");

    sendButtonChat.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        sendMessage(recipientField.getText(), messageField.getText());
      }
    });

    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.gridwidth = 1;
    inputPanel.add(recipientLabel, gbc);

    gbc.gridx = 1;
    gbc.gridwidth = 2;
    inputPanel.add(recipientField, gbc);

    gbc.gridx = 0;
    gbc.gridy = 2;
    gbc.gridwidth = 1;
    inputPanel.add(messageLabel, gbc);

    gbc.gridx = 1;
    gbc.gridwidth = 2;
    inputPanel.add(messageField, gbc);

    gbc.gridx = 1;
    gbc.gridy = 3;
    gbc.gridwidth = 1;
    inputPanel.add(sendButtonChat, gbc);

    chatPanel.add(inputPanel, BorderLayout.SOUTH);

    JPanel requestPanel = new JPanel(new BorderLayout());

    JPanel requestInputPanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbcRequest = new GridBagConstraints();
    gbcRequest.insets = new Insets(5, 5, 5, 5);
    gbcRequest.fill = GridBagConstraints.HORIZONTAL;

    JLabel requestRecipientTitleLabel = new JLabel("REQUEST");
    gbcRequest.gridx = 0;
    gbcRequest.gridy = 0;
    gbcRequest.gridwidth = 3;
    requestInputPanel.add(requestRecipientTitleLabel, gbcRequest);

    JLabel requestRecipientLabel = new JLabel("Destination");
    JTextField requestRecipientField = new JTextField(30);

    JLabel requestMessageLabel = new JLabel("Message");
    JTextField requestMessageField = new JTextField(30);

    JButton sendButtonRequest = new JButton("Send Request");
    sendButtonRequest.setToolTipText("Send Request");

    sendButtonRequest.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        sendRequest(requestRecipientField.getText(), requestMessageField.getText());
      }
    });

    gbcRequest.gridx = 0;
    gbcRequest.gridy = 1;
    gbcRequest.gridwidth = 1;
    requestInputPanel.add(requestRecipientLabel, gbcRequest);

    gbcRequest.gridx = 1;
    gbcRequest.gridwidth = 2;
    requestInputPanel.add(requestRecipientField, gbcRequest);

    gbcRequest.gridx = 0;
    gbcRequest.gridy = 2;
    gbcRequest.gridwidth = 1;
    requestInputPanel.add(requestMessageLabel, gbcRequest);

    gbcRequest.gridx = 1;
    gbcRequest.gridwidth = 2;
    requestInputPanel.add(requestMessageField, gbcRequest);

    gbcRequest.gridx = 1;
    gbcRequest.gridy = 3;
    gbcRequest.gridwidth = 1;
    requestInputPanel.add(sendButtonRequest, gbcRequest);

    requestPanel.add(requestInputPanel, BorderLayout.SOUTH);

    add(chatPanel, BorderLayout.NORTH);
    add(requestPanel, BorderLayout.CENTER);

    addWindowListener(new java.awt.event.WindowAdapter() {
      @Override
      public void windowClosing(java.awt.event.WindowEvent windowEvent) {
        //ignore
      }
    });

    setResizable(true);
    setMinimumSize(new Dimension(600, 550));
    setMaximumSize(new Dimension(6001, 551));

    setVisible(true);
  }

  /**
   * Sends a message to the specified recipient.
   * If the recipient is "broadcast", the message is sent as a broadcast.
   * If the recipient is a valid multicast IP address, the message is sent to the multicast group.
   * Otherwise, the message is sent directly to the recipient.
   * 
   * @param recipient The recipient of the message.
   * @param message The message to be sent.
   */
  private void sendMessage(String recipient, String message) {
    if (recipient.trim().isEmpty() || message.trim().isEmpty()) {
      return;
    }
    try {
      JSONObject json = EventsHandler.createMessage(recipient, message);
      if (recipient.equals("broadcast")) {
        // Broadcast
        DatagramPacket packet = new DatagramPacket(json.toString().getBytes(), json.toString().getBytes().length,
            InetAddress.getByName(Client.BROADCAST_ADDRESS), Client.CLIENT_PORT);
        try {
          SharedObject.getBroadcastSocket().send(packet);
        } catch (IOException io) {
          logger.severe("Error sending message to broadcast: " + io.getMessage());
          return;
        }
      } else if (recipient.matches(
        // Multicast
          "^(22[4-9]|23[0-9]|2[4-9][0-9]|[3-9][0-9]{2}|[12][0-9]{3})\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")) {
        InetAddress group = InetAddress.getByName(recipient);
        try {
          SharedObject.getMulticastSocket().joinGroup(group);
        } catch (IOException ignored) {}
        EventsHandler.announceJoinGroup(recipient);
        DatagramPacket packet = new DatagramPacket(json.toString().getBytes(), json.toString().getBytes().length,
            group, Client.MULTICAST_PORT);
        try {
          SharedObject.getMulticastSocket().send(packet);
        } catch (IOException io) {
          logger.severe("Error sending message to group: " + io.getMessage());
          return;
        }
      } else {
        // Direct
        try {
          PrintWriter out = SharedObject.getDirectOut();
          out.println(json.toString());
        } catch (Exception ignored) {
          System.out.println("Error sending message to user " + recipient + "!");
          return;
        }
      }
      SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
      Date date = new Date();
      synchronized (chatArea) {
        chatArea.append("[" + sdf.format(date) + "][Message to " + recipient + "]: " + message + "\n");
        scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
      }
    } catch (JSONException e) {
      JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    } catch (IOException io) {
      JOptionPane.showMessageDialog(null, "Error: " + io.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Receives a message and displays it in the chat area.
   *
   * @param sender   the sender of the message
   * @param receiver the receiver of the message
   * @param message  the content of the message
   * @param date     the date of the message
   */
  public void receiveMessage(String sender, String receiver, String message, String date) {
    synchronized (chatArea) {
      chatArea.append("[" + date + "][Message from " + sender + " to " + receiver + "]: " + message + "\n");
      scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
    }
  }

  /**
   * Sends a request to the specified recipient with the given message.
   * If the recipient or message is empty, the request is not sent.
   * The request can be sent to a specific user, a broadcast address, or a multicast group.
   * If the recipient is a multicast group, the client will join the group before sending the request.
   * The request is sent using the appropriate socket based on the recipient type.
   * If an error occurs while sending the request, an error message is logged or displayed.
   * The date and time of the request, along with the recipient and message, are appended to the chat area.
   *
   * @param recipient The recipient of the request (user, broadcast, or multicast group)
   * @param message   The message to be sent
   */
  private void sendRequest(String recipient, String message) {
    if (recipient.trim().isEmpty() || message.trim().isEmpty()) {
      return;
    }
    try {
      JSONObject json = EventsHandler.createRequest(recipient, message);
      if (recipient.equals("broadcast")) {
        DatagramPacket packet = new DatagramPacket(json.toString().getBytes(), json.toString().getBytes().length, InetAddress.getByName(Client.BROADCAST_ADDRESS), Client.CLIENT_PORT);
        try {
          SharedObject.getBroadcastSocket().send(packet);
        } catch (IOException io) {
          logger.severe("Error sending request to broadcast: " + io.getMessage());
          return;
        }
      } else if (recipient.matches(
          "^(22[4-9]|23[0-9]|2[4-9][0-9]|[3-9][0-9]{2}|[12][0-9]{3})\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")) {
        InetAddress group = InetAddress.getByName(recipient);
        try {
          SharedObject.getMulticastSocket().joinGroup(group);
        } catch (IOException ignored) {}
        EventsHandler.announceJoinGroup(recipient);
        DatagramPacket packet = new DatagramPacket(json.toString().getBytes(), json.toString().getBytes().length, group,
            Client.MULTICAST_PORT);
        try {
          SharedObject.getMulticastSocket().send(packet);
        } catch (IOException io) {
          logger.severe("Error sending request to group: " + io.getMessage());
          return;
        }
      } else {
        try {
          PrintWriter out = SharedObject.getDirectOut();
          out.println(json.toString());
        } catch (Exception ignored) {
          System.out.println("Error sending request to user " + recipient + "!");
          return;
        }
      }
      SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
      Date date = new Date();
      synchronized (chatArea) {
        chatArea.append("[" + sdf.format(date) + "][Request to " + recipient + "]: " + message + "\n");
        scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
      }
    } catch (JSONException | IOException e) {
      JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Receives a request message and displays it in the chat area.
   * 
   * @param sender    the sender of the request
   * @param receiver  the receiver of the request
   * @param message   the content of the request message
   * @param date      the date of the request
   * @param accepter  the accepter of the request (empty if not accepted)
   */
  public void receiveRequest(String sender, String receiver, String message, String date, String accepter) {
    synchronized (chatArea) {
      if (!accepter.equals("")) {
        chatArea.append("[" + date + "][Request from " + sender + " to " + receiver + "]: " + message
            + " (Accepted by " + accepter + ")\n");
      } else {
        chatArea.append("[" + date + "][Request from " + sender + " to " + receiver + "]: " + message + "\n");
      }
      scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
    }
  }
}
