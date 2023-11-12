package com.example.autorepairai.api;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;
import android.widget.TextView;


import com.example.autorepairai.ui.notifications.NotificationsViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RestApi {
    public static final MediaType JSON = MediaType.get("application/json");
    private static final String createAppIdUrl = "http://10.0.2.2:8001/api/v1/public/detection/application/create";
    private static final String uploadFileUrl = "http://10.0.2.2:8001/api/v1/public/detection/%s/file";
    private static final String sendFileUrl = "http://10.0.2.2:8001/api/v1/public/detection/%s/send";
    private static final String getResultUrl = "http://10.0.2.2:8001/api/v1/public/detection/%s/result";

    public static String createAppId() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(createAppIdUrl)
                .post(RequestBody.create("", JSON))
                .build();
        String id = "";
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Запрос к серверу не был успешен: " +
                        response.code() + " " + response.message());
            }
            String responseBody = response.body().string();
            int index = responseBody.indexOf(":") + 1;
            id = responseBody.substring(index, responseBody.length() - 1);
        } catch (IOException e) {
            System.out.println("Ошибка подключения: " + e);
        }

        return id;
    }

    public static void uploadFile(String id, ImageView imageViewForDownload) {
        JSONObject parameters = new JSONObject();
        BitmapDrawable bitmapDrawable = ((BitmapDrawable) imageViewForDownload.getDrawable());
        Bitmap bitmap = bitmapDrawable.getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] imageInByte = stream.toByteArray();
        String encodedString = Base64.getEncoder().encodeToString(imageInByte);

        try {
            parameters.put("fileBase64", encodedString);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(String.format(uploadFileUrl,id))
                .post(RequestBody.create(String.valueOf(parameters), JSON))
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Запрос к серверу не был успешен: " +
                        response.code() + " " + response.message());
            }
        } catch (IOException e) {
            System.out.println("Ошибка подключения: " + e);
        }
    }

    public static void sendFile(String id) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(String.format(sendFileUrl,id))
                .post(RequestBody.create("", JSON))
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Запрос к серверу не был успешен: " +
                        response.code() + " " + response.message());
            }
        } catch (IOException e) {
            System.out.println("Ошибка подключения: " + e);
        }
    }

    public static void getResult(String id, TextView tv1) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(String.format(getResultUrl,id))
                .build();
        try (Response response = client.newCall(request).execute()) {
            tv1.setText(response.body().string());
            if (!response.isSuccessful()) {
                throw new IOException("Запрос к серверу не был успешен: " +
                        response.code() + " " + response.message());
            }
        } catch (IOException e) {
            System.out.println("Ошибка подключения: " + e);
        }
    }
}
