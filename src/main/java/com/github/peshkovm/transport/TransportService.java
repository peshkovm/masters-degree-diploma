package com.github.peshkovm.transport;

import com.github.peshkovm.common.codec.Message;

/**
 * Transport service for nodes network communication.
 */
public interface TransportService {

  /**
   * Asynchronously connects to node. This method will wait for connection completion.
   *
   * @param discoveryNode node to connect to
   */
  void connectToNode(DiscoveryNode discoveryNode);

  /**
   * Asynchronously disconnects from node. This method will wait for disconnection completion.
   *
   * @param discoveryNode node to disconnect from
   */
  void disconnectFromNode(DiscoveryNode discoveryNode);

  /**
   * Asynchronously sends message to node. This method will not wait for sending completion.
   *
   * @param discoveryNode node to send message to
   * @param msg msg to send
   */
  void send(DiscoveryNode discoveryNode, Message msg);
}
