package com.fine47.http;

public class BinaryRequest extends Request {

  /**
   * Create a new binary request for the specified end-point URL. No content
   * type will be sent.
   *
   * @param url request URL
   */
  public BinaryRequest(String url) {
    this(url, null);
  }

  /**
   * Create a new binary request for the specified end-point URL along with the
   * specified content type.
   *
   * @param url request URL
   * @param contentType request's content type
   */
  public BinaryRequest(String url, String contentType) {
    super(url, contentType, true);
  }
}
