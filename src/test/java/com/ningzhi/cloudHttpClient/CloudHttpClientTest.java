package com.ningzhi.cloudHttpClient;

import com.ningzhi.cloudHttpClient.Model.HttpResult;
import org.junit.Test;

import java.io.IOException;

/** @Author: aghost(ggg17226 @ gmail.com) @Date: Created in 2018/11/26 11:43 */
public class CloudHttpClientTest {

  @Test
  public void test() throws IOException {
    HttpResult httpResult = CloudHttpClient.getInstance().get("https://wap.cmpassport.com:8443/api/tokenValidate/");
    System.out.println(httpResult);
  }
}
