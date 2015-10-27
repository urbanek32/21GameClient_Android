package tk.daruhq.uberoczkoprojekt;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;


import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class LoginActivity extends Activity {

    private Context loginContext = this;

    // UI references.
    private AutoCompleteTextView nicknameView;
    private EditText passwordView;
    private View progressView;
    private View loginFormView;

    private final OkHttpClient httpClient = new OkHttpClient();
    private HttpAsyncLoginTask userLoginTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        nicknameView = (AutoCompleteTextView) findViewById(R.id.nickname);
        passwordView = (EditText) findViewById(R.id.password);
        progressView = findViewById(R.id.login_progress);
        loginFormView = findViewById(R.id.login_form);

        /*Button myPostButton = (Button)findViewById(R.id.postButton);
        myPostButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new HttpAsyncPostTask(loginContext).execute("http://eti.endrius.tk/test/api/test_string_body",
                        "word", "supcioWyraz");
            }
        });

        Button myGetButton = (Button)findViewById(R.id.getButton);
        myGetButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new HttpAsyncGetTask(loginContext).execute("http://eti.endrius.tk/test/api/test_empty_body");
            }
        });*/

        Button myLoginButton = (Button)findViewById(R.id.login_button);
        myLoginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });


    }

    private void attemptLogin() {

        if(userLoginTask != null) {
            return;
        }

        // Reset errors.
        nicknameView.setError(null);
        passwordView.setError(null);

        // Store values at the time of the login attempt.
        String nickname = nicknameView.getText().toString();
        String password = passwordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password
        if (TextUtils.isEmpty(password)) {
            passwordView.setError(getString(R.string.error_field_required));
            focusView = passwordView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            passwordView.setError(getString(R.string.error_invalid_password));
            focusView = passwordView;
            cancel = true;
        }

        // Check for a valid nickname
        if (TextUtils.isEmpty(nickname)) {
            nicknameView.setError(getString(R.string.error_field_required));
            focusView = nicknameView;
            cancel = true;
        } else if (!isNicknameValid(nickname)) {
            nicknameView.setError(getString(R.string.error_invalid_nickname));
            focusView = nicknameView;
            cancel = true;
        }

        if(cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            showProgress(false);
            focusView.requestFocus();
            return;
        }

        showProgress(true);
        userLoginTask = new HttpAsyncLoginTask(loginContext);
        userLoginTask.execute(
                "http://java21.endrius.tk/login",
                nickname,
                password);

    }

    private void showProgress(boolean show) {
        progressView.setVisibility(show ? View.VISIBLE : View.GONE);
        loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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

    private String POST(String... params) {
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

    private String CreateLoginJSON(String nickname, String password) {
        return String.format("{ \"nickname\": \"%s\", \"password\": \"%s\" }", nickname, password);
    }

    private String LoginPOST(String... params)
    {
        try {
            MediaType JSON = MediaType.parse("application/JSON; charset=utf-8");
            RequestBody formBody = RequestBody.create(JSON, CreateLoginJSON(params[1], params[2]));

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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == 2){
            Toast.makeText(getBaseContext(), data.getExtras().get("siema").toString(), Toast.LENGTH_LONG).show();
        }


    }

    private boolean isNicknameValid(String email) {
        //TODO: Replace this with your own logic
        return email.length() > 3;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 3;
    }





    public class HttpAsyncLoginTask  extends AsyncTask<String, Void, String> {
        private Context context;

        public HttpAsyncLoginTask(Context context){
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            // write show progress Dialog code here
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            return LoginPOST(params);
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                Toast.makeText(getBaseContext(), jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                showProgress(false);
                userLoginTask = null;

                Intent intent = new Intent(context, LobbyActivity.class);
                startActivityForResult(intent, 1);
            } catch (JSONException e) {
                e.printStackTrace();
            }

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
                Toast.makeText(getBaseContext(), jsonObject.getString("message"), Toast.LENGTH_LONG).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    public class HttpAsyncGetTask  extends AsyncTask<String, Void, String> {
        private Context context;

        public HttpAsyncGetTask(Context context) {
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
            //TextView view = (TextView) findViewById(R.id.textView);
            try {
                JSONObject jsonObject = new JSONObject(result);
                Toast.makeText(getBaseContext(), jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                //view.setText(result);

                Intent intent = new Intent(context, LobbyActivity.class);
                startActivityForResult(intent, 1);

            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }

}



