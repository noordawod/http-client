package com.fine47.http;

import android.graphics.Bitmap;

public class FixedImageBinaryRequest extends ImageBinaryRequest {

  public final int width;
  public final int height;

  public FixedImageBinaryRequest(String url) {
    this(url, 0, 0);
  }

  public FixedImageBinaryRequest(
    String url,
    int width,
    int height
  ) {
    super(url);
    this.width = width;
    this.height = height;
  }

  public FixedImageBinaryRequest(
    String url,
    int width,
    int height,
    Bitmap.Config bitmapConfig
  ) {
    super(url, bitmapConfig);
    this.width = width;
    this.height = height;
  }
}
