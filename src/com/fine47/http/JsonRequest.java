package com.fine47.http;

import com.fine47.json.JsonArrayInterface;
import com.fine47.json.JsonObjectInterface;
import java.io.InputStream;

public class JsonRequest extends Request {

  private final static UnsupportedOperationException
    NO_ACCESS = new UnsupportedOperationException();

  public JsonRequest(String url) {
    super(url, ActivityHttpClient.CONTENT_TYPE_JSON);
    setUseJsonStreamer(true);
    setAutoCloseInputStreams(true);
  }

  @Override
  @Deprecated
  public void add(String key, String value) {
    // Use put(String key, String value) instead.
    throw NO_ACCESS;
  }

  @Override
  @Deprecated
  public void put(String key, InputStream io, String name, String type) {
    throw NO_ACCESS;
  }

  @Override
  public void put(String key, Object value) {
    if(null != key && null != value) {
      super.put(key, value);
    }
  }

  @Override
  public void put(String key, String value) {
    if(null != key && null != value) {
      super.put(key, value);
    }
  }

  public void put(String key, Number value) {
    if(null != key && null != value) {
      super.put(key, value);
    }
  }

  public void put(String key, Boolean value) {
    if(null != key && null != value) {
      super.put(key, value);
    }
  }

  public void put(String key, JsonObjectInterface value) {
    if(null != key && null != value) {
      super.put(key, value);
    }
  }

  public void put(String key, JsonArrayInterface value) {
    if(null != key && null != value) {
      super.put(key, value);
    }
  }
}
