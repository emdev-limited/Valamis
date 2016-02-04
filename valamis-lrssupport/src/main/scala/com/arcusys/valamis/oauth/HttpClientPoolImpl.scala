package com.arcusys.valamis.oauth

import java.io.Closeable
import java.net.URL

import net.oauth.client.httpclient4.HttpClientPool
import org.apache.http.client.HttpClient
import org.apache.http.impl.client.HttpClientBuilder

/**
 * Created by mminin on 09.07.15.
 */
class HttpClientPoolImpl extends HttpClientPool with Closeable {
  val client = HttpClientBuilder.create().build()

  override def getHttpClient(url: URL): HttpClient = {
    client
  }

  override def close(): Unit = {
    client.close()
  }
}
