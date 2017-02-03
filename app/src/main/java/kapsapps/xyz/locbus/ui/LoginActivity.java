package kapsapps.xyz.locbus.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import kapsapps.xyz.locbus.BuildConfig;
import kapsapps.xyz.locbus.R;
import kapsapps.xyz.locbus.utils.AppRoot;
import kapsapps.xyz.locbus.utils.Constants;
import kapsapps.xyz.locbus.utils.NetworkController;
import kapsapps.xyz.locbus.utils.PrefUtils;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity{


    private static final String TAG = LoginActivity.class.getSimpleName();
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private AppRoot mAppRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);

        mAppRoot = AppRoot.getInstance();


        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }

    private void attemptLogin() {

        String userName = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        if(userName.isEmpty()){
            mEmailView.setError(getResources().getString(R.string.emailErr));
            return;
        }

        if(password.isEmpty()){
            mPasswordView.setError(getResources().getString(R.string.passErr));
            return;
        }

        performServerCall(userName,password);
    }

    private void performServerCall(String userName,String password) {
        if(NetworkController.isNetworkAvailable(getApplicationContext())){

            String url = BuildConfig.host + Constants.LOGIN;
            url = String.format(url,userName,password);

            JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    mAppRoot.removeRequestFromQueue(TAG);
                    checkLogin(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mAppRoot.removeRequestFromQueue(TAG);
                    Toast.makeText(getApplicationContext(),R.string.loginFailed,Toast.LENGTH_SHORT).show();
                }
            });

            mAppRoot.addRequest(request,TAG);


        }else{
            Toast.makeText(getApplicationContext(),R.string.noInternet,Toast.LENGTH_SHORT).show();
        }
    }

    private void checkLogin(JSONArray response) {
        try{
            if(response.length() == 0){
                Toast.makeText(getApplicationContext(),R.string.loginFailed,Toast.LENGTH_SHORT).show();
                return;
            }

            JSONObject object = response.getJSONObject(0);

            String message = object.getString("Message");
            if(message.equals("Success")){
                PrefUtils.setUserDetails(object);
                Intent intent = new Intent(LoginActivity.this,MapsActivity.class);
                startActivity(intent);
                finish();
            }else{
                Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}

