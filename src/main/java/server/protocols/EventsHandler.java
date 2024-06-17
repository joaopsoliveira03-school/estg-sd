package server.protocols;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import server.dataStructures.SharedObject;
import server.dataStructures.classes.MessageImpl;
import server.dataStructures.classes.RequestImpl;
import server.dataStructures.interfaces.Event;
import server.dataStructures.interfaces.Message;
import server.dataStructures.interfaces.Request;
import server.dataStructures.interfaces.User;
import server.threads.AcceptRequestThread;
import shared.enumerations.ConnectionType;

public class EventsHandler {
  private static final Logger logger = Logger.getLogger(EventsHandler.class.getName());
  private static final ExecutorService executorService = Executors.newFixedThreadPool(50);

  /**
   * Returns the ExecutorService used by the EventsHandler.
   *
   * @return the ExecutorService used by the EventsHandler
   */
  public static ExecutorService getExecutorService() {
    return executorService;
  }

  /**
   * Converts an event to a JSON object.
   * 
   * @param event The event to convert.
   * @return The JSON object representation of the event.
   */
  public static <E extends Event> JSONObject eventToJson(E event) throws JSONException {
    JSONObject json = new JSONObject();
    json.put("from", event.getSender().getUsername());
    Object receiver = event.getReceiver();
    if (receiver instanceof User) {
      json.put("to", ((User) receiver).getUsername());
    } else if (receiver instanceof String) {
      String receiverString = (String) receiver;
      if (receiverString.equals("broadcast")) {
        json.put("to", "broadcast");
      } else if (receiverString.matches(
          "^(22[4-9]|23[0-9]|2[4-9][0-9]|[3-9][0-9]{2}|[12][0-9]{3})\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")) {
        json.put("to", receiverString);
      }
    } else {
      logger.severe("Invalid receiver type!");
    }
    json.put("content", event.getContent());
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
    json.put("date", sdf.format(event.getDate()));
    if (event instanceof Message) {
      json.put("command", "message");
      return json;
    } else if (event instanceof Request) {
      json.put("command", "request");
      Request request = (Request) event;
      if (request.getAccepter() != null) {
        json.put("accepter", request.getAccepter().getUsername());
      } else {
        json.put("accepter", "");
      }
      return json;
    }
    throw new JSONException("Invalid event type");
  }


  /**
   * Converts a collection of events to a JSON array.
   *
   * @param events The collection of events to convert.
   * @return The JSON array representation of the events.
   * @throws JSONException If an error occurs while converting the events to JSON.
   */
  public static JSONArray eventsToJson(Collection<? extends Event> events) throws JSONException {
    JSONArray jsonArray = new JSONArray();
    for (Event event : events) {
      jsonArray.put(eventToJson(event));
    }
    return jsonArray;
  }

  /**
   * Converts a JSON object to a message.
   * 
   * @param json The JSON object to convert.
   * @return The message representation of the JSON object.
   */
  public static Message messageFromJson(JSONObject json) {
    try {
      if (!json.has("from") || !json.has("to") || !json.has("content")) {
        logger.severe("Invalid message received! (field missing)");
        return null;
      }
      if (json.getString("from").equals("server")) {
        return null;
      }
      User from = SharedObject.getUser(json.getString("from"));
      if (from == null) {
        logger.severe("Invalid message received! (User from)");
        return null;
      }
      String to = json.getString("to");
      String content = json.getString("content");
      Message message = new MessageImpl(from, null, content);
      if (to.equals("broadcast")) {
        message.setReceiver(to);
      } else if (to.matches(
          "^(22[4-9]|23[0-9]|2[4-9][0-9]|[3-9][0-9]{2}|[12][0-9]{3})\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")) {
        message.setReceiver(to);
      } else {
        message.setReceiver(SharedObject.getUser(to));
      }
      if (message.getReceiver() == null) {
        logger.severe("Invalid message received! (User to)");
        return null;
      }
      return message;
    } catch (JSONException ignored) {
      logger.severe("Invalid message received! (JSONException)");
      return null;
    }
  }

  /**
   * Converts a JSON object to a request.
   * 
   * @param json The JSON object to convert.
   * @return The request representation of the JSON object.
   */
  public static Request requestFromJson(JSONObject json) {
    try {
      if (!json.has("from") || !json.has("to") || !json.has("content")) {
        logger.severe("Invalid request received! (field missing)");
        return null;
      }
      User from = SharedObject.getUser(json.getString("from"));
      if (from == null) {
        logger.severe("Invalid request received! (User from)");
        return null;
      }
      String to = json.getString("to");
      String content = json.getString("content");
      Request request = new RequestImpl(from, null, content);
      if (to.equals("broadcast")) {
        request.setReceiver(to);
      } else if (to.matches(
          "^(22[4-9]|23[0-9]|2[4-9][0-9]|[3-9][0-9]{2}|[12][0-9]{3})\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")) {
        request.setReceiver(to);
      } else {
        request.setReceiver(SharedObject.getUser(to));
      }
      if (request.getReceiver() == null) {
        logger.severe("Invalid request received! (User to)");
        return null;
      }
      return request;
    } catch (JSONException ignored) {
      logger.severe("Invalid request received! (JSONException)");
      return null;
    }
  }

  /// ! Protocol methods

  /**
   * Receives a message and processes it based on the connection type and the message content.
   * 
   * @param connectionType the type of connection (DIRECT or BROADCAST)
   * @param json the JSON object containing the message data
   * @return always returns null
   */
  public static String receiveMessage(ConnectionType connectionType, JSONObject json) {
    Message message = messageFromJson(json);
    if (message != null) {
      Object Receiver = message.getReceiver();
      if (Receiver instanceof User) {
        SharedObject.addUserEvent((User) message.getReceiver(), message);
      } else if (Receiver instanceof String) {
        String receiverString = (String) Receiver;
        if (receiverString.equals("broadcast")) {
          Collection<User> users = SharedObject.getUsers();
          for (User user : users) {
            SharedObject.addUserEvent(user, message);
          }
        } else if (receiverString.matches(
            "^(22[4-9]|23[0-9]|2[4-9][0-9]|[3-9][0-9]{2}|[12][0-9]{3})\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")) {
          Collection<User> users = SharedObject.getUsersFromGroup(receiverString);
          for (User user : users) {
            SharedObject.addUserEvent(user, message);
          }
        } else {
          User user = SharedObject.getUser(receiverString);
          if (user != null) {
            SharedObject.addUserEvent(user, message);
          }
        }
      }
      SharedObject.addUserEvent(message.getSender(), message);
      if (connectionType == ConnectionType.DIRECT) {
        SharedObject.addEventToDeliver(message);
      }
    }
    return null;
  }

  /**
   * Receives a request and processes it based on the connection type, JSON data, and socket packet.
   * 
   * @param connectionType The type of connection (DIRECT or INDIRECT).
   * @param json The JSON object containing the request data.
   * @param socketPacket The socket packet associated with the request.
   * @return The response string.
   */
  public static String receiveRequest(ConnectionType connectionType, JSONObject json, Object socketPacket) {
    Request request = requestFromJson(json);
    if (request != null) {
      Object Receiver = request.getReceiver();
      if (Receiver instanceof User) {
        SharedObject.addUserEvent((User) request.getReceiver(), request);
      } else if (Receiver instanceof String) {
        String receiverString = (String) Receiver;
        if (receiverString.equals("broadcast")) {
          Collection<User> users = SharedObject.getUsers();
          for (User user : users) {
            SharedObject.addUserEvent(user, request);
          }
        } else if (receiverString.matches(
            "^(22[4-9]|23[0-9]|2[4-9][0-9]|[3-9][0-9]{2}|[12][0-9]{3})\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")) {
          Collection<User> users = SharedObject.getUsersFromGroup(receiverString);
          for (User user : users) {
            SharedObject.addUserEvent(user, request);
          }
        } else {
          User user = SharedObject.getUser(receiverString);
          if (user != null) {
            SharedObject.addUserEvent(user, request);
          }
        }
      }
      SharedObject.addUserEvent(request.getSender(), request);
      if (connectionType == ConnectionType.DIRECT) {
        SharedObject.addEventToDeliver(request);
      }
      executorService.execute(new AcceptRequestThread(connectionType, request));
    }
    return null;
  }
}
