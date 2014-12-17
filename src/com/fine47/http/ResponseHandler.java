package com.fine47.http;

import com.loopj.android.http.JsonHttpResponseHandler;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

class ResponseHandler extends JsonHttpResponseHandler {

  private final Response response;

  ResponseHandler(Response response) {
    super();
    setUseSynchronousMode(true);
    this.response = response;
  }

  @Override
  public void onCancel() {
  }

  @Override
  public void onSuccess(
    int statusCode,
    Header[] headers,
    JSONObject result
  ) {
    response.onSuccess(
      HttpClient.getInstance().normalizeJson(result)
    );
  }

  @Override
  public void onSuccess(
    int statusCode,
    Header[] headers,
    JSONArray result
  ) {
    response.onSuccess(
      HttpClient.getInstance().normalizeJson(result)
    );
  }

  @Override
  public void onFailure(
    int statusCode,
    Header[] headers,
    String result,
    Throwable error
  ) {
    onFailure(
      statusCode,
      headers,
      error,
      (JSONObject)null
    );
  }

  @Override
  public void onFailure(
    int statusCode,
    Header[] headers,
    Throwable error,
    JSONObject result
  ) {
    response.onFailure(
      error,
      HttpClient.getInstance().normalizeJson(result)
    );
  }

  @Override
  public void onFailure(
    int statusCode,
    Header[] headers,
    Throwable error,
    JSONArray result
  ) {
    response.onFailure(
      error,
      HttpClient.getInstance().normalizeJson(result)
    );
  }
}
