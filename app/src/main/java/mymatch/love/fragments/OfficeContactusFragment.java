package mymatch.love.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import mymatch.love.utility.AppConstants;
import mymatch.love.utility.Common;
import mymatch.love.R;
import mymatch.love.utility.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Objects;

public class OfficeContactusFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private RelativeLayout loader;
    private TextView tv_mobile, tv_address, tv_email;
    private Common common;
    private SessionManager session;
    private Context context;
    private String addres = "";
    ImageView whatsapp_icon, mail_icon;

    public OfficeContactusFragment() {
        // Required empty public constructor
    }

    public static OfficeContactusFragment newInstance(String param1, String param2) {
        OfficeContactusFragment fragment = new OfficeContactusFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_office_contactus, container, false);

        common = new Common(getActivity());
        session = new SessionManager(getActivity());
        context = getActivity();

        loader = view.findViewById(R.id.loader);
        whatsapp_icon = view.findViewById(R.id.whatsapp_icon);
        mail_icon = view.findViewById(R.id.mail_icon);
        tv_mobile = view.findViewById(R.id.tv_mobile);
        tv_address = view.findViewById(R.id.tv_address);
        tv_email = view.findViewById(R.id.tv_email);

        getData();
        return view;
    }

    private void getData() {
        common.showProgressRelativeLayout(loader);

        common.makePostRequest(AppConstants.site_data, new HashMap<String, String>(), response -> {
            Log.d("resp", response);
            common.hideProgressRelativeLayout(loader);
            try {
                JSONObject object = new JSONObject(response);
                JSONObject config_data = object.getJSONObject("config_data");

                tv_address.setText(Html.fromHtml(config_data.getString("full_address")));
                tv_email.setText(config_data.getString("contact_email"));
                tv_mobile.setText(config_data.getString("contact_no"));

                addres = config_data.getString("map_address");

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> common.hideProgressRelativeLayout(loader));


        whatsapp_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://api.whatsapp.com/send?phone=" + "+91" + tv_mobile.getText().toString();
                try {
                    PackageManager pm = requireActivity().getPackageManager();
                    pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    intent.setPackage("com.whatsapp");
                    startActivity(intent);
                } catch (PackageManager.NameNotFoundException e) {
                    try {
                        PackageManager pm1 = requireActivity().getPackageManager();
                        pm1.getPackageInfo("com.whatsapp.w4b", PackageManager.GET_ACTIVITIES);
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        intent.setPackage("com.whatsapp.w4b");
                        startActivity(intent);
                        e.printStackTrace();
                    } catch (PackageManager.NameNotFoundException nameNotFoundException) {
                        nameNotFoundException.printStackTrace();
                        try {
                            PackageManager pm1 = requireActivity().getPackageManager();
                            pm1.getPackageInfo("com.gbwhatsapp", PackageManager.GET_ACTIVITIES);
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(url));
                            intent.setPackage("com.gbwhatsapp");
                            startActivity(intent);
                            e.printStackTrace();
                        } catch (PackageManager.NameNotFoundException nameNotFoundException1) {
                            nameNotFoundException1.printStackTrace();
                            Toast.makeText(requireActivity(), "Whatsapp app not installed in your phone", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
        mail_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
//                    sendEmail(tv_email.getText().toString());
//                    startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + tv_email.getText().toString())));
                    Intent mailIntent = new Intent(Intent.ACTION_VIEW);
                    Uri data = Uri.parse("mailto:?subject=" + "Regarding MyMatch.Love" + "&body=" + " " + "&to=" + tv_email.getText().toString());
                    mailIntent.setData(data);
                    startActivity(Intent.createChooser(mailIntent, "Send mail..."));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected void sendEmail(String s) {
        Log.i("Send email", "");

        String[] TO = {s};
//        String[] CC = {"xyz@gmail.com"};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");

        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
//        emailIntent.putExtra(Intent.EXTRA_CC, CC);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Regarding MyMatch.Love");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "");

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getActivity(),
                    "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }
}
