package server.threads;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import server.Server;
import server.dataStructures.SharedObject;
import server.dataStructures.interfaces.Request;

/**
 * This class represents a thread that periodically logs and broadcasts the statistics of requests and accepted requests.
 */
public class RequestsStatsThread implements Runnable {
  private static final Logger logger = Logger.getLogger(RequestsStatsThread.class.getName());

  /**
   * Runs the thread and performs the logging and broadcasting of request statistics.
   */
  @Override
  public void run() {
    // Requests & Accepted Requests For all users (broadcast)
    while (true) {
      try {
        Thread.sleep(10000);
      } catch (InterruptedException ignored) {
        Thread.currentThread().interrupt();
      }
      TreeSet<Request> requests = new TreeSet<>(SharedObject.getRequests());
      TreeSet<Request> acceptedRequests = new TreeSet<>(SharedObject.getAcceptedRequests(requests));
      logger.info("Requests: " + requests.size());
      logger.info("Accepted Requests: " + acceptedRequests.size());
      try {
        JSONObject json = new JSONObject();
        json.put("command", "message");
        json.put("from", "server");
        json.put("to", "broadcast");
        json.put("content", "Total Requests / Accepted Requests: " + requests.size() + " / " + acceptedRequests.size());
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        json.put("date", sdf.format(new Date()));

        String output = json.toString();

        DatagramPacket packet = new DatagramPacket(output.getBytes(), output.getBytes().length,
            InetAddress.getByName(Server.BROADCAST_ADDRESS), Server.USER_PORT);
        DatagramSocket socket = SharedObject.getBroadcastSocket();
        if (socket == null) {
          logger.severe("Broadcast socket is null!");
          continue;
        }
        socket.send(packet);
      } catch (IOException | JSONException error) {
        logger.severe("Error Sending Requests Stats! " + error.getMessage());
      }
    }
  }
}
