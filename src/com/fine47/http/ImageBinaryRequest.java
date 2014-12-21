package com.fine47.http;

import android.graphics.Bitmap;

public class ImageBinaryRequest extends BinaryRequest {

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
  public ImageBinaryRequest(String url) {
    this(url, getPreferredBitmapConfig());
  }

  /**
   * Create a new download request for the specified image URL and attach the
   * specified bitmap configuration in case the image needs to be resized or
   * down-sized.
   *
   * @param url image's URL
   * @param bitmapConfig bitmap configuration to use
   */
  public ImageBinaryRequest(String url, Bitmap.Config bitmapConfig) {
    super(url);
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
