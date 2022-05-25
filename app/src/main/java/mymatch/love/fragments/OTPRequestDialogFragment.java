package mymatch.love.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import mymatch.love.R;
import mymatch.love.utility.AppConstants;
import mymatch.love.utility.Common;
import mymatch.love.utility.SessionManager;
import com.mukesh.OtpView;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class OTPRequestDialogFragment extends DialogFragment {
    private static final String RESEND_OTP_REQUEST = "resend_otp_request";

    private Dialog dialog;
    private RelativeLayout layoutProgressBar;
    private View view;

    private Common common;
    private SessionManager session;

    private OtpView otpView;
    private TextView tvMobileNumber;
    private LinearLayout layoutMobileNumber;
    private CountryCodePicker spin_code;
    private EditText txtPhoneNumber;
    private TextView btnResendOTP;
    private String mVerificationId;

    public static OTPRequestDialogFragment newInstance() {
        return  new OTPRequestDialogFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.dialog_verify_otp, container, false);

        session = new SessionManager(getActivity());
        common = new Common(getActivity());

        Button btnVerify = view.findViewById(R.id.btnVerify);
        Button btnCancel = view.findViewById(R.id.btnVerifyCancel);
        tvMobileNumber = view.findViewById(R.id.tvMobileNumber);
        spin_code = view.findViewById(R.id.spin_code);
        layoutMobileNumber = view.findViewById(R.id.layoutMobileNumber);
        txtPhoneNumber = view.findViewById(R.id.txtPhoneNumber);
        btnResendOTP = view.findViewById(R.id.btnResendOTP);

        otpView = view.findViewById(R.id.otpView);

//        btnResend = view.findViewById(R.id.btnResend);
//        btnResend.setOnClickListener(view -> {
//        });

        otpView.setVisibility(View.GONE);
        tvMobileNumber.setVisibility(View.GONE);
        btnResendOTP.setVisibility(View.GONE);
        layoutMobileNumber.setVisibility(View.VISIBLE);

        btnVerify.setText("Send OTP");

        SessionManager session = new SessionManager(getActivity());
        tvMobileNumber.setText("OTP sent to "+session.getLoginData("full_mobile"));

        String[] arr_mob = session.getLoginData("full_mobile").split("-");

        if (arr_mob.length == 2) {
            txtPhoneNumber.setText(arr_mob[1]);
            spin_code.setCountryForPhoneCode(Integer.parseInt(arr_mob[0]));
        }

        otpView.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override public void afterTextChanged(Editable editable) {
                if(editable.length() == 6) {
                    showProgressLayout();
                    verifyPhoneNumberWithCode(mVerificationId, otpView.getText().toString());
                }
            }
        });

        btnVerify.setOnClickListener(view -> {
            if (dialog != null) {
                if(btnVerify.getText().toString().equalsIgnoreCase("Send OTP")) {
                    sendOTP(session.getLoginData("full_mobile"));
                }else{
                    if (otpView.length() == 6) {
                        showProgressLayout();
                        verifyPhoneNumberWithCode(mVerificationId, otpView.getText().toString());
                    }
                }
            }
        });

        btnCancel.setOnClickListener(view1 -> {
            dialog.dismiss();
        });

        btnResendOTP.setOnClickListener(view1 -> {
            showProgressLayout();

        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialog = super.onCreateDialog(savedInstanceState);

        dialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
        //request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        return dialog;
    }

    //Todo show progress layout
    private void showProgressLayout() {
        layoutProgressBar = view.findViewById(R.id.layoutProgressBar);
        layoutProgressBar.setVisibility(View.VISIBLE);
    }

    //Todo hide progress layout
    private void hideProgressLayout() {
        if (layoutProgressBar != null)
            layoutProgressBar.setVisibility(View.GONE);
    }

    private void sendOTP(String mobileNumber) {
        showProgressLayout();

    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {

    }


    private void setVerifyStatus() {
        showProgressLayout();
        HashMap<String, String> param = new HashMap<>();
        param.put("user_id", session.getLoginData(SessionManager.KEY_USER_ID));
        common.makePostRequest(AppConstants.set_mobile_verify, param, response -> {
            try {
                JSONObject object = new JSONObject(response);
                common.showToast(object.getString("errmessage"));
                hideProgressLayout();
                if(object.getString("status").equalsIgnoreCase("success")) {
                    dialog.dismiss();
                }
            } catch (JSONException e) {
                hideProgressLayout();
                e.printStackTrace();
                common.showToast(getString(R.string.err_msg_try_again_later));
            }
        }, error -> {
            hideProgressLayout();
            if (error.networkResponse != null) {
                common.showToast(Common.getErrorMessageFromErrorCode(error.networkResponse.statusCode));
            }
        });
    }
}
