package com.arcusys.valamis.slide.service.contentProvider.util

import java.io.{BufferedReader, InputStreamReader}

import com.arcusys.learn.liferay.LogFactoryHelper
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.{HttpGet, HttpPost}
import org.apache.http.impl.client.HttpClients

object EduAppsHelper {

  private val eduAppsUrl = "https://www.edu-apps.org/"
  private lazy val logger = LogFactoryHelper.getLog(getClass)

  private lazy val minute = 60000
  private lazy val requestConfig = RequestConfig.custom()
    .setConnectTimeout(minute)
    .setConnectionRequestTimeout(minute)
    .setSocketTimeout(minute)
    .build()
  private lazy val httpClientBuilder = HttpClients.custom().
    setDefaultRequestConfig(requestConfig)

  def getApps(offset: Int): String = {
    val url = eduAppsUrl + s"/api/v1/apps?offset=${offset}"
    getData(url)
  }


  private def getData(url: String): String = {
    val httpClient = httpClientBuilder.build()
    try {
      val request = new HttpGet(url)
      val resp = httpClient.execute(request)
      val rd = new BufferedReader(new InputStreamReader(resp.getEntity.getContent))

      rd.readLine().replace("\uFEFF", "")
    } finally {
      httpClient.close
    }
  }
}
