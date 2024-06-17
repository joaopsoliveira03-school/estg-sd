package server.dataStructures.interfaces;

import java.io.Serializable;

import shared.enumerations.Role;

/**
 * Represents a user in the system.
 * 
 * This interface defines the methods for accessing and modifying user information.
 * Users have a username, password, name, and role.
 * 
 * @see Serializable
 * @see Comparable
 */
public interface User extends Serializable, Comparable<User> {

  /**
   * Gets the username of the user.
   * 
   * @return the username of the user
   */
  String getUsername();

  /**
   * Sets the username of the user.
   * 
   * @param username the new username for the user
   */
  void setUsername(String username);

  /**
   * Gets the password of the user.
   * 
   * @return the password of the user
   */
  String getPassword();

  /**
   * Sets the password of the user.
   * 
   * @param password the new password for the user
   */
  void setPassword(String password);

  /**
   * Gets the name of the user.
   * 
   * @return the name of the user
   */
  String getName();

  /**
   * Sets the name of the user.
   * 
   * @param name the new name for the user
   */
  void setName(String name);

  /**
   * Gets the role of the user.
   * 
   * @return the role of the user
   */
  Role getRole();

  /**
   * Sets the role of the user.
   * 
   * @param role the new role for the user
   */
  void setRole(Role role);
}
