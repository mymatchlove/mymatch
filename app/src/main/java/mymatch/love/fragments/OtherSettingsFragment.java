package mymatch.love.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import mymatch.love.R;
import mymatch.love.utility.AppConstants;
import mymatch.love.utility.AppDebugLog;
import mymatch.love.utility.Common;
import mymatch.love.utility.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class OtherSettingsFragment extends Fragment implements RadioGroup.OnCheckedChangeListener {
    private TextView lbl_contact_visi;
    private Common common;
    private RadioGroup rgBirthDate,rgBirthTime,rgSalary,rgHoroscope,rgBioData,rgNumber,rgEmail;
    private SessionManager session;
    private RelativeLayout loader;

    public OtherSettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_other_settings, container, false);

        common = new Common(getActivity());
        session = new SessionManager(getActivity());

        loader = view.findViewById(R.id.loader);
        lbl_contact_visi = view.findViewById(R.id.lbl_contact_visi);
//        common.setDrawableLeftTextViewLeft(R.drawable.eye_pink, lbl_contact_visi);
        rgBirthDate = view.findViewById(R.id.rgBirthDate);
        rgBirthTime = view.findViewById(R.id.rgBirthTime);
        rgSalary = view.findViewById(R.id.rgSalary);
        rgHoroscope = view.findViewById(R.id.rgHoroscope);
        rgBioData = view.findViewById(R.id.rgBioData);
        rgNumber = view.findViewById(R.id.rgNumber);
        rgEmail = view.findViewById(R.id.rgEmail);

//        rgBirthDate.setOnCheckedChangeListener(this);
        rgBirthDate.setOnCheckedChangeListener(null);
        rgBirthTime.setOnCheckedChangeListener(null);
        rgSalary.setOnCheckedChangeListener(null);
        rgHoroscope.setOnCheckedChangeListener(null);
        rgBioData.setOnCheckedChangeListener(null);
        rgNumber.setOnCheckedChangeListener(null);
        rgEmail.setOnCheckedChangeListener(null);

        getMyProfile();

        return view;
    }

    private void getMyProfile() {
        common.showProgressRelativeLayout(loader);

        HashMap<String, String> param = new HashMap<>();
        param.put("member_id", session.getLoginData(SessionManager.KEY_USER_ID));

        common.makePostRequest(AppConstants.get_my_profile, param, response -> {
            common.hideProgressRelativeLayout(loader);
            //  Log.d("resp",response);
            try {
                JSONObject object = new JSONObject(response);
                session.setUserData(SessionManager.TOKEN, object.getString("tocken"));
                if (object.getString("status").equals("success")) {
                    JSONObject data = object.getJSONObject("data");

                    String birthdate_view_status = data.getString("birthdate_view_status");
                    String birthtime_view_status = data.getString("birthtime_view_status");
                    String salary_view_status = data.getString("salary_view_status");
                    String horoscope_view_status = data.getString("horoscope_view_status");
                    String biodata_view_status = data.getString("biodata_view_status");
                    String number_view_status = data.getString("number_view_status");
                    String email_view_status = data.getString("email_view_status");
                    setRadioGroup(rgBirthDate,birthdate_view_status);
                    setRadioGroup(rgBirthTime,birthtime_view_status);
                    setRadioGroup(rgSalary,salary_view_status);
                    setRadioGroup(rgHoroscope,horoscope_view_status);
                    setRadioGroup(rgBioData,biodata_view_status);
                    setRadioGroup(rgNumber,number_view_status);
                    setRadioGroup(rgEmail,email_view_status);

                    rgBirthDate.setOnCheckedChangeListener(this);
                    rgBirthTime.setOnCheckedChangeListener(this);
                    rgSalary.setOnCheckedChangeListener(this);
                    rgHoroscope.setOnCheckedChangeListener(this);
                    rgBioData.setOnCheckedChangeListener(this);
                    rgNumber.setOnCheckedChangeListener(this);
                    rgEmail.setOnCheckedChangeListener(this);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> common.hideProgressRelativeLayout(loader));
    }

    private void setRadioGroup(RadioGroup radioGroup,String status) {
        if (status.equals("0")) {
            ((RadioButton) radioGroup.getChildAt(0)).setChecked(true);
        } else if (status.equals("1")) {
            ((RadioButton) radioGroup.getChildAt(2)).setChecked(true);
        } else if (status.equals("2")) {
            ((RadioButton) radioGroup.getChildAt(1)).setChecked(true);
        }
    }

    private void changeSettingRequest(int radioGroupPosition) {
        common.showProgressRelativeLayout(loader);
        HashMap<String, String> param = new HashMap<>();
        param.put("matri_id", session.getLoginData(SessionManager.KEY_MATRI_ID));

        String url;

        switch (radioGroupPosition) {
            case 1:
                url = AppConstants.birth_date_setting;
                getParams(param, rgBirthDate.findViewById(rgBirthDate.getCheckedRadioButtonId()), "birthdate_view_status");
                break;
            case 2:
                url = AppConstants.birth_time_setting;
                getParams(param,rgBirthTime.findViewById(rgBirthTime.getCheckedRadioButtonId()),"birthtime_view_status");
                break;
            case 3:
                url = AppConstants.salary_setting;
                getParams(param,rgSalary.findViewById(rgSalary.getCheckedRadioButtonId()),"salary_view_status");
                break;
            case 4:
                url = AppConstants.horocope_setting;
                getParams(param,rgHoroscope.findViewById(rgHoroscope.getCheckedRadioButtonId()),"horoscope_view_status");
                break;
            case 5:
                url = AppConstants.biodata_setting;
                getParams(param,rgBioData.findViewById(rgBioData.getCheckedRadioButtonId()),"biodata_view_status");
                break;
            case 6:
                url = AppConstants.number_setting;
                getParams(param,rgNumber.findViewById(rgNumber.getCheckedRadioButtonId()),"number_view_status");
                break;
            case 7:
                url = AppConstants.email_setting;
                getParams(param,rgEmail.findViewById(rgEmail.getCheckedRadioButtonId()),"email_view_status");
                break;
            default:
                url = AppConstants.birth_date_setting;
                getParams(param, rgBirthDate.findViewById(rgBirthDate.getCheckedRadioButtonId()), "birthdate_view_status");
                break;
        }

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

    private void getParams(HashMap<String, String> param, RadioButton btn, String key) {
        if (btn.getText().toString().equals(getString(R.string.lbl_hide_for_all))) {
            param.put(key, "0");
        } else if (btn.getText().toString().equals(getString(R.string.lbl_only_to_interest_sent_amp_interest_accepted))) {
            param.put(key, "2");
        } else if (btn.getText().toString().equals(getString(R.string.lbl_open_to_all))) {
            param.put(key, "1");
        }
    }

    @Override public void onCheckedChanged(RadioGroup radioGroup, int i) {
        switch (radioGroup.getId()) {
            case R.id.rgBirthDate:
                changeSettingRequest(1);
                break;
            case R.id.rgBirthTime:
                changeSettingRequest(2);
                break;
            case R.id.rgSalary:
                changeSettingRequest(3);
                break;
            case R.id.rgHoroscope:
                changeSettingRequest(4);
                break;
            case R.id.rgBioData:
                changeSettingRequest(5);
                break;
            case R.id.rgNumber:
                changeSettingRequest(6);
                break;
            case R.id.rgEmail:
                changeSettingRequest(7);
                break;
            default:
                AppDebugLog.print("issue in onCheckedChanged");
                break;
        }
    }

}
