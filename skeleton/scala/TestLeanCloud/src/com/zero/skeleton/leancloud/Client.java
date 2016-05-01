package com.zero.skeleton.leancloud;

import com.alibaba.fastjson.JSON;
import okhttp3.*;

import java.io.IOException;

/**
 * Created by zero on 4/28/16.
 */
public class Client {

    public static void main(String[] args) {
        Client client = new Client("csUaPwlbgCbf1ykgNW7m4DDo", "XtPGKidFtu9IYxUNYcqqK19f");
    }

    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    private static final String BASE_URL = "https://api.leancloud.cn/1.1";

    private final String id_;

    private final String key_;

    public Client(String id, String key) {
        id_ = id;
        key_ = key;
    }

    protected String get(String path) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
            .url(BASE_URL + path)
            .header("X-LC-Id", id_)
            .header("X-LC-Key", key_)
            .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.code() != 200) {
                return null;
            }
            // FIXME: encoding
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected String post(String path, String data) {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, data);
        Request request = new Request.Builder()
            .url(BASE_URL + path)
            .header("X-LC-Id", id_)
            .header("X-LC-Key", key_)
            .post(body)
            .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.code() != 200) {
                return null;
            }
            // FIXME: encoding
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // User

    public int register(String user_name, String password, String email) {

        post("users", )
    }
}
