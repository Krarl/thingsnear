package se.karllundstig.thingsnear;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class NetQueue {
    private static NetQueue instance;
    private RequestQueue requestQueue;
    private static Context context;

    private NetQueue(Context context) {
        this.context = context;
        requestQueue = getRequestQueue();
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

    public <T> void add(Request<T> req) {
        getRequestQueue().add(req);
    }
}
