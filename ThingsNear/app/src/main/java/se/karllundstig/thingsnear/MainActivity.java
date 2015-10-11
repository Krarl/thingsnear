package se.karllundstig.thingsnear;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    String server;
    NetQueue netQueue;
    String token;

    RecyclerView feedView;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        feedView = (RecyclerView)findViewById(R.id.feed);
        feedView.setHasFixedSize(true); //den ändrar inte storlek, gör det tydligen snabbare med dt här

        layoutManager = new LinearLayoutManager(this);
        feedView.setLayoutManager(layoutManager);

        netQueue = NetQueue.getInstance(this);
        server = getString(R.string.server);

        //laddar vårt token och sätter sen igång hela laddningskedjan
        loadToken();
    }

    private void loadToken() {
        SharedPreferences settings = getSharedPreferences("prefs", MODE_PRIVATE);
        if (!settings.contains("token")) {
            //vi har inget token, dags att logga in
            login();
        } else {
            token = settings.getString("token", "");
            //testa om det fortfarande är giltigt
            Request request = new JsonObjectRequest(Request.Method.GET, server + "/test", null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getBoolean("success")) {
                                loadFeed();
                            } else {
                                login();
                            }
                        } catch(Exception e) {
                            Log.e("MainActivity)", e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    login();
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("x-access-token", token);
                    return params;
                }
            };

            netQueue.add(request);
        }
    }

    private void loadFeed() {
        float longitude = 0.0f;
        float latitude = 0.0f;
        String url = String.format(server + "/feed?longitude=%f&latitude=%f", longitude, latitude);
        Request request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray data = response.getJSONArray("posts");
                            ArrayList<Post> posts = new ArrayList<>();

                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'");
                            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                            for (int i = 0; i < data.length(); ++i) {
                                try {
                                    Post p = new Post();
                                    p.content = data.getJSONObject(i).getString("content");
                                    p.creator = data.getJSONObject(i).getJSONObject("creator").getString("username");
                                    p.date = dateFormat.parse(data.getJSONObject(i).getString("date"));
                                    posts.add(p);
                                } catch(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            adapter = new FeedAdapter(posts);
                            feedView.setAdapter(adapter);
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                login();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("x-access-token", token);
                return params;
            }
        };

        netQueue.add(request);
    }

    private void login() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
