package tk.daruhq.uberoczkoprojekt;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LobbyActivity extends AppCompatActivity {

    private Context lobbyContext;
    private final OkHttpClient httpClient = new OkHttpClient();

    private HttpAsyncGetLobbiesTask getLobbiesTask = null;
    private HttpAsyncJoinToLobbyTask joinLobbyTask = null;

    private String userName = "";

    private ProgressBar progressView;
    private ListView lobbyListView;
    private Button refreshButton;
    private Button createLobbyButton;

    private List<LobbyViewModel> listOfLobbies = new ArrayList<LobbyViewModel>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        lobbyContext = this;
        httpClient.setConnectTimeout(10, TimeUnit.SECONDS);
        httpClient.setReadTimeout(10, TimeUnit.SECONDS);
        httpClient.setWriteTimeout(10, TimeUnit.SECONDS);

        lobbyListView = (ListView) findViewById(R.id.lobbyListView);
        lobbyListView.setClickable(false);

        userName = getIntent().getExtras().get("playerName").toString();

        LobbyAdapter adapter = new LobbyAdapter(this, listOfLobbies);
        lobbyListView.setAdapter(adapter);

        progressView = (ProgressBar)findViewById(R.id.lobbiesProgressBar);

        createLobbyButton = (Button)findViewById(R.id.createLobbyButton);
        createLobbyButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewLobby();
            }
        });

        refreshButton = (Button)findViewById(R.id.refreshLobby);
        refreshButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getLobbies();
            }
        });

        getLobbies();
    }

    public void joinToLobby(String lobbyName, String lobbyOwner) {
        if(joinLobbyTask != null) return;

        if(lobbyOwner.equals(userName)) {
            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtra("playerName", userName);
            startActivity(intent);
            return;
        }

        joinLobbyTask = new HttpAsyncJoinToLobbyTask(lobbyContext);
        joinLobbyTask.execute("http://java21.endrius.tk/addNewAssignment",
                lobbyName,
                userName);
    }

    public void createNewLobby() {
        Intent intent = new Intent(this, CreateNewLobbyActivity.class);
        intent.putExtra("playerName", userName);
        startActivityForResult(intent, 0);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showProgress(boolean show) {
        progressView.setVisibility(show ? View.VISIBLE : View.GONE);
        lobbyListView.setVisibility(show ? View.GONE : View.VISIBLE);
        createLobbyButton.setVisibility(show ? View.GONE : View.VISIBLE);
        refreshButton.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void getLobbies() {
        showProgress(true);
        getLobbiesTask = new HttpAsyncGetLobbiesTask(lobbyContext);
        getLobbiesTask.execute(
                "http://java21.endrius.tk/allRooms"
        );
    }

    private void parseLobbiesResponse(String response) {
        try {
            JSONObject obj = new JSONObject(response);
            JSONArray lobbiesArray = obj.getJSONArray("body");
            listOfLobbies.clear();

            for(int i = 0; i < lobbiesArray.length(); i++) {
                JSONObject lobby = lobbiesArray.getJSONObject(i);
                listOfLobbies.add(new LobbyViewModel(
                        lobby.getInt("membersCount"),
                        lobby.getString("name"),
                        lobby.getString("ownerNickname"),
                        lobby.getInt("maxMembersCount"))
                );
            }

            LobbyAdapter adapter = new LobbyAdapter(this, listOfLobbies);
            lobbyListView.setAdapter(adapter);

            showProgress(false);
            getLobbiesTask = null;

        } catch (JSONException e) {
            showProgress(false);
            showToast(e.getMessage());
            e.printStackTrace();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        showToast("Refreshing lobbies...");
        getLobbies();
    }




    private String GET(String... params) {
        try {
            Request request = new Request.Builder()
                    .url(params[0])
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

    public class HttpAsyncGetLobbiesTask  extends AsyncTask<String, Void, String> {
        private Context context;

        public HttpAsyncGetLobbiesTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            // write show progress Dialog code here
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            return GET(params);
        }

        @Override
        protected void onPostExecute(String result) {

            parseLobbiesResponse(result);

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

        public HttpAsyncJoinToLobbyTask(Context context) {
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
                joinLobbyTask = null;

                if(responseCode == 500) {
                    showToast(message);
                } else if(responseCode == 201) {
                    showToast(message);

                    Intent intent = new Intent(context, GameActivity.class);
                    intent.putExtra("playerName", userName);
                    startActivity(intent);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}
