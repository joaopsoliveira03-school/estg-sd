package server;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import server.dataStructures.SharedObject;
import server.threads.ActiveUsersThread;
import server.threads.BroadcastThread;
import server.threads.DataPersistenceThread;
import server.threads.DirectThread;
import server.threads.EventsThread;
import server.threads.MulticastThread;
import server.threads.RequestsStatsThread;

/**
 * The Server class represents the main server application.
 * It handles direct connections, broadcasts, multicasts, events, active users, request statistics,
 * data persistence, and provides methods for starting and closing the server.
 */
public class Server {
  private static final Logger logger = Logger.getLogger(Server.class.getName());
  private static final ExecutorService executorService = Executors.newFixedThreadPool(100);

  public static final int BUFFER_SIZE = 1024;
  public static final int SERVER_PORT = 9000;
  public static final int USER_PORT = 9001;
  public static final int MULTICAST_PORT = 9002;
  public static final String BROADCAST_ADDRESS = "192.168.5.255";

  /**
   * The main method of the Server class.
   * It loads the shared data, creates sockets, and starts various threads for server operations.
   *
   * @param args The command line arguments.
   */
  public static void main(String[] args) {
    try {
      SharedObject.loadData();
    } catch (Exception ignored) {
    }
    
    executorService.execute(() -> handleDirect(SERVER_PORT));

    try {
      SharedObject.setMulticastSocket(new MulticastSocket(MULTICAST_PORT));
      DatagramSocket broadcastSocket = new DatagramSocket(USER_PORT, InetAddress.getByName(BROADCAST_ADDRESS));
      broadcastSocket.setBroadcast(true);
      SharedObject.setBroadcastSocket(broadcastSocket);
    } catch (IOException io) {
      logger.severe("Error Creating Sockets! " + io.getMessage());
      close();
    }

    executorService.execute(new BroadcastThread());
    executorService.execute(new MulticastThread());
    executorService.execute(new EventsThread());
    executorService.execute(new ActiveUsersThread());
    executorService.execute(new RequestsStatsThread());
    executorService.execute(new DataPersistenceThread());
  }

  /**
   * Handles direct connections on the specified port.
   *
   * @param port The port number for direct connections.
   */
  public static void handleDirect(int port) {
    try (ServerSocket serverSocket = new ServerSocket(port)) {
      while (true) {
        executorService.execute(new DirectThread(serverSocket.accept()));
      }
    } catch (Exception e) {
      logger.severe("Error Handling Direct Connection! " + e.getMessage());
      close();
    }
  }

  /**
   * Closes the server by shutting down the executor service, saving the shared data, and exiting the application.
   */
  public static void close() {
    try {
      executorService.shutdown();
      SharedObject.saveData();
      System.exit(0);
    } catch (Exception ignored) {
    }
  }
}
