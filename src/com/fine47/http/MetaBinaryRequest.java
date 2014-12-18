package com.fine47.http;

public class MetaBinaryRequest<T> extends BinaryRequest {

  public final T meta;

  public MetaBinaryRequest(T meta, String url) {
    super(url);
    this.meta = meta;
  }
}
