package server.threads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import server.Server;
import server.dataStructures.SharedObject;
import server.dataStructures.interfaces.Event;
import server.dataStructures.interfaces.Message;
import server.dataStructures.interfaces.Request;
import server.dataStructures.interfaces.User;
import server.protocols.EventsHandler;

/**
 * The EventsThread class represents a thread that delivers events to users.
 * It continuously checks for events in the shared object and delivers them to the appropriate users.
 */
public class EventsThread implements Runnable {

  private static final Logger logger = Logger.getLogger(EventsThread.class.getName());

  /**
   * The run method is the entry point for the thread.
   * It continuously checks for events in the shared object and delivers them to the appropriate users.
   */
  @Override
  public void run() {
    while (true) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException ignored) {
        Thread.currentThread().interrupt();
      }
      List<? extends Event> events = SharedObject.getEventsToDeliver();
      logger.info("Events to deliver: " + events.size());
      for (int i = 0; i < events.size(); i++) {
        Event event = events.get(i);

        Object receiver = event.getReceiver();

        if (receiver instanceof User) {
          User user = ((User) receiver);
          logger.info("Event to deliver to " + user.getUsername());
          Socket socket = SharedObject.getUserSocket(user);
          if (socket == null || socket.isClosed() || !socket.isConnected()) {
            logger.severe("User Socket is Null or Closed");
            SharedObject.removeEventDelivered(event);
            continue;
          }
          try (
              Socket newSocket = new Socket(socket.getInetAddress(), Server.USER_PORT);
              BufferedReader in = new BufferedReader(new InputStreamReader(newSocket.getInputStream()));
              PrintWriter out = new PrintWriter(newSocket.getOutputStream(), true)) {
            if (event instanceof Message) {
              Message message = ((Message) event);
              JSONObject json = EventsHandler.eventToJson(message);
              if (json == null) {
                logger.severe("Event to JSON returned null!");
                SharedObject.removeEventDelivered(event);
                continue;
              }
              out.println(EventsHandler.eventToJson(message).toString());
              logger.info("Message delivered to " + user.getUsername());
            } else if (event instanceof Request) {
              Request request = ((Request) event);
              JSONObject json = EventsHandler.eventToJson(request);
              if (json == null) {
                logger.severe("Event to JSON returned null!");
                SharedObject.removeEventDelivered(event);
                continue;
              }
              out.println(EventsHandler.eventToJson(request).toString());
              logger.info("Request delivered to " + user.getUsername());
            }
            SharedObject.removeEventDelivered(event);
          } catch (IOException io) {
            SharedObject.removeUserSocket(user);
          } catch (JSONException json) {
            SharedObject.removeEventDelivered(event);
          }
        } else {
          logger.severe("Receiver is not a user!");
          SharedObject.removeEventDelivered(event);
        }
      }
    }
  }
}
