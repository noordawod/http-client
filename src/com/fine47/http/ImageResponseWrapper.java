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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import com.fine47.http.request.ImageRequest;
import com.fine47.http.response.ImageResponse;

class ImageResponseWrapper<M>
  extends AbstractResponseWrapper<Bitmap, M>
{

  public ImageResponseWrapper(
    ImageRequest<M> request,
    ImageResponse<M> response
  ) {
    super(new String[] {"^image/[a-z-]+$"}, request, response);
  }

  @Override
  Bitmap bytesToValue(byte[] bytes) {
    final ImageRequest imageRequest = (ImageRequest)request;
    final ImageResponse imageResponse = (ImageResponse)response;
    
    // Use high-resolution first to generate the bitmap.
    Bitmap bitmap = generateBitmap(
      Bitmap.Config.ARGB_8888,
      bytes,
      imageResponse.isMutable()
    );

    // If fallback configuration is not the highest, try it also.
    if(null == bitmap && Bitmap.Config.ARGB_8888 != imageRequest.bitmapConfig) {
      bitmap = generateBitmap(
        Bitmap.Config.ARGB_8888,
        bytes,
        imageResponse.isMutable()
      );
    }
    
    return bitmap;
  }

  static Bitmap generateBitmap(
    Bitmap.Config config,
    byte[] bytes,
    boolean isMutable
  ) {
    try {
      return BitmapFactory
        .decodeByteArray(bytes, 0, bytes.length)
        .copy(config, isMutable);
    } catch(OutOfMemoryError error) {
      if(ActivityHttpClient.isDebugging()) {
        Log.e(
          ActivityHttpClient.LOG_TAG,
          "Out of memory error while decoding " +
            bytes.length +
          " bytes to generate a bitmap",
          error
        );
      }
    }
    return null;
  }
}
