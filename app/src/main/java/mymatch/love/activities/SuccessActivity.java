package mymatch.love.activities;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import mymatch.love.R;
import mymatch.love.network.ConnectionDetector;
import mymatch.love.utility.AppConstants;
import mymatch.love.utility.AppDebugLog;
import mymatch.love.utility.Common;
import mymatch.love.utility.SessionManager;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class SuccessActivity extends AppCompatActivity {
    private String LOGIN_TAG = "login";
    private MaterialButton btnLogin;//,btn_partner;
    private String ragistered_id;
    private AVLoadingIndicatorView progressBar;

    private Common common;
    private SessionManager session;

    private String mobileNumber = "", countryCode = "", latitude = "", longitude = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        common = new Common(this);
        session = new SessionManager(this);

        progressBar = findViewById(R.id.progressBar);
        btnLogin = findViewById(R.id.btnLogin);

        ragistered_id = getIntent().getStringExtra("ragistered_id");
        mobileNumber = getIntent().getStringExtra("mobile_number");
        countryCode = getIntent().getStringExtra("country_code");
        latitude = getIntent().getStringExtra("latitude");
        longitude = getIntent().getStringExtra("longitude");

        AppDebugLog.print("Narjis : " + ragistered_id + "\n" +
                mobileNumber + "\n" +
                countryCode + "\n" +
                latitude + "\n" +
                longitude);

        btnLogin.setOnClickListener(view -> {
            startActivity(new Intent(SuccessActivity.this, LoginActivityNew.class));
            finish();
        });

        new Handler().postDelayed(this::loginApi, 3000);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(SuccessActivity.this, LoginActivityNew.class));
        finish();
    }

    private void loginApi() {
        if (!ConnectionDetector.isConnectingToInternet(this)) {
            Toast.makeText(this, "Please check your internet connection!", Toast.LENGTH_LONG).show();
            return;
        }

        Common.hideSoftKeyboard(this);
        common.showProgressLayout(progressBar);

        HashMap<String, String> params = new HashMap<>();
        params.put("country_code", countryCode);
        params.put("mobile", mobileNumber);
        params.put("is_mobile_login", "Yes");
        if (latitude != null && longitude != null) {
            params.put("latitude", latitude);
            params.put("longitude", longitude);
        } else {
            params.put("latitude", "");
            params.put("longitude", "");
        }
        params.put("android_device_id", session.getLoginData(SessionManager.KEY_DEVICE_TOKEN));
        Log.d("resp", params.toString());

        common.makePostRequestWithTag(AppConstants.login, params, response -> {
            Log.d("resp", response);
            common.hideProgressLayout(progressBar);
            try {
                JSONObject object = new JSONObject(response);
                Toast.makeText(getApplicationContext(), object.getString("errmessage"), Toast.LENGTH_SHORT).show();
                session.setUserData(SessionManager.TOKEN, object.getString("token"));

                if (object.getString("status").equals("success")) {
                    JSONObject user_data = object.getJSONObject("user_data");

                    AppDebugLog.print("Gender in login : " + user_data.getString("gender"));
                    session.createLoginSession(user_data.getString("id"));
                    session.setUserData(SessionManager.KEY_EMAIL, user_data.getString("email"));
                    session.setUserData(SessionManager.KEY_username, user_data.getString("username"));
                    session.setUserData(SessionManager.KEY_GENDER, user_data.getString("gender"));
                    session.setUserData(SessionManager.KEY_MATRI_ID, user_data.getString("matri_id"));
                    session.setUserData(SessionManager.KEY_PLAN_STATUS, user_data.getString("plan_status"));
                    session.setUserData(SessionManager.LOGIN_WITH, "local");

                    startActivity(new Intent(SuccessActivity.this, DashboardActivity.class));
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                common.showToast(getString(R.string.err_msg_try_again_later));
            }

        }, error -> {
            Log.d("resp", error.getMessage() + "   ");
            common.hideProgressLayout(progressBar);
            if (error.networkResponse != null) {
                common.showToast(Common.getErrorMessageFromErrorCode(error.networkResponse.statusCode));
            }
        }, LOGIN_TAG);
    }
}
