package com.fine47.http;

import android.content.Context;
import android.util.Log;
import com.fine47.json.JsonArrayInterface;
import com.fine47.json.JsonObjectInterface;
import com.loopj.android.http.AsyncHttpClient;
import java.net.SocketTimeoutException;
import java.util.Date;
import javax.net.ssl.SSLException;
import org.apache.http.client.CookieStore;
import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONArray;
import org.json.JSONObject;

public class ActivityHttpClient extends AsyncHttpClient {

  public final static String CONTENT_TYPE_JSON = "application/json";

  /**
   * Debug tag for this class.
   */
  public final static String LOG_TAG = "AndroidHttpClient";

  private static String userAgent = System.getProperty("http.agent");

  private final Class<? extends JsonObjectInterface> jsonObjectClass;
  private final Class<? extends JsonArrayInterface> jsonArrayClass;

  private Context ctx;
  private CookieStore store;
  private long lastCleanup;

  public ActivityHttpClient(
    Context ctx,
    Class<? extends JsonObjectInterface> jsonObjectClass,
    Class<? extends JsonArrayInterface> jsonArrayClass
  ) {
    super();

    // Set up sane retry classes for a mobile device.
    allowRetryExceptionClass(SSLException.class);
    allowRetryExceptionClass(SocketTimeoutException.class);
    allowRetryExceptionClass(ConnectTimeoutException.class);

    // Set up the same user agent string as Android's.
    setUserAgent(getDefaultUserAgent());

    // Set up sane values for a mobile device.
    setConnectTimeout(10000);
    setResponseTimeout(30000);
    setMaxRetriesAndTimeout(DEFAULT_MAX_RETRIES, 250);

    // Initialize initial values.
    this.ctx = ctx;
    this.jsonObjectClass = jsonObjectClass;
    this.jsonArrayClass = jsonArrayClass;
  }

  public Context getContext() {
    return ctx;
  }

  public void shutdown() {
    cancelRequests();
    ctx = null;
  }

  /**
   * Returns the default User-Agent string to be used with every HTTP request.
   *
   * @return default user agent
   */
  public String getDefaultUserAgent() {
    if(null == userAgent) {
      // Taken from Android's source code: http://bit.ly/1zkQKHE
      StringBuilder result = new StringBuilder(64);

      result.append("Dalvik/");
      result.append(System.getProperty("java.vm.version")); // such as 1.1.0
      result.append(" (Linux; U; Android ");

      String version = android.os.Build.VERSION.RELEASE; // "1.0" or "3.4b5"
      result.append(version.length() > 0 ? version : "1.0");

      // add the model for the release build
      if("REL".equals(android.os.Build.VERSION.CODENAME)) {
        String model = android.os.Build.MODEL;
        if(model.length() > 0) {
          result.append("; ");
          result.append(model);
        }
      }
      String id = android.os.Build.ID; // "MASTER" or "M4-rc20"
      if(id.length() > 0) {
        result.append(" Build/");
        result.append(id);
      }
      result.append(")");

      userAgent = result.toString();
    }
    return userAgent;
  }

  public void cancelRequests() {
    cancelRequests(true);
  }

  public void cancelRequests(boolean mayInterruptIfRunning) {
    cancelRequests(ctx, mayInterruptIfRunning);
  }

  public JsonObjectInterface normalizeJson(JSONObject data) {
    try {
      // Create a new JSON object.
      JsonObjectInterface json = jsonObjectClass.newInstance();

      // If there's an initial data, embed it in.
      if(null != data) {
        json.merge(data);
      }

      return json;
    } catch(InstantiationException error) {
      Log.e(
        LOG_TAG,
        "Unable to instantiate a new JSON object.",
        error);
      return null;
    } catch(IllegalAccessException error) {
      Log.e(
        LOG_TAG,
        "Illegal access while instantiating a new JSON object.",
        error);
      return null;
    }
  }

  public JsonArrayInterface normalizeJson(JSONArray data) {
    try {
      // Create a new JSON object.
      JsonArrayInterface json = jsonArrayClass.newInstance();

      // If there's an initial data, embed it in.
      if(null != data) {
        json.merge(data);
      }

      return json;
    } catch(InstantiationException error) {
      Log.e(
        LOG_TAG,
        "Unable to instantiate a new JSON object.",
        error);
      return null;
    } catch(IllegalAccessException error) {
      Log.e(
        LOG_TAG,
        "Illegal access while instantiating a new JSON object.",
        error);
      return null;
    }
  }

  /**
   * Returns the currently active cookie store.
   *
   * @return
   */
  public CookieStore getCookieStore() {
    long now = System.currentTimeMillis();

    // Clean the cookie store every 5 minutes.
    if(now > lastCleanup * 300000) {
      store.clearExpired(new Date(now));
      lastCleanup = now;
    }

    return store;
  }

  @Override
  public void setCookieStore(CookieStore store) {
    super.setCookieStore(store);
    this.store = store;
  }

  public void dispatch(Request request, Response response) {
    Log.d(LOG_TAG, "Dispatching: POST " + request.url);
    post(ctx,
      request.url,
      request.getHeaders(),
      request,
      request.contentType,
      new ResponseHandler(this, response)
    );
  }

  public void dispatch(BinaryRequest request, BinaryResponse response) {
    Log.d(LOG_TAG, "Dispatching: GET " + request.url);
    get(ctx,
      request.url,
      request.getHeaders(),
      request,
      new BinaryResponseHandler(request.url, response)
    );
  }
}
