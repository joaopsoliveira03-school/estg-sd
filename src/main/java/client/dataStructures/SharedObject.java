package client.dataStructures;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import client.Client;
import client.gui.ChatRoom;
import client.gui.InitialMenu;

/**
 * The SharedObject class is a class that contains all the data structures that are used by the client.
 */
public class SharedObject {
  private static final Logger logger = Logger.getLogger(SharedObject.class.getName());
  
  private static String username;
  
  private static Socket directSocket;
  private static BufferedReader directIn;
  private static PrintWriter directOut;
  private static ServerSocket serverSocket;
  private static MulticastSocket multicastSocket;
  private static DatagramSocket broadcastSocket;
  
  private static ChatRoom chatRoom;

  /**
   * Initializes the sockets that are used by the client.
   * Also initializes the initial menu.
   */
  public static void init() {
    try {
      directSocket = new Socket(Client.SERVER_ADDRESS, Client.SERVER_PORT);
      directSocket.setSoTimeout(5000);
      directIn = new BufferedReader(new InputStreamReader(directSocket.getInputStream()));
      directOut = new PrintWriter(directSocket.getOutputStream(), true);
      SwingUtilities.invokeLater(() -> {
        new InitialMenu();
      });
    } catch (IOException io) {
      logger.severe("Error Creating Initial Socket! " + io.getMessage());
      Client.close();
    }
  }

  /**
   * Initializes the sockets that are used by the client after the authentication.
   * Also initializes the chat room.
   */
  public static void initAuthenticated() {
    try {
      serverSocket = new ServerSocket(Client.CLIENT_PORT);
      multicastSocket = new MulticastSocket(Client.MULTICAST_PORT);
      broadcastSocket = new DatagramSocket(Client.CLIENT_PORT, InetAddress.getByName(Client.BROADCAST_ADDRESS));
      broadcastSocket.setBroadcast(true);
      SwingUtilities.invokeLater(() -> {
        chatRoom = new ChatRoom();
      });
    } catch (IOException io) {
      logger.severe("Error Creating After Authentication Sockets! " + io.getMessage());
      Client.close();
    }
  }

  /**
   * Closes all the sockets that are used by the client.
   */
  public static void close() {
    try {
      serverSocket.close();
      directSocket.close();
      multicastSocket.close();
      broadcastSocket.close();
    } catch (IOException io) {
      logger.severe("Error Closing Sockets! " + io.getMessage());
    }
  }

  //#region Getters and Setters
  public static String getUsername() {
    return username;
  }

  public static void setUsername(String username) {
    SharedObject.username = username;
  }

  public static Socket getDirectSocket() {
    return directSocket;
  }

  public static BufferedReader getDirectIn() throws IOException {
    return directIn;
  }

  public static PrintWriter getDirectOut() throws IOException {
    return directOut;
  }

  public static ServerSocket getServerSocket() {
    return serverSocket;
  }

  public static MulticastSocket getMulticastSocket() {
    return multicastSocket;
  }

  public static DatagramSocket getBroadcastSocket() {
    return broadcastSocket;
  }

  public static ChatRoom getChatRoom() {
    return chatRoom;
  }
  //#endregion
}