package com.fine47.http;

import com.loopj.android.http.JsonHttpResponseHandler;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

class ResponseHandler extends JsonHttpResponseHandler {

  private Response response;
  private ActivityHttpClient client;

  public ResponseHandler(ActivityHttpClient client, Response response) {
    super();
    this.client = client;
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
      client.normalizeJson(result)
    );
    destroy();
  }

  @Override
  public void onSuccess(
    int statusCode,
    Header[] headers,
    JSONArray result
  ) {
    response.onSuccess(
      client.normalizeJson(result)
    );
    destroy();
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
      client.normalizeJson(result)
    );
    destroy();
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
      client.normalizeJson(result)
    );
    destroy();
  }

  private void destroy() {
    response = null;
    client = null;
  }
}
