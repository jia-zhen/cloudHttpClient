package com.ningzhi.cloudHttpClient;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ningzhi.cloudHttpClient.Model.HttpResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;

/** @author: aghost(ggg17226 @ gmail.com) @Date: Created in 2018/7/11 14:44 */
public class SpringLoadBlancerClient {
  public static final String JSON_MIME = "application/json";
  private static final int DEFAULT_RETRY_TIMES = 3;

  private static JSONObject checkHttpResult(HttpResult httpResult) {
    return (null != httpResult
            && httpResult.getStatusCode() == 200
            && !StringUtils.isEmpty(httpResult.getContentType())
            && httpResult.getContentType().trim().toLowerCase().startsWith(JSON_MIME))
        ? JSON.parseObject(httpResult.getContent())
        : null;
  }

  /**
   * get请求访问服务
   *
   * @param loadBalancer 负载均衡器实例
   * @param serviceName 服务注册名
   * @param path uri地址
   * @param retries 重试次数
   * @param timeout 超时时间，单位：秒
   * @return 请求结果，null为失败
   */
  public static JSONObject get(
      LoadBalancerClient loadBalancer, String serviceName, String path, int retries, int timeout) {
    JSONObject result = null;
    if (retries < 1) {
      retries = 1;
    }
    int count = 0;
    while (count < retries && result == null) {
      try {
        result =
            loadBalancer.execute(
                serviceName,
                instance -> {
                  String url = instance.getUri().toString();
                  if (StringUtils.isEmpty(url)) {
                    return null;
                  }
                  url += path;
                  HttpResult httpResult = CloudHttpClient.getInstance().get(url, timeout);
                  return checkHttpResult(httpResult);
                });
      } catch (Exception e) {
        result = null;
      }
      count++;
    }
    return result;
  }

  /**
   * get请求访问服务
   *
   * @param loadBalancer 负载均衡器实例
   * @param serviceName 服务注册名
   * @param path uri地址
   * @param retries 重试次数
   * @return 请求结果，null为失败
   */
  public static JSONObject get(
      LoadBalancerClient loadBalancer, String serviceName, String path, int retries) {
    JSONObject result = null;
    if (retries < 1) {
      retries = 1;
    }
    int count = 0;
    while (count < retries && result == null) {
      try {
        result =
            loadBalancer.execute(
                serviceName,
                instance -> {
                  String url = instance.getUri().toString();
                  if (StringUtils.isEmpty(url)) {
                    return null;
                  }
                  url += path;
                  HttpResult httpResult = CloudHttpClient.getInstance().get(url);
                  return checkHttpResult(httpResult);
                });
      } catch (Exception e) {
        result = null;
      }
      count++;
    }
    return result;
  }

  /**
   * get请求访问服务
   *
   * @param loadBalancer 负载均衡器实例
   * @param serviceName 服务注册名
   * @param path uri地址
   * @return 请求结果，null为失败
   */
  public static JSONObject get(LoadBalancerClient loadBalancer, String serviceName, String path) {
    return get(loadBalancer, serviceName, path, DEFAULT_RETRY_TIMES);
  }

  /**
   * post请求访问服务
   *
   * @param loadBalancer 负载均衡器实例
   * @param serviceName 服务注册名
   * @param path uri地址
   * @param data 请求数据
   * @param retries 重试次数
   * @param timeout 超时时间，单位：秒
   * @return 请求结果，null为失败
   */
  public static JSONObject post(
      LoadBalancerClient loadBalancer,
      String serviceName,
      String path,
      JSONObject data,
      int retries,
      int timeout) {
    JSONObject result = null;
    if (retries < 1) {
      retries = 1;
    }
    if (data == null || data.size() == 0) {
      return null;
    }
    int count = 0;
    while (count < retries && result == null) {
      try {
        result =
            loadBalancer.execute(
                serviceName,
                instance -> {
                  String url = instance.getUri().toString();
                  if (StringUtils.isEmpty(url)) {
                    return null;
                  }
                  url += path;
                  HttpResult httpResult =
                      CloudHttpClient.getInstance().post(url, JSON.toJSONString(data), timeout);
                  return checkHttpResult(httpResult);
                });
      } catch (Exception e) {
        result = null;
      }
      count++;
    }
    return result;
  }

  /**
   * post请求访问服务
   *
   * @param loadBalancer 负载均衡器实例
   * @param serviceName 服务注册名
   * @param path uri地址
   * @param data 请求数据
   * @param retries 重试次数
   * @return 请求结果，null为失败
   */
  public static JSONObject post(
      LoadBalancerClient loadBalancer,
      String serviceName,
      String path,
      JSONObject data,
      int retries) {
    JSONObject result = null;
    if (retries < 1) {
      retries = 1;
    }
    if (data == null || data.size() == 0) {
      return null;
    }
    int count = 0;
    while (count < retries && result == null) {
      try {
        result =
            loadBalancer.execute(
                serviceName,
                instance -> {
                  String url = instance.getUri().toString();
                  if (StringUtils.isEmpty(url)) {
                    return null;
                  }
                  url += path;
                  HttpResult httpResult =
                      CloudHttpClient.getInstance().post(url, JSON.toJSONString(data));
                  return checkHttpResult(httpResult);
                });
      } catch (Exception e) {
        result = null;
      }
      count++;
    }
    return result;
  }

  /**
   * post请求访问服务
   *
   * @param loadBalancer 负载均衡器实例
   * @param serviceName 服务注册名
   * @param path uri地址
   * @param data 请求数据
   * @return 请求结果，null为失败
   */
  public static JSONObject post(
      LoadBalancerClient loadBalancer, String serviceName, String path, JSONObject data) {
    return post(loadBalancer, serviceName, path, data, DEFAULT_RETRY_TIMES);
  }
}
