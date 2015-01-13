/**
 * This file is part of HTTP Client library.
 * Copyright (C) 2014 Noor Dawod. All rights reserved.
 * https://github.com/noordawod/http-client
 *
 * Released under the MIT license
 * http://en.wikipedia.org/wiki/MIT_License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.fine47.http.request;

import android.util.Log;
import com.fine47.http.ActivityHttpClient;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import java.util.ArrayList;
import java.util.Map;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

/**
 * The base class for all requests that are to be handled by this library.
 *
 * @param <M> meta-data type which could be accompanying this request
 */
public class AbstractRequest<M> extends RequestParams {

  public enum TYPE {
    HEAD, GET, POST, PUT, PATCH, DELETE
  };

  /**
   * The request's end-point URL.
   */
  public final String url;

  /**
   * Optional meta-data to accompany this request.
   */
  public final M metaData;

  /**
   * The headers to send along with this request.
   */
  public final ArrayList<Header> headers;

  /**
   * The request's content type.
   */
  public final String contentType;

  /**
   * Create a new request for the specified end-point URL. No content type will
   * be sent.
   *
   * @param url request URL
   */
  public AbstractRequest(String url) {
    this(url, null, null);
  }

  /**
   * Create a new request for the specified end-point URL along with the
   * specified content type. Caching will be disabled.
   *
   * @param url request URL
   * @param contentType request's content type
   */
  public AbstractRequest(String url, String contentType) {
    this(url, contentType, null);
  }

  /**
   * Create a new request for the specified end-point URL along with the
   * specified content type. Caching will be disabled.
   *
   * @param url request URL
   * @param metaData optional meta-data to accompany the request
   */
  public AbstractRequest(String url, M metaData) {
    this(url, null, metaData);
  }

  /**
   * Create a new request for the specified end-point URL along with the
   * specified content type and optionally an accompanying meta-data.
   *
   * @param url request URL
   * @param contentType request's content type
   * @param metaData optional meta-data to accompany the request
   */
  protected AbstractRequest(String url, String contentType, M metaData) {
    super();

    this.url = url;
    this.metaData = metaData;
    this.contentType = contentType;

    // Add a header to signal that we can decode GZIP data.
    headers = new ArrayList();
    headers.add(new BasicHeader(
      AsyncHttpClient.HEADER_ACCEPT_ENCODING,
      AsyncHttpClient.ENCODING_GZIP)
    );

    if(ActivityHttpClient.isDebugging()) {
      Log.d(ActivityHttpClient.LOG_TAG, "Request: " + this);
    }
  }

  @Override
  public String toString() {
    return url + (
      null == contentType ? "" : "(Content-Type: " + contentType + ")"
    );
  }

  /**
   * Add the specified header to the request.
   *
   * @param header to add to the request
   * @return "this" request, suitable for chaining
   */
  public AbstractRequest addHeader(Header header) {
    headers.add(header);
    return this;
  }

  /**
   * Add the specified header's name and value to the request.
   *
   * @param name header's name to add to the request
   * @param value header's value to add to the request
   * @return "this" request, suitable for chaining
   */
  public AbstractRequest addHeader(String name, String value) {
    return addHeader(new BasicHeader(name, value));
  }

  /**
   * Checks whether the request includes files as well.
   *
   * @return TRUE if the request includes files, FALSE otherwise
   */
  public boolean hasFiles() {
    return null != fileParams && !fileParams.isEmpty();
  }

  /**
   * Returns a map of all files that are to be sent with this request.
   *
   * @return list of files as a hash map
   */
  public Map<String, RequestParams.FileWrapper> getFilesList() {
    return hasFiles() ? fileParams : null;
  }

  /**
   * Returns a map of all GET parameters that are to be sent with this request.
   *
   * @return list of GET parameters as a hash map
   */
  public Map<String, String> getParameters() {
    return urlParams.isEmpty() ? null : urlParams;
  }

  /**
   * Returns a list of all headers that are supposed to be sent with this
   * request.
   *
   * @return list of all headers
   */
  public Header[] getHeaders() {
    Header[] httpHeaders = new Header[headers.size()];
    headers.toArray(httpHeaders);
    return httpHeaders;
  }
}
