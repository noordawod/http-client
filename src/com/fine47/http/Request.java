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

  public final String url;
  public final boolean noCache;
  public final ArrayList<Header> headers;
  public final String contentType;

  public Request(String url) {
    this(url, null);
  }

  public Request(String url, String contentType) {
    this(url, contentType, true);
  }

  public Request(String url, String contentType, boolean noCache) {
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

  public Request addHeader(Header header) {
    headers.add(header);
    return this;
  }

  public Request addHeader(String name, String value) {
    return addHeader(new BasicHeader(name, value));
  }

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

  boolean hasFiles() {
    return null != fileParams && !fileParams.isEmpty();
  }

  Map<String, RequestParams.FileWrapper> getFilesList() {
    return hasFiles() ? fileParams : null;
  }

  Map<String, String> getStringList() {
    return urlParams.isEmpty() ? null : urlParams;
  }

  Header[] getHeaders() {
    Header[] httpHeaders = new Header[headers.size()];
    headers.toArray(httpHeaders);
    return httpHeaders;
  }
}
