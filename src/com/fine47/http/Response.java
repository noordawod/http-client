package com.fine47.http;

import com.fine47.json.JsonArrayInterface;
import com.fine47.json.JsonObjectInterface;

public interface Response {

  public boolean isAlive();

  public void onSuccess(JsonObjectInterface response);

  public void onSuccess(JsonArrayInterface response);

  public void onFailure(Throwable error, JsonObjectInterface response);

  public void onFailure(Throwable error, JsonArrayInterface response);
}
