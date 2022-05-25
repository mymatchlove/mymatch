package mymatch.love.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import mymatch.love.R;
import mymatch.love.application.MyApplication;
import mymatch.love.custom.TouchImageView;
import mymatch.love.dynamicprofile.ItemClickListener;
import mymatch.love.dynamicprofile.SectionedExpandableLayoutHelper;
import mymatch.love.dynamicprofile.ViewProfileFieldsBean;
import mymatch.love.dynamicprofile.ViewProfileSectionBean;
import mymatch.love.utility.AppConstants;
import mymatch.love.utility.AppDebugLog;
import mymatch.love.utility.Common;
import mymatch.love.utility.SessionManager;

import com.rajat.pdfviewer.PdfViewerActivity;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class OtherUserProfileActivity extends AppCompatActivity implements View.OnClickListener, TabHost.OnTabChangeListener, ItemClickListener {
    private final String TAB_FIRST = "first";
    private final String TAB_SECOND = "second";
    private final int TAB_FIRST_POSITION = 0;
    private final int TAB_SECOND_POSITION = 1;
    private final int PERMISSION_REQUEST_CODE = 122;
    private Common common;
    private SessionManager session;
    private RelativeLayout loader;
    private TabHost host;
    private TextView tv_basic_label_pref, tv_photo_count;
    private RecyclerView userProfileRecyclerView;
    private List<ViewProfileSectionBean> userProfileDataList = new ArrayList<>();
    private int placeHolder, photoProtectPlaceHolder;
    private LinearLayout lay_basic_pref;
    private ImageView imgProfile;
    private JSONArray photo_arr;
    private String other_id, other_matri_id;
    private boolean isProtected = false;
    private SectionedExpandableLayoutHelper myProfileSectionedExpandableLayoutHelper;

    private NestedScrollView scrollView;
    private TextView txtFocus;

    private boolean isVisibleViewHoroscope, isVisibleViewBiodata;
    private String horoscopeUrl = "", biodataUrl = "";

    private ImageView fabCall, fabBlock, fabShortlist, fabMessage, fabSendInterest;
//    private FloatingActionsMenu fabMenu;
//    private FloatingActionButton fabCall, fabLike, fabBlock, fabShortlist, fabMessage, fabSendInterest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionManager(this);
        common = new Common(this);

        if (!session.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivityNew.class));
            finish();
        }
        setContentView(R.layout.activity_other_user_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("User Profile");
        toolbar.setNavigationOnClickListener(view -> finish());

        tv_photo_count = findViewById(R.id.tv_photo_count);
        tv_basic_label_pref = findViewById(R.id.tv_basic_label_pref);
        lay_basic_pref = findViewById(R.id.lay_basic_pref);

        imgProfile = findViewById(R.id.imgProfile);
        userProfileRecyclerView = findViewById(R.id.userProfileRecyclerView);

        scrollView = findViewById(R.id.scrollView);
        txtFocus = findViewById(R.id.txtFocus);

        fabCall = findViewById(R.id.fabCall);
        fabBlock = findViewById(R.id.fabBlock);
        fabShortlist = findViewById(R.id.fabShortlist);
        fabMessage = findViewById(R.id.fabMessage);
        fabSendInterest = findViewById(R.id.fabSendInterest);

        fabCall.setOnClickListener(this);
        fabBlock.setOnClickListener(this);
        fabShortlist.setOnClickListener(this);
        fabMessage.setOnClickListener(this);
        fabSendInterest.setOnClickListener(this);

        if (session.getLoginData(SessionManager.KEY_GENDER).equals("Female")) {
            photoProtectPlaceHolder = R.drawable.photo_protected;
            placeHolder = R.drawable.male;
        } else if (session.getLoginData(SessionManager.KEY_GENDER).equals("Male")) {
            photoProtectPlaceHolder = R.drawable.photo_protected;
            placeHolder = R.drawable.female;
        }
        imgProfile.setImageResource(placeHolder);

        Bundle b = getIntent().getExtras();
        if (b != null && b.containsKey("other_id")) {
            other_id = b.getString("other_id");
        }

        host = findViewById(R.id.tabHost);
        host.setup();
        TabHost.TabSpec spec = host.newTabSpec(TAB_FIRST);
        spec.setContent(R.id.tab1);
        spec.setIndicator("My Profile");
        host.addTab(spec);

        spec = host.newTabSpec(TAB_SECOND);
        spec.setContent(R.id.tab2);
        spec.setIndicator("Preferences");
        host.addTab(spec);

        host.setCurrentTab(TAB_FIRST_POSITION);
        host.getTabWidget().getChildAt(TAB_FIRST_POSITION).setBackgroundResource(R.drawable.tab_selector);
        host.getTabWidget().getChildAt(TAB_SECOND_POSITION).setBackgroundResource(R.drawable.tabunselcolor);

        for (int i = 0; i < host.getTabWidget().getChildCount(); i++) {
            TextView tv = host.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            common.setDrawableLeftTextViewLefttab(R.drawable.user_fill_pink, tv);
            tv.setTextColor(Color.parseColor("#2b2767"));
        }

        TextView tv = host.getTabWidget().getChildAt(TAB_FIRST_POSITION).findViewById(android.R.id.title);
        common.setDrawableLeftTextViewLefttab(R.drawable.user_fill_pink, tv);

        TextView tv1 = host.getTabWidget().getChildAt(TAB_SECOND_POSITION).findViewById(android.R.id.title);
        common.setDrawableLeftTextViewLefttab(R.drawable.user_pink, tv1);

        setTextViewDrawableColor(tv, R.color.colorAccent);
        setTextViewDrawableColor(tv1, R.color.colorAccent);

        host.setOnTabChangedListener(this);

        loader = findViewById(R.id.loader);
        tv_photo_count = findViewById(R.id.tv_photo_count);
        tv_photo_count.setOnClickListener(this);

        imgProfile.setOnClickListener(view -> {
            if (isProtected) {
                alertPhotoPassword();
            } else {
                if (photo_arr.length() != 0) {
                    Intent intent = new Intent(getApplicationContext(), GallaryNewActivity.class);
                    intent.putExtra("imagePosition", 0);
                    intent.putExtra("imageArray", photo_arr.toString());
                    startActivity(intent);
                }
            }
        });
        getMyProfile();
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle b = intent.getExtras();
        if (b != null && b.containsKey("other_id")) {
            if (!other_id.equals(b.getString("other_id"))) {
                other_id = b.getString("other_id");
                getMyProfile();
            }
        }
    }

    private void getMyProfile() {
        common.showProgressRelativeLayout(loader);

        HashMap<String, String> param = new HashMap<>();
        param.put("member_id", other_id);
        param.put("user_id", session.getLoginData(SessionManager.KEY_USER_ID));

        common.makePostRequest(AppConstants.other_user_profile, param, response -> {
            AppDebugLog.print("profile response in other user profile : " + response);
            common.hideProgressRelativeLayout(loader);
            try {
                JSONObject object = new JSONObject(response);
                session.setUserData(SessionManager.TOKEN, object.getString("tocken"));
                if (object.getString("status").equals("success")) {
                    JSONObject data = object.getJSONObject("data");
                    if (data.has("contact_viewed"))
                        isProfileViewed = data.getString("contact_viewed");
                    //tv_photo_count
                    JSONArray fileds = data.getJSONArray("fileds");
                    photo_arr = fileds.getJSONObject(fileds.length() - 1).getJSONArray("value");
                    if (photo_arr.length() >= 2)
                        tv_photo_count.setText((photo_arr.length() - 1) + "+");
                    else
                        tv_photo_count.setVisibility(View.GONE);

                    Gson gson = new GsonBuilder().setDateFormat("MMM dd, yyyy hh:mm:ss a").create();
                    JsonObject jsonObject = new JsonParser().parse(response).getAsJsonObject();
                    JsonObject dataObject = jsonObject.getAsJsonObject("data");

                    String photo_view_status = dataObject.get("photo_view_status").getAsString();
                    String photo_view_count = dataObject.get("photo_view_count").getAsString();

                    //if (!ApplicationData.isImageRatioSet) {
                    common.setImage(photo_view_count, photo_view_status, data.getString("photo1_approve"),
                            data.getString("photo1"), imgProfile, null, 0);

                    //}
//                    if (photo_view_status.equals("0")) {
//                        if (photo_view_count.equals("0")) {
//                            isProtected = true;
//                            imgProfile.setImageResource(photoProtectPlaceHolder);
//                        } else if (photo_view_count.equals("1")) {
//                            if (!data.getString("photo1_approve").equals("UNAPPROVED")) {
//                                Picasso.get().load(data.getString("photo1")).into(imgProfile);
//                            } else {
//                                imgProfile.setImageResource(placeHolder);
//                            }
//                        }
//                    }

                    TextView tv = host.getTabWidget().getChildAt(TAB_FIRST_POSITION).findViewById(android.R.id.title);
                    tv.setText(data.getString("username"));

                    other_matri_id = data.getString("matri_id");
                    JSONObject action = data.getJSONArray("action").getJSONObject(0);
                    forCallMobileNo = data.getString("mobile");
                    forWhatsAppMobileNo = data.getString("whatsapp_number");

                    if (data.has("horoscope_photo_url") && data.getString("horoscope_photo_url").length() > 0) {
                        horoscopeUrl = data.getString("horoscope_photo_url");
                        isVisibleViewHoroscope = true;
                    }
                    if (data.has("bio_data_url") && data.getString("bio_data_url").length() > 0) {
                        isVisibleViewBiodata = true;
                        biodataUrl = data.getString("bio_data_url");
                    }
                    invalidateOptionsMenu();

                    //TODO add my profile data dynamically
                    userProfileDataList = gson.fromJson(dataObject.getAsJsonArray("fileds"), new TypeToken<List<ViewProfileSectionBean>>() {
                    }.getType());
                    //Remove photo url list
                    userProfileDataList.remove(userProfileDataList.size() - 1);

                    userProfileRecyclerView.setNestedScrollingEnabled(false);
                    myProfileSectionedExpandableLayoutHelper = new SectionedExpandableLayoutHelper(this, userProfileRecyclerView, this, 2, false);
                    for (ViewProfileSectionBean viewProfileSectionBean : userProfileDataList) {
                        if (!viewProfileSectionBean.getId().equalsIgnoreCase("contact_info")) {
                            viewProfileSectionBean.setExpanded(true);
                        }
                        myProfileSectionedExpandableLayoutHelper.addSection(viewProfileSectionBean, viewProfileSectionBean.getViewProfileFieldList());
                    }
                    myProfileSectionedExpandableLayoutHelper.notifyDataSetChanged();
                    //TODO add my profile data dynamically

                    JSONArray partners_field = data.getJSONArray("partners_field");
                    displayPref(partners_field);

                    try {
                        if (action.getInt("is_block") == 1) {
                            fabBlock.setImageResource(R.drawable.ban);
                            fabBlock.setTag(1);
                        } else {
                            fabBlock.setImageResource(R.drawable.ban_gry);
                            fabBlock.setTag(0);
                        }

                        if (action.getInt("is_shortlist") == 1) {
                            fabShortlist.setTag(1);
                            fabShortlist.setImageResource(R.drawable.star_fill_yellow);

                        } else {
                            fabShortlist.setTag(0);
                            fabShortlist.setImageResource(R.drawable.star_gray_fill);
                        }

                        if (!action.getString("is_interest").equals("")) {
                            fabSendInterest.setTag(1);
                            fabSendInterest.setImageResource(R.drawable.check_fill_green);
                        } else {
                            fabSendInterest.setTag(0);
                            fabSendInterest.setImageResource(R.drawable.check_gray_fill);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (object.getString("status").equals("warning")) {
                    common.showToast("Your profile viewed count has been not available,Please upgrade your membership.");
                    startActivity(new Intent(OtherUserProfileActivity.this, PlanListActivity.class));
                    finish();
                } else if (object.getString("status").equals("error")) {
                    if (object.has("errmessage")) {
                        common.showToast(object.getString("errmessage"));
                        finish();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                common.showToast(getString(R.string.err_msg_try_again_later));
            }
        }, error -> {
            common.hideProgressRelativeLayout(loader);
            if (error.networkResponse != null) {
                common.showToast(Common.getErrorMessageFromErrorCode(error.networkResponse.statusCode));
            }
        });
    }

    private void displayPref(JSONArray partners_field) throws JSONException {
        JSONArray array = partners_field.getJSONObject(0).getJSONArray("value");
        lay_basic_pref.removeAllViews();
        int pref = 0;
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            String title = obj.getString("title");
            String type = obj.getString("type");
            String value = obj.getString("value");

            LinearLayout main = new LinearLayout(this);
            main.setOrientation(LinearLayout.HORIZONTAL);
            main.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            main.setBackgroundResource(R.drawable.underline_gray);

            LinearLayout submain = new LinearLayout(this);
            submain.setOrientation(LinearLayout.VERTICAL);
            submain.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            TextView textView1 = new TextView(this);
            textView1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            textView1.setPadding(20, 10, 10, 10);
            textView1.setText(title);
            textView1.setTextSize(16f);
            submain.addView(textView1);

            TextView textView2 = new TextView(this);
            textView2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            textView2.setTextColor(Color.BLACK);
            textView2.setTypeface(Typeface.DEFAULT_BOLD);

            String prefValue = "N/A";
            if (value != null && value.length() > 0) {
                prefValue = value;
            } else {
                prefValue = "N/A";
            }
            textView2.setText(prefValue);
            textView2.setPadding(20, 10, 10, 10);
            textView2.setTextSize(15f);
            submain.addView(textView2);

            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 4);
            param.setMargins(0, 20, 0, 0);
            LinearLayout submain1 = new LinearLayout(this);
            submain1.setOrientation(LinearLayout.VERTICAL);
            submain1.setGravity(Gravity.CENTER);
            submain1.setLayoutParams(param);

            main.addView(submain);
            if (type.equals("Yes")) {
                pref = pref + 1;
                ImageView img = new ImageView(this);
                img.setImageResource(R.drawable.check_fill_green);
                img.setLayoutParams(new LinearLayout.LayoutParams(50, 50));

                submain1.addView(img);
                main.addView(submain1);
            }
            lay_basic_pref.addView(main);
        }

        tv_basic_label_pref.setText(" You match " + pref + " out of 9 Preferences");
        String mainTitle = partners_field.getJSONObject(0).getString("name");
        if (mainTitle != null && mainTitle.length() > 0) {
            tv_basic_label_pref.setText(mainTitle);
        }
    }

    private void alertPhotoPassword() {
        final String[] arr = new String[]{"We found your profile to be a good match. Please accept photo password request to proceed further.",
                "I am interested in your profile. I would like to view photo now, accept photo request."};
        final String[] selected = {"We found your profile to be a good match. Please accept photo password request to proceed further."};
        AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);

        alt_bld.setTitle("Photos View Request");
        alt_bld.setSingleChoiceItems(arr, 0, (dialog, item) -> {
            //dialog.dismiss();// dismiss the alertbox after chose option
            selected[0] = arr[item];
        });
        alt_bld.setPositiveButton("Send", (dialogInterface, i) -> sendRequest(selected[0]));
        alt_bld.setNegativeButton("Cancel", (dialogInterface, i) -> {
            //alertpassword(password,url);
        });
        AlertDialog alert = alt_bld.create();
        alert.show();
    }

    private void sendRequest(String int_msg) {
        common.showProgressRelativeLayout(loader);

        HashMap<String, String> param = new HashMap<>();
        param.put("interest_message", int_msg);
        param.put("receiver_id", other_matri_id);
        param.put("requester_id", session.getLoginData(SessionManager.KEY_MATRI_ID));

        common.makePostRequest(AppConstants.photo_password_request, param, response -> {
            common.hideProgressRelativeLayout(loader);
            try {
                JSONObject object = new JSONObject(response);
                Toast.makeText(getApplicationContext(), object.getString("errmessage"), Toast.LENGTH_LONG).show();

            } catch (JSONException e) {
                e.printStackTrace();
                common.showToast(getString(R.string.err_msg_try_again_later));
            }
        }, error -> {
            common.hideProgressRelativeLayout(loader);
            if (error.networkResponse != null) {
                common.showToast(Common.getErrorMessageFromErrorCode(error.networkResponse.statusCode));
            }
        });

    }

    private boolean isNormalCall = false;
    private String forCallMobileNo = "";
    private String forWhatsAppMobileNo = "";

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_photo_count:
                if (!isProtected && photo_arr.length() > 0) {
                    Intent intent = new Intent(getApplicationContext(), GallaryNewActivity.class);
                    intent.putExtra("imagePosition", 0);
                    intent.putExtra("imageArray", photo_arr.toString());
                    startActivity(intent);
                }
                break;

            case R.id.fabCall:
                if (forCallMobileNo.length() > 0) {
                    if (!MyApplication.getPlan()) {
                        startActivity(new Intent(this, PlanListActivity.class));
                        common.showToast("Please upgrade your membership to Call/Whatsapp.");
                    } else if (!MyApplication.getIsApproved().equalsIgnoreCase("APPROVED")) {
                        common.showDialog(OtherUserProfileActivity.this, MyApplication.getIsApproved(), MyApplication.getIsApprovedPos());
                    } else {
                        AlertDialog.Builder alertConfirmViewContactDetails = new AlertDialog.Builder(this);
                        alertConfirmViewContactDetails.setTitle("Connect with");
                        alertConfirmViewContactDetails.setMessage("Please select option from below to connect");
                        alertConfirmViewContactDetails.setPositiveButton("Call", (dialogInterface, i) -> {
                            isNormalCall = true;
                            showContactDeductConfirm();
                        });
                        if (!forWhatsAppMobileNo.equalsIgnoreCase("")) {
                            alertConfirmViewContactDetails.setNegativeButton("Whatsapp", (dialogInterface, i) -> {
                                isNormalCall = false;
                                showContactDeductConfirm();
                            });
                        }
                        AlertDialog alert = alertConfirmViewContactDetails.create();
                        alert.show();
                    }
                } else {
                    Common.showToast("Mobile number is not available");
                }

                break;
            case R.id.fabBlock:
                int blockTag = (int) fabBlock.getTag();
                if (blockTag == 1) {
                    blockRequest("remove", other_matri_id);
                } else {
                    blockRequest("add", other_matri_id);
                }
                break;
            case R.id.fabShortlist:
                int shortlistTag = (int) fabShortlist.getTag();
                if (shortlistTag == 1) {
                    shortlistRequest("remove", other_matri_id);
                } else {
                    shortlistRequest("add", other_matri_id);
                }
                break;
            case R.id.fabMessage:
                if (!MyApplication.getPlan()) {
                    common.showToast("Please upgrade your membership to chat with this member.");
                    startActivity(new Intent(this, PlanListActivity.class));
                } else if (!MyApplication.getIsApproved().equalsIgnoreCase("APPROVED")) {
                    common.showDialog(OtherUserProfileActivity.this, MyApplication.getIsApproved(), MyApplication.getIsApprovedPos());
                } else {
                    Intent i = new Intent(this, ConversationActivity.class);
                    i.putExtra("matri_id", other_matri_id);
                    startActivity(i);
                }
                break;
            case R.id.fabSendInterest:
                int interestTag = (int) fabSendInterest.getTag();
                if (interestTag == 1) {
                    common.showToast("You already sent interest to this user.");
                } else {
                    fabSendInterest.setPressed(false);
                    LayoutInflater inflater1 = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                    final View vv = inflater1.inflate(R.layout.bottom_sheet_interest, null, true);
                    //context.getLayoutInflater().inflate(R.layout.bottom_sheet_interest, null);
                    final RadioGroup grp_interest = vv.findViewById(R.id.grp_interest);

                    final BottomSheetDialog dialog = new BottomSheetDialog(this);
                    dialog.setContentView(vv);
                    dialog.show();

                    Button send = vv.findViewById(R.id.btn_send_intr);
                    send.setOnClickListener(view12 -> {
                        dialog.dismiss();
                        if (grp_interest.getCheckedRadioButtonId() != -1) {
                            RadioButton btn = vv.findViewById(grp_interest.getCheckedRadioButtonId());
                            interestRequest(other_matri_id, btn.getText().toString().trim());
                        }
                    });
                }
                break;
        }
    }


    @Override
    public void onTabChanged(String tabId) {
        switch (tabId) {
            case TAB_FIRST:
                host.getTabWidget().getChildAt(TAB_FIRST_POSITION).setBackgroundResource(R.drawable.tab_selector);
                host.getTabWidget().getChildAt(TAB_SECOND_POSITION).setBackgroundResource(R.drawable.tabunselcolor);

                TextView tv = host.getTabWidget().getChildAt(TAB_FIRST_POSITION).findViewById(android.R.id.title);
                TextView tv1 = host.getTabWidget().getChildAt(TAB_SECOND_POSITION).findViewById(android.R.id.title);

                common.setDrawableLeftTextViewLefttab(R.drawable.user_fill_pink, tv);
                common.setDrawableLeftTextViewLefttab(R.drawable.user_pink, tv1);

                setTextViewDrawableColor(tv, R.color.colorAccent);
                setTextViewDrawableColor(tv1, R.color.colorAccent);
                break;
            case TAB_SECOND:
                host.getTabWidget().getChildAt(TAB_FIRST_POSITION).setBackgroundResource(R.drawable.tabunselcolor);
                host.getTabWidget().getChildAt(TAB_SECOND_POSITION).setBackgroundResource(R.drawable.tab_selector);

                TextView tv2 = host.getTabWidget().getChildAt(TAB_FIRST_POSITION).findViewById(android.R.id.title);
                TextView tv3 = host.getTabWidget().getChildAt(TAB_SECOND_POSITION).findViewById(android.R.id.title);

                common.setDrawableLeftTextViewLefttab(R.drawable.user_pink, tv2);
                common.setDrawableLeftTextViewLefttab(R.drawable.user_fill_pink, tv3);

                setTextViewDrawableColor(tv2, R.color.colorAccent);
                setTextViewDrawableColor(tv3, R.color.colorAccent);
                break;
        }
    }

    @Override
    public void itemClicked(ViewProfileFieldsBean item) {
    }

    @Override
    public void itemClicked(ViewProfileSectionBean section) {
    }

    @Override
    public void lastSectionExpand(ViewProfileSectionBean section) {
        if (section.getId().equalsIgnoreCase("contact_info")) {
            AppDebugLog.print("section id in itemClicked : " + section.getId());
            new Handler().postDelayed(() -> {
                txtFocus.requestFocus();
                int height = (section.getViewProfileFieldList().size() / 2) * Common.convertDpToPixels(100, this);
                scrollView.scrollTo(0, scrollView.getBottom() + height);
            }, 100);
        } else {
            txtFocus.clearFocus();
        }
    }

    private String isProfileViewed = "";

    @Override
    public void viewContact(ViewProfileSectionBean section) {
        if (isProfileViewed == "0") {
            AlertDialog.Builder alertConfirmViewContactDetails = new AlertDialog.Builder(this);
            alertConfirmViewContactDetails.setTitle("Contact Details");
            alertConfirmViewContactDetails.setMessage("This action will deduct by one contact view count, are you sure want continue?");
            alertConfirmViewContactDetails.setPositiveButton("Yes", (dialogInterface, i) -> viewContactRequest(section));
            alertConfirmViewContactDetails.setNegativeButton("No", (dialogInterface, i) -> {
            });
            AlertDialog alert = alertConfirmViewContactDetails.create();
            alert.show();
        } else {
            viewContactRequest(section);
        }
    }

    private void viewContactRequest(ViewProfileSectionBean section) {
        common.showProgressRelativeLayout(loader);

        HashMap<String, String> param = new HashMap<>();
        param.put("matri_id", session.getLoginData(SessionManager.KEY_MATRI_ID));
        param.put("receiver_matri_id", other_matri_id);

        common.makePostRequest(AppConstants.view_contact, param, response -> {
            common.hideProgressRelativeLayout(loader);
            Log.d("resp", response);
            try {
                JSONObject object = new JSONObject(response);
                if (object.getString("success").equals("success")) {
                    section.setContactVisible(true);
                    section.setExpanded(true);

                    new Handler().postDelayed(() -> {
                        txtFocus.requestFocus();
                        int height = (section.getViewProfileFieldList().size() / 2) * Common.convertDpToPixels(100, this);
                        scrollView.scrollTo(0, scrollView.getBottom() + height);
                    }, 100);
                    myProfileSectionedExpandableLayoutHelper.notifyDataSetChanged();
                } else
                    common.showToast(object.getString("errmessage"));

            } catch (JSONException e) {
                e.printStackTrace();
                common.showToast(getString(R.string.err_msg_try_again_later));
            }
        }, error -> {
            common.hideProgressRelativeLayout(loader);
            if (error.networkResponse != null) {
                common.showToast(Common.getErrorMessageFromErrorCode(error.networkResponse.statusCode));
            }
        });
    }

    private void setTextViewDrawableColor(TextView textView, int color) {
        for (Drawable drawable : textView.getCompoundDrawables()) {
            if (drawable != null) {
                drawable.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(textView.getContext(), color), PorterDuff.Mode.SRC_IN));
            }
        }
    }

    private void blockRequest(final String tag, String id) {
        common.showProgressRelativeLayout(loader);
        HashMap<String, String> param = new HashMap<>();
        param.put("matri_id", session.getLoginData(SessionManager.KEY_MATRI_ID));
        if (tag.equals("remove")) {
            param.put("unblockuserid", id);
        } else
            param.put("blockuserid", id);

        param.put("blacklist_action", tag);

        common.makePostRequestTime(AppConstants.block_user, param, response -> {
            common.hideProgressRelativeLayout(loader);
            try {
                JSONObject object = new JSONObject(response);
                if (tag.equals("add")) {
                    fabBlock.setImageResource(R.drawable.ban);
                    common.showAlert("Block", object.getString("errmessage"), R.drawable.ban);
                    fabBlock.setTag(1);
                } else {
                    fabBlock.setImageResource(R.drawable.ban_gry);
                    common.showAlert("Unblock", object.getString("errmessage"), R.drawable.ban_gry);
                    fabBlock.setTag(0);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                common.showToast(getString(R.string.err_msg_try_again_later));
            }
        }, error -> {
            common.hideProgressRelativeLayout(loader);
            if (error.networkResponse != null) {
                common.showToast(Common.getErrorMessageFromErrorCode(error.networkResponse.statusCode));
            }
        });
    }

    private void shortlistRequest(final String tag, String id) {
        common.showProgressRelativeLayout(loader);
        HashMap<String, String> param = new HashMap<>();
        param.put("matri_id", session.getLoginData(SessionManager.KEY_MATRI_ID));
        if (tag.equals("remove")) {
            param.put("shortlisteduserid", id);
        } else
            param.put("shortlistuserid", id);

        param.put("shortlist_action", tag);

        common.makePostRequestTime(AppConstants.shortlist_user, param, response -> {
            common.hideProgressRelativeLayout(loader);
            try {
                JSONObject object = new JSONObject(response);
                if (tag.equals("add")) {
                    fabShortlist.setTag(1);
                    fabShortlist.setImageResource(R.drawable.star_fill_yellow);
                    common.showAlert("Shortlist", object.getString("errmessage"), R.drawable.star_fill_yellow);
                } else {
                    fabShortlist.setTag(0);
                    fabShortlist.setImageResource(R.drawable.star_gray_fill);
                    common.showAlert("Remove From Shortlist", object.getString("errmessage"), R.drawable.star_gray_fill);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                common.showToast(getString(R.string.err_msg_try_again_later));
            }
        }, error -> {
            common.hideProgressRelativeLayout(loader);
            if (error.networkResponse != null) {
                common.showToast(Common.getErrorMessageFromErrorCode(error.networkResponse.statusCode));
            }
        });
    }

    private void interestRequest(String matri_id, String int_msg) {
        common.showProgressRelativeLayout(loader);

        HashMap<String, String> param = new HashMap<>();
        param.put("matri_id", session.getLoginData(SessionManager.KEY_MATRI_ID));
        param.put("receiver", matri_id);
        param.put("message", int_msg);

        common.makePostRequestTime(AppConstants.send_interest, param, response -> {
            common.hideProgressRelativeLayout(loader);
            //Log.d("resp",response);
            try {
                JSONObject object = new JSONObject(response);

                if (object.getString("status").equals("success")) {
                    fabSendInterest.setTag(1);
                    fabSendInterest.setImageResource(R.drawable.check_fill_green);
                    common.showAlert("Interest", object.getString("errmessage"), R.drawable.check_fill_green);
                } else {
                    fabSendInterest.setTag(0);
                    fabSendInterest.setImageResource(R.drawable.check_gray_fill);
                    common.showAlert("Interest", object.getString("errmessage"), R.drawable.check_fill_green);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                common.showToast(getString(R.string.err_msg_try_again_later));
            }
        }, error -> {
            common.hideProgressRelativeLayout(loader);
            if (error.networkResponse != null) {
                common.showToast(Common.getErrorMessageFromErrorCode(error.networkResponse.statusCode));
            }
        });
    }

    private void showContactDeductConfirm() {
        AlertDialog.Builder alertConfirmViewContactDetails = new AlertDialog.Builder(this);
        alertConfirmViewContactDetails.setTitle("Contact Details");
        alertConfirmViewContactDetails.setMessage("This action will deduct by one contact view count, are you sure want continue?");
        alertConfirmViewContactDetails.setPositiveButton("Yes", (dialogInterface, i) -> viewContactApi());
        alertConfirmViewContactDetails.setNegativeButton("No", (dialogInterface, i) -> {
        });
        AlertDialog alert = alertConfirmViewContactDetails.create();
        alert.show();
    }

    private void viewContactApi() {
        common.showProgressRelativeLayout(loader);

        HashMap<String, String> param = new HashMap<>();
        param.put("matri_id", session.getLoginData(SessionManager.KEY_MATRI_ID));
        param.put("receiver_matri_id", other_matri_id);

        common.makePostRequest(AppConstants.view_contact, param, response -> {
            common.hideProgressRelativeLayout(loader);
            Log.d("resp", response);
            try {
                JSONObject object = new JSONObject(response);
                if (object.getString("success").equals("success")) {
                    JSONObject object1 = new JSONObject(object.getString("contact_details"));
                    if (object1.has("mobile") && object1.getString("mobile") != null && object1.getString("mobile").length() > 0) {
                        if (isNormalCall) {
                            //open dialer
                            Intent intent = new Intent(Intent.ACTION_DIAL);
                            intent.setData(Uri.parse("tel:" + forCallMobileNo));
                            startActivity(intent);
                        } else {
                            // whatsapp call
                            String url = "https://api.whatsapp.com/send?phone=" + "+91" + forWhatsAppMobileNo;
                            try {
                                PackageManager pm = this.getPackageManager();
                                pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES);
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse(url));
                                intent.setPackage("com.whatsapp");
                                startActivity(intent);
                            } catch (PackageManager.NameNotFoundException e) {
                                try {
                                    PackageManager pm1 = this.getPackageManager();
                                    pm1.getPackageInfo("com.whatsapp.w4b", PackageManager.GET_ACTIVITIES);
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setData(Uri.parse(url));
                                    intent.setPackage("com.whatsapp.w4b");
                                    startActivity(intent);
                                    e.printStackTrace();
                                } catch (PackageManager.NameNotFoundException nameNotFoundException) {
                                    nameNotFoundException.printStackTrace();
                                    try {
                                        PackageManager pm1 = this.getPackageManager();
                                        pm1.getPackageInfo("com.gbwhatsapp", PackageManager.GET_ACTIVITIES);
                                        Intent intent = new Intent(Intent.ACTION_VIEW);
                                        intent.setData(Uri.parse(url));
                                        intent.setPackage("com.gbwhatsapp");
                                        startActivity(intent);
                                        e.printStackTrace();
                                    } catch (PackageManager.NameNotFoundException nameNotFoundException1) {
                                        nameNotFoundException1.printStackTrace();
                                        Toast.makeText(this, "Whatsapp app not installed in your phone", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                    } else {
                        common.showToast(object.getString("errmessage"));
                    }
                } else {
                    common.showToast(object.getString("errmessage"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                common.showToast(getString(R.string.err_msg_try_again_later));
            }
        }, error -> {
            common.hideProgressRelativeLayout(loader);
            if (error.networkResponse != null) {
                common.showToast(Common.getErrorMessageFromErrorCode(error.networkResponse.statusCode));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_other_user, menu);

        MenuItem itemHoroscope = menu.findItem(R.id.action_view_horoscope);
        MenuItem itemBiodata = menu.findItem(R.id.action_view_biodata);

        AppDebugLog.print("isVisibleViewHoroscope : " + isVisibleViewHoroscope);
        AppDebugLog.print("isVisibleViewBiodata : " + isVisibleViewBiodata);
        if (!isVisibleViewHoroscope && !isVisibleViewBiodata) {
            return false;
        } else {
            if (isVisibleViewHoroscope) itemHoroscope.setVisible(true);
            else itemHoroscope.setVisible(false);
            if (isVisibleViewBiodata) itemBiodata.setVisible(true);
            else itemBiodata.setVisible(false);
            return true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_view_horoscope) {
            viewFile(horoscopeUrl);
        } else if (item.getItemId() == R.id.action_view_biodata) {
            viewFile(biodataUrl);
        }
        return super.onOptionsItemSelected(item);
    }

    private void viewFile(String url) {
        String extension = url.substring(url.lastIndexOf("."));
        if (extension.equalsIgnoreCase(".pdf")) {
            startActivity(PdfViewerActivity.Companion.launchPdfFromUrl(
                    this,
                    url,
                    Common.getFileNameFromfilePath(url),
                    "",
                    false
            ));
//            requestPermission(url);
        } else {
            final Dialog dialog = new Dialog(this);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setContentView(R.layout.show_image_alert);
            TouchImageView img_url = dialog.findViewById(R.id.img_url);
            Picasso.get().load(url).placeholder(R.drawable.placeholder).error(R.drawable.placeholder).into(img_url);
            dialog.show();
        }
    }

    @AfterPermissionGranted(122)
    private void requestPermission(String url) {
        if (!checkPermission()) {
            // Ask for one permission
            EasyPermissions.requestPermissions(
                    this,
                    "This needs permission to use feature. You can grant them in app settings.",
                    PERMISSION_REQUEST_CODE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
        } else {
            PdfViewerActivity.Companion.launchPdfFromUrl(
                    this,
                    url,
                    Common.getFileNameFromfilePath(url),
                    "",
                    false
            );
        }
    }

    //TODO Permission related
    private boolean checkPermission() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE};

        if (EasyPermissions.hasPermissions(this, perms)) {
            return true;
        } else {
            return false;
        }
    }
}
