package server.protocols;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import server.Server;
import server.dataStructures.SharedObject;
import server.dataStructures.classes.UserImpl;
import server.dataStructures.interfaces.User;
import server.threads.MessageHistoryThread;
import shared.enumerations.Role;

/**
 * The ReceiverHandler class handles the registration, login, and communication
 * with users in the server.
 */
public class ReceiverHandler {
  private static final Logger logger = Logger.getLogger(ReceiverHandler.class.getName());
  
  /**
   * Registers a user with the provided JSON object and socket packet.
   * 
   * @param json The JSON object containing user information.
   * @param socketPacket The socket packet associated with the user.
   * @return A response message indicating the success or failure of the registration.
   * @throws JSONException If there is an error in parsing the JSON object.
   */
  public static String register(JSONObject json, Object socketPacket) throws JSONException {
    try {
      if (SharedObject.getUser(json.getString("username")) != null) {
        JSONObject response = new JSONObject();
        response.put("response", "User already exists!");
        logger.info("Attempted to create an existing user!");
        return response.toString();
      }
      Role roleEnum = Role.valueOf(json.getString("role").toUpperCase());
      User user = new UserImpl(json.getString("username"), json.getString("name"), json.getString("password"), roleEnum);
      SharedObject.addUser(user);
      Socket socket = (Socket) socketPacket;
      SharedObject.addUserSocket(user, socket);
      JSONObject response = new JSONObject();
      response.put("response", "OK");
      return response.toString();
    } catch (IllegalArgumentException e) {
      JSONObject response = new JSONObject();
      response.put("response", "Invalid role!");
      return response.toString();
    } catch (Exception e) {
      return e.getMessage();
    }
  }

  /**
   * This method handles the login process for a user.
   * It takes a JSON object containing the username and password,
   * and a socket packet object.
   * It checks if the username is valid and if the password matches.
   * If the login is successful, it adds the user's socket to the shared object
   * and starts a new thread for message history.
   * It returns a JSON string response indicating the result of the login process.
   *
   * @param json The JSON object containing the username and password.
   * @param socketPacket The socket packet object.
   * @return A JSON string response indicating the result of the login process.
   * @throws JSONException If there is an error accessing the JSON object.
   */
  public static String login(JSONObject json, Object socketPacket) throws JSONException {
    JSONObject response = new JSONObject();
    User user = SharedObject.getUser(json.getString("username"));
    if (user == null) {
      response.put("response", "Invalid username!");
      logger.info("Attempted to login with an invalid username!");
      return response.toString();
    }
    if (!user.getPassword().equals(json.getString("password"))) {
      logger.info("Attempted to login with an invalid password!");
      response.put("response", "Invalid password!");
      return response.toString();
    }
    Socket socket = (Socket) socketPacket;
    SharedObject.addUserSocket(user, socket);
    new Thread(new MessageHistoryThread(user)).start();
    response.put("response", "OK");
    return response.toString();
  }

  /**
   * Sends a message to the specified user.
   *
   * @param user     the user to send the message to
   * @param something the message to be sent
   * @throws IOException if an I/O error occurs while sending the message
   */
  public static void sendSomething(User user, String something) throws IOException {
    Socket socket = SharedObject.getUserSocket(user);
    Socket newSocket = new Socket(socket.getInetAddress(), Server.USER_PORT);
    PrintWriter out = new PrintWriter(newSocket.getOutputStream(), true);
    out.println(something);
    out.close();
    newSocket.close();
  }

  /**
    * Sends a request to the server and receives a response.
    * 
    * @param user the user making the request
    * @param something the request to be sent
    * @return the response received from the server
    * @throws IOException if an I/O error occurs while sending or receiving data
    */
  public static String sendAndReceiveSomething(User user, String something) throws IOException {
    Socket socket = SharedObject.getUserSocket(user);
    Socket newSocket = new Socket(socket.getInetAddress(), Server.USER_PORT);
    PrintWriter out = new PrintWriter(newSocket.getOutputStream(), true);
    BufferedReader in = new BufferedReader(new InputStreamReader(newSocket.getInputStream()));
    out.println(something);
    String response = in.readLine();
    out.close();
    newSocket.close();
    return response;
  }

  /**
   * Joins a group specified by the given JSON object.
   * 
   * @param json the JSON object containing the group and username information
   * @return null if the group joining is successful, otherwise null
   * @throws JSONException if the JSON object is invalid
   */
  public static String joinGroup(JSONObject json) throws JSONException {
    if (!json.has("group")) {
      logger.info("Attempted to join a group without specifying the group!");
      return null;
    }
    if(!json.getString("group").matches("^(22[4-9]|23[0-9]|2[4-9][0-9]|[3-9][0-9]{2}|[12][0-9]{3})\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")) {
      logger.info("Attempted to join a group with an invalid group!");
      return null;
    }
    if (!json.has("username")) {
      logger.info("Attempted to join a group without specifying the username!");
      return null;
    }
    User user = SharedObject.getUser(json.getString("username"));
    if (user == null) {
      logger.info("Attempted to join a group with an invalid username!");
      return null;
    }
    try {
      SharedObject.getMulticastSocket().joinGroup(InetAddress.getByName(json.getString("group")));
    } catch (IOException io) {}
    SharedObject.addUserToGroup(json.getString("group"), user);
    return null;
  }
}
