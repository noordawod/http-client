package com.fine47.http;

import com.fine47.json.JsonArrayInterface;
import com.fine47.json.JsonObjectInterface;

public interface Response {

  /**
   * Returns whether the response handler in an "alive" condition when the time
   * comes to fire its callbacks methods.
   *
   * @return TRUE if the response handler is alive, FALSE otherwise
   */
  public boolean isAlive();

  /**
   * Returns whether the callbacks in this response handler require a long time
   * to operate; this will cause them to be fired within the pool thread and not
   * the UI thread.
   *
   * @return TRUE if the callbacks require long time to operate, FALSE otherwise
   */
  public boolean isLongRunning();

  /**
   * A callback to be fired when the response is a JSON object and has been
   * received successfully.
   *
   * @param response received from remote server
   */
  public void onSuccess(JsonObjectInterface response);

  /**
   * A callback to be fired when the response is a JSON array and has been
   * received successfully.
   *
   * @param response received from remote server
   */
  public void onSuccess(JsonArrayInterface response);

  /**
   * A callback to be fired when an error has occurred during the operation to
   * receive a JSON object from the remote server.
   *
   * @param error that caused the failure
   * @param response received from remote server, if any
   */
  public void onFailure(Throwable error, JsonObjectInterface response);

  /**
   * A callback to be fired when an error has occurred during the operation to
   * receive a JSON array from the remote server.
   *
   * @param error that caused the failure
   * @param response received from remote server, if any
   */
  public void onFailure(Throwable error, JsonArrayInterface response);
}
