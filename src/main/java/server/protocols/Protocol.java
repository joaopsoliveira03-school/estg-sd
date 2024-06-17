package server.protocols;

import java.net.Socket;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import server.dataStructures.SharedObject;
import server.dataStructures.interfaces.User;
import shared.enumerations.ConnectionType;

/**
 * This class represents the protocol used for processing input in the server.
 */
public class Protocol {
  private static final Logger logger = Logger.getLogger(Protocol.class.getName());
  /**
   * Processes the input based on the given connection type, socket packet, and input string.
   * 
   * @param connectionType The type of connection (DIRECT or INDIRECT).
   * @param socketPacket The socket packet object.
   * @param input The input string to be processed.
   * @return The response as a JSON string.
   */
  public static String processInput(ConnectionType connectionType, Object socketPacket, String input) {
    JSONObject response = new JSONObject();
    try {
      JSONObject json = new JSONObject(input);
      if (!json.has("command")) {
        response.put("response", "Invalid command!");
        return response.toString();
      }

      // Register the user's socket if it is not already registered
      Socket socket;
      if (connectionType == ConnectionType.DIRECT) {
        if (json.has("username") || json.has("from")) {
          User User = null;
          if (json.has("username")) {
            User = SharedObject.getUser(json.getString("username"));
          }
          if (json.has("from")) {
            User = SharedObject.getUser(json.getString("from"));
          }
          if (User != null) {
            if (socketPacket instanceof Socket) {
              socket = (Socket) socketPacket;
              if (socket != SharedObject.getUserSocket(User)) {
                SharedObject.addUserSocket(User, socket);
              }
            }
          }
        }
      }

      // Process the input based on the command
      switch (json.getString("command")) {
        case "register":
          if (connectionType != ConnectionType.DIRECT) {
            return null;
          }
          socket = (Socket) socketPacket;
          return ReceiverHandler.register(json, socketPacket);
        case "login":
          if (connectionType != ConnectionType.DIRECT) {
            return null;
          }
          socket = (Socket) socketPacket;
          return ReceiverHandler.login(json, socketPacket);
        case "message":
          EventsHandler.receiveMessage(connectionType, json);
          return null;
        case "request":
          EventsHandler.receiveRequest(connectionType, json, socketPacket);
          return null;
        case "joinGroup":
          ReceiverHandler.joinGroup(json);
          return null;
        default:
          logger.severe("Invalid command received! " + input);
          return null;
      }

    } catch (JSONException e) {
      logger.severe("Invalid JSON received! " + e.getMessage());
      return null;
    }
  }
}
