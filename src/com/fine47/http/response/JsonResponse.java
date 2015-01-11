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

import com.fine47.json.JsonInterface;

/**
 * Interface for receiving and handling a JSON response, either an object or an
 * array.
 *
 * @param <T> type of JSON entity which this response will handle
 * @param <M> meta-data type which could be accompanying original request
 */
public interface JsonResponse<T extends JsonInterface, M>
  extends AbstractResponse<T, M>
{

  /**
   * Allows implementations to convert a a byte array to the JSON value handled
   * by this implementation.
   *
   * @param bytes byte array representing the JSON value
   * @return JSON value for configured type
   */
  public T convertBytes(byte[] bytes);
}
