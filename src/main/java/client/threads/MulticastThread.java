package client.threads;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Logger;

import client.Client;
import client.dataStructures.SharedObject;
import client.protocols.Protocol;
import shared.enumerations.ConnectionType;

/**
 * This class represents a thread that handles multicast communication.
 */
public class MulticastThread implements Runnable {
  private static final Logger logger = Logger.getLogger(MulticastThread.class.getName());

  /**
   * Constructs a new MulticastThread object.
   */
  public MulticastThread() {
  }

  /**
   * Runs the multicast thread.
   */
  @Override
  public void run() {
    try (
        DatagramSocket socket = SharedObject.getMulticastSocket();) {
      while (true) {
        try {
          byte[] buffer = new byte[Client.BUFFER_SIZE];
          DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
          socket.receive(packet);
          if (packet.getAddress().equals(InetAddress.getLocalHost())) {
            continue;
          }
          String input = new String(packet.getData());
          String output = Protocol.processInput(ConnectionType.MULTICAST, input);
          if (output == null) {
            continue;
          }
          DatagramPacket response = new DatagramPacket(output.getBytes(), output.length(), packet.getAddress(),
              packet.getPort());
          socket.send(response);
        } catch (IOException io) {
          logger.severe("Error Handling Multicast Communication: " + io.getMessage());
        }
      }
    }
  }
}
