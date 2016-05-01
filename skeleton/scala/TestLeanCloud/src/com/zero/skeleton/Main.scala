package com.zero.skeleton

import java.util
import java.util.Collections

import com.alibaba.fastjson.JSON
import com.zero.skeleton.leancloud.model.Conversation
import okhttp3.{MediaType, OkHttpClient, Request, RequestBody}

/**
  * Created by zero on 4/27/16.
  */
object Main {

  def main(args: Array[String]) {
    println("Just a test.")

    val data = new Conversation()
    data.name = "Test Chat"
    data.m = new util.ArrayList[String]()
    data.m.add("zaopuppy")
    data.m.add("hiroober")
    createConversation(JSON.toJSONString(data, true))

    println("done")
  }



  // {"objectId":"5722267b39b057006acfa056","createdAt":"2016-04-28T15:04:27.718Z"}
  // {"objectId":"5722268f5bbb50006271512a","createdAt":"2016-04-28T15:04:47.207Z"}
  def createConversation(data: String): Unit = {
    val client = new OkHttpClient()
    val body = RequestBody.create(MEDIA_TYPE_JSON, data)
    val request = new Request.Builder()
      .url("https://api.leancloud.cn/1.1/classes/_Conversation")
      .header("X-LC-Id", "csUaPwlbgCbf1ykgNW7m4DDo")
      .header("X-LC-Key", "XtPGKidFtu9IYxUNYcqqK19f")
      .post(body)
      .build()
    val response = client.newCall(request).execute()
    println(response.body().string())
  }
}
