package server.threads;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Logger;

import server.Server;
import server.dataStructures.SharedObject;
import server.protocols.Protocol;
import shared.enumerations.ConnectionType;

/**
 * The BroadcastThread class represents a thread that handles broadcasting messages to all connected clients.
 */
public class BroadcastThread implements Runnable {
  private static final Logger logger = Logger.getLogger(BroadcastThread.class.getName());
  
  /**
   * Constructs a new BroadcastThread.
   */
  public BroadcastThread() {}

  /**
   * Runs the broadcast thread.
   */
  @Override
  public void run() {
    try (
      DatagramSocket broadcastSocket = SharedObject.getBroadcastSocket();
    ) {
      while (true) {
        byte[] buffer = new byte[Server.BUFFER_SIZE];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        broadcastSocket.receive(packet);
        if (packet.getAddress().equals(InetAddress.getLocalHost())) {
          continue;
        }
        String input = new String(packet.getData());
        String output = Protocol.processInput(ConnectionType.BROADCAST, packet, input);
        if (output == null) {
          continue;
        }
        DatagramPacket response = new DatagramPacket(output.getBytes(), output.length(), packet.getAddress(), packet.getPort());
        try {
          broadcastSocket.send(response);
        } catch (IOException io) {
          logger.severe("Error Sending Broadcast Response: " + io.getMessage());
        }
      }
    } catch (IOException io) {
      logger.severe("Error Handling Broadcast Connection! " + io.getMessage());
    }
  }
}
