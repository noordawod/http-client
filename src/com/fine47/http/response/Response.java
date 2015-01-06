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

package com.fine47.http.response;

import com.fine47.http.request.Request;

/**
 * An abstract response to handle results of type {@link E} and requests having
 * accompanying meta-data of type {link M}.
 *
 * @param <E> type of resources which the response handles
 * @param <M> meta-data type which could be accompanying the request
 */
public interface Response<E, M> {

  /**
   * Returns whether the response handler in an "alive" condition when the time
   * comes to fire its callbacks methods.
   *
   * @return TRUE if the response handler is alive, FALSE otherwise
   */
  public boolean isAlive();

  /**
   * A callback to be fired when the response has been received successfully.
   *
   * @param response received from remote server
   * @param request original request for this response
   */
  public void onSuccess(E response, Request<M> request);

  /**
   * A callback to be fired when an error has occurred during the operation to
   * receive a response from the remote server.
   *
   * @param response received from remote server, if any
   * @param request original request for this response
   * @param error that caused the failure
   */
  public void onFailure(E response, Request<M> request, Throwable error);
}
