package server.dataStructures.classes;

import server.dataStructures.interfaces.Request;
import server.dataStructures.interfaces.User;

/**
 * Represents an implementation of the Request interface.
 * Extends the EventImpl class and provides additional functionality for managing requests.
 */
public class RequestImpl extends EventImpl implements Request {
  protected User accepter;

  /**
   * Constructs a new RequestImpl object.
   * 
   * @param sender   the User who sent the request
   * @param receiver the recipient of the request
   * @param content  the content of the request
   */
  public RequestImpl(User sender, Object receiver, String content) {
    super(sender, receiver, content);
    accepter = null;
  }

  /**
   * Gets the User who accepted the request.
   * 
   * @return the User who accepted the request, or null if the request has not been accepted yet
   */
  public User getAccepter() {
    return accepter;
  }

  /**
   * Sets the User who accepted the request.
   * 
   * @param accepter the User who accepted the request
   */
  public void setAccepter(User accepter) {
    this.accepter = accepter;
  }
}
