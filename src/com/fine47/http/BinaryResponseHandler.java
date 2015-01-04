package com.fine47.http;

import android.util.Log;
import com.loopj.android.http.BinaryHttpResponseHandler;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.http.Header;

class BinaryResponseHandler extends BinaryHttpResponseHandler {

  private final static HashMap<String, ArrayList<BinaryResponse>>
    HANDLERS = new HashMap(100);

  public final String url;

  public BinaryResponseHandler(String url, BinaryResponse response) {
    super();

    // Binary downloads will always use the pool thread to fire callbacks.
    setUsePoolThread(true);

    // Remember the URL.
    this.url = url;

    // Get defined handlers for this URL.
    ArrayList<BinaryResponse> handlers = getHandlers();

    // If there are no handlers defined, create a new list.
    if(null == handlers) {
      handlers = new ArrayList();
      HANDLERS.put(this.url, handlers);
      if(ActivityHttpClient.isDebugging()) {
        Log.i(
          ActivityHttpClient.LOG_TAG,
          "Created new handlers array for URL: " + url);
      }
    }

    // Add the response handler to the list.
    handlers.add(response);
  }

  @Override
  public void onCancel() {
    if(ActivityHttpClient.isDebugging()) {
      Log.w(
        ActivityHttpClient.LOG_TAG,
        "Binary request cancelled for URL: " + url);
    }
  }

  @Override
  public void onFinish() {
    ArrayList<BinaryResponse> handlers = getHandlers();
    if(null != handlers) {
      handlers.clear();
    }
    HANDLERS.remove(url);
    if(ActivityHttpClient.isDebugging()) {
      Log.i(
        ActivityHttpClient.LOG_TAG,
        "Binary request finished for URL: " + url);
    }
  }

  @Override
  public void onSuccess(
    int statusCode,
    Header[] headers,
    byte[] response
  ) {
    ArrayList<BinaryResponse> handlers = getHandlers();
    if(null != handlers) {
      for(BinaryResponse handler : handlers) {
        handler.onSuccess(response);
      }
    }
    if(ActivityHttpClient.isDebugging()) {
      Log.i(
        ActivityHttpClient.LOG_TAG,
        "Binary request successful for URL: " + url);
    }
  }

  @Override
  public void onFailure(
    int statusCode,
    Header[] headers,
    byte[] response,
    Throwable error
  ) {
    ArrayList<BinaryResponse> handlers = getHandlers();
    if(null != handlers) {
      for(BinaryResponse handler : handlers) {
        handler.onFailure(error, response);
      }
      handlers.clear();
    }
    HANDLERS.remove(url);
    if(ActivityHttpClient.isDebugging()) {
      Log.e(
        ActivityHttpClient.LOG_TAG,
        "Binary request failed for URL: " + url + " (code=" + statusCode + ")",
        error);
    }
  }

  private ArrayList<BinaryResponse> getHandlers() {
    return HANDLERS.get(url);
  }
}
