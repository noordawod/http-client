package com.fine47.http;

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

public class ActivityHttpClient extends AsyncHttpClient {

  public final static String CONTENT_TYPE_JSON = "application/json";

  /**
   * Debug tag for this class.
   */
  public final static String LOG_TAG = "ActivityHttpClient";

  private static String userAgent = System.getProperty("http.agent");

  protected final Class<? extends JsonObjectInterface> jsonObjectClass;
  protected final Class<? extends JsonArrayInterface> jsonArrayClass;

  private static boolean isDebugging;

  private Context ctx;
  private CookieStore store;
  private long lastCleanup;

  private boolean isConnected;
  private boolean isWifiConnected;
  private boolean isMobileConnected;

  /**
   * Returns whether debugging mode is turned on.
   *
   * @return TRUE if debugging is on, FALSE otherwise
   */
  public static boolean isDebugging() {
    return isDebugging;
  }

  /**
   * Sets debugging mode.
   *
   * @param value TRUE to turn debugging mode on
   */
  public static void setIsDebugging(boolean value) {
    isDebugging = value;
  }

  /**
   * Create a new HTTP client and attach it to the specified context.
   *
   * @param ctx context to attach the HTTP client to
   * @param jsonObjectClass JSON object class handler
   * @param jsonArrayClass JSON array class handler
   */
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

    // Initial network state; run it on a separate thread.
    getThreadPool().execute(new Runnable() {

      @Override
      public void run() {
        isOnline();
      }
    });

    if(isDebugging()) {
      Log.d(LOG_TAG, "Created new " + getClass().getName() + " instance.");
    }
  }

  /**
   * Returns the current context attached to this HTTP client.
   *
   * @return current context
   */
  public Context getContext() {
    return ctx;
  }

  /**
   * Shuts down the HTTP client and cancels all pending requests.
   */
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

  /**
   * Cancels all pending requests and cancel any running ones, too.
   */
  public void cancelRequests() {
    cancelRequests(true);
  }

  /**
   * Cancels all pending requests and optionally cancel any running ones, too.
   *
   * @param mayInterruptIfRunning TRUE to cancel running requests
   */
  public void cancelRequests(boolean mayInterruptIfRunning) {
    cancelRequests(ctx, mayInterruptIfRunning);
  }

  /**
   * Converts a native Android JSON object into a JSON object of the defined
   * type for this HTTP client.
   *
   * @param data JSON object data to convert
   * @return converted JSON object
   */
  public JsonObjectInterface normalizeJson(JSONObject data) {
    Exception error;
    try {
      // Create a new JSON object.
      JsonObjectInterface json = jsonObjectClass.newInstance();

      // If there's an initial data, embed it in.
      if(null != data) {
        json.merge(data);
      }

      return json;
    } catch(InstantiationException e) {
      error = e;
    } catch(IllegalAccessException e) {
      error = e;
    }
    Log.e(LOG_TAG, "Unable to instantiate a new JSON object.", error);
    return null;
  }

  /**
   * Converts a native Android JSON array into a JSON array of the defined
   * type for this HTTP client.
   *
   * @param data JSON array data to convert
   * @return converted JSON array
   */
  public JsonArrayInterface normalizeJson(JSONArray data) {
    Exception error;
    try {
      // Create a new JSON object.
      JsonArrayInterface json = jsonArrayClass.newInstance();

      // If there's an initial data, embed it in.
      if(null != data) {
        json.merge(data);
      }

      return json;
    } catch(InstantiationException e) {
      error = e;
    } catch(IllegalAccessException e) {
      error = e;
    }
    Log.e(LOG_TAG, "Unable to instantiate a new JSON array.", error);
    return null;
  }

  /**
   * Returns the currently active cookie store. Before returning it, though, a
   * cleanup is made to the cookie store to remove expired cookies (once every
   * 5 minutes).
   *
   * @return active cookie store
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

  /**
   * Checks whether the system is online (connected to a network) or not.
   *
   * @return TRUE if the system is online, FALSE otherwise
   */
  public boolean isOnline() {
    try {
      ConnectivityManager cm = (ConnectivityManager)getContext()
        .getApplicationContext()
        .getSystemService(
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

  /**
   * Returns whether the system is connected to a WiFi network.
   *
   * @return TRUE if system is connected to a WiFi network, FALSE otherwise
   */
  public boolean isWifi() {
    return isWifiConnected;
  }

  /**
   * Returns whether the system is connected to a mobile network (GPRS, ex.)
   *
   * @return TRUE if system is connected to a mobile network, FALSE otherwise
   */
  public boolean isMobile() {
    return isMobileConnected;
  }

  /**
   * Checks whether the system is in "airplane mode".
   *
   * @return TRUE if system is in airplane mode, FALSE otherwise
   */
  @SuppressWarnings("deprecation")
  public boolean isAirplaneMode() {
    String airplaneMode = 17 <= android.os.Build.VERSION.SDK_INT
        ? Settings.Global.AIRPLANE_MODE_ON
        : Settings.System.AIRPLANE_MODE_ON;
    return 0 != Settings.System.getInt(
      getContext()
        .getApplicationContext()
        .getContentResolver(),
      airplaneMode,
      0);
  }

  /**
   * Dispatches the specified request to the HTTP client and use the specified
   * response object to handle the result or any errors.
   *
   * @param request to dispatch
   * @param response to handle the result
   */
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

  /**
   * Dispatches the specified binary request to the HTTP client and use the
   * specified response object to handle the result or any errors.
   *
   * Binary requests will cause the response to be fired *always* within the
   * pool thread, so if the response needs to update the UI, it must submit a
   * task to the UI thread.
   *
   * @param request to dispatch
   * @param response to handle the result
   */
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
