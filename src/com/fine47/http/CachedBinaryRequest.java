package com.fine47.http;

public class CachedBinaryRequest extends Request {

  /**
   * Create a new binary request for the specified end-point URL and enable
   * caching for it. No content type will be sent.
   *
   * @param url request URL
   */
  public CachedBinaryRequest(String url) {
    this(url, null);
  }

  /**
   * Create a new binary request for the specified end-point URL along with the
   * specified content type and enable caching for it.
   *
   * @param url request URL
   * @param contentType request's content type
   */
  public CachedBinaryRequest(String url, String contentType) {
    super(url, contentType, false);
  }
}
