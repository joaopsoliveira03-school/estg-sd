package server.threads;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import server.dataStructures.SharedObject;
import server.dataStructures.interfaces.User;
import server.protocols.ReceiverHandler;

/**
 * This class represents a thread that periodically checks the number of online users and sends a message to the user with the highest role.
 */
public class ActiveUsersThread implements Runnable {

  private static final Logger logger = Logger.getLogger(ActiveUsersThread.class.getName());

  /**
   * The run method of the ActiveUsersThread class.
   * This method is executed when the thread starts.
   * It periodically checks the number of online users and sends a message to the user with the highest role.
   */
  @Override
  public void run() {
    // Number of Online Users Only for the highest role
    while (true) {
      try {
        Thread.sleep(10000);
      } catch (InterruptedException ignored) {
        Thread.currentThread().interrupt();
      }
      List<User> onlineUsers = SharedObject.getOnlineUsers();
      logger.info("Number of Online Users: " + onlineUsers.size());
      if (onlineUsers.size() == 0) {
        continue;
      }
      User highestRoleUser = SharedObject.getHighestRoleUser(onlineUsers);
      if (highestRoleUser == null) {
        logger.severe("Highest Role User is null!");
        continue;
      }
      try {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("command", "message");
        jsonObject.put("from", "server");
        jsonObject.put("to", highestRoleUser.getUsername());
        jsonObject.put("content", "Number of Online Users: " + onlineUsers.size());
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        jsonObject.put("date", sdf.format(new Date()));
        ReceiverHandler.sendSomething(highestRoleUser, jsonObject.toString());
      } catch (IOException | JSONException ignored) {
        logger.severe("Error Sending Active Users! " + ignored.getMessage());
      }
    }
  }
}
