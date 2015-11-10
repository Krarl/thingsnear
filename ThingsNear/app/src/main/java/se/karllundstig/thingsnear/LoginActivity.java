package se.karllundstig.thingsnear;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    private EditText usernameView;
    private EditText passwordView;
    private View progressView;
    private View loginFormView;

    private Context context;

    String server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        this.context = this;
        server = getString(R.string.server);

        // Set up the login form.
        usernameView = (AutoCompleteTextView) findViewById(R.id.username);

        passwordView = (EditText) findViewById(R.id.password);
        passwordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button usernameSignInButton = (Button) findViewById(R.id.username_sign_in_button);
        usernameSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        loginFormView = findViewById(R.id.login_form);
        progressView = findViewById(R.id.login_progress);
    }

    private void launchMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        //nollställ errors
        usernameView.setError(null);
        passwordView.setError(null);

        final String username = usernameView.getText().toString();
        final String password = passwordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        //kollar efter giltigt lösenord
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            passwordView.setError(getString(R.string.error_invalid_password));
            focusView = passwordView;
            cancel = true;
        }

        //kollar efter giltigt användarnamn
        if (TextUtils.isEmpty(username)) {
            usernameView.setError(getString(R.string.error_field_required));
            focusView = usernameView;
            cancel = true;
        } else if (!isUsernameValid(username)) {
            usernameView.setError(getString(R.string.error_invalid_username));
            focusView = usernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to perform the user login attempt.
            showProgress(true);

            JSONObject body = new JSONObject();
            try {
                body.put("username", username);
                body.put("password", password);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            NetQueue.getInstance(this).post("/login", body, new NetQueue.RequestCallback() {
                @Override
                public void onFinished(JSONObject result) {
                    try {
                        SharedPreferences settings = getSharedPreferences("prefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString("token", result.getString("token"));
                        editor.apply();
                        launchMain();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(String error) {
                    showProgress(false);
                    if (error.equals("Wrong username or password")) {
                        passwordView.setError(getString(R.string.error_incorrect_login));
                        passwordView.requestFocus();
                    } else {
                        Toast.makeText(context, "Connection error", Toast.LENGTH_LONG).show();
                        Log.e("LoginActivity", error);
                    }
                }
            });

            /*Request request = new JsonObjectRequest(Request.Method.POST, server + "/login", body,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        showProgress(false);
                        try {
                            if (response.getBoolean("success")) {
                                SharedPreferences settings = getSharedPreferences("prefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = settings.edit();
                                editor.putString("token", response.getString("token"));
                                editor.apply();
                                launchMain();
                            } else {
                                passwordView.setError(getString(R.string.error_incorrect_login));
                                passwordView.requestFocus();
                            }
                        } catch(Exception e) {
                            Log.e("LoginActivity", e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
                @Override
                    public void onErrorResponse(VolleyError error) {
                        showProgress(false);
                        Toast.makeText(context, "Connection error", Toast.LENGTH_LONG).show();
                        Log.e("LoginActivity", error.toString());
                    }
                });

            NetQueue.getInstance(this).add(request);*/
        }
    }

    private boolean isUsernameValid(String username) {
        //tillåt inte flera ord eller mellanslag
        return !username.contains(" ");
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }

    //visar laddningssymbol och gömmer UI
    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        loginFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        progressView.setVisibility(show ? View.VISIBLE : View.GONE);
        progressView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }
}

