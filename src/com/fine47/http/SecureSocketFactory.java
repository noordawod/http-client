package com.fine47.http;

import android.util.Log;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.concurrent.ConcurrentHashMap;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.conn.ssl.SSLSocketFactory;

public class SecureSocketFactory extends SSLSocketFactory {

  private final static String LOG_TAG = "SecureSocketFactory";

  private final static ConcurrentHashMap<String, SecureSocketFactory>
    instances = new ConcurrentHashMap();

  private final String storeId;
  private final SSLContext sslCtx;
  private final X509Certificate[] acceptedIssuers;
  private final PublicKey publicKey;

  private SecureSocketFactory(
    String storeId,
    KeyStore store,
    String alias
  ) throws
    CertificateException,
    NoSuchAlgorithmException,
    KeyManagementException,
    KeyStoreException,
    UnrecoverableKeyException
  {
    super(store);

    // Loading the CA certificate from store.
    Certificate rootca = store.getCertificate(alias);

    // Turn it to X509 format.
    InputStream is = new ByteArrayInputStream(rootca.getEncoded());
    X509Certificate x509ca = (X509Certificate)CertificateFactory
      .getInstance("X.509")
      .generateCertificate(is);
    ActivityHttpClient.silentCloseInputStream(is);

    if(null == x509ca) {
      throw new CertificateException(
        "Found expired SSL certificate in this store: " + storeId);
    }

    // Check the CA's validity.
    x509ca.checkValidity();

    this.storeId = storeId;

    // Accepted CA is only the one installed in the store.
    acceptedIssuers = new X509Certificate[] {x509ca};

    // Get public key.
    publicKey = rootca.getPublicKey();

    sslCtx = SSLContext.getInstance("TLS");
    sslCtx.init(null, new TrustManager[] {new X509TrustManager() {

      @Override
      public void checkClientTrusted(
        X509Certificate[] chain,
        String authType
      ) throws CertificateException {
      }

      @Override
      public void checkServerTrusted(
        X509Certificate[] chain,
        String authType
      ) throws CertificateException {
        Exception error = null;

        if(null == chain || 0 == chain.length) {
          error = new CertificateException("Certificate chain is invalid");
        } else if(null == authType || 0 == authType.length()) {
          error = new CertificateException("Authentication type is invalid");
        } else try {
          for(X509Certificate cert : chain) {
/*
            Log.d(LOG_TAG, "Server Certificate Details:");
            Log.d(LOG_TAG, "---------------------------");
            Log.d(LOG_TAG, "IssuerDN: " + cert.getIssuerDN().toString());
            Log.d(LOG_TAG, "SubjectDN: " + cert.getSubjectDN().toString());
            Log.d(LOG_TAG, "Serial Number: " + cert.getSerialNumber());
            Log.d(LOG_TAG, "Version: " + cert.getVersion());
            Log.d(LOG_TAG, "Not before: " + cert.getNotBefore().toGMTString());
            Log.d(LOG_TAG, "Not after: " + cert.getNotAfter().toGMTString());
            Log.d(LOG_TAG, "---------------------------");
*/
            // Make sure that it hasn't expired.
            cert.checkValidity();

            // Verify the certificate's chain.
            cert.verify(publicKey);
          }
        } catch(InvalidKeyException ex) {
          error = ex;
        } catch(NoSuchAlgorithmException ex) {
          error = ex;
        } catch(NoSuchProviderException ex) {
          error = ex;
        } catch(SignatureException ex) {
          error = ex;
        }
        if(null != error) {
          Log.e(
            LOG_TAG,
            "Error while setting up a secure socket factory.",
            error);
          throw new CertificateException(error);
        }
      }

      @Override
      public X509Certificate[] getAcceptedIssuers() {
        return acceptedIssuers;
      }
    }}, null);

    setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
  }

  public static SecureSocketFactory register(
    String storeId,
    KeyStore store,
    String alias
  ) throws
    CertificateException,
    NoSuchAlgorithmException,
    KeyManagementException,
    KeyStoreException,
    UnrecoverableKeyException
  {
    synchronized(instances) {
      SecureSocketFactory instance = instances.get(storeId);
      if(null != instance) {
        throw new IllegalArgumentException(
          "This store has been registered already: " + storeId);
      }
      instance = new SecureSocketFactory(storeId, store, alias);
      instances.put(storeId, instance);
      return instance;
    }
  }

  public static SecureSocketFactory getInstance(
    String storeId,
    KeyStore store,
    String alias
  ) throws
    CertificateException,
    NoSuchAlgorithmException,
    KeyManagementException,
    KeyStoreException,
    UnrecoverableKeyException
  {
    synchronized(instances) {
      SecureSocketFactory instance = instances.get(storeId);
      if(null == instance) {
        throw new IllegalArgumentException(
          "This store has not been registered yet: " + storeId);
      }
      return instance;
    }
  }

  @Override
  public Socket createSocket(
    Socket socket,
    String host,
    int port,
    boolean autoClose
  ) throws IOException, UnknownHostException {
    injectHostname(socket, host);
    return sslCtx
      .getSocketFactory()
      .createSocket(socket, host, port, autoClose);
  }

  @Override
  public Socket createSocket() throws IOException {
    return sslCtx
      .getSocketFactory()
      .createSocket();
  }

  /**
   * Pre-ICS Android had a bug resolving HTTPS addresses. This workaround
   * fixes that bug.
   *
   * @param socket The socket to alter
   * @param host Hostname to connec to
   * @see <a href="https://code.google.com/p/android/issues/detail?id=13117#c14">Details about this workaround</a>
   */
  private void injectHostname(Socket socket, String host) {
    if(14 > android.os.Build.VERSION.SDK_INT) {
      try {
        Field field = InetAddress.class.getDeclaredField("hostName");
        field.setAccessible(true);
        field.set(socket.getInetAddress(), host);
      } catch (Exception ignored) {
      }
    }
  }
}
