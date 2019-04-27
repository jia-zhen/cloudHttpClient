package com.ningzhi.cloudHttpClient.Model;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import org.apache.http.Header;

@Data
public class HttpResult {
  protected int leased, pending, available, max;
  protected Header[] headers;
  protected String protocolVersion;
  protected int statusCode;
  protected String content;
  protected String contentEncoding, contentType;
  protected long contentLength;
  protected boolean chunked, streaming;
  protected JSONObject jsonObject;
}
