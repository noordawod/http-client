package com.fine47.http;

public class CachedRequest extends Request {

  public CachedRequest(String url) {
    this(url, null);
  }

  public CachedRequest(String url, String contentType) {
    super(url, contentType, false);
  }
}
