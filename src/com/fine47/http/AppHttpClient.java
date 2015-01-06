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

import android.app.Application;

/**
 * An {@link android.app.Application}-based HTTP client. Use this if you're not
 * interested in grouping requests by Activity or Context, which is the case
 * for most use cases.
 *
 * @see ActivityHttpClient
 */
public class AppHttpClient extends ActivityHttpClient {

  private static AppHttpClient instance;

  /**
   * Create an app-wide, global HTTP client and attach it to the specified
   * application instance. Note that only one app-wide instance can be created,
   * and an exception will be thrown if this class is instantiated more than
   * once.
   *
   * @param app application to attach the HTTP client to
   */
  public AppHttpClient(Application app) {
    super(app);

    // Keep the reference to this instance.
    if(null != instance) {
      throw new IllegalStateException(
        "Application-wide HTTP client has already been created.");
    }
    instance = AppHttpClient.this;
  }

  /**
   * Returns the app-wide, previously created HTTP client instance.
   *
   * @return app-wide HTTP client instance
   */
  public static AppHttpClient getInstance() {
    return instance;
  }

  @Override
  public void shutdown() {
    super.shutdown();
    instance = null;
  }
}
