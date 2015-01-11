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
import com.fine47.http.response.AbstractResponse;
import com.loopj.android.http.BinaryHttpResponseHandler;
import org.apache.http.Header;
import org.apache.http.HttpException;

abstract class AbstractResponseWrapper<E, M> 
  extends BinaryHttpResponseHandler 
{

  final AbstractRequest<M> request;
  final AbstractResponse<E, M> response;
  
  public AbstractResponseWrapper(
    AbstractRequest<M> request,
    AbstractResponse<E, M> response
  ) {
    super();

    // Always use the pool thread to fire callbacks.
    setUsePoolThread(true);

    // Keep references to parameters.
    this.request = request;
    this.response = response;
  }

  public AbstractResponseWrapper(
    String[] allowedContentTypes,
    AbstractRequest<M> request,
    AbstractResponse<E, M> response
  ) {
    super(allowedContentTypes);

    // Always use the pool thread to fire callbacks.
    setUsePoolThread(true);

    // Keep references to parameters.
    this.request = request;
    this.response = response;
  }
  
  @Override
  public void onCancel() {
    if(ActivityHttpClient.isDebugging()) {
      Log.w(
        ActivityHttpClient.LOG_TAG,
        "Request cancelled for URL: " + request.url
      );
    }
  }

  @Override
  public void onFinish() {
    if(ActivityHttpClient.isDebugging()) {
      Log.i(
        ActivityHttpClient.LOG_TAG,
        "Request finished for URL: " + request.url
      );
    }
  }

  @Override
  public void onSuccess(
    int statusCode,
    Header[] headers,
    byte[] bytes
  ) {
    E value;
    if(
      null == bytes || 
      0 == bytes.length || 
      null == (value = bytesToValue(bytes))
    ) {
      onFailure(
        statusCode, 
        headers, 
        bytes, 
        new HttpException(
          "Response body is empty or cannot be converted to a value."
        )
      );
    } else {
      response.onSuccess(value, request);
      if(ActivityHttpClient.isDebugging()) {
        Log.i(
          ActivityHttpClient.LOG_TAG,
          "Request successful for URL: " + request.url
        );
      }
    }
  }

  @Override
  public void onFailure(
    int statusCode,
    Header[] headers,
    byte[] bytes,
    Throwable error
  ) {
    response.onFailure(
      null == bytes ? null : bytesToValue(bytes), 
      request, 
      error
    );
    if(ActivityHttpClient.isDebugging()) {
      Log.e(
        ActivityHttpClient.LOG_TAG,
        "Request failed for URL: " +
          request.url +
        " (code=" + statusCode + ")",
        error
      );
    }
  }

  abstract E bytesToValue(byte[] bytes);
}
