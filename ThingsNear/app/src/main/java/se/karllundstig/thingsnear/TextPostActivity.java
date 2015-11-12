package se.karllundstig.thingsnear;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class TextPostActivity extends AppCompatActivity {

    Context context;
    Location location;

    EditText editText;
    Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_post);
        context = this;

        editText = (EditText)findViewById(R.id.editText);
        submit = (Button)findViewById(R.id.submit);

        location = getIntent().getExtras().getParcelable("location");

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("")) {
                    submit.setEnabled(false);
                } else {
                    submit.setEnabled(true);
                }
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject body = new JSONObject();
                try {
                    body.put("latitude", location.getLatitude());
                    body.put("longitude", location.getLongitude());
                    body.put("content", editText.getText());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                final ProgressDialog progress = new ProgressDialog(context);
                progress.setTitle("Loading");
                progress.setMessage("Submitting post...");
                progress.setCancelable(false);
                progress.show();

                NetQueue netQueue = NetQueue.getInstance(context);
                netQueue.post("/feed", body, new NetQueue.RequestCallback() {
                    @Override
                    public void onFinished(JSONObject result) {
                        finish();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFinally() {
                        progress.dismiss();
                    }
                });
            }
        });
    }
}