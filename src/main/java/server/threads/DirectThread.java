package server.threads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Logger;

import server.protocols.Protocol;
import shared.enumerations.ConnectionType;

/**
 * Represents a thread that handles direct connections with clients.
 */
public class DirectThread implements Runnable {
  private static final Logger logger = Logger.getLogger(DirectThread.class.getName());

  private Socket socket;

  /**
   * Constructs a DirectThread object with the specified socket.
   *
   * @param socket the socket representing the client connection
   */
  public DirectThread(Socket socket) {
    this.socket = socket;
  }

  /**
   * Runs the thread and handles the communication with the client.
   */
  @Override
  public void run() {
    try (
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);) {
      try {
        while (true) {
          String input = in.readLine();
          if (input == null) {
            in.close();
            out.close();
            if (!socket.isClosed()) socket.close();
            return;
          }
          String output = Protocol.processInput(ConnectionType.DIRECT, socket, input);
          if (output == null) {
            continue;
          }
          out.println(output);
        }
      } catch (IOException io) {
        logger.severe("Error Handling Direct Message! " + io.getMessage());
      }
    } catch (IOException io) {
      logger.severe("Error Handling Direct Connection! " + io.getMessage());
    }
  }
}
