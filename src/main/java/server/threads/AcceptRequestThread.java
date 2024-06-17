package server.threads;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import server.Server;
import server.dataStructures.SharedObject;
import server.dataStructures.interfaces.Request;
import server.dataStructures.interfaces.User;
import server.protocols.EventsHandler;
import server.protocols.ReceiverHandler;
import shared.enumerations.ConnectionType;
import shared.enumerations.Role;

/**
 * The AcceptRequestThread class represents a thread that handles the acceptance of a request.
 * It implements the Runnable interface, allowing it to be executed in a separate thread.
 */
public class AcceptRequestThread implements Runnable {
  private static final Logger logger = Logger.getLogger(AcceptRequestThread.class.getName());

  private ConnectionType connectionType;
  private Request request;

  /**
   * Constructs a new AcceptRequestThread with the specified connection type and request.
   *
   * @param connectionType the type of connection (DIRECT, MULTICAST, or BROADCAST)
   * @param request        the request to be accepted
   */
  public AcceptRequestThread(ConnectionType connectionType, Request request) {
    this.connectionType = connectionType;
    this.request = request;
  }

  /**
   * Executes the thread logic for accepting a request.
   * This method is called when the thread is started.
   */
  @Override
  public void run() {
    try {
      // Create a JSON object to represent the request answer
      JSONObject json = new JSONObject();
      json.put("command", "requestAnswer");
      json.put("from", request.getSender().getUsername());
      json.put("content", request.getContent());

      // Set the receiver of the request answer
      Object receiver = request.getReceiver();
      if (receiver instanceof User) {
        json.put("to", ((User) receiver).getUsername());
      } else if (receiver instanceof String) {
        String receiverString = (String) receiver;
        if (receiverString.equals("broadcast")) {
          json.put("to", "broadcast");
        } else if (receiverString.matches(
            "^(22[4-9]|23[0-9]|2[4-9][0-9]|[3-9][0-9]{2}|[12][0-9]{3})\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")) {
          json.put("to", receiverString);
        } else {
          logger.severe("Invalid receiver type! Not broadcast or group!");
          return;
        }
      } else {
        logger.severe("Invalid receiver type! Not user or string!");
        return;
      }

      // Send the request answer to the receiver
      DatagramPacket responsePacket;
      User from = request.getSender();
      String response;
      JSONObject jsonResponse;
      List<User> users;
      List<User> onlineUsers;
      switch (connectionType) {
        case DIRECT:
          User to = (User) request.getReceiver();
          response = ReceiverHandler.sendAndReceiveSomething(to, json.toString());
          if (response == null) {
            logger.severe("Error while receiving requestAnswer!");
            return;
          }
          jsonResponse = new JSONObject(response);
          if (jsonResponse.getString("response").equals("YES")) {
            request.setAccepter(to);
            ReceiverHandler.sendSomething(from, EventsHandler.eventToJson(request).toString());
            ReceiverHandler.sendSomething(to, EventsHandler.eventToJson(request).toString());
          }
          break;

        case MULTICAST:
          String group = (String) request.getReceiver();
          users = new ArrayList<>(SharedObject.getUsersFromGroup(group));
          logger.info("Users in group " + group + ": " + users.size());
          onlineUsers = new ArrayList<>(SharedObject.getUsersFromGroup(group));
          logger.info("Online users: " + onlineUsers.toString());
          users.remove(from);
          users.removeIf(user -> !onlineUsers.contains(user));
          users.removeIf(user -> Role.getIndex(user.getRole()) < Role.getIndex(from.getRole()));
          users.sort((user1, user2) -> Role.getIndex(user1.getRole()) - Role.getIndex(user2.getRole()));
          if (users.isEmpty()) {
            logger.info("No user to send requestAnswer to!");
            return;
          }
          for (User user : users) {
            if (user.equals(from)) {
              continue;
            }
            response = ReceiverHandler.sendAndReceiveSomething(user, json.toString());
            if (response == null) {
              logger.severe("Error while receiving requestAnswer!");
              continue;
            }
            logger.info("Received requestAnswer from " + user.getUsername());
            jsonResponse = new JSONObject(response);
            if (jsonResponse.getString("response").equals("YES")) {
              logger.info(user.getUsername() + " accepted requestAnswer!");
              request.setAccepter(user);
              String eventJson = EventsHandler.eventToJson(request).toString();
              if (eventJson == null) {
                logger.severe("Error while creating JSON object!");
                return;
              }
              responsePacket = new DatagramPacket(eventJson.getBytes(), eventJson.getBytes().length, InetAddress.getByName(group), Server.MULTICAST_PORT);
              SharedObject.getMulticastSocket().send(responsePacket);
              logger.info("Sent requestAnswer to " + group);
              break;
            } else {
              logger.info(user.getUsername() + " rejected requestAnswer!");
              users.remove(user);
            }
          }
          break;

        case BROADCAST:
          users = new ArrayList<>(SharedObject.getUsers());
          onlineUsers = new ArrayList<>(SharedObject.getOnlineUsers());
          users.remove(from);
          users.removeIf(user -> !onlineUsers.contains(user));
          users.removeIf(user -> Role.getIndex(user.getRole()) < Role.getIndex(from.getRole()));
          users.sort((user1, user2) -> Role.getIndex(user1.getRole()) - Role.getIndex(user2.getRole()));
          if (users.isEmpty()) {
            logger.info("No user to send requestAnswer to!");
            return;
          }
          for (User user : users) {
            response = ReceiverHandler.sendAndReceiveSomething(user, json.toString());
            if (response == null) {
              logger.severe("Error while receiving requestAnswer!");
              continue;
            }
            jsonResponse = new JSONObject(response);
            if (jsonResponse.getString("response").equals("YES")) {
              request.setAccepter(user);
              String eventJson = EventsHandler.eventToJson(request).toString();
              responsePacket = new DatagramPacket(eventJson.getBytes(), eventJson.getBytes().length, InetAddress.getByName(Server.BROADCAST_ADDRESS), Server.USER_PORT);
              SharedObject.getBroadcastSocket().send(responsePacket);
              break;
            }
          }
          break;
      }

    } catch (IOException io) {
      logger.severe("Error while sending requestAnswer!");
    } catch (JSONException json) {
      logger.severe("Error while creating JSON object!");
    }
  }
}
