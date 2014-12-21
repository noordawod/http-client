package com.fine47.http;

import com.fine47.json.JsonArrayInterface;
import com.fine47.json.JsonObjectInterface;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import java.util.ArrayList;
import java.util.Map;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

public class Request extends RequestParams {

  /**
   * The request's end-point URL.
   */
  public final String url;

  /**
   * Whether this request could be cached or not.
   */
  public final boolean noCache;

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
  public Request(String url) {
    this(url, null);
  }

  /**
   * Create a new request for the specified end-point URL along with the
   * specified content type. Caching will be disabled.
   *
   * @param url request URL
   * @param contentType request's content type
   */
  public Request(String url, String contentType) {
    this(url, contentType, true);
  }

  /**
   * Create a new request for the specified end-point URL along with the
   * specified content type and optionally enable caching.
   *
   * Note that caching is not implemented by this library at this point.
   *
   * @param url request URL
   * @param contentType request's content type
   * @param noCache TRUE to enable caching for this request
   */
  protected Request(String url, String contentType, boolean noCache) {
    super();

    this.url = url;
    this.noCache = noCache;
    this.contentType = contentType;

    // Add a header to signal that we can decode GZIP data.
    headers = new ArrayList();
    headers.add(new BasicHeader(
      AsyncHttpClient.HEADER_ACCEPT_ENCODING,
      AsyncHttpClient.ENCODING_GZIP)
    );
  }

  /**
   * Add the specified header to the request.
   *
   * @param header to add to the request
   * @return "this" request, suitable for chaining
   */
  public Request addHeader(Header header) {
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
  public Request addHeader(String name, String value) {
    return addHeader(new BasicHeader(name, value));
  }

  /**
   * Cycles through the JSON object's keys and adds them, along with their
   * values, to the request.
   *
   * @param json JSON object data to add to the request
   */
  public void put(JsonObjectInterface json) {
    if(null == json) {
      return;
    }

    final String[] keys = json.keys();
    for(final String key : keys) {
      Object value = json.get(key);
      if(value instanceof JsonObjectInterface) {
        put(key, (JsonObjectInterface)value);
      } else if(value instanceof JsonArrayInterface) {
        put(key, (JsonArrayInterface)value);
      } else if(value instanceof Number) {
        put(key, (Number)value);
      } else if(value instanceof Boolean) {
        put(key, (Boolean)value);
      } else {
        put(key, value.toString());
      }
    }
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
