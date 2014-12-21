package com.fine47.http;

import android.app.Application;
import com.fine47.json.JsonArrayInterface;
import com.fine47.json.JsonObjectInterface;

public class AppHttpClient extends ActivityHttpClient {

  private static AppHttpClient instance;

  /**
   * Create an app-wide, global HTTP client and attach it to the specified
   * application instance. Note that only one app-wide instance can be created,
   * and an exception will be thrown if this class is instantiated more than
   * once.
   *
   * @param app application to attach the HTTP client to
   * @param jsonObjectClass JSON object class handler
   * @param jsonArrayClass JSON array class handler
   */
  public AppHttpClient(
    Application app,
    Class<? extends JsonObjectInterface> jsonObjectClass,
    Class<? extends JsonArrayInterface> jsonArrayClass
  ) {
    super(app, jsonObjectClass, jsonArrayClass);

    // Keep the reference to this instance.
    if(null != instance) {
      throw new IllegalStateException(
        "Application-wide HTTP client has already been created.");
    }
    instance = AppHttpClient.this;
  }

  /**
   * Returns the app-wide, previously created HTTP client instance.
   *
   * @return app-wide HTTP client instance
   */
  public static AppHttpClient getInstance() {
    return instance;
  }

  @Override
  public void shutdown() {
    super.shutdown();
    instance = null;
  }
}
