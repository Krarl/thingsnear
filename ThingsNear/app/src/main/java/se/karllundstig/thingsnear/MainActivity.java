package se.karllundstig.thingsnear;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    //nätverk
    NetQueue netQueue;
    String server;

    //location
    GoogleApiClient googleApiClient;
    Location location;
    boolean updatingLocation = false;

    //user interface
    RecyclerView feedView;
    FeedAdapter adapter;
    RecyclerView.LayoutManager layoutManager;
    FloatingActionButton fab;
    SwipeRefreshLayout swipeRefreshLayout;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setEnabled(false);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, TextPostActivity.class);
                intent.putExtra("location", location);
                startActivity(intent);
            }
        });

        feedView = (RecyclerView)findViewById(R.id.feed);
        feedView.setHasFixedSize(true); //den ändrar inte storlek, gör det tydligen snabbare med dt här

        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadFeed();
            }
        });

        layoutManager = new LinearLayoutManager(this);
        feedView.setLayoutManager(layoutManager);

        netQueue = NetQueue.getInstance(this);
        server = getString(R.string.server);

        //skaffar tillgång till location api
        buildGoogleApiClient();

        //laddar vårt token och sätter sen igång hela laddningskedjan
        if (savedInstanceState != null && savedInstanceState.containsKey("posts"))
            setInstanceState(savedInstanceState);
        else
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

    @Override
    public void onSaveInstanceState(Bundle state) {
        state.putParcelable("location", location);
        state.putBoolean("updatingLocation", updatingLocation);
        state.putString("token", netQueue.getToken());
        state.putParcelable("layoutManager", layoutManager.onSaveInstanceState());
        if (adapter != null)
            state.putParcelableArrayList("posts", adapter.getPosts());
    }

    private void setInstanceState(Bundle state) {
        if (state == null)
            return;

        location = state.getParcelable("location");
        updatingLocation = state.getBoolean("updatingLocation");
        netQueue.setToken(state.getString("token"));
        layoutManager.onRestoreInstanceState(state.getParcelable("layoutManager"));
        adapter = new FeedAdapter(state.<Post>getParcelableArrayList("posts"), this, location);
        feedView.setAdapter(adapter);
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
            locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(5000);
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
        Log.d("MainActivity", "Recieved location: " + location.toString());
        this.location = location;

        if (adapter != null) {
            adapter.setLocation(location);
            adapter.notifyDataSetChanged();
        } else {
            loadFeed();
        }

        fab.setEnabled(true);
    }

    private void loadToken() {
        SharedPreferences settings = getSharedPreferences("prefs", MODE_PRIVATE);
        if (!settings.contains("token")) {
            //vi har inget token, dags att logga in
            login();
        } else {
            netQueue.setToken(settings.getString("token", ""));
            //testa om det fortfarande är giltigt
            netQueue.get("/test", new NetQueue.RequestCallback() {
                @Override
                public void onFinished(JSONObject result) {
                    loadFeed();
                }

                @Override
                public void onError(String error) {
                    Log.e("MainActivity", error);
                    login();
                }

                @Override
                public void onFinally() {}
            });
        }
    }

    boolean loadingFeed = false;
    private void loadFeed() {
        //det är dumt att ladda den igen om den inte har laddat färdigt, eller om vi inte har några koordinater, eller om vi inte har ett token
        if (loadingFeed || location == null || netQueue.getToken() == null)
            return;
        loadingFeed = true;

        String query = String.format("/feed?longitude=%f&latitude=%f", location.getLongitude(), location.getLatitude());

        netQueue.get(query, new NetQueue.RequestCallback() {
            @Override
            public void onFinished(JSONObject result) {
                try {
                    JSONArray data = result.getJSONArray("posts");
                    ArrayList<Post> posts = new ArrayList<>();

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'", Locale.US);
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
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String error) {
                login();
            }

            @Override
            public void onFinally() {
                loadingFeed = false;
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void login() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
