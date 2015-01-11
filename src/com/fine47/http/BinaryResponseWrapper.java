package com.fine47.http;

import com.fine47.http.request.AbstractRequest;
import com.fine47.http.response.BinaryResponse;

class BinaryResponseWrapper<M> extends AbstractResponseWrapper<byte[], M> {
  
  public BinaryResponseWrapper(
    AbstractRequest<M> request, 
    BinaryResponse<M> response
  ) {
    super(new String[] {"^.+/.+$"}, request, response);
  }

  @Override
  byte[] bytesToValue(byte[] bytes) {
    return bytes;
  }
}
