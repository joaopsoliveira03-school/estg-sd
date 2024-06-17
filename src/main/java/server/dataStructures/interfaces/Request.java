package server.dataStructures.interfaces;

/**
 * This interface represents a request.
 * It extends the Event interface.
 */
public interface Request extends Event {

  /**
   * Gets the accepter of the request.
   * 
   * @return the accepter of the request
   */
  User getAccepter();

  /**
   * Sets the accepter of the request.
   * 
   * @param accepter the accepter of the request
   */
  void setAccepter(User accepter);
}
