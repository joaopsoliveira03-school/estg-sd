package server.threads;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import server.dataStructures.SharedObject;
import server.dataStructures.interfaces.Event;
import server.dataStructures.interfaces.User;
import server.protocols.EventsHandler;
import server.protocols.ReceiverHandler;


/**
 * This class represents a thread that retrieves and sends message history for a user.
 */
public class MessageHistoryThread implements Runnable {
  private static final Logger logger = Logger.getLogger(MessageHistoryThread.class.getName());

  private User user;

  /**
   * Constructs a new MessageHistoryThread object.
   *
   * @param user the user for whom the message history will be retrieved and sent
   */
  public MessageHistoryThread(User user) {
    this.user = user;
  }

  /**
   * Runs the thread, retrieving and sending the message history for the user.
   */
  @Override
  public void run() {
    try {
      Thread.sleep(1000);
    } catch (InterruptedException ignored) {
      Thread.currentThread().interrupt();
    }
    List<? extends Event> events = SharedObject.getUserEvents(user);
    if (events.size() == 0) {
      return;
    }
    JSONObject jsonObject = new JSONObject();
    try {
      jsonObject.put("command", "history");
      jsonObject.put("events", EventsHandler.eventsToJson(events));
      ReceiverHandler.sendSomething(user, jsonObject.toString());
    } catch (JSONException | IOException error) {
      logger.severe("Error Sending Message History! " + error.getMessage());
    }
  }
}
