package com.fine47.http;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.util.Log;
import static com.fine47.http.ActivityHttpClient.LOG_TAG;
import com.fine47.json.JsonArrayInterface;
import com.fine47.json.JsonObjectInterface;

public class AppHttpClient extends ActivityHttpClient {

  private static AppHttpClient instance;

  private boolean isConnected;
  private boolean isWifiConnected;
  private boolean isMobileConnected;

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

    // Initial network state; run it on a separate thread.
    getThreadPool().execute(new Runnable() {

      @Override
      public void run() {
        isOnline();
      }
    });
  }

  public static AppHttpClient getInstance() {
    return instance;
  }

  @Override
  public void shutdown() {
    super.shutdown();
    instance = null;
  }

  public boolean isOnline() {
    try {
      ConnectivityManager cm = (ConnectivityManager)getContext()
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
    return 0 != Settings.System.getInt(
      getContext().getContentResolver(),
      airplaneMode,
      0);
  }
}
