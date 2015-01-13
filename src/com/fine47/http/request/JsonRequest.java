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
import com.fine47.json.JsonArrayInterface;
import com.fine47.json.JsonObjectInterface;
import java.io.InputStream;
import org.json.JSONException;

/**
 * A JSON request with easy methods to set data to be sent to a remote server.
 *
 * @param <M> meta-data type which could be accompanying this request
 */
public class JsonRequest<M> extends AbstractRequest<M> {

  /**
   * Creates a new JSON request and use the specified URL as its end-point.
   *
   * @param url request's end-point URL
   */
  public JsonRequest(String url) {
    this(url, null);
  }

  /**
   * Creates a new JSON request and use the specified URL as its end-point.
   *
   * @param url request's end-point URL
   * @param metaData optional meta-data to accompany the request
   */
  public JsonRequest(String url, M metaData) {
    super(url, ActivityHttpClient.CONTENT_TYPE_JSON, metaData);
    setUseJsonStreamer(true);
    setAutoCloseInputStreams(true);
  }

  @Override
  @Deprecated
  public void add(String key, String value) {
    // Use put(String key, String value) instead.
    throw ActivityHttpClient.NO_ACCESS;
  }

  @Override
  @Deprecated
  public void put(String key, InputStream io, String name, String type) {
    throw ActivityHttpClient.NO_ACCESS;
  }

  @Override
  public void put(String key, Object value) {
    if(null != key && null != value) {
      super.put(key, value);
    }
  }

  /**
   * Adds a JSON object identified by the specified key to the request.
   *
   * @param key to identify the value
   * @param value JSON object to add to the request
   */
  public void put(String key, JsonObjectInterface value) {
    try {
      put(key, new org.json.JSONObject(value.getAsString()));
    } catch (JSONException error) {
      if(ActivityHttpClient.isDebugging()) {
        Log.e(
          ActivityHttpClient.LOG_TAG,
          "Unable to attach a JSON object: " + value,
          error
        );
      }
    }
  }

  /**
   * Adds a JSON array identified by the specified key to the request.
   *
   * @param key to identify the value
   * @param value JSON array to add to the request
   */
  public void put(String key, JsonArrayInterface value) {
    try {
      put(key, new org.json.JSONArray(value.getAsString()));
    } catch (JSONException error) {
      if(ActivityHttpClient.isDebugging()) {
        Log.e(
          ActivityHttpClient.LOG_TAG,
          "Unable to attach a JSON array: " + value,
          error
        );
      }
    }
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
   * Adds a string value identified by the specified key to the request.
   *
   * @param key to identify the value
   * @param value string value to add to the request
   */
  @Override
  public void put(String key, String value) {
    if(null != key && null != value) {
      super.put(key, value);
    }
  }

  /**
   * Adds a numeric value identified by the specified key to the request.
   *
   * @param key to identify the value
   * @param value numeric value to add to the request
   */
  public void put(String key, Number value) {
    if(null != key && null != value) {
      super.put(key, value);
    }
  }

  /**
   * Adds a boolean value identified by the specified key to the request.
   *
   * @param key to identify the value
   * @param value boolean value to add to the request
   */
  public void put(String key, Boolean value) {
    if(null != key && null != value) {
      super.put(key, value);
    }
  }
}
