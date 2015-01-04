package com.fine47.http;

public class BinaryRequest extends Request {

  /**
   * Create a new binary request for the specified end-point URL. No content
   * type will be sent.
   *
   * @param url request URL
   */
  public BinaryRequest(String url) {
    this(url, false);
  }

  /**
   * Create a new binary request for the specified end-point URL and control
   * whether the response should be cached or not.
   *
   * @param url request URL
   * @param noCache TRUE to enable caching for this request
   */
  public BinaryRequest(String url, boolean noCache) {
    super(url, null, noCache);
  }
}
