package server.dataStructures.classes;

import java.util.Date;

import server.dataStructures.interfaces.Event;
import server.dataStructures.interfaces.User;

/**
 * Represents an abstract implementation of the Event interface.
 */
public abstract class EventImpl implements Event {
  protected User sender;
  private Object receiver;
  private String content;
  protected Date date;

  /**
   * Constructs a new EventImpl object.
   *
   * @param sender   the User who sent the event
   * @param receiver the recipient of the event
   * @param content  the content of the event
   */
  public EventImpl(User sender, Object receiver, String content) {
    this.sender = sender;
    this.receiver = receiver;
    this.content = content;
    this.date = new Date();
  }

  /**
   * Gets the sender of the event.
   *
   * @return the sender of the event
   */
  public User getSender() {
    return sender;
  }

  /**
   * Sets the sender of the event.
   *
   * @param sender the sender of the event
   */
  public void setSender(User sender) {
    this.sender = sender;
  }

  /**
   * Gets the receiver of the event.
   *
   * @return the receiver of the event
   */
  public Object getReceiver() {
    return receiver;
  }

  /**
   * Sets the receiver of the event.
   *
   * @param receiver the receiver of the event
   */
  public void setReceiver(Object receiver) {
    this.receiver = receiver;
  }

  /**
   * Gets the content of the event.
   *
   * @return the content of the event
   */
  public String getContent() {
    return content;
  }

  /**
   * Sets the content of the event.
   *
   * @param content the content of the event
   */
  public void setContent(String content) {
    this.content = content;
  }

  /**
   * Gets the date of the event.
   *
   * @return the date of the event
   */
  public Date getDate() {
    return date;
  }

  /**
   * Sets the date of the event.
   *
   * @param date the date of the event
   */
  public void setDate(Date date) {
    this.date = date;
  }

  /**
   * Compares this event with the specified event for order.
   *
   * @param o the event to be compared
   * @return a negative integer, zero, or a positive integer as this event is less than, equal to, or greater than the specified event
   */
  @Override
  public int compareTo(Event o) {
    return this.date.compareTo(o.getDate());
  }

  /**
   * Indicates whether some other object is "equal to" this event.
   *
   * @param obj the reference object with which to compare
   * @return true if this event is the same as the obj argument; false otherwise
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (obj == null || getClass() != obj.getClass()) return false;
    Event event = (Event) obj;
    return this.sender.equals(event.getSender()) && this.receiver.equals(event.getReceiver()) && this.content.equals(event.getContent()) && this.date.equals(event.getDate());
  }

  /**
   * Returns a hash code value for the event.
   *
   * @return a hash code value for this event
   */
  @Override
  public int hashCode() {
    return sender.hashCode() + receiver.hashCode() + content.hashCode() + date.hashCode();
  }
}
