package se.karllundstig.thingsnear;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class NetQueue {
    private static NetQueue instance;
    private Context context;
    private String server;
    private String token;
    private RequestQueue requestQueue;
    private long serverDelta;

    private NetQueue(Context context) {
        this.context = context;
        requestQueue = getRequestQueue();
        token = "";
        server = context.getString(R.string.server);
        serverDelta = 0;
    }

    public static synchronized NetQueue getInstance(Context context) {
        if (instance == null)
            instance = new NetQueue(context);

        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());

        return requestQueue;
    }

    public void setToken(String token) {
        this.token = token;
        Log.i("NetQueue", "Token set to " + token);
    }

    public String getToken() {
        return token;
    }

    public String getServer() {
        return server;
    }

    public long getServerDelta() {
        return serverDelta;
    }

    public interface RequestCallback {
        void onFinished(JSONObject result);
        void onError(String error);
        void onFinally();
    }

    public void get(String query, final RequestCallback callback) {
        request(Request.Method.GET, query, null, callback);
    }

    public void post(String query, JSONObject body, final RequestCallback callback) {
        request(Request.Method.POST, query, body, callback);
    }

    public void request(int method, String query, @Nullable JSONObject body, final RequestCallback callback) {
        Request request = new JsonObjectRequest(method, server + query, body,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            if (response.getBoolean("success")) {
                                callback.onFinished(response);
                            } else {
                                if (response.has("error")) {
                                    callback.onError(response.getString("error"));
                                } else {
                                    callback.onError("Unknown server error");
                                }
                            }
                        } catch(Exception e) {
                            Log.e("NetQueue", e.getMessage());
                            callback.onError(e.getMessage());
                        }
                        callback.onFinally();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.toString().equals("com.android.volley.TimeoutError")) {
                    callback.onError("Could not connect to server");
                } else {
                    callback.onError(error.toString());
                }
                callback.onFinally();
            }
        }) {
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    serverDelta = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US).parse(response.headers.get("Date")).getTime() - Calendar.getInstance().getTime().getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return super.parseNetworkResponse(response);
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                if (token != null && !token.equals("")) {
                    params.put("x-access-token", token);
                }
                return params;
            }
        };

        add(request);
    }

    public <T> void add(Request<T> req) {
        getRequestQueue().add(req);
    }
}
