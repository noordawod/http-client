package com.fine47.http;

public interface BinaryResponse {

  public boolean isAlive();

  public void onSuccess(byte[] response);

  public void onFailure(Throwable error, byte[] response);
}
