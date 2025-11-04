package com.example.book_store_mobileapp;


import org.json.JSONObject;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpProvider {

    private static final OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    // ✅ Hàm gửi POST request với JSON body
    public static JSONObject sendPost(String url, RequestBody body) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response: " + response);
            }
            String resStr = response.body().string();
            return new JSONObject(resStr);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("HTTP Request failed: " + e.getMessage());
        }
    }
}
