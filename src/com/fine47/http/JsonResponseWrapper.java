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
import com.fine47.http.ActivityHttpClient;
import com.fine47.http.request.JsonRequest;
import com.fine47.http.response.JsonResponse;
import com.fine47.json.JsonInterface;
import com.loopj.android.http.JsonHttpResponseHandler;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

class JsonResponseWrapper extends JsonHttpResponseHandler {

  private final JsonRequest request;
  private final JsonResponse response;

  public JsonResponseWrapper(JsonRequest request, JsonResponse response) {
    super();

    // Always use the pool thread to fire callbacks.
    setUsePoolThread(true);

    // Keep references to parameters.
    this.request = request;
    this.response = response;
  }

  @Override
  public void onCancel() {
  }

  @Override
  public void onSuccess(
    int statusCode,
    Header[] headers,
    JSONObject result
  ) {
    JsonInterface json = response.normalizeNativeJson(result);
    if(ActivityHttpClient.isDebugging()) {
      Log.d(
        ActivityHttpClient.LOG_TAG,
        "Response JSON (Object): \n" +
        (null == json ? "null" : json.toString())
      );
    }
    response.onSuccess(json, request);
  }

  @Override
  public void onSuccess(
    int statusCode,
    Header[] headers,
    JSONArray result
  ) {
    JsonInterface json = response.normalizeNativeJson(result);
    if(ActivityHttpClient.isDebugging()) {
      Log.d(
        ActivityHttpClient.LOG_TAG,
        "Response JSON (Array): \n" +
        (null == result ? "null" : result.toString())
      );
    }
    response.onSuccess(json, request);
  }

  @Override
  public void onFailure(
    int statusCode,
    Header[] headers,
    String result,
    Throwable error
  ) {
    onFailure(
      statusCode,
      headers,
      error,
      (JSONObject)null
    );
  }

  @Override
  public void onFailure(
    int statusCode,
    Header[] headers,
    Throwable error,
    JSONObject result
  ) {
    JsonInterface json = response.normalizeNativeJson(result);
    if(ActivityHttpClient.isDebugging()) {
      Log.d(
        ActivityHttpClient.LOG_TAG,
        "Failure while expecting JSON (Object) result: \n" +
        (null == json ? "null" : json.toString())
      );
    }
    response.onFailure(json, request, error);
  }

  @Override
  public void onFailure(
    int statusCode,
    Header[] headers,
    Throwable error,
    JSONArray result
  ) {
    JsonInterface json = response.normalizeNativeJson(result);
    if(ActivityHttpClient.isDebugging()) {
      Log.e(
        ActivityHttpClient.LOG_TAG,
        "Failure while expecting JSON (Array) result: \n" +
        (null == json ? "null" : json.toString())
      );
    }
    response.onFailure(json, request, error);
  }
}
