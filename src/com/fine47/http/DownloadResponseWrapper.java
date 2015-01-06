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

import com.fine47.cache.CacheInterface;
import com.fine47.http.request.AbstractRequest;
import com.fine47.http.response.BinaryResponse;
import com.fine47.http.response.AbstractResponse;
import org.apache.http.HttpException;

class DownloadResponseWrapper
  extends BinaryResponseWrapper
  implements BinaryResponse<Object>
{

  private final CacheInterface cache;
  private final AbstractResponse response;

  public DownloadResponseWrapper(
    CacheInterface cache,
    AbstractRequest request,
    AbstractResponse response
  ) {
    super(request);

    // Add response handler.
    addHandler(DownloadResponseWrapper.this);

    // Keep references to parameters.
    this.cache = cache;
    this.response = response;
  }

  @Override
  public boolean isAlive() {
    return response.isAlive();
  }

  @Override
  public void onSuccess(byte[] bytes, AbstractRequest request) {
    if(null == bytes || 0 == bytes.length) {
      response.onFailure(
        bytes,
        request,
        new HttpException("Downloaded zero bytes from remote server")
      );
    } else {
      // Store the result in the cache always.
      Object value = cache.store(request.url, bytes);

      // Fire the callback.
      response.onSuccess(value, request);
    }
  }

  @Override
  public void onFailure(
    byte[] bytes,
    AbstractRequest request,
    Throwable error
  ) {
    response.onFailure(null, request, error);
  }
}
