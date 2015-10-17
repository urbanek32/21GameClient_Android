package tk.daruhq.uberoczkoprojekt;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class GameActivity extends AppCompatActivity {

    private final OkHttpClient httpClient = new OkHttpClient();
    private Context gameContext = this;

    private Timer timer;
    private TimerTask timerTask;
    private final Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Button myGetButton = (Button)findViewById(R.id.backButton);
        myGetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer = new Timer();
                initTimerTask();
                timer.schedule(timerTask, 500, 1000);
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Write your code here
        super.onBackPressed();
    }

    private void ole(String res) {
        getIntent().putExtra("siema", res);
        setResult(2, getIntent());
        finish();
    }

    private void initTimerTask() {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        new HttpAsyncPostTask(gameContext).execute("http://eti.endrius.tk/test/api/test_string_body",
                                "word", String.valueOf(System.currentTimeMillis()));
                    }
                });
            }
        };
    }

    private String POST(String... params)
    {
        try {
            FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();
            for(int i = 1; i < params.length-1; i++) {
                formEncodingBuilder.add(params[i], params[i+1]);
            }
            RequestBody formBody = formEncodingBuilder.build();

            Request request = new Request.Builder()
                    .url(params[0])
                    .post(formBody)
                    .build();

            Response response = httpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            return response.body().string();
        }
        catch(IOException e) {
            return e.getMessage();
        }
    }

    public class HttpAsyncPostTask  extends AsyncTask<String, Void, String> {
        private Context context;

        public HttpAsyncPostTask(Context context){
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            // write show progress Dialog code here
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            return POST(params);
        }

        @Override
        protected void onPostExecute(String result) {

            try {
                JSONObject jsonObject = new JSONObject(result);
                Toast.makeText(getBaseContext(), jsonObject.getString("result"), Toast.LENGTH_LONG).show();
                //ole(jsonObject.getString("result"));

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}
