package server.dataStructures.interfaces;

import java.io.Serializable;
import java.util.Date;

/**
 * The Event interface represents an event in the system.
 * It provides methods to get and set the sender, receiver, content, and date of the event.
 * The Event interface extends the Serializable and Comparable interfaces.
 */
public interface Event extends Serializable, Comparable<Event> {

  /**
   * Gets the sender of the event.
   *
   * @return the sender of the event
   */
  User getSender();

  /**
   * Sets the sender of the event.
   *
   * @param sender the sender of the event
   */
  void setSender(User sender);

  /**
   * Gets the receiver of the event.
   *
   * @return the receiver of the event
   */
  Object getReceiver();

  /**
   * Sets the receiver of the event.
   *
   * @param receiver the receiver of the event
   */
  void setReceiver(Object receiver);

  /**
   * Gets the content of the event.
   *
   * @return the content of the event
   */
  String getContent();

  /**
   * Sets the content of the event.
   *
   * @param content the content of the event
   */
  void setContent(String content);

  /**
   * Gets the date of the event.
   *
   * @return the date of the event
   */
  Date getDate();

  /**
   * Sets the date of the event.
   *
   * @param date the date of the event
   */
  void setDate(Date date);
}
