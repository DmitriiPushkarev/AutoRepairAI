package com.example.autorepairai.api;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.autorepairai.MainActivity;
import com.example.autorepairai.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

public class RestApi {
    private static final String createAppIdUrl = "http://10.0.2.2:8001/api/v1/public/detection/application/create";
    private static final String uploadFileUrl = "http://10.0.2.2:8001/api/v1/public/detection/%s/file";
    private static final String sendFileUrl = "http://10.0.2.2:8001/api/v1/public/detection/%s/send";
    private static final String getResultUrl = "http://10.0.2.2:8001/api/v1/public/detection/%s/result";

    public static void createAppId(RequestQueue mRequestQueue, ImageView imageViewForDownload, TextView tv1) {
        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                createAppIdUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String id = response.getString("applicationId");
                    uploadFile(mRequestQueue, id, imageViewForDownload);
                    sendFile(mRequestQueue, id);
                    getResult(mRequestQueue, id, tv1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        mRequestQueue.add(request);
    }

    public static void uploadFile(RequestQueue mRequestQueue, String id, ImageView imageViewForDownload) {
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
        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                String.format(uploadFileUrl,id), parameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        mRequestQueue.add(request);
    }

    public static void sendFile(RequestQueue mRequestQueue, String id) {
        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                String.format(sendFileUrl,id), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        mRequestQueue.add(request);
    }

    public static void getResult(RequestQueue mRequestQueue, String id, TextView tv1) {
        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                String.format(getResultUrl, id), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                tv1.setText(response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        mRequestQueue.add(request);
    }
}
