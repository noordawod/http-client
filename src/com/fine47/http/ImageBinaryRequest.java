package com.fine47.http;

import android.graphics.Bitmap;

public class ImageBinaryRequest extends BinaryRequest {

  public final Bitmap.Config bitmapConfig;

  public ImageBinaryRequest(String url) {
    this(url, getPreferredBitmapConfig());
  }

  public ImageBinaryRequest(String url, Bitmap.Config bitmapConfig) {
    super(url);
    this.bitmapConfig = bitmapConfig;
  }

  public static Bitmap.Config getPreferredBitmapConfig() {
    // If total heap size is larger than 33MB, then use full ARGB.
    return Runtime.getRuntime().maxMemory() > 34603008
      ? Bitmap.Config.ARGB_8888
      : Bitmap.Config.RGB_565;
  }
}
