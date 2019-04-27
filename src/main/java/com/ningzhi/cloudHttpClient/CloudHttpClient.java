package com.ningzhi.cloudHttpClient;

import com.ningzhi.cloudHttpClient.Model.HttpResult;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.pool.PoolStats;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.apache.http.util.CharArrayBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

/** @author: aghost(ggg17226 @ gmail.com) @Date: Created in 2018/7/9 17:58 */
public class CloudHttpClient {
  private static CloudHttpClient ourInstance = new CloudHttpClient();

  public static CloudHttpClient getInstance() {
    return ourInstance;
  }

  /** 默认keepalive 超时时间 */
  private long keepAliveTime = 64;
  /** 默认请求超时时间 */
  private int defaultTimeout = 5;

  private String defaultContentType = "application/json; charset=utf-8";

  private PoolingHttpClientConnectionManager cm;
  private ConnectionKeepAliveStrategy keepAliveStrategy;

  private CloudHttpClient() {
    // 初始化keepalive代理
    keepAliveStrategy =
        (HttpResponse response, HttpContext context) -> {
          HeaderElementIterator it =
              new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));

          while (it.hasNext()) {
            HeaderElement he = it.nextElement();
            String param = he.getName();
            String value = he.getValue();
            if (value != null && param.equalsIgnoreCase("timeout")) {
              try {
                return Long.parseLong(value) * 1000;
              } catch (NumberFormatException ignore) {
              }
            }
          }
          return keepAliveTime * 1000;
        };
    cm = new PoolingHttpClientConnectionManager();
    cm.setMaxTotal(500);
    cm.setDefaultMaxPerRoute(500);
  }

  private HttpClientBuilder getClientBuilder() {
    // 绑定到连接获取器
    HttpClientBuilder custom = HttpClients.custom();
    custom.setSSLHostnameVerifier(new NoopHostnameVerifier());
    custom.setKeepAliveStrategy(keepAliveStrategy);
    custom.setConnectionManager(cm);
    return custom;
  }

  /**
   * 获取连接
   *
   * @param timeout 超时时间，单位：秒
   * @return
   */
  private CloseableHttpClient getConnection(int timeout) {
    HttpClientBuilder custom = getClientBuilder();
    // 设置超时
    custom.setDefaultRequestConfig(
        RequestConfig.custom()
            .setConnectTimeout(timeout * 1000)
            .setConnectionRequestTimeout(timeout * 1000)
            .setSocketTimeout(timeout * 1000)
            .build());
    return custom.build();
  }

  private String getEntityContent(HttpEntity entity) throws IOException {
    //    Args.notNull(entity, "Entity");
    //    return toString(entity, ContentType.get(entity));
    return getEntityContent(entity, "utf-8");
  }

  private String getEntityContent(HttpEntity entity, String charset) throws IOException {
    Args.notNull(entity, "Entity");
    return toString(entity, ContentType.get(entity), Charset.forName(charset));
  }

  private String toString(HttpEntity entity, ContentType contentType, Charset charset)
      throws IOException {
    InputStream instream = entity.getContent();
    if (instream == null) {
      return null;
    } else {
      try {
        Args.check(
            entity.getContentLength() <= 2147483647L,
            "HTTP entity too large to be buffered in memory");
        int capacity = (int) entity.getContentLength();
        if (capacity < 0) {
          capacity = 4096;
        }
        if (charset == null) {
          charset = Charset.forName("utf-8");
        }

        Reader reader = new InputStreamReader(instream, charset);
        CharArrayBuffer buffer = new CharArrayBuffer(capacity);
        char[] tmp = new char[1024];

        int l;
        while ((l = reader.read(tmp)) != -1) {
          buffer.append(tmp, 0, l);
        }

        String var9 = buffer.toString();
        return var9;
      } finally {
        instream.close();
      }
    }
  }

  /**
   * 执行请求
   *
   * @param client 连接类
   * @param httpRequestBase http请求类
   * @return 请求结果
   * @throws IOException io错误
   */
  private HttpResult execute(CloseableHttpClient client, HttpRequestBase httpRequestBase)
      throws IOException {
    CloseableHttpResponse response = client.execute(httpRequestBase);

    HttpResult httpResult = new HttpResult();
    PoolStats totalStats = cm.getTotalStats();
    // 线程池数据
    httpResult.setLeased(totalStats.getLeased());
    httpResult.setPending(totalStats.getPending());
    httpResult.setAvailable(totalStats.getAvailable());
    httpResult.setMax(totalStats.getMax());

    // http response 头
    httpResult.setHeaders(response.getAllHeaders());
    // http版本号
    httpResult.setProtocolVersion(response.getStatusLine().getProtocolVersion().toString());
    // http状态码
    httpResult.setStatusCode(response.getStatusLine().getStatusCode());
    // http 内容编码
    httpResult.setContentEncoding(
        response.getEntity().getContentEncoding() != null
            ? response.getEntity().getContentEncoding().getValue()
            : "");
    // http 内容类型
    httpResult.setContentType(
        response.getEntity().getContentType() != null
            ? response.getEntity().getContentType().getValue()
            : "");
    // http 内容长度
    httpResult.setContentLength(response.getEntity().getContentLength());
    // is chunked
    httpResult.setChunked(response.getEntity().isChunked());
    // is streaming
    httpResult.setStreaming(response.getEntity().isStreaming());
    // 获取内容
    httpResult.setContent(getEntityContent(response.getEntity()));

    response.close();
    return httpResult;
  }

  /**
   * get请求
   *
   * @param url 请求地址
   * @return 请求结果
   * @throws IOException io错误
   */
  public HttpResult get(String url) throws IOException {
    CloseableHttpClient client = getConnection(defaultTimeout);
    HttpGet httpGet = new HttpGet(url);
    return execute(client, httpGet);
  }

  /**
   * get请求
   *
   * @param url 请求地址
   * @param timeout 超时时间，单位：秒
   * @return 请求结果
   * @throws IOException io错误
   */
  public HttpResult get(String url, int timeout) throws IOException {
    CloseableHttpClient client = getConnection(timeout);
    HttpGet httpGet = new HttpGet(url);
    return execute(client, httpGet);
  }

  /**
   * post请求
   *
   * @param url 请求地址
   * @param data 请求内容
   * @param contentType 内容类型
   * @param timeout 超时时间，单位：秒
   * @return 请求结果
   * @throws IOException io错误
   */
  public HttpResult post(String url, String data, String contentType, int timeout)
      throws IOException {
    CloseableHttpClient client = getConnection(timeout);
    HttpPost httpPost = new HttpPost(url);
    StringEntity stringEntity = new StringEntity(data, "UTF-8");
    stringEntity.setContentType(contentType);
    httpPost.setEntity(stringEntity);
    return execute(client, httpPost);
  }

  /**
   * post请求
   *
   * @param url 请求地址
   * @param data 请求内容
   * @param contentType 内容类型
   * @return 请求结果
   * @throws IOException io错误
   */
  public HttpResult post(String url, String data, String contentType) throws IOException {
    return post(url, data, contentType, defaultTimeout);
  }

  /**
   * post请求
   *
   * @param url 请求地址
   * @param data 请求内容
   * @param timeout 超时时间，单位：秒
   * @return 请求结果
   * @throws IOException io错误
   */
  public HttpResult post(String url, String data, int timeout) throws IOException {
    return post(url, data, defaultContentType, timeout);
  }

  /**
   * post请求
   *
   * @param url 请求地址
   * @param data 请求内容
   * @return 请求结果
   * @throws IOException io错误
   */
  public HttpResult post(String url, String data) throws IOException {
    return post(url, data, defaultContentType, defaultTimeout);
  }
}
