package mymatch.love.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import mymatch.love.R;
import mymatch.love.utility.AppConstants;
import mymatch.love.utility.Common;
import mymatch.love.utility.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class PushNotificationSettingsActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    private SessionManager session;
    private Common common;
    private Switch switchViewProfile, switchSendMsg, switchSendInterest, switchRecommendations;
    private RelativeLayout loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_notification_settings);
        init();
    }

    private void init() {
        common = new Common(this);
        session = new SessionManager(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Push Notification Settings");

        loader = findViewById(R.id.loader);

        switchViewProfile = findViewById(R.id.switchViewProfile);
        switchSendMsg = findViewById(R.id.switchSendMsg);
        switchSendInterest = findViewById(R.id.switchSendInterest);
        switchRecommendations = findViewById(R.id.switchRecommendations);

        getMyProfile();
    }

    private void getMyProfile() {
        common.showProgressRelativeLayout(loader);

        HashMap<String, String> param = new HashMap<>();
        param.put("member_id", session.getLoginData(SessionManager.KEY_USER_ID));

        common.makePostRequest(AppConstants.get_my_profile, param, response -> {
            common.hideProgressRelativeLayout(loader);
            try {
                JSONObject object = new JSONObject(response);
                session.setUserData(SessionManager.TOKEN, object.getString("tocken"));
                if (object.getString("status").equals("success")) {
                    JSONObject data = object.getJSONObject("data");

                    if (data.has("view_profile_notification") && data.getString("view_profile_notification").equals("1")) {
                        switchViewProfile.setChecked(true);
                    } else {
                        switchViewProfile.setChecked(false);
                    }
                    if (data.has("send_message_notification") && data.getString("send_message_notification").equals("1")) {
                        switchSendMsg.setChecked(true);
                    } else  {
                        switchSendMsg.setChecked(false);
                    }
                    if (data.has("send_interest_notification") && data.getString("send_interest_notification").equals("1")) {
                        switchSendInterest.setChecked(true);
                    } else {
                        switchSendInterest.setChecked(false);
                    }
                    if (data.has("recommendations_notification") && data.getString("recommendations_notification").equals("1")) {
                        switchRecommendations.setChecked(true);
                    } else {
                        switchRecommendations.setChecked(false);
                    }

                    switchViewProfile.setOnCheckedChangeListener(this);
                    switchSendMsg.setOnCheckedChangeListener(this);
                    switchSendInterest.setOnCheckedChangeListener(this);
                    switchRecommendations.setOnCheckedChangeListener(this);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> common.hideProgressRelativeLayout(loader));
    }

    private void changeNotificationRequest(String url,String key,String val) {
        common.showProgressRelativeLayout(loader);
        HashMap<String, String> param = new HashMap<>();
        param.put("matri_id", session.getLoginData(SessionManager.KEY_MATRI_ID));
        param.put(key, val);

        common.makePostRequest(url, param, response -> {
            common.hideProgressRelativeLayout(loader);
            try {
                JSONObject object = new JSONObject(response);
                common.showToast(object.getString("errmessage"));
                if (object.getString("status").equals("success")) {

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> common.hideProgressRelativeLayout(loader));

    }

    @Override public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        String val = b ? "1" : "0";
        switch (compoundButton.getId()) {
            case R.id.switchViewProfile:
                changeNotificationRequest(AppConstants.view_profile_noti_setting,"view_profile_notification",val);
                break;
            case R.id.switchSendMsg:
                changeNotificationRequest(AppConstants.send_message_noti_setting,"send_message_notification",val);
                break;
            case R.id.switchSendInterest:
                changeNotificationRequest(AppConstants.send_interest_setting,"send_interest_notification",val);
                break;
            case R.id.switchRecommendations:
                changeNotificationRequest(AppConstants.recommendation_setting,"recommendations_notification",val);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}