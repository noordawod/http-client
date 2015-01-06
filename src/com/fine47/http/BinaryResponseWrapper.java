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

package com.fine47.http;

import android.util.Log;
import com.fine47.http.request.AbstractRequest;
import com.fine47.http.response.BinaryResponse;
import com.loopj.android.http.BinaryHttpResponseHandler;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.http.Header;

class BinaryResponseWrapper extends BinaryHttpResponseHandler {

  private final static HashMap<String, ArrayList<BinaryResponse>>
    HANDLERS = new HashMap(100);

  private final AbstractRequest request;

  public BinaryResponseWrapper(AbstractRequest request) {
    super();

    // Always use the pool thread to fire callbacks.
    setUsePoolThread(true);

    // Keep references to parameters.
    this.request = request;
  }

  void addHandler(BinaryResponse response) {
    // Get defined handlers for this URL.
    String url = request.url;
    ArrayList<BinaryResponse> handlers = getHandlers(url);

    // If there are no handlers defined, create a new list.
    if(null == handlers) {
      handlers = new ArrayList();
      HANDLERS.put(url, handlers);
      if(ActivityHttpClient.isDebugging()) {
        Log.i(
          ActivityHttpClient.LOG_TAG,
          "Created new handlers array for URL: " + url
        );
      }
    }

    // Add the response handler to the list.
    handlers.add(response);
  }

  @Override
  public void onCancel() {
    if(ActivityHttpClient.isDebugging()) {
      Log.w(
        ActivityHttpClient.LOG_TAG,
        "Binary request cancelled for URL: " + request.url
      );
    }
  }

  @Override
  public void onFinish() {
    String url = request.url;
    final ArrayList<BinaryResponse> handlers = getHandlers(url);
    if(null != handlers) {
      handlers.clear();
    }
    HANDLERS.remove(url);
    if(ActivityHttpClient.isDebugging()) {
      Log.i(
        ActivityHttpClient.LOG_TAG,
        "Binary request finished for URL: " + url
      );
    }
  }

  @Override
  public void onSuccess(
    int statusCode,
    Header[] headers,
    byte[] response
  ) {
    final ArrayList<BinaryResponse> handlers = getHandlers(request.url);
    if(null != handlers) {
      for(BinaryResponse handler : handlers) {
        handler.onSuccess(response, request);
      }
    }
    if(ActivityHttpClient.isDebugging()) {
      Log.i(
        ActivityHttpClient.LOG_TAG,
        "Binary request successful for URL: " + request.url
      );
    }
  }

  @Override
  public void onFailure(
    int statusCode,
    Header[] headers,
    byte[] response,
    Throwable error
  ) {
    final ArrayList<BinaryResponse> handlers = getHandlers(request.url);
    if(null != handlers) {
      for(BinaryResponse handler : handlers) {
        handler.onFailure(response, request, error);
      }
      handlers.clear();
    }
    HANDLERS.remove(request.url);
    if(ActivityHttpClient.isDebugging()) {
      Log.e(
        ActivityHttpClient.LOG_TAG,
        "Binary request failed for URL: " +
          request.url +
        " (code=" + statusCode + ")",
        error
      );
    }
  }

  private ArrayList<BinaryResponse> getHandlers(String url) {
    return HANDLERS.get(url);
  }
}
