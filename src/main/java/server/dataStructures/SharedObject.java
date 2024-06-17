package server.dataStructures;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import server.dataStructures.interfaces.Event;
import server.dataStructures.interfaces.Request;
import server.dataStructures.interfaces.User;
import shared.enumerations.Role;

/**
 * The SharedObject class represents a shared object that stores data and provides methods for managing users, sockets, events, groups, and data persistence.
 */
public class SharedObject {
  private static Map<String, User> users = new HashMap<>();
  private static Map<User, Socket> userSockets = new HashMap<>();
  private static Map<User, TreeSet<Event>> userEvents = new HashMap<>();
  private static List<Event> eventsToDeliver = new ArrayList<>();
  private static Map<String, List<User>> groups = new HashMap<>();

  private static MulticastSocket multicastSocket;
  private static DatagramSocket broadcastSocket;

  //#region Entities Management
  /**
   * Adds a user to the shared object.
   * 
   * @param user the user to be added
   * @throws IllegalArgumentException if the user is null or already exists
   */
  public static void addUser(User user) {
    if (user == null) {
      throw new IllegalArgumentException("User cannot be null!");
    }
    synchronized (users) {
      if (users.containsKey(user.getUsername())) {
        throw new IllegalArgumentException("User already exists!");
      }
      users.put(user.getUsername(), user);
    }
  }

  /**
   * Retrieves the User object associated with the given username.
   *
   * @param username the username of the User to retrieve
   * @return the User object associated with the given username
   * @throws IllegalArgumentException if the username is null or empty
   */
  public static User getUser(String username) {
    if (username == null || username.isEmpty()) {
      throw new IllegalArgumentException("Username cannot be null or empty!");
    }
    synchronized (users) {
      return users.get(username);
    }
  }

  /**
   * Returns a list of all users.
   *
   * @return a list of User objects representing all users
   */
  public static List<User> getUsers() {
    synchronized (users) {
      return new ArrayList<>(users.values());
    }
  }

  /**
   * Gets the highest role user from a list of users.
   */
  public static User getHighestRoleUser(List<User> users) {
    User highestRoleUser = null;
    for (User user : users) {
      if (highestRoleUser == null || Role.getIndex(user.getRole()) > Role.getIndex(highestRoleUser.getRole())) {
        highestRoleUser = user;
      }
    }
    return highestRoleUser;
  }
  //#endregion

  //#region Entities Sockets Management
  /**
   * Adds a user and its corresponding socket to the userSockets map.
   * 
   * @param user   the user to be added
   * @param socket the socket associated with the user
   * @throws IllegalArgumentException if either the user or the socket is null
   */
  public static void addUserSocket(User user, Socket socket) {
    if (user == null) {
      throw new IllegalArgumentException("User cannot be null!");
    }
    if (socket == null) {
      throw new IllegalArgumentException("Socket cannot be null!");
    }
    synchronized (userSockets) {
      userSockets.put(user, socket);
    }
  }

  /**
   * Retrieves the socket associated with the specified user.
   *
   * @param user the user whose socket is to be retrieved
   * @return the socket associated with the user
   * @throws IllegalArgumentException if the user is null
   */
  public static Socket getUserSocket(User user) {
    if (user == null) {
      throw new IllegalArgumentException("User cannot be null!");
    }
    synchronized (userSockets) {
      return userSockets.get(user);
    }
  }

  /**
   * Retrieves a list of online users.
   * 
   * @return A list of User objects representing the online users.
   */
  public static List<User> getOnlineUsers() {
    Map<User, Socket> onlineUsers = new HashMap<>(userSockets);
    for (Map.Entry<User, Socket> entry : onlineUsers.entrySet()) {
      Socket socket = onlineUsers.get(entry.getKey());
      if (socket == null || socket.isClosed() || !socket.isConnected() || socket.isInputShutdown() || socket.isOutputShutdown()) {
        userSockets.remove(entry.getKey());
      }
    }
    return new ArrayList<User>(userSockets.keySet());
  }

  /**
   * Removes the specified user's socket from the userSockets list.
   * 
   * @param user the user whose socket needs to be removed
   * @throws IllegalArgumentException if the user is null
   */
  public static void removeUserSocket(User user) {
    if (user == null) {
      throw new IllegalArgumentException("User cannot be null!");
    }
    synchronized (userSockets) {
      userSockets.remove(user);
    }
  }
  //#endregion

  //#region Entities Events Management
  /**
   * Adds an event to the user's event list.
   * 
   * @param user the user to add the event to
   * @param event the event to be added
   * @throws IllegalArgumentException if the event or user is null
   */
  public static <E extends Event> void addUserEvent(User user, E event) {
    if (event == null) {
      throw new IllegalArgumentException("Event cannot be null!");
    }
    if (user == null) {
      throw new IllegalArgumentException("User cannot be null!");
    }
    synchronized (userEvents) {
      if (!userEvents.containsKey(user)) {
        userEvents.put(user, new TreeSet<>());
      }
      userEvents.get(user).add(event);
    }
  }

  /**
   * Retrieves the list of events associated with a given user.
   * 
   * @param user the user for which to retrieve the events
   * @return a list of events associated with the user, or an empty list if the user has no events
   * @throws IllegalArgumentException if the user is null
   */
  public static List<? extends Event> getUserEvents(User user) {
    if (user == null) {
      throw new IllegalArgumentException("User cannot be null!");
    }
    synchronized (userEvents) {
      if (!userEvents.containsKey(user)) {
        return new ArrayList<>();
      }
      return new ArrayList<>(userEvents.get(user));
    }
  }

  /**
   * Retrieves a list of all requests stored in the shared object.
   *
   * @return A list of Request objects representing the requests.
   */
  public static List<Request> getRequests() {
    List<Request> requests = new ArrayList<>();
    synchronized (userEvents) {
      for (Map.Entry<User, TreeSet<Event>> entry : userEvents.entrySet()) {
        for (Event event : entry.getValue()) {
          if (event instanceof Request) {
            requests.add((Request) event);
          }
        }
      }
    }
    return requests;
  }

  /**
   * Returns a list of accepted requests from the given collection of requests.
   *
   * @param requests the collection of requests to filter
   * @return a list of accepted requests
   */
  public static List<Request> getAcceptedRequests(Collection<Request> requests) {
    List<Request> acceptedRequests = new ArrayList<>();
    for (Request request : requests) {
      if (request.getAccepter() != null) {
        acceptedRequests.add(request);
      }
    }
    return acceptedRequests;
  }

  /**
   * Adds an event to the list of events to be delivered.
   * 
   * @param event the event to be added
   * @throws IllegalArgumentException if the event is null
   */
  public static <E extends Event> void addEventToDeliver(E event) {
    if (event == null) {
      throw new IllegalArgumentException("Event cannot be null!");
    }
    synchronized (eventsToDeliver) {
      eventsToDeliver.add(event);
    }
  }

  /**
   * Returns the list of events to be delivered.
   *
   * @return The list of events to be delivered.
   */
  public static List<? extends Event> getEventsToDeliver() {
    synchronized (eventsToDeliver) {
      return eventsToDeliver;
    }
  }

  /**
   * Removes the specified event from the list of events to be delivered.
   * 
   * @param event the event to be removed
   * @throws IllegalArgumentException if the event is null
   */
  public static <T extends Event> void removeEventDelivered(T event) {
    if (event == null) {
      throw new IllegalArgumentException("Event cannot be null!");
    }
    synchronized (eventsToDeliver) {
      eventsToDeliver.remove(event);
    }
  }
  //#endregion

  //#region Groups Management
  /**
   * Adds a user to a group.
   * 
   * @param group the group to add the user to
   * @param user the user to be added
   * @throws IllegalArgumentException if the group is null or empty, if the group is not a valid IP address, or if the user is null
   */
  public static void addUserToGroup(String group, User user) {
    if (group == null || group.isEmpty()) {
      throw new IllegalArgumentException("Group cannot be null or empty!");
    }
    if (!group.matches("^(22[4-9]|23[0-9]|2[4-9][0-9]|[3-9][0-9]{2}|[12][0-9]{3})\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")) {
      throw new IllegalArgumentException("Group must be a valid IP address!");
    }
    if (user == null) {
      throw new IllegalArgumentException("User cannot be null!");
    }
    synchronized (groups) {
      if (!groups.containsKey(group)) {
        groups.put(group, new ArrayList<>());
      }
      groups.get(group).add(user);
    }
  }

  /**
   * Retrieves the list of users belonging to a specific group.
   * 
   * @param group the name of the group
   * @return the list of users in the group, or an empty list if the group does not exist
   * @throws IllegalArgumentException if the group name is null or empty
   */
  public static List<User> getUsersFromGroup(String group) {
    if (group == null || group.isEmpty()) {
      throw new IllegalArgumentException("Group cannot be null or empty!");
    }
    synchronized (groups) {
      if (!groups.containsKey(group)) {
        return new ArrayList<>();
      }
      return new ArrayList<>(groups.get(group));
    }
  }
  //#endregion

  //#region Sockets Management
  /**
   * Returns the multicast socket used for communication.
   *
   * @return the multicast socket
   */
  public static MulticastSocket getMulticastSocket() {
    return multicastSocket;
  }

  /**
   * Sets the multicast socket for the shared object.
   *
   * @param multicastSocket the multicast socket to be set
   */
  public static void setMulticastSocket(MulticastSocket multicastSocket) {
    SharedObject.multicastSocket = multicastSocket;
  }

  /**
   * Returns the broadcast socket used for sending datagrams.
   *
   * @return the broadcast socket
   */
  public static DatagramSocket getBroadcastSocket() {
    return broadcastSocket;
  }

  /**
   * Sets the broadcast socket for the shared object.
   *
   * @param broadcastSocket the DatagramSocket to set as the broadcast socket
   */
  public static void setBroadcastSocket(DatagramSocket broadcastSocket) {
    SharedObject.broadcastSocket = broadcastSocket;
  }
  //#endregion

  //#region Data Persistence
  /**
   * Retrieves the data structures used in the application.
   * 
   * @return A map containing the data structures.
   */
  private static Map<String, Object> getDataStructures() {
    Map<String, Object> structures = new HashMap<>();
    structures.put("users.bin", users);
    structures.put("userEvents.bin", userEvents);
    structures.put("eventsToDeliver.bin", eventsToDeliver);
    structures.put("groups.bin", groups);
    return structures;
  }

  /**
   * Loads data from serialized files into the corresponding data structures.
   * The method reads serialized objects from files and populates the appropriate data structures
   * based on the file name.
   * 
   * @throws IOException            if an I/O error occurs while reading the files.
   * @throws ClassNotFoundException if the class of a serialized object cannot be found.
   */
  public static void loadData() throws IOException, ClassNotFoundException {
    for (Map.Entry<String, Object> entry : getDataStructures().entrySet()) {
      FileInputStream fileIn = new FileInputStream(entry.getKey());
      ObjectInputStream in = new ObjectInputStream(fileIn);

      switch (entry.getKey()) {
        case "users.bin":
          users.putAll((Map<String, User>) in.readObject());
          break;
        case "userEvents.bin":
          userEvents.putAll((Map<User, TreeSet<Event>>) in.readObject());
          break;
        case "eventsToDeliver.bin":
          eventsToDeliver.addAll((List<Event>) in.readObject());
          break;
        case "groups.bin":
          groups.putAll((Map<String, List<User>>) in.readObject());
          break;
      }

      in.close();
      fileIn.close();
    }
  }

  /**
   * Saves the data of all the data structures to separate files.
   * Each data structure is saved using its corresponding key as the file name.
   * @throws IOException if an I/O error occurs while saving the data.
   */
  public static void saveData() throws IOException {
    for (Map.Entry<String, Object> entry : getDataStructures().entrySet()) {
      FileOutputStream fileOut = new FileOutputStream("./" + entry.getKey());
      ObjectOutputStream out = new ObjectOutputStream(fileOut);
      out.writeObject(entry.getValue());
      out.close();
      fileOut.close();
    }
  }
  //#endregion
}
