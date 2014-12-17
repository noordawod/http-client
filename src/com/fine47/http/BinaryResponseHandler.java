package com.fine47.http;

import com.loopj.android.http.BinaryHttpResponseHandler;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.http.Header;

class BinaryResponseHandler extends BinaryHttpResponseHandler {

  private final static HashMap<String, ArrayList<BinaryResponse>>
    HANDLERS = new HashMap(100);

  public final String url;

  BinaryResponseHandler(String url, BinaryResponse response) {
    super();
    setUseSynchronousMode(true);
    this.url = url;
    ArrayList<BinaryResponse> handlers = getHandlers();
    if(null == handlers) {
      handlers = new ArrayList();
      HANDLERS.put(this.url, handlers);
    }
    handlers.add(response);
  }

  @Override
  public void onCancel() {
  }

  @Override
  public void onFinish() {
    ArrayList<BinaryResponse> handlers = getHandlers();
    if(null != handlers) {
      handlers.clear();
    }
    HANDLERS.remove(url);
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
  }

  private ArrayList<BinaryResponse> getHandlers() {
    return HANDLERS.get(url);
  }
}
