package server.threads;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.logging.Logger;

import server.Server;
import server.dataStructures.SharedObject;
import server.protocols.Protocol;
import shared.enumerations.ConnectionType;

/**
 * MulticastThread class.
 * This class is responsible for handling the multicast connection.
 * It receives multicast packets, processes them, and sends responses back to the clients.
 */
public class MulticastThread implements Runnable {
  private static final Logger logger = Logger.getLogger(MulticastThread.class.getName());

  /**
   * MulticastThread constructor.
   */
  public MulticastThread() {}

  /**
   * Runs the multicast thread.
   * It continuously receives multicast packets, processes them, and sends responses back to the clients.
   */
  @Override
  public void run() {
    try (
        MulticastSocket multicastSocket = SharedObject.getMulticastSocket()) {
      while (true) {
        byte[] buffer = new byte[Server.BUFFER_SIZE];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        multicastSocket.receive(packet);
        if (packet.getAddress().equals(InetAddress.getLocalHost())) {
          continue;
        }
        String input = new String(packet.getData());
        String output = Protocol.processInput(ConnectionType.MULTICAST, packet, input);
        if (output == null) {
          continue;
        }
        DatagramPacket response = new DatagramPacket(output.getBytes(), output.length(), packet.getAddress(), packet.getPort());
        try {
          multicastSocket.send(response);
        } catch (IOException io) {
          logger.severe("Error sending Multicast Response: " + io.getMessage());
        }
      }
    } catch (IOException io) {
      logger.severe("Error Handling Multicast Connection! " + io.getMessage());
    }
  }
}
