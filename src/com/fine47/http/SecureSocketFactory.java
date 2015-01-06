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

/**
 * A concise and robust SSL factory to connect to custom HTTPS backend using a
 * custom certificate using {@link KeyStore}s and aliases. In order to make
 * this an easy process, only a key store and an alias it required.
 *
 * Before utilizing certificates, however, one must register a key store and
 * alias using {@link #register(String, KeyStore, String)} function. This needs
 * to be done only once.
 *
 * Afterwards, one needs to use {@link #getInstance(String)} to get a factory
 * for the factory identifier.
 */
public class SecureSocketFactory extends SSLSocketFactory {

  private final static ConcurrentHashMap<String, SecureSocketFactory>
    instances = new ConcurrentHashMap();

  private final SSLContext sslCtx;
  private final X509Certificate[] acceptedIssuers;
  private final PublicKey publicKey;

  private SecureSocketFactory(
    String factoryId,
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
        "Found expired SSL certificate in this store: " + factoryId);
    }

    // Check the CA's validity.
    x509ca.checkValidity();

    // Accepted CA is only the one installed in the store.
    acceptedIssuers = new X509Certificate[] {x509ca};

    // Get the public key.
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
            if(ActivityHttpClient.isDebugging()) {
              Log.d(
                ActivityHttpClient.LOG_TAG,
                "Server Certificate Details:");
              Log.d(
                ActivityHttpClient.LOG_TAG,
                "---------------------------");
              Log.d(
                ActivityHttpClient.LOG_TAG,
                "IssuerDN: " + cert.getIssuerDN().toString());
              Log.d(
                ActivityHttpClient.LOG_TAG,
                "SubjectDN: " + cert.getSubjectDN().toString());
              Log.d(
                ActivityHttpClient.LOG_TAG,
                "Serial Number: " + cert.getSerialNumber());
              Log.d(
                ActivityHttpClient.LOG_TAG,
                "Version: " + cert.getVersion());
              Log.d(
                ActivityHttpClient.LOG_TAG,
                "Not before: " + cert.getNotBefore().toString());
              Log.d(
                ActivityHttpClient.LOG_TAG,
                "Not after: " + cert.getNotAfter().toString());
              Log.d(
                ActivityHttpClient.LOG_TAG,
                "---------------------------");
            }

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
        if(null != error && ActivityHttpClient.isDebugging()) {
          Log.e(
            ActivityHttpClient.LOG_TAG,
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

  /**
   * Registers a key store and an alias to be collectively identified by the
   * specified factory identifier.
   *
   * @param factoryId unique identifier for specified key store and alias
   * @param store key store containing the certificate
   * @param alias pointing to the certificate
   * @return newly-created SSL factory instance
   * @throws CertificateException on generic certificate exceptions
   * @throws NoSuchAlgorithmException when requested algorithm is not found
   * @throws KeyManagementException for an operation concerning key management
   * @throws KeyStoreException on generic key store exceptions
   * @throws UnrecoverableKeyException when a key cannot be recovered
   */
  public static SecureSocketFactory register(
    String factoryId,
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
      SecureSocketFactory instance = instances.get(factoryId);
      if(null != instance) {
        throw new IllegalArgumentException(
          "This store has been registered already: " + factoryId);
      }
      instance = new SecureSocketFactory(factoryId, store, alias);
      instances.put(factoryId, instance);
      return instance;
    }
  }

  /**
   * Returns a previously-registered SSL factory instance identified by the
   * specified factory identifier. Note that a previous call must have been
   * made to {@link #register(String, KeyStore, String)} prior to one's ability
   * to call this method.
   *
   * @param factoryId unique identifier of request SSL factory instance
   * @return previously-created SSL factory instance
   * @throws CertificateException on generic certificate exceptions
   * @throws NoSuchAlgorithmException when requested algorithm is not found
   * @throws KeyManagementException for an operation concerning key management
   * @throws KeyStoreException on generic key store exceptions
   * @throws UnrecoverableKeyException when a key cannot be recovered
   */
  public static SecureSocketFactory getInstance(String factoryId) throws
    CertificateException,
    NoSuchAlgorithmException,
    KeyManagementException,
    KeyStoreException,
    UnrecoverableKeyException
  {
    synchronized(instances) {
      SecureSocketFactory instance = instances.get(factoryId);
      if(null == instance) {
        throw new IllegalArgumentException(
          "This store has not been registered yet: " + factoryId);
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
   * Returns the SSL context associated with this SSL factory.
   *
   * @return SSL context
   */
  public SSLContext getSslContext() {
    return sslCtx;
  }

  /**
   * Returns the accepted issuers contained within the attached key store. The
   * issuers are identified by their X.509 certificates.
   *
   * @return list of accepted issuers
   */
  public X509Certificate[] getAcceptedIssuers() {
    return acceptedIssuers;
  }

  /**
   * Returns the public key embedded in the attached key store.
   *
   * @return public key
   */
  public PublicKey getPublicKey() {
    return publicKey;
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
