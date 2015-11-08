package tk.daruhq.uberoczkoprojekt;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class CreateNewLobbyActivity extends AppCompatActivity {

    private final OkHttpClient httpClient = new OkHttpClient();

    private Context context = null;
    private ProgressBar progressView;
    private EditText lobbyNameView;
    private EditText maxPlayersView;
    private Button createLobbyButton;

    private HttpAsyncCreateNewLobbyTask createNewLobbyTask = null;
    private HttpAsyncJoinToLobbyTask joinToLobbyTask = null;

    private String userName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_lobby);

        context = this;
        httpClient.setConnectTimeout(10, TimeUnit.SECONDS);
        httpClient.setReadTimeout(10, TimeUnit.SECONDS);
        httpClient.setWriteTimeout(10, TimeUnit.SECONDS);

        progressView = (ProgressBar) findViewById(R.id.progressBar);
        lobbyNameView = (EditText) findViewById(R.id.lobbyNameBox);
        maxPlayersView = (EditText) findViewById(R.id.maxPlayersBox);
        createLobbyButton = (Button) findViewById(R.id.createLobbyButton);
        createLobbyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewLobby();
            }
        });

        userName = getIntent().getExtras().getString("playerName");
        showToast(userName);
    }

    public void CreateNewLobby() {
        if(createNewLobbyTask != null) return;

        // Reset errors.
        lobbyNameView.setError(null);
        maxPlayersView.setError(null);

        // Store values at the time of the login attempt.
        String lobbyName = lobbyNameView.getText().toString();
        String maxPlayers = maxPlayersView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid maxPlayers
        if (TextUtils.isEmpty(maxPlayers)) {
            maxPlayersView.setError(getString(R.string.error_field_required));
            focusView = maxPlayersView;
            cancel = true;
        } else if (!isMaxPlayersValid(maxPlayers)) {
            maxPlayersView.setError(getString(R.string.error_maxPlayers_invalid));
            focusView = maxPlayersView;
            cancel = true;
        }

        // Check for a valid lobby name
        if (TextUtils.isEmpty(lobbyName)) {
            lobbyNameView.setError(getString(R.string.error_field_required));
            focusView = lobbyNameView;
            cancel = true;
        } else if (!isNameValid(lobbyName)) {
            lobbyNameView.setError(getString(R.string.error_lobbyName_invalid));
            focusView = lobbyNameView;
            cancel = true;
        }

        if(cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            return;
        }

        createNewLobbyTask = new HttpAsyncCreateNewLobbyTask(context);
        createNewLobbyTask.execute("http://java21.endrius.tk/addRoom",
                userName,
                lobbyNameView.getText().toString(),
                maxPlayersView.getText().toString());
    }

    private boolean isNameValid(String name) {
        return name.length() > 2 && name.length() < 5;
    }

    private boolean isMaxPlayersValid(String maxPlayers) {
        int players = Integer.parseInt(maxPlayers);
        return players >= 1 && players <= 4;
    }

    private void showProgress(boolean show)
    {
        progressView.setVisibility(show ? View.VISIBLE : View.GONE);
        lobbyNameView.setVisibility(show ? View.GONE : View.VISIBLE);
        maxPlayersView.setVisibility(show ? View.GONE : View.VISIBLE);
        createLobbyButton.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private String CreateLobbyJSON(String owner, String name, String maxPlayers) {
        return String.format("{ \"ownerNickname\": \"%s\", \"name\": \"%s\", \"maxMembersCount\": %s }",
                owner, name, maxPlayers);
    }

    private String NewLobbyPOST(String... params)
    {
        try {
            MediaType JSON = MediaType.parse("application/JSON; charset=utf-8");
            RequestBody formBody = RequestBody.create(JSON, CreateLobbyJSON(params[1], params[2], params[3]));

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
            showToast(e.getMessage());
            return e.getMessage();
        }
    }

    public class HttpAsyncCreateNewLobbyTask  extends AsyncTask<String, Void, String> {
        private Context context;

        public HttpAsyncCreateNewLobbyTask(Context context){
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            showProgress(true);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            return NewLobbyPOST(params);
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                int responseCode = jsonObject.getInt("code");
                String message = jsonObject.getString("message");

                showProgress(false);
                createNewLobbyTask = null;

                if(responseCode == 500) {
                    showToast(message);
                } else if(responseCode == 201) {
                    showToast(message);

                    joinToLobbyTask = new HttpAsyncJoinToLobbyTask(context);
                    joinToLobbyTask.execute("http://java21.endrius.tk/addNewAssignment",
                            lobbyNameView.getText().toString(),
                            userName);
                }

                /*Intent intent = new Intent(context, LobbyActivity.class);
                intent.putExtra("playerName", userName);
                startActivityForResult(intent, 1);*/
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }


    private String JoinToLobbyJSON(String roomName, String nickname) {
        return String.format("{ \"roomName\": \"%s\", \"nickname\": \"%s\" }", roomName, nickname);
    }

    private String JoinToLobbyPOST(String... params)
    {
        try {
            MediaType JSON = MediaType.parse("application/JSON; charset=utf-8");
            RequestBody formBody = RequestBody.create(JSON, JoinToLobbyJSON(params[1], params[2]));

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
            showToast(e.getMessage());
            return e.getMessage();
        }
    }

    public class HttpAsyncJoinToLobbyTask  extends AsyncTask<String, Void, String> {
        private Context context;

        public HttpAsyncJoinToLobbyTask(Context context){
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            showProgress(true);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            return JoinToLobbyPOST(params);
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                int responseCode = jsonObject.getInt("code");
                String message = jsonObject.getString("message");

                showProgress(false);
                joinToLobbyTask = null;

                if(responseCode == 500) {
                    showToast(message);

                    /*Intent intent = new Intent(context, LobbyActivity.class);
                    startActivity(intent);*/
                    Intent intent = new Intent();
                    setResult(0, intent);
                    finish();

                } else if(responseCode == 201) {
                    showToast(message);
                    showToast("Graj");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}
