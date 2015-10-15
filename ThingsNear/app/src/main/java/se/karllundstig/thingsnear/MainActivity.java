package se.karllundstig.thingsnear;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    //nätverk
    NetQueue netQueue;
    String server;
    String token;

    //location
    GoogleApiClient googleApiClient;
    Location location;
    boolean updatingLocation = false;

    //user interface
    RecyclerView feedView;
    FeedAdapter adapter;
    RecyclerView.LayoutManager layoutManager;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;

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

        //skaffar tillgång till location api
        buildGoogleApiClient();

        //laddar vårt token och sätter sen igång hela laddningskedjan
        loadToken();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("MainActivity", "Connecting to GoogleApiClient");
        googleApiClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdate();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (googleApiClient.isConnected())
            startLocationUpdate();
    }

    private synchronized void buildGoogleApiClient() {
        Log.d("MainActivity", "Building GoogleApiClient");
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("MainActivity", "Connected to GoogleApiClient");
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (lastLocation != null)
            onLocationChanged(lastLocation);
        startLocationUpdate();
    }

    private void startLocationUpdate() {
        Log.d("MainActivity", "Starting location updates");
        if (!updatingLocation) {
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(5000);
            locationRequest.setFastestInterval(1000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

            updatingLocation = true;
        }
    }

    private void stopLocationUpdate() {
        Log.d("MainActivity", "Stopping location updates");
        if (updatingLocation) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            updatingLocation = false;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    boolean resolvingError = false;
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.e("MainActivity", "Error connecting to GoogleApiServices");
        if (resolvingError) {
            return;
        } else if (result.hasResolution()) {
            try {
                resolvingError = true;
                final int REQUEST_RESOLVE_ERROR = 1001;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                //error i lösningen, testa igen
                googleApiClient.connect();
            }
        } else {
            Toast.makeText(this, "Error connecting to Google Location services", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        Log.d("MainActivity", "Recieved location: " + location.toString());
        if (adapter != null) {
            adapter.setLocation(location);
            adapter.notifyDataSetChanged();
        } else if (!loadingFeed && token != null) {
            loadFeed();
        }
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
                                if (!loadingFeed && location != null)
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

    boolean loadingFeed = false;
    private void loadFeed() {
        //det är dumt att ladda den igen om den inte har laddat färdigt
        if (loadingFeed)
            return;
        loadingFeed = true;

        String url = String.format(server + "/feed?longitude=%f&latitude=%f", location.getLongitude(), location.getLatitude());
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
                                    p.longitude = data.getJSONObject(i).getJSONObject("location").getJSONArray("coordinates").getDouble(0);
                                    p.latitude = data.getJSONObject(i).getJSONObject("location").getJSONArray("coordinates").getDouble(1);
                                    posts.add(p);
                                } catch(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            adapter = new FeedAdapter(posts, context, location);
                            feedView.setAdapter(adapter);
                            loadingFeed = false;
                        } catch(Exception e) {
                            loadingFeed = false;
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loadingFeed = false;
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
