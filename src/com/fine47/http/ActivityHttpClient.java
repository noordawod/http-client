/**
 * This file is part of HTTP Client library.
 * Copyright (C) 2014 Noor Dawod. All rights reserved.
 * https://github.com/noordawod/http-client
 *
 * Released under the MIT license
 * http://en.wikipedia.org/wiki/MIT_License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.fine47.http;

import com.fine47.http.request.JsonRequest;
import com.fine47.http.request.ImageRequest;
import com.fine47.http.request.AbstractRequest;
import com.fine47.http.response.JsonResponse;
import com.fine47.http.response.ImageResponse;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.util.Log;
import com.fine47.cache.CacheInterface;
import com.fine47.http.response.BinaryResponse;
import com.fine47.json.*;
import com.loopj.android.http.*;
import java.net.SocketTimeoutException;
import java.util.*;
import javax.net.ssl.SSLException;
import org.apache.http.HttpEntity;
import org.apache.http.client.CookieStore;
import org.apache.http.conn.ConnectTimeoutException;

/**
 * An {@link android.app.Activity}-based HTTP client. Use this if you'd like to
 * control HTTP requests by activity and have the ability to collectively cancel
 * requests belonging to an activity.
 *
 * Otherwise, it's most wise to use {@link AppHttpClient} which is an App-wide
 * HTTP client and should be a better choice to fights memory leaks.
 *
 * @see AppHttpClient
 */
public class ActivityHttpClient extends AsyncHttpClient {

  /**
   * MIME content type for JSON entities.
   */
  public final static String CONTENT_TYPE_JSON = "application/json";

  /**
   * Debug tag for this class.
   */
  public final static String LOG_TAG = "ActivityHttpClient";

  /**
   * Reusable exception when implementation is inaccessible.
   */
  public final static UnsupportedOperationException
    NO_ACCESS = new UnsupportedOperationException();

  private static String userAgent = System.getProperty("http.agent");

  private final HashMap<CacheInterface, DownloadManager>
    downloadManagers = new HashMap();

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
   */
  public ActivityHttpClient(Context ctx) {
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

    // Keep the context.
    this.ctx = ctx;

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
    synchronized(downloadManagers) {
      final Collection<DownloadManager> managers = downloadManagers.values();
      for(final DownloadManager manager : managers) {
        manager.shutdown();
      }
      downloadManagers.clear();
    }
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
      if(isDebugging()) {
        Log.e(LOG_TAG, "Error while detecting network status.", error);
      }
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
   * Returns a cache-backed download manager for easily working with cacheable
   * Internet resources.
   *
   * @param <E> type of resources which the download manager handles
   * @param cache the cache instance to use with the download manager
   * @return download manager ready for use
   */
  public <E>DownloadManager getDownloadManager(
    CacheInterface<String, E> cache
  ) {
    synchronized(downloadManagers) {
      if(null == cache) {
        throw new IllegalArgumentException(
          "A caching engine must be provided to the download manager.");
      }
      DownloadManager downloadManager = downloadManagers.get(cache);
      if(null == downloadManager) {
        downloadManager = new DownloadManager<E>(this, cache);
        downloadManagers.put(cache, downloadManager);
      }
      return downloadManager;
    }
  }

  /**
   * Dispatches the specified JSON request to the HTTP client and use the
   * specified JSON response instance to handle the result or any errors.
   *
   * @param <T> type of JSON entity which will be received
   * @param <M> meta-data type which could be accompanying this request
   * @param type type of request to dispatch
   * @param request JSON request to dispatch
   * @param response JSON handler to handle the result
   */
  public <T extends JsonInterface, M>void dispatch(
    AbstractRequest.TYPE type,
    JsonRequest<M> request,
    JsonResponse<T, M> response
  ) {
    dispatch(
      type,
      request,
      new JsonResponseWrapper(request, response)
    );
  }

  /**
   * Dispatches the specified image request to the HTTP client and use the
   * specified image response instance to handle the result or any errors.
   *
   * @param <M> meta-data type which could be accompanying this request
   * @param type type of request to dispatch
   * @param request image request to dispatch
   * @param response image handler to handle the result
   */
  public <M>void dispatch(
    AbstractRequest.TYPE type,
    ImageRequest<M> request,
    ImageResponse<M> response
  ) {
    dispatch(
      type,
      request,
      new ImageResponseWrapper(request, response)
    );
  }

  /**
   * Dispatches the specified abstract request to the HTTP client and use the
   * specified binary response instance to handle the result or any errors.
   *
   * @param <M> meta-data type which could be accompanying this request
   * @param type type of request to dispatch
   * @param request abstract request to dispatch
   * @param response binary handler to handle the result
   */
  public <E, M>void dispatch(
    AbstractRequest.TYPE type,
    AbstractRequest<M> request,
    BinaryResponse<M> response
  ) {
    dispatch(
      type,
      request,
      (new BinaryResponseWrapper(request, response))
    );
  }

  /**
   * Dispatches the specified generic request to the HTTP client and use the
   * specified generic response instance to handle the result or any errors.
   *
   * @param <M> meta-data type which could be accompanying this request
   * @param type type of request to dispatch
   * @param request generic request to dispatch
   * @param handler generic handler to handle the result
   */
  <E, M>void dispatch(
    AbstractRequest.TYPE type,
    AbstractRequest<M> request,
    AbstractResponseWrapper<E, M> handler
  ) {
    if(isDebugging()) {
      Log.d(LOG_TAG, "Dispatching: " + request.url);
    }
    switch(type) {
      case HEAD:
        headImpl(request, handler);
        break;

      case GET:
        getImpl(request, handler);
        break;

      case POST:
        postImpl(request, handler);
        break;

      case PUT:
        putImpl(request, handler);
        break;

      case PATCH:
        patchImpl(request, handler);
        break;

      case DELETE:
        deleteImpl(request, handler);
        break;
    }
  }

  /**
   * Dispatches a HEAD request.
   *
   * @param request to dispatch
   * @param handler response to handle the result
   * @see AbstractRequest.TYPE#HEAD
   */
  protected void headImpl(
    AbstractRequest request,
    ResponseHandlerInterface handler
  ) {
    head(
      ctx,
      request.url,
      request.getHeaders(),
      request,
      handler
    );
    if(isDebugging()) {
      Log.d(LOG_TAG, "Dispatching HEAD: " + request.url);
    }
  }

  /**
   * Dispatches a GET request.
   *
   * @param request to dispatch
   * @param handler response to handle the result
   * @see AbstractRequest.TYPE#GET
   */
  protected void getImpl(
    AbstractRequest request,
    ResponseHandlerInterface handler
  ) {
    get(
      ctx,
      request.url,
      request.getHeaders(),
      request,
      handler
    );
    if(isDebugging()) {
      Log.d(LOG_TAG, "Dispatching GET: " + request.url);
    }
  }

  /**
   * Dispatches a POST request.
   *
   * @param request to dispatch
   * @param handler response to handle the result
   * @see AbstractRequest.TYPE#POST
   */
  protected void postImpl(
    AbstractRequest request,
    ResponseHandlerInterface handler
  ) {
    post(
      ctx,
      request.url,
      request.getHeaders(),
      getEntity(request, handler),
      request.contentType,
      handler
    );
    if(isDebugging()) {
      Log.d(LOG_TAG, "Dispatching POST: " + request.url);
    }
  }

  /**
   * Dispatches a PUT request.
   *
   * @param request to dispatch
   * @param handler response to handle the result
   * @see AbstractRequest.TYPE#PUT
   */
  protected void putImpl(
    AbstractRequest request,
    ResponseHandlerInterface handler
  ) {
    put(
      ctx,
      request.url,
      request.getHeaders(),
      getEntity(request, handler),
      request.contentType,
      handler
    );
    if(isDebugging()) {
      Log.d(LOG_TAG, "Dispatching PUT: " + request.url);
    }
  }

  /**
   * Dispatches a PATCH request.
   *
   * @param request to dispatch
   * @param handler response to handle the result
   * @see AbstractRequest.TYPE#PATCH
   */
  protected void patchImpl(
    AbstractRequest request,
    ResponseHandlerInterface handler
  ) {
    patch(
      ctx,
      request.url,
      request.getHeaders(),
      getEntity(request, handler),
      request.contentType,
      handler
    );
    if(isDebugging()) {
      Log.d(LOG_TAG, "Dispatching PATCH: " + request.url);
    }
  }

  /**
   * Dispatches a DELETE request.
   *
   * @param request to dispatch
   * @param handler response to handle the result
   * @see AbstractRequest.TYPE#DELETE
   */
  protected void deleteImpl(
    AbstractRequest request,
    ResponseHandlerInterface handler
  ) {
    delete(
      ctx,
      request.url,
      getEntity(request, handler),
      request.contentType,
      handler
    );
    if(isDebugging()) {
      Log.d(LOG_TAG, "Dispatching DELETE: " + request.url);
    }
  }

  /**
   * Returns an HTTP entity for the specified request and response handler.
   *
   * @param request to dispatch
   * @param handler response to handle the result
   * @return HTTP entity on success, NULL otherwise
   */
  protected HttpEntity getEntity(
    AbstractRequest request,
    ResponseHandlerInterface handler
  ) {
    try {
      return request.getEntity(handler);
    } catch(java.io.IOException error) {
      if(isDebugging()) {
        Log.e(LOG_TAG, "Cannot get HTTP entity for: " + request.url, error);
      }
      return null;
    }
  }
}
