package mymatch.love.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;

import mymatch.love.R;
import mymatch.love.utility.AppConstants;
import mymatch.love.utility.Common;
import mymatch.love.utility.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class PushNotificationSettingsFragment extends Fragment implements CompoundButton.OnCheckedChangeListener{

    private View view;
    private SessionManager session;
    private Common common;
    private Switch switchViewProfile, switchSendMsg, switchSendInterest, switchRecommendations;
    private RelativeLayout loader;

    public PushNotificationSettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_push_notification_settings, container, false);
        init();
        return view;
    }

    private void init() {
        common = new Common(requireActivity());
        session = new SessionManager(requireActivity());

        loader = view.findViewById(R.id.loader);

        switchViewProfile = view.findViewById(R.id.switchViewProfile);
        switchSendMsg = view.findViewById(R.id.switchSendMsg);
        switchSendInterest = view.findViewById(R.id.switchSendInterest);
        switchRecommendations = view.findViewById(R.id.switchRecommendations);

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
}