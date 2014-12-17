package com.fine47.http;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
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

public class HttpClient extends AsyncHttpClient {

  public final static String CONTENT_TYPE_JSON = "application/json";

  /**
   * Debug tag for this class.
   */
  public final static String LOG_TAG = "HttpClient";

  private static HttpClient instance;
  private static String userAgent = System.getProperty("http.agent");

  private final Application app;
  private final Class<? extends JsonObjectInterface> jsonObjectClass;
  private final Class<? extends JsonArrayInterface> jsonArrayClass;

  private CookieStore store;
  private long lastCleanup;

  private boolean isConnected;
  private boolean isWifiConnected;
  private boolean isMobileConnected;

  private HttpClient(
    Application app,
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

    // Initialize final values.
    this.app = app;
    this.jsonObjectClass = jsonObjectClass;
    this.jsonArrayClass = jsonArrayClass;

    // Initial values for network status.
    isOnline();
  }

  public static void start(
    Application app,
    Class<JsonObjectInterface> jsonObjectClass,
    Class<JsonArrayInterface> jsonArrayClass
  ) {
    assert null == instance;
    assert null != app;
    instance = new HttpClient(app, jsonObjectClass, jsonArrayClass);
  }

  public static void stop() {
    assert null != instance;
    instance.cancelRequests();
    instance = null;
  }

  public static HttpClient getInstance() {
    assert null != instance;
    return instance;
  }

  public void cancelAllRequests() {
    cancelAllRequests(true);
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

  public boolean isOnline() {
    try {
      ConnectivityManager cm = (ConnectivityManager)app.getSystemService(
        Context.CONNECTIVITY_SERVICE);

      NetworkInfo netInfo = cm.getActiveNetworkInfo();
      if(null != netInfo) {
        // Check for availability and if it's really connected.
        isConnected =
          netInfo.isAvailable() &&
          netInfo.isConnectedOrConnecting();

        // Get available networks' info.
        NetworkInfo[] netsInfo = cm.getAllNetworkInfo();

        // What kind of networks are available.
        for(NetworkInfo ni : netsInfo) {
          if(ni.isConnected()) {
            String niType = ni.getTypeName();
            if("WIFI".equalsIgnoreCase(niType)) {
              isWifiConnected = true;
            } else if("MOBILE".equalsIgnoreCase(niType)) {
              isMobileConnected = true;
            }
          }
        }
      } else {
        isConnected = false;
      }

      return isConnected;
    } catch(Throwable error) {
      Log.e(LOG_TAG, "Error while detecting network status.", error);
    }

    return isConnected;
  }

  public boolean isWifi() {
    return isWifiConnected;
  }

  public boolean isMobile() {
    return isMobileConnected;
  }

  @SuppressWarnings("deprecation")
  public boolean isAirplaneMode() {
    String airplaneMode = 17 <= android.os.Build.VERSION.SDK_INT
        ? Settings.Global.AIRPLANE_MODE_ON
        : Settings.System.AIRPLANE_MODE_ON;
    return 0 != Settings.System.getInt(app.getContentResolver(), airplaneMode, 0);
  }

  public void cancelRequests() {
    cancelRequests(true);
  }

  public void cancelRequests(boolean mayInterruptIfRunning) {
    cancelRequests(app, mayInterruptIfRunning);
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
    post(app,
      request.url,
      request.getHeaders(),
      request,
      request.contentType,
      new ResponseHandler(response)
    );
  }

  public void dispatch(BinaryRequest request, BinaryResponse response) {
    Log.d(LOG_TAG, "Dispatching: GET " + request.url);
    get(app,
      request.url,
      request.getHeaders(),
      request,
      new BinaryResponseHandler(request.url, response)
    );
  }
}
