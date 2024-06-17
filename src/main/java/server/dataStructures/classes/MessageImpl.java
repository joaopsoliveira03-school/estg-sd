package server.dataStructures.classes;

import server.dataStructures.interfaces.Message;
import server.dataStructures.interfaces.User;

/**
 * Represents an implementation of the Message interface.
 * Extends the EventImpl class and implements the Message interface.
 */
public class MessageImpl extends EventImpl implements Message {
  
  /**
   * Constructs a new MessageImpl object.
   * 
   * @param sender   the User who sent the message
   * @param receiver the recipient of the message
   * @param content  the content of the message
   */
  public MessageImpl(User sender, Object receiver, String content) {
    super(sender, receiver, content);
  }
}
