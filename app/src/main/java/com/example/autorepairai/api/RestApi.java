package com.example.autorepairai.api;

import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class RestApi {
    private static final String testUrl = "http://localhost:8082/feedback";

    public static void getTestApi(RequestQueue mRequestQueue) {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("uid", 5);
            parameters.put("operationUid", 123);
            parameters.put("systemName", "ERP");
            parameters.put("systemTime", "2023-10-22T16:50:06.419Z");
            parameters.put("source", "");
            parameters.put("communicationId", 10000);
            parameters.put("templateId", 8);
            parameters.put("productCode", 1500);
            parameters.put("smsCode", 10);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                testUrl, parameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                System.out.println(response);
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
