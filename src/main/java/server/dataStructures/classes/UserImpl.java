package server.dataStructures.classes;

import server.dataStructures.interfaces.User;
import shared.enumerations.Role;

/**
 * Represents a concrete implementation of the User interface.
 */
public class UserImpl implements User {
  private String username;
  private String name;
  private String password;
  private Role role;

  /**
   * Constructs a UserImpl object with the specified username, name, password, and role.
   *
   * @param username the username of the user
   * @param name the name of the user
   * @param password the password of the user
   * @param role the role of the user
   */
  public UserImpl(String username, String name, String password, Role role) {
    setUsername(username);
    setName(name);
    setPassword(password);
    setRole(role);
  }

  /**
   * Gets the username of the user.
   *
   * @return the username of the user
   */
  public String getUsername() {
    return username;
  }

  /**
   * Sets the username of the user.
   *
   * @param username the username to set
   * @throws IllegalArgumentException if the username is null or empty
   */
  public void setUsername(String username) {
    if (username == null || username.isEmpty()) {
      throw new IllegalArgumentException("Username cannot be null or empty!");
    }
    this.username = username;
  }

  /**
   * Gets the name of the user.
   *
   * @return the name of the user
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name of the user.
   *
   * @param name the name to set
   * @throws IllegalArgumentException if the name is null or empty
   */
  public void setName(String name) {
    if (name == null || name.isEmpty()) {
      throw new IllegalArgumentException("Name cannot be null or empty!");
    }
    this.name = name;
  }

  /**
   * Gets the password of the user.
   *
   * @return the password of the user
   */
  public String getPassword() {
    return password;
  }

  /**
   * Sets the password of the user.
   *
   * @param password the password to set
   * @throws IllegalArgumentException if the password is null or empty
   */
  public void setPassword(String password) {
    if (password == null || password.isEmpty()) {
      throw new IllegalArgumentException("Password cannot be null or empty!");
    }
    this.password = password;
  }

  /**
   * Gets the role of the user.
   *
   * @return the role of the user
   */
  public Role getRole() {
    return role;
  }

  /**
   * Sets the role of the user.
   *
   * @param role the role to set
   * @throws IllegalArgumentException if the role is null
   */
  public void setRole(Role role) {
    if (role == null) {
      throw new IllegalArgumentException("Role cannot be null!");
    }
    this.role = role;
  }

  /**
   * Compares this user to the specified user for order based on their roles.
   *
   * @param o the user to be compared
   * @return a negative integer, zero, or a positive integer as this user is less than, equal to, or greater than the specified user
   */
  @Override
  public int compareTo(User o) {
    return Role.getIndex(this.getRole()) - Role.getIndex(o.getRole());
  }

  /**
   * Indicates whether some other object is "equal to" this one.
   *
   * @param obj the reference object with which to compare
   * @return true if this object is the same as the obj argument; false otherwise
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (obj == null || getClass() != obj.getClass()) return false;
    UserImpl user = (UserImpl) obj;
    return this.username.equals(user.username) && this.name.equals(user.name) && this.password.equals(user.password) && this.role.equals(user.role);
  }

  /**
   * Returns a hash code value for the object.
   *
   * @return a hash code value for this object
   */
  @Override
  public int hashCode() {
    return username.hashCode();
  }
}
