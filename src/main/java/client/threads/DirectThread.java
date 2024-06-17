package client.threads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import client.dataStructures.SharedObject;
import client.protocols.Protocol;
import shared.enumerations.ConnectionType;

/**
 * Represents a thread that handles direct connections with clients.
 */
public class DirectThread implements Runnable {
  private static final Logger logger = Logger.getLogger(DirectThread.class.getName());
  private static final ExecutorService executorService = Executors.newFixedThreadPool(50);

  /**
   * Returns the executor service used by the DirectThread.
   *
   * @return the executor service
   */
  public static ExecutorService getExecutorService() {
    return executorService;
  }

  /**
   * Constructs a new DirectThread.
   */
  public DirectThread() {
  }

  /**
   * Runs the DirectThread, accepting incoming connections and handling them in separate threads.
   */
  @Override
  public void run() {
    ServerSocket serverSocket = SharedObject.getServerSocket();
    while (true) {
      try {
        Socket socket = serverSocket.accept();
        executorService.execute(new Runnable() {
          @Override
          public void run() {
            logger.info("New Direct Thread Created! " + socket.getInetAddress() + " " + socket.getPort());
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                while (true) {
                String input = in.readLine();
                if (input == null) {
                  in.close();
                  out.close();
                  if (!socket.isClosed()) socket.close();
                  return;
                }
                String output = Protocol.processInput(ConnectionType.DIRECT, input);
                if (output == null) {
                  continue;
                }
                out.println(output);
              }
            } catch (IOException io) {
              logger.severe("Error Handling Direct Connection! " + io.getMessage());
            }
            logger.info("Direct Thread Closed!" + socket.getInetAddress() + " " + socket.getPort());
          }
        });
      } catch (IOException io) {
        logger.severe("Error Accepting Direct Connection! " + io.getMessage());
      }
    }
  }
}