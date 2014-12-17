package com.fine47.http;

import com.fine47.json.JsonObjectInterface;

public interface JsonResponse {

  public boolean isAlive();

  public void onSuccess(JsonObjectInterface response);

  public void onFailure(int errCode, String err, JsonObjectInterface response);
}
