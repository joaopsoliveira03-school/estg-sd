package client.protocols;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import client.dataStructures.SharedObject;

/**
 * This class handles user authentication operations such as registration and login.
 */
public class UserAuthHandler {

  // #region Register

  /**
   * Registers a new user with the provided information.
   * 
   * @param username The username of the user.
   * @param password The password of the user.
   * @param name     The name of the user.
   * @param role     The role of the user.
   * @return The response from the server indicating the success or failure of the registration.
   * @throws JSONException If there is an error in JSON processing.
   * @throws IOException   If there is an error in I/O operations.
   */
  public static String register(String username, String password, String name, String role) throws JSONException, IOException {
    JSONObject json = new JSONObject();

    json.put("command", "register");
    json.put("username", username);
    json.put("password", password);
    json.put("name", name);
    json.put("role", role);

    SharedObject.getDirectOut().println(json);

    JSONObject response = new JSONObject(SharedObject.getDirectIn().readLine());

    if (response.getString("response").equals("OK")) {
      SharedObject.setUsername(username);
    }
    return response.getString("response").toString();
  }
  // #endregion

  // #region Login

  /**
   * Logs in a user with the provided username and password.
   * 
   * @param username The username of the user.
   * @param password The password of the user.
   * @return The response from the server indicating the success or failure of the login.
   * @throws JSONException If there is an error in JSON processing.
   * @throws IOException   If there is an error in I/O operations.
   */
  public static String login(String username, String password) throws JSONException, IOException {
    JSONObject json = new JSONObject();

    json.put("command", "login");
    json.put("username", username);
    json.put("password", password);

    SharedObject.getDirectOut().println(json);

    JSONObject response = new JSONObject(SharedObject.getDirectIn().readLine());

    if (response.getString("response").equals("OK")) {
      SharedObject.setUsername(username);
    }
    return response.getString("response").toString();
  }
  // #endregion
}
