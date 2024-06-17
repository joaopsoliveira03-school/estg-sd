package client.protocols;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import client.dataStructures.SharedObject;
/**
 * The EventsHandler class handles various events related to messaging and requests.
 * It provides methods for creating and receiving messages, requests, and event notifications.
 */
public class EventsHandler {
  private static final Logger logger = Logger.getLogger(EventsHandler.class.getName());
  
  /**
    * Creates a JSON object representing a message.
    *
    * @param destination the destination of the message
    * @param content the content of the message
    * @return the JSON object representing the message
    * @throws JSONException if there is an error creating the JSON object
    */
  public static JSONObject createMessage(String destination, String content) throws JSONException {
    JSONObject json = new JSONObject();

    json.put("command", "message");
    json.put("from", SharedObject.getUsername());
    json.put("to", destination);
    json.put("content", content);
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
    Date date = new Date();
    json.put("date", sdf.format(date));
    
    return json;
  }

  /**
   * Receives a JSON message and processes it.
   * 
   * @param json the JSON message to be processed
   * @return null
   * @throws JSONException if the JSON message is invalid
   */
  public static String receiveMessage(JSONObject json) throws JSONException {
    if (!(json.has("from") || json.has("to") || json.has("content") || json.has("date"))) {
      logger.severe("Invalid message received!");
      return null;
    }
    SharedObject.getChatRoom().receiveMessage(json.getString("from"), json.getString("to"), json.getString("content"), json.getString("date"));
    return null;
  }

  /**
    * Creates a JSON object representing a request.
    *
    * @param destination the destination of the request
    * @param content the content of the request
    * @return the JSON object representing the request
    * @throws JSONException if there is an error creating the JSON object
    */
  public static JSONObject createRequest(String destination, String content) throws JSONException {
    JSONObject json = new JSONObject();

    json.put("command", "request");
    json.put("from", SharedObject.getUsername());
    json.put("to", destination);
    json.put("content", content);
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
    Date date = new Date();
    json.put("date", sdf.format(date));
    json.put("accepter", "");
    
    return json;
  }

  /**
   * Receives a request in the form of a JSON object and processes it.
   * 
   * @param json the JSON object representing the request
   * @return null
   * @throws JSONException if the JSON object is invalid
   */
  public static String receiveRequest(JSONObject json) throws JSONException {
    if (!(json.has("from") || json.has("to") || json.has("content") || json.has("date") || json.has("accepter"))) {
      logger.severe("Invalid request received!");
      return null;
    }
    SharedObject.getChatRoom().receiveRequest(json.getString("from"), json.getString("to"), json.getString("content"), json.getString("date"), json.getString("accepter"));
    return null;
  }

  /**
   * Receives an answer request in the form of a JSON object and returns a response as a string.
   * 
   * @param json the JSON object representing the answer request
   * @return the response as a string
   * @throws JSONException if there is an error parsing the JSON object
   */
  public static String receiveAnswerRequest(JSONObject json) throws JSONException {
    if (!(json.has("from") || json.has("to") || json.has("content"))) {
      logger.severe("Invalid request received!");
      return null;
    }
    JSONObject response = new JSONObject();
    if (JOptionPane.showConfirmDialog(null, "Accept request from " + json.getString("from") + " to " + json.getString("to") + ": " + json.getString("content"), "Request: " + SharedObject.getUsername(), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
      response.put("response", "YES");
    } else {
      response.put("response", "NO");
    }
    return response.toString();
  }

  /**
   * Receives events from a JSON object and processes them accordingly.
   *
   * @param json The JSON object containing the events.
   * @return A string representing the result of processing the events.
   * @throws JSONException If there is an error parsing the JSON object.
   */
  public static String receiveEvents(JSONObject json) throws JSONException {
    if (!json.has("events")) {
      logger.severe("Invalid events received!");
      return null;
    }
    JSONArray events = json.getJSONArray("events");
    for (int i = 0; i < events.length(); i++) {
      JSONObject event = events.getJSONObject(i);
      if (!event.has("command")) {
        logger.severe("Invalid event received!");
        return null;
      }
      switch (event.getString("command")) {
        case "message":
          receiveMessage(event);
          break;
        case "request":
          receiveRequest(event);
          break;
        default:
          break;
      }
    }
    return null;
  }

  /**
   * Announces the joining of a group by sending a JSON message with the group IP, username, and command.
   * 
   * @param ip The IP address of the group to join.
   * @throws IOException If an I/O error occurs while sending the message.
   */
  public static void announceJoinGroup(String ip) throws IOException {
    try {
      JSONObject json = new JSONObject();
      json.put("command", "joinGroup");
      json.put("group", ip);
      json.put("username", SharedObject.getUsername());
      SharedObject.getDirectOut().println(json);
    } catch (JSONException e) {
      logger.severe("Error announcing join group! " + e.getMessage());
    }
  }
}
