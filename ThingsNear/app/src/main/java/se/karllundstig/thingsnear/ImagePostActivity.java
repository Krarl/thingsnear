package se.karllundstig.thingsnear;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class ImagePostActivity extends AppCompatActivity {

    Context context;
    Location location;
    String imagePath;

    ImageView imageView;
    EditText editText;
    Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_post);
        context = this;

        imageView = (ImageView)findViewById(R.id.imageView);
        editText = (EditText)findViewById(R.id.editText);
        submit = (Button)findViewById(R.id.submit);

        location = getIntent().getExtras().getParcelable("location");
        imagePath = getIntent().getExtras().getString("imagePath");

        //Visar bilden vi ska ladda upp
        Bitmap myBitmap = BitmapFactory.decodeFile(imagePath);
        imageView.setImageBitmap(myBitmap);

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
                final ProgressDialog progress = new ProgressDialog(context);
                progress.setTitle("Loading");
                progress.setMessage("Submitting post...");
                progress.setCancelable(false);
                progress.show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final NetQueue netQueue = NetQueue.getInstance(context);
                            final String response = FileUploader.sendFile(netQueue.getServer() + "/images/", imagePath, netQueue.getToken(), "image", "image/jpeg");
                            final JSONObject res = new JSONObject(response);
                            if (!res.getBoolean("success")) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Toast.makeText(context, res.getString("error"), Toast.LENGTH_LONG).show();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            Toast.makeText(context, "Unknown error", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            } else {
                                final String image = res.getString("image");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        JSONObject body = new JSONObject();
                                        try {
                                            body.put("latitude", location.getLatitude());
                                            body.put("longitude", location.getLongitude());
                                            body.put("content", editText.getText());
                                            body.put("image", image);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

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
                        } catch (Exception err) {
                            final Exception e = err;
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                }).start();
            }
        });
    }
}