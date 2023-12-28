package com.example.autorepairai.api;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

public class RestApi {

    //Логин - dima@mail.ru
    //Пароль - qwerty123@Q
    public static final MediaType JSON = MediaType.get("application/json");
    private static final String domain = "https://test.npp-arts.ru/api/v1/public/";
    //private static final String domain = "http://10.0.2.2:8001/api/v1/public/";
    private static final String createAppIdUrl = domain + "detection/application/create";
    private static final String uploadFileUrl = domain + "detection/%s/file";
    private static final String sendFileUrl = domain + "detection/%s/send";
    private static final String getStatusUrl = domain + "detection/%s/status";
    private static final String getResultUrl = domain + "detection/%s/result";
    private static final String updateAutoInfo = domain + "detection/%s/auto_info";
    private static final String updateAutoDamage = domain + "detection/%s/damage_info";
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String getPricesList = domain + "detection/%s/list_prices?report_date="+ LocalDate.now().format(formatter)+"&rf_subject=64";
    private static final String auth = domain + "account/auth";
    private static final String reg = domain + "account/register";

    public static String createAppId(String apiKey) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(createAppIdUrl)
                .post(RequestBody.create("", JSON))
                .addHeader("X-API-Key", apiKey)
                .build();
        String id = "";
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Запрос к серверу не был успешен: " + createAppIdUrl + " " + response.body().string() + " " +
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

    public static void uploadFile(String id, ImageView imageViewForDownload, String apiKey) {
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
                .addHeader("X-API-Key", apiKey)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Запрос к серверу не был успешен: " + String.format(uploadFileUrl,id) + " " + response.body().string() + " " +
                        response.code() + " " + response.message());
            }
        } catch (IOException e) {
            System.out.println("Ошибка подключения: " + e);
        }
    }

    public static void sendFile(String id, String apiKey) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(String.format(sendFileUrl,id))
                .addHeader("X-API-Key", apiKey)
                .post(RequestBody.create("", JSON))
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Запрос к серверу не был успешен: " + String.format(sendFileUrl,id) + " "+ response.body().string() + " " +
                        response.code() + " " + response.message());
            }
        } catch (IOException e) {
            System.out.println("Ошибка подключения: " + e);
        }
    }

    public static String getStatus(String id, String apiKey) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(String.format(getStatusUrl,id))
                .addHeader("X-API-Key", apiKey)
                .build();
        String responseStr = null;
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Запрос к серверу не был успешен: " + String.format(getStatusUrl,id) + " " + response.body().string() + " " +
                        response.code() + " " + response.message());
            }
            responseStr = response.body().string();
        } catch (IOException e) {
            System.out.println("Ошибка подключения: " + e);
        }

        return responseStr;
    }

    public static String registration(String login, String password, View view) {
        OkHttpClient client = new OkHttpClient();

        JSONObject parameters = new JSONObject();

        try {
            parameters.put("login", login);
            parameters.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Request request = new Request.Builder()
                .url(reg)
                .post(RequestBody.create(String.valueOf(parameters), JSON))
                .build();
        String responseStr = null;

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                JSONObject Jobject = new JSONObject(response.body().string());
                Snackbar.make(view, Jobject.get("detail").toString().replace("System message: ",""), Snackbar.LENGTH_SHORT).show();
                throw new IOException("Запрос к серверу не был успешен: " +
                        response.code() + " " + response.message());
            }
            responseStr = response.body().string();

        } catch (IOException | JSONException e) {
            System.out.println("Ошибка подключения: " + e);
        }
        return responseStr;
    }

    public static String authorization(String login, String password, View view) {
        OkHttpClient client = new OkHttpClient();

        JSONObject parameters = new JSONObject();

        try {
            parameters.put("login", login);
            parameters.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Request request = new Request.Builder()
                .url(auth)
                .post(RequestBody.create(String.valueOf(parameters), JSON))
                .build();
        String responseStr = null;

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                JSONObject Jobject = new JSONObject(response.body().string());
                Snackbar.make(view, Jobject.get("detail").toString().replace("System message",""), Snackbar.LENGTH_SHORT).show();
                throw new IOException("Запрос к серверу не был успешен: " +
                        response.code() + " " + response.message());
            }
            responseStr = response.body().string();

        } catch (IOException | JSONException e) {
            System.out.println("Ошибка подключения: " + e);
        }
        return responseStr;
    }

    public static String getResult(String id, String apiKey) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(String.format(getResultUrl,id))
                .addHeader("X-API-Key", apiKey)
                .build();
        String responseStr = null;
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Запрос к серверу не был успешен: " + String.format(getResultUrl,id) + " " + response.body().string() + " " +
                        response.code() + " " + response.message());
            }
            responseStr = response.body().string();
        } catch (IOException e) {
            System.out.println("Ошибка подключения: " + e);
        }

        Log.i("getResult", responseStr);

        return responseStr;
    }

    public static void getPrices(String id, String apiKey) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(String.format(getPricesList,id))
                .addHeader("X-API-Key", apiKey)
                .build();
        String responseStr = null;
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Запрос к серверу не был успешен: " + String.format(getPricesList,id) + " " + response.body().string() + " " +
                        response.code() + " " + response.message());
            }
            responseStr = response.body().string();
        } catch (IOException e) {
            System.out.println("Ошибка подключения: " + e + " " + String.format(getPricesList,id));
        }
    }

    public static String updateAutoInfo(String apiKey, String mark, String model, String year, String id) {
        OkHttpClient client = new OkHttpClient();

        JSONObject parameters = new JSONObject();

        try {
            parameters.put("mark", mark);
            parameters.put("model", model);
            parameters.put("year", year);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Request request = new Request.Builder()
                .url(String.format(updateAutoInfo,id))
                .patch(RequestBody.create(String.valueOf(parameters), JSON))
                .addHeader("X-API-Key", apiKey)
                .build();
        String responseStr = null;
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Запрос к серверу не был успешен: " + createAppIdUrl + " " + response.body().string() + " " +
                        response.code() + " " + response.message());
            }
            responseStr = response.body().string();
        } catch (IOException e) {
            System.out.println("Ошибка подключения: " + e);
        }

        Log.i("PATCH Request:" , responseStr);
        return responseStr;
    }

    public static String updateAutoDamage(String apiKey, String id, List<String> damages) {
        OkHttpClient client = new OkHttpClient();

        JSONObject parameters = new JSONObject();

        JSONArray array = new JSONArray(damages);

        try {
            parameters.put("damageList", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.i("updateAutoDamage", parameters.toString());

        Request request = new Request.Builder()
                .url(String.format(updateAutoDamage,id))
                .patch(RequestBody.create(String.valueOf(parameters), JSON))
                .addHeader("X-API-Key", apiKey)
                .build();
        String responseStr = null;
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Запрос к серверу не был успешен: " + createAppIdUrl + " " + response.body().string() + " " +
                        response.code() + " " + response.message());
            }
            responseStr = response.body().string();
        } catch (IOException e) {
            System.out.println("Ошибка подключения: " + e);
        }

        Log.i("PATCH Request:" , responseStr);
        return responseStr;
    }
}
