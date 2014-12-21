package com.fine47.http;

public class CachedRequest extends Request {

  /**
   * Create a new request for the specified end-point URL and enable caching for
   * it. No content type will be sent.
   *
   * @param url request URL
   */
  public CachedRequest(String url) {
    this(url, null);
  }

  /**
   * Create a new request for the specified end-point URL and enable caching for
   * it along with the specified content type.
   *
   * @param url request URL
   * @param contentType request's content type
   */
  public CachedRequest(String url, String contentType) {
    super(url, contentType, false);
  }
}
