package client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import client.dataStructures.SharedObject;
import client.threads.BroadcastThread;
import client.threads.DirectThread;
import client.threads.MulticastThread;

/**
 * The Client class represents a client application that interacts with a server.
 * It provides methods for initializing the client, starting the client threads,
 * and shutting down the client.
 */
public class Client {
  private static Logger logger = Logger.getLogger(Client.class.getName());
  
  public static final String SERVER_ADDRESS = "server";
  public static final String BROADCAST_ADDRESS = "192.168.5.255";
  public static final int SERVER_PORT = 9000;
  public static final int CLIENT_PORT = 9001;
  public static final int MULTICAST_PORT = 9002;
  public static final int BUFFER_SIZE = 1024;

  private static ExecutorService executorService;

  /**
   * The main method of the Client class.
   * It initializes the client, starts the client threads, and adds a shutdown hook.
   *
   * @param args The command line arguments.
   */
  public static void main(String[] args) {
    logger.info("Starting...");
    SharedObject.init();
    executorService = Executors.newFixedThreadPool(3);

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      logger.info("Shutting down...");
      close();
    }));
  }

  /**
   * Initializes the client in an authenticated state.
   * It initializes the shared object, and starts the direct, broadcast, and multicast threads.
   */
  public static void initAuthenticated() {
    SharedObject.initAuthenticated();
    executorService.execute(new DirectThread());
    executorService.execute(new BroadcastThread());
    executorService.execute(new MulticastThread());
  }

  /**
   * Closes the client.
   * It shuts down the direct thread executor service, shuts down the client executor service,
   * closes the shared object, and exits the application.
   */
  public static void close() {
    try {
      DirectThread.getExecutorService().shutdown();
      executorService.shutdown();
      SharedObject.close();
      System.exit(0);
    } catch (Exception ignored) {}
  }
}
