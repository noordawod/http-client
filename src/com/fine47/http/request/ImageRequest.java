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

import android.graphics.Bitmap;

/**
 * A request suitable for downloading images as it allows including a handy
 * configuration for late-bitmap processing once the image is downloaded.
 *
 * @param <M> meta-data type which could be accompanying this request
 */
public class ImageRequest<M> extends Request<M> {

  /**
   * Preferred bitmap configuration for the downloaded image.
   */
  public final Bitmap.Config bitmapConfig;

  /**
   * Create a new download request for the specified image URL. Use a heuristic
   * test to determine the preferred bitmap configuration to use for this image,
   * in case it needs to be resized or down-sized.
   *
   * @param url image's URL
   */
  public ImageRequest(String url) {
    this(url, getPreferredBitmapConfig(), null);
  }

  /**
   * Create a new download request for the specified image URL. Use a heuristic
   * test to determine the preferred bitmap configuration to use for this image,
   * in case it needs to be resized or down-sized.
   *
   * @param url image's URL
   * @param metaData optional meta-data to accompany the request
   */
  public ImageRequest(String url, M metaData) {
    this(url, getPreferredBitmapConfig(), null);
  }

  /**
   * Create a new download request for the specified image URL and attach the
   * specified bitmap configuration in case the image needs to be resized or
   * down-sized.
   *
   * @param url image's URL
   * @param bitmapConfig bitmap configuration to use
   */
  public ImageRequest(String url, Bitmap.Config bitmapConfig) {
    this(url, bitmapConfig, null);
  }

  /**
   * Create a new download request for the specified image URL and attach the
   * specified bitmap configuration in case the image needs to be resized or
   * down-sized.
   *
   * @param url image's URL
   * @param bitmapConfig bitmap configuration to use
   * @param metaData optional meta-data to accompany the request
   */
  public ImageRequest(String url, Bitmap.Config bitmapConfig, M metaData) {
    super(url, null, metaData);
    this.bitmapConfig = bitmapConfig;
  }

  /**
   * Returns the preferred bitmap configuration for the device/system where this
   * app is running.
   *
   * @return preferred bitmap configuration
   */
  public static Bitmap.Config getPreferredBitmapConfig() {
    // If total heap size is larger than 33MB, then use full ARGB.
    return Runtime.getRuntime().maxMemory() > 34603008
      ? Bitmap.Config.ARGB_8888
      : Bitmap.Config.RGB_565;
  }
}
