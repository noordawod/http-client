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

import com.fine47.http.request.AbstractRequest;
import com.fine47.cache.CacheInterface;
import com.fine47.http.response.AbstractResponse;
import com.fine47.http.response.BinaryResponse;
import java.util.concurrent.ExecutorService;

/**
 * A handy download manager which uses a simple caching interface to store,
 * fetch and delete Internet resources of type {@link E}.
 *
 * @param <E> type of resources which the download manager handles
 * @see <a href="https://github.com/noordawod/android-cache">Android Cache</a>
 */
public class DownloadManager<E> {

  /**
   * The HTTP Client instance associated with this download manager.
   */
  public final ActivityHttpClient client;

  /**
   * The Caching instance associated with this download manager.
   */
  public final CacheInterface<String, E> cache;

  DownloadManager(ActivityHttpClient client, CacheInterface<String, E> cache) {
    this.client = client;
    this.cache = cache;
  }

  /**
   * This will cause the download manager to shutdown and run a garbage-
   * collection on the cache engine. The process, by the way, will be dispatched
   * to the thread pool and should, thus, produce no noticeable slowdown.
   */
  public void shutdown() {
    final ExecutorService threadPool = client.getThreadPool();
    if(null != threadPool) {
      threadPool.execute(new Runnable() {

        @Override
        public void run() {
          cache.runGc();
        }
      });
    }
  }

  /**
   * Dispatch a request to download a resource from the Internet. All downloads
   * use GET method.
   *
   * @param <M> meta-data type which could be accompanying this request
   * @param request generic request to dispatch
   * @param response generic handler to handle the result
   */
  public <M>void dispatch(
    final AbstractRequest<M> request,
    final AbstractResponse<E, M> response
  ) {
    // Try to get a cached entry first for this URL.
    final E cacheEntry = cache.get(request.url);

    // If there's no cached entry...
    if(null == cacheEntry) {
      getImpl(request, response);
    } else {
      // Cached entry is available, let's call the response handler on a
      // thread pool, as is expected.
      final ExecutorService threadPool = client.getThreadPool();

      if(null == threadPool) {
        // No thread pool configured, so call the handler directly.
        response.onSuccess(cacheEntry, request);
      } else {
        // Execute the handler on a separate thread.
        threadPool.execute(new Runnable() {

          @Override
          public void run() {
            response.onSuccess(cacheEntry, request);
          }
        });
      }
    }
  }
  
  protected <M>void getImpl(
    final AbstractRequest<M> request,
    final AbstractResponse<E, M> response
  ) {
    // Dispatch a request to download this URL.
    client.getImpl(
      request,
      new BinaryResponseWrapper(request, new BinaryResponse<M>() {

        @Override
        public boolean isAlive() {
          return response.isAlive();
        }

        @Override
        public void onSuccess(
          byte[] bytes, 
          AbstractRequest<M> req
        ) {
          E cacheEntry = cache.store(request.url, bytes);
          if(null == cacheEntry) {
            // Cache entry not saved, call failure handler.
            onFailure(
              null, 
              null, 
              new RuntimeException("Unable to store bytes into cache.")
            );
          } else {
            // Cache entry has been saved, call success handler.
            response.onSuccess(cacheEntry, request);
          }
        }

        @Override
        public void onFailure(
          byte[] bytes, 
          AbstractRequest<M> req, 
          Throwable error
        ) {
          response.onFailure(null, request, error);
        }
      })
    );
  }
}
