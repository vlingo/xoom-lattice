// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.exchange;

/**
 * A configuration for making a connection to the underlying exchange,
 * including information for the host, port, virtual host, and user.
 */
public class ConnectionSettings {
  /** A default value for undefined port numbers. */
  public static final int UndefinedPort = -1;

  /** My hostName, which is the name of the host server. */
  public final String hostName;

  /** My password, which is the password of the connecting user. */
  public final String password;

  /** My port, which is the host server port. */
  public final int port;

  /** My username, which is the name of the connecting user. */
  public final String username;

  /** My virtualHost, which is the name of the RabbitMQ virtual host. */
  public final String virtualHost;

  /**
   * Answer a new ConnectionSettings with defaults, used for tests only.
   * @return ConnectionSettings
   */
  public static ConnectionSettings instance() {
    return new ConnectionSettings("localhost", UndefinedPort, "/", null, null);
  }

  /**
   * Answer a new ConnectionSettings with a specific host name and virtual host
   * and remaining defaults.
   * @param hostName the String name of the host server
   * @param virtualHost the String name of the virtual host
   * @return ConnectionSettings
   */
  public static ConnectionSettings instance(String hostName, String virtualHost) {
    return new ConnectionSettings(hostName, UndefinedPort, virtualHost, null, null);
  }

  /**
   * Constructs my default state.
   * @param hostName the String name of the host server
   * @param port the int port number on the host server, or -1
   * @param virtualHost the String name of the virtual host
   * @param username the String name of the user, or null
   * @param password the String password of the user, or null
   * @return ConnectionSettings
   */
  public static ConnectionSettings instance(final String hostName, final int port, final String virtualHost, final String username, final String password) {
    return new ConnectionSettings(hostName, port, virtualHost, username, password);
  }

  /**
   * Constructs my default state.
   * @param hostName the String name of the host server
   * @param port the int port number on the host server, or -1
   * @param virtualHost the String name of the virtual host
   * @param username the String name of the user, or null
   * @param password the String password of the user, or null
   */
  public ConnectionSettings(final String hostName, final int port, final String virtualHost, final String username, final String password) {
    assert(hostName != null);
    this.hostName = hostName;
    assert (virtualHost != null);
    this.virtualHost = virtualHost;
    this.port = port;
    this.password = password;
    this.username = username;
  }

  /**
   * Answer whether or not a port is included.
   * @return boolean
   */
  public boolean hasPort() {
    return this.port > 0;
  }

  /**
   * Answer whether or not the user credentials are included.
   * @return boolean
   */
  public boolean hasUserCredentials() {
    return username != null && password != null;
  }
}
