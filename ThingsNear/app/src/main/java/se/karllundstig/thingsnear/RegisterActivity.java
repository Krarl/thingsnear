package se.karllundstig.thingsnear;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {
    Context context;

    //UI
    Button registerButton;
    EditText usernameView;
    EditText passwordView;
    EditText emailView;
    View progressView;
    View registerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        this.context = this;

        registerButton = (Button)findViewById(R.id.register);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptRegister();
            }
        });

        progressView = findViewById(R.id.register_progress);
        registerView = findViewById(R.id.register_form);

        usernameView = (EditText)findViewById(R.id.username);
        passwordView = (EditText)findViewById(R.id.password);
        emailView = (EditText)findViewById(R.id.email);
    }

    private void attemptRegister() {
        boolean cancel = false;
        View focusView = null;

        String username = usernameView.getText().toString();
        String password = passwordView.getText().toString();
        String email = emailView.getText().toString();

        //email
        if (TextUtils.isEmpty(email)) {
            emailView.setError(getString(R.string.error_field_required));
            cancel = true;
            focusView = emailView;
        } else if (!Verification.isEmailValid(email)) {
            emailView.setError(getString(R.string.error_invalid_email));
            cancel = true;
            focusView = emailView;
        }

        //password
        if (TextUtils.isEmpty(password)) {
            passwordView.setError(getString(R.string.error_field_required));
            cancel = true;
            focusView = passwordView;
        } else if (!Verification.isPasswordValid(password)) {
            passwordView.setError(getString(R.string.error_invalid_password));
            cancel = true;
            focusView = passwordView;
        }

        //username
        if (TextUtils.isEmpty(username)) {
            usernameView.setError(getString(R.string.error_field_required));
            cancel = true;
            focusView = usernameView;
        } else if (!Verification.isUsernameValid(username)) {
            usernameView.setError(getString(R.string.error_invalid_username));
            cancel = true;
            focusView = usernameView;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);

            JSONObject body = new JSONObject();
            try {
                body.put("username", username);
                body.put("password", password);
                body.put("email", email);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            NetQueue.getInstance(this).post("/users", body, new NetQueue.RequestCallback() {
                @Override
                public void onFinished(JSONObject result) {
                    //här borde den spara ens nyss inskrivna uppgifter så man loggas in automatiskt
                    finish();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(context, "Error creating account: " + error, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFinally() {
                    showProgress(false);
                }
            });
        }
    }

    //visar laddningssymbol och gömmer UI
    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        registerView.setVisibility(show ? View.GONE : View.VISIBLE);
        registerView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                registerView.setVisibility(show ? View.GONE : View.VISIBLE);
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
