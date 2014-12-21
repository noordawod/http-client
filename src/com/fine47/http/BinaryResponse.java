package com.fine47.http;

public interface BinaryResponse {

  /**
   * Returns whether the response handler in an "alive" condition when the time
   * comes to fire its callbacks methods.
   *
   * @return TRUE if the response handler is alive, FALSE otherwise
   */
  public boolean isAlive();

  /**
   * A callback to be fired when the response is received successfully.
   *
   * @param response received from remote server
   */
  public void onSuccess(byte[] response);

  /**
   * A callback to be fired when an error has occurred during the networking
   * operation.
   *
   * @param error that caused the failure
   * @param response received from remote server, if any
   */
  public void onFailure(Throwable error, byte[] response);
}
