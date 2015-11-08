package tk.daruhq.uberoczkoprojekt;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
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
    private String userName = "";

    private Button drawCardButton;
    private Button readyButton;
    private Button noMoreButton;

    private TextView player1Box;
    private TextView player1Score;
    private TextView player2Box;
    private TextView player2Score;
    private TextView player3Box;
    private TextView player3Score;
    private TextView mainPlayerBox;
    private TextView mainPlayerScore;

    private ProgressBar progressBar;

    private HttpAsyncGetCardTask getCardTask = null;
    private HttpAsyncSwitchStatusTask switchStatusTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        /*Button myGetButton = (Button)findViewById(R.id.readyButton);
        myGetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer = new Timer();
                initTimerTask();
                timer.schedule(timerTask, 500, 1000);
            }
        });*/

        player1Box = (TextView) findViewById(R.id.player1Box);
        player1Score = (TextView) findViewById(R.id.player1Score);
        player2Box = (TextView) findViewById(R.id.player2Box);
        player2Score = (TextView) findViewById(R.id.player2Score);
        player3Box = (TextView) findViewById(R.id.player3Box);
        player3Score = (TextView) findViewById(R.id.player3Score);
        mainPlayerBox = (TextView) findViewById(R.id.mainPlayerBox);
        mainPlayerScore = (TextView) findViewById(R.id.mainPlayerScore);

        drawCardButton = (Button) findViewById(R.id.drawCardButton);
        readyButton = (Button) findViewById(R.id.readyButton);
        noMoreButton = (Button) findViewById(R.id.noMoreButton);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        userName = getIntent().getExtras().get("playerName").toString();
        mainPlayerBox.setText(userName);
        mainPlayerScore.setText("0");
    }

    @Override
    public void onBackPressed() {
        // Write your code here
        //timer.cancel();
        super.onBackPressed();
    }

    /*private void ole(String res) {
        getIntent().putExtra("siema", res);
        setResult(2, getIntent());
        finish();
    }*/

    /*private void initTimerTask() {
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
    }*/

    private void checkGameState() {
        if(getCardTask != null) return;


    }

    private void processGameStateResponse(String response) {

    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        drawCardButton.setVisibility(show ? View.GONE : View.VISIBLE);
        readyButton.setVisibility(show ? View.GONE : View.VISIBLE);
        noMoreButton.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    /*
    *
    * SWITCH STATUS TASK
    *
    * */

    private String SwitchStatusPOST(String... params)
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

    public class HttpAsyncSwitchStatusTask  extends AsyncTask<String, Void, String> {
        private Context context;

        public HttpAsyncSwitchStatusTask(Context context){
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            // write show progress Dialog code here
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            return SwitchStatusPOST(params);
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



    /*
    *
    * DRAW CARD TASK
    *
    * */

    private String DrawCardGET(String... params) {
        try {
            RequestBody formBody = new FormEncodingBuilder()
                    .add("username", params[1])
                    .add("giveCard", params[2])
                    .build();

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

    public class HttpAsyncGetCardTask  extends AsyncTask<String, Void, String> {
        private Context context;

        public HttpAsyncGetCardTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            // write show progress Dialog code here
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            return DrawCardGET(params);
        }

        @Override
        protected void onPostExecute(String result) {

            //parseLobbiesResponse(result);

        }
    }
}
