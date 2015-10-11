package se.karllundstig.thingsnear;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    String server;
    NetQueue netQueue;

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
            final String token = settings.getString("token", "");
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
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("x-access-token", token);
                    return params;
                }
            };

            netQueue.add(request);
        }
    }

    private void loadFeed() {

    }

    private void login() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
