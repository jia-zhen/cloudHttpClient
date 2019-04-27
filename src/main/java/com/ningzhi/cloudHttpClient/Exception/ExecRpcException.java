package com.ningzhi.cloudHttpClient.Exception;

import com.ningzhi.cloudHttpClient.Model.HttpResult;

public class ExecRpcException extends Exception {
  HttpResult result;

  public ExecRpcException() {}

  public ExecRpcException(String message) {
    super(message);
  }

  public ExecRpcException(String message, HttpResult result) {
    super(message);
    this.result = result;
  }
}
