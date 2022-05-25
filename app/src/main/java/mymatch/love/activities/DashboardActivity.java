package mymatch.love.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.drawable.LayerDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mymatch.love.BuildConfig;
import mymatch.love.R;
import mymatch.love.adapter.MenuListAdapter;
import mymatch.love.application.MyApplication;
import mymatch.love.fragments.DiscoverFragment;
import mymatch.love.fragments.NearMeFragment;
import mymatch.love.fragments.OTPRequestDialogWithFirebaseFragment;
import mymatch.love.fragments.ProfileIViewedFragment;
import mymatch.love.fragments.RecommendationFragment;
import mymatch.love.fragments.ShortlistedFragment;
import mymatch.love.model.MenuChildBean;
import mymatch.love.model.MenuGroupBean;
import mymatch.love.utility.AppConstants;
import mymatch.love.utility.AppDebugLog;
import mymatch.love.utility.ApplicationData;
import mymatch.love.utility.Common;
import mymatch.love.utility.SessionManager;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class DashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, EasyPermissions.PermissionCallbacks {
    private TextView tv_edit_profile, tv_name, tv_matri_id;
    private Common common;
    private SessionManager session;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private Button btn_member;
    boolean doubleBackToExitPressedOnce = false;
    private RelativeLayout loader;
    private ImageView imgProfile;
    private int placeHolder, photoProtectPlaceHolder;

    private ExpandableListView drawerExpandableListView;
    private List<MenuGroupBean> drawerListData;
    private List<MenuGroupBean> menuList = new ArrayList<>();
    private String notificationCountAll = "";
    private String notificationCountChat = "";
    private String notificationCountInterest = "";

    //location
    private final int TAG_PERMISSIONS = 124;
    private final int GPS_REQUEST = 333;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Location currentLocation;
    private LocationRequest locationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionManager(this);
        if (!session.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivityNew.class));
            finish();
        }
        setContentView(R.layout.activity_deshboard);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("HOME");
        setSupportActionBar(toolbar);

        common = new Common(this);

        if (session.getLoginData(SessionManager.KEY_GENDER).equals("Female")) {
            photoProtectPlaceHolder = R.drawable.photo_protected;
            placeHolder = R.drawable.female;
        } else if (session.getLoginData(SessionManager.KEY_GENDER).equals("Male")) {
            photoProtectPlaceHolder = R.drawable.photo_protected;
            placeHolder = R.drawable.male;
        }

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        loader = findViewById(R.id.loader);
        tv_edit_profile = headerView.findViewById(R.id.tv_edit_profile);
        tv_name = headerView.findViewById(R.id.tv_name);
        // tv_matri_id = headerView.findViewById(R.id.tv_matri_id);
        imgProfile = headerView.findViewById(R.id.img_profile);
        common.setDrawableLeftTextView(R.drawable.eye_white, tv_edit_profile);
        btn_member = findViewById(R.id.btn_member);
        getMyprofile();
        initMenu();

        viewPager = findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.setOffscreenPageLimit(5);

        btn_member.setOnClickListener(view -> {
            startActivity(new Intent(DashboardActivity.this, CurrentPlanActivity.class));
            drawer.closeDrawer(GravityCompat.START);
        });

        tv_edit_profile.setOnClickListener(view -> {
            startActivity(new Intent(DashboardActivity.this, ViewMyProfileActivity.class));
            drawer.closeDrawer(GravityCompat.START);
        });

    }

//    @Override
//    protected void onResume() {
//        super.onResume();
////        getMyprofile();
//    }

    private void getMyprofile() {
        HashMap<String, String> param = new HashMap<>();
        param.put("member_id", session.getLoginData(SessionManager.KEY_USER_ID));
        common.makePostRequest(AppConstants.get_my_profile, param, response -> {
            common.hideProgressRelativeLayout(loader);
            AppDebugLog.print("get_my_profile response : " + response.toString());
            try {
                JSONObject object = new JSONObject(response);
                JSONObject data = object.getJSONObject("data");
                setMyProfile(data);

                setToastMessages(data);
                getCurrentPlan();

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

    private void setToastMessages(JSONObject data) {
        try {
            if (data != null) {
                if (data.getString("photo1").equals("") && data.getString("id_proof").equals("")) {
                    MyApplication.setIsApprovedPos(1);
                    MyApplication.setIsApproved("Dear user, please upload your Profile Photo and ID Proof, so we can verify your profile and you can start getting the suggestions of your preferred profile.");
                } else if (data.getString("photo1").equals("") && !data.getString("id_proof").equals("")) {
                    MyApplication.setIsApprovedPos(2);
                    MyApplication.setIsApproved("Dear user, please upload your Profile Photo, so we can verify your profile and you can start getting the suggestions of your preferred profile.");
                } else if (!data.getString("photo1").equals("") && data.getString("id_proof").equals("")) {
                    MyApplication.setIsApprovedPos(3);
                    MyApplication.setIsApproved("Dear user, please upload your ID Proof, so we can verify your profile and you can start getting the suggestions of your preferred profile.");
                } else if (!data.getString("photo1_approve").equalsIgnoreCase("APPROVED") && !data.getString("id_proof_approve").equalsIgnoreCase("APPROVED")) {
                    MyApplication.setIsApprovedPos(4);
                    MyApplication.setIsApproved("Dear user, your ID Proof and Profile Photo are with admin. So, please wait till admin approves or connects with you.");
                } else if (data.getString("photo1_approve").equalsIgnoreCase("APPROVED") && !data.getString("id_proof_approve").equalsIgnoreCase("APPROVED")) {
                    MyApplication.setIsApprovedPos(5);
                    MyApplication.setIsApproved("Dear user, your Profile Photo is approved but your ID Proof is not approved by the admin try re-uploading the ID Proof with better clarity of the image.");
                } else if (!data.getString("photo1_approve").equalsIgnoreCase("APPROVED") && data.getString("id_proof_approve").equalsIgnoreCase("APPROVED")) {
                    MyApplication.setIsApprovedPos(6);
                    MyApplication.setIsApproved("Dear user, your ID Proof is approved but your Profile Photo is not approved by the admin try re-uploading the Profile Photo with better clarity of the image.");
                } else {
                    MyApplication.setIsApprovedPos(0);
                    MyApplication.setIsApproved("APPROVED");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getCurrentPlan() {
        common.showProgressRelativeLayout(loader);
        HashMap<String, String> param = new HashMap<>();
        param.put("matri_id", session.getLoginData(SessionManager.KEY_MATRI_ID));
        common.makePostRequest(AppConstants.check_plan, param, response -> {
            AppDebugLog.print("plan response : " + response);
            common.hideProgressRelativeLayout(loader);
            try {
                JSONObject object = new JSONObject(response);
                MyApplication.setPlan(object.getBoolean("is_show"));

            } catch (JSONException e) {
                common.hideProgressRelativeLayout(loader);
                e.printStackTrace();
            }
            //  pd.dismiss();
        }, error -> {
            common.hideProgressRelativeLayout(loader);
        });
    }

    public void setMyProfile(JSONObject data) {
        try {
            tv_name.setText(data.getString("username") + " (" + data.getString("matri_id") + ")");
            if (!data.getString("photo1").equals(""))
                Picasso.get().load(data.getString("photo1"))
                        .placeholder(placeHolder)
                        .error(placeHolder)
                        .fit()
                        .centerCrop(Gravity.TOP | Gravity.CENTER_HORIZONTAL)
                        .into(imgProfile);
            else {
                imgProfile.setImageResource(placeHolder);
            }

            if (data.has("notification_all") && data.getString("notification_all").length() > 0) {
                notificationCountAll = data.getString("notification_all");
                invalidateOptionsMenu();
            }

            if (data.has("notification_chat") && data.getString("notification_chat").length() > 0) {
                notificationCountChat = data.getString("notification_chat");
                invalidateOptionsMenu();
            }

            if (data.has("notification_interest") && data.getString("notification_interest").length() > 0) {
                notificationCountInterest = data.getString("notification_interest");
                invalidateOptionsMenu();
            }

            session.setUserData("full_mobile", data.getString("mobile"));
            if (!ApplicationData.isOTPProcess && data.has("mobile_verify_status")
                    && data.getString("mobile_verify_status").equalsIgnoreCase("No")) {
                ApplicationData.isOTPProcess = true;
                OTPRequestDialogWithFirebaseFragment dialogFragment = OTPRequestDialogWithFirebaseFragment.newInstance();
                dialogFragment.show(getSupportFragmentManager(), "OTP Dialog");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        //adapter.addFragment(dashboardFragment, "Dashboard");
        adapter.addFragment(new RecommendationFragment(), "Matches");
        adapter.addFragment(new DiscoverFragment(), "Recently Join");
        adapter.addFragment(new NearMeFragment(), "Near Me");
        adapter.addFragment(new ShortlistedFragment(), "Shortlisted");
        adapter.addFragment(new ProfileIViewedFragment(), "Recently Viewed");
        viewPager.setAdapter(adapter);
    }

    private void initMenu() {
        drawerExpandableListView = findViewById(R.id.list_menu);
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);

        Gson gson = new GsonBuilder().setDateFormat(AppConstants.GSONDateTimeFormat).create();
        menuList.clear();
        String jsonData = session.getDrawerMenuArrayObject();
        if (jsonData != null) {
            JsonObject jsonObject = JsonParser.parseString(jsonData).getAsJsonObject();
            menuList = gson.fromJson(jsonObject.getAsJsonArray("menu_arr"), new TypeToken<List<MenuGroupBean>>() {
            }.getType());
            AppDebugLog.print("menu_arr : " + menuList.size());
        } else {
            JsonObject jsonObject = JsonParser.parseString(AppConstants.DRAWER_MENU_DATA).getAsJsonObject();
            menuList = gson.fromJson(jsonObject.getAsJsonArray("menu_arr"), new TypeToken<List<MenuGroupBean>>() {
            }.getType());
        }

        drawerListData = menuList; //new ArrayList<>();
        AppDebugLog.print("drawerListData size : " + drawerListData.size());

        MenuListAdapter adapter = new MenuListAdapter(this, menuList);
        drawerExpandableListView.setAdapter(adapter);

        drawerExpandableListView.setOnChildClickListener((expandableListView, view, groupPosition, childPosition, l) -> {
            MenuChildBean menuChildBean = menuList.get(groupPosition).getDrawerChildList().get(childPosition);
            try {
                AppDebugLog.print("Group Menu Action : " + ("mymatch.love.activities." + menuChildBean.getSubMenuAction()));
                if (menuChildBean.getSubMenuAction().equalsIgnoreCase("CurrentLocationActivity")) {
                    updateLocation();
                } else if (menuChildBean.getSubMenuAction().equalsIgnoreCase("ReportBugActivity")) {
                    Intent intent = new Intent(this, ContactUsActivity.class);
                    intent.putExtra("sub_menu_title", "Report Bug / Issue");
                    startActivity(intent);
                } else {
                    Class<?> c = Class.forName("mymatch.love.activities." + menuChildBean.getSubMenuAction());
                    Intent intent = new Intent(this, c);
                    if (menuChildBean.getSubmenuTag() != null && menuChildBean.getSubmenuTag().length() > 0) {
                        intent.putExtra(AppConstants.KEY_INTENT, menuChildBean.getSubmenuTag());
                    }
                    if (menuChildBean.getSubMenuTitle() != null && menuChildBean.getSubMenuTitle().length() > 0) {
                        intent.putExtra("sub_menu_title", menuChildBean.getSubMenuTitle());
                    }
                    startActivity(intent);
                }
            } catch (ClassNotFoundException ignored) {
                ignored.printStackTrace();
                AppDebugLog.print("Activity not found");
            }
            drawer.closeDrawer(GravityCompat.START);
            return true;
        });

        drawerExpandableListView.setOnGroupClickListener((expandableListView, view, groupPosition, l) -> {
            MenuGroupBean groupBean = menuList.get(groupPosition);
            if (groupBean.getMenuAction() != null && groupBean.getMenuAction().length() > 0) {
                if (groupBean.getMenuAction().equalsIgnoreCase("Home")) {
                    //nothing
                } else if (groupBean.getMenuAction().equalsIgnoreCase("Logout")) {
                    conformLogout();
                } else if (groupBean.getMenuAction().equalsIgnoreCase("ShareApp")) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);
                } else {
                    try {
                        AppDebugLog.print("Group Menu Action : " + ("mymatch.love.activities." + groupBean.getMenuAction()));
                        Class<?> c = Class.forName("mymatch.love.activities." + groupBean.getMenuAction());
                        Intent intent = new Intent(this, c);
                        startActivity(intent);
                    } catch (ClassNotFoundException ignored) {
                        ignored.printStackTrace();
                        AppDebugLog.print("Activity not found");
                    }
                }
                drawer.closeDrawer(GravityCompat.START);
                return true;
            } else {
                return false;
            }
        });
    }

    private void conformLogout() {
        AlertDialog.Builder alert = new AlertDialog.Builder(DashboardActivity.this);
        alert.setTitle("Logout");
        alert.setMessage("Are you sure you want logout from this app?");
        alert.setPositiveButton("Logout", (dialogInterface, i) -> session.logoutUser());
        alert.setNegativeButton("Cancel", null);
        alert.show();
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (doubleBackToExitPressedOnce) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//***Change Here***
                startActivity(intent);
                finish();
                System.exit(0);
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(getApplicationContext(), "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_notifications, menu);

        MenuItem itemNotificationAll = menu.findItem(R.id.action_notifications_all);
        MenuItem itemNotificationChat = menu.findItem(R.id.action_notifications_chat);
        MenuItem itemNotificationInterest = menu.findItem(R.id.action_notifications_interest);

        LayerDrawable iconAll = (LayerDrawable) itemNotificationAll.getIcon();
        if (notificationCountAll.length() > 0) {
            Common.setBadgeCount(this, iconAll, notificationCountAll);
        } else {
            Common.setBadgeCount(this, iconAll, "0");
        }

        LayerDrawable iconChat = (LayerDrawable) itemNotificationChat.getIcon();
        if (notificationCountChat.length() > 0) {
            Common.setBadgeCount(this, iconChat, notificationCountChat);
        } else {
            Common.setBadgeCount(this, iconChat, "0");
        }

        LayerDrawable iconInterest = (LayerDrawable) itemNotificationInterest.getIcon();
        if (notificationCountInterest.length() > 0) {
            Common.setBadgeCount(this, iconInterest, notificationCountInterest);
        } else {
            Common.setBadgeCount(this, iconInterest, "0");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_notifications_all) {
            Intent i = new Intent(this, NotificationListActivity.class);
            i.putExtra("list_type", "All");
            startActivity(i);
        } else if (item.getItemId() == R.id.action_notifications_chat) {
//            Intent i = new Intent(this, NotificationListActivity.class);
//            i.putExtra("list_type", "Chat");
//            startActivity(i);
            Intent i = new Intent(this, ChatActivity.class);
//            i.putExtra("list_type", "Chat");
            startActivity(i);

        } else if (item.getItemId() == R.id.action_notifications_interest) {
            Intent i = new Intent(this, NotificationListActivity.class);
            i.putExtra("list_type", "Interest");
            startActivity(i);
        } else if (item.getItemId() == R.id.action_search) {
            startActivity(new Intent(this, SearchActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateLocation() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Update Location");
        alert.setMessage("This action set your current location for get better matches in near you.");
        alert.setPositiveButton("Set", (dialogInterface, i) -> getLocationAccess());
        alert.setNegativeButton("Cancel", null);
        alert.show();
    }

    @AfterPermissionGranted(TAG_PERMISSIONS)
    private void getLocationAccess() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (EasyPermissions.hasPermissions(this, perms)) {
            createLocationRequest();
        } else {
            // Do not have permissions, request them now
            ActivityCompat.requestPermissions(this, perms, TAG_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {

            }
            if (requestCode == GPS_REQUEST) {
                getCurrentLocationFromFusedLocationLibrary();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(locationSettingsResponse -> {
            getCurrentLocationFromFusedLocationLibrary();
        });

        task.addOnFailureListener(e -> {
            if (e instanceof ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                // Show the dialog by calling startResolutionForResult(),
                // and check the result in onActivityResult().
                try {
                    ((ResolvableApiException) e).startResolutionForResult(this, GPS_REQUEST);
                } catch (IntentSender.SendIntentException sendIntentException) {
                    sendIntentException.printStackTrace();
                }
            }
        });
    }

    private void getCurrentLocationFromFusedLocationLibrary() {
        //fusion library
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                if (locationResult.getLastLocation() != null) {
                    currentLocation = locationResult.getLastLocation();
                    AppDebugLog.print("narjis location : " + currentLocation.getLatitude() + "," + currentLocation.getLongitude());
                    //getMyprofile();
                    setCurrentLocation(currentLocation);
                    removeLocationUpdateCallback();
                }
            }
        };
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void removeLocationUpdateCallback() {
        if (fusedLocationClient != null)
            fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void setCurrentLocation(Location currentLocation) {
        if (currentLocation != null) {
            common.showProgressRelativeLayout(loader);
            HashMap<String, String> param = new HashMap<>();
            param.put("member_id", session.getLoginData(SessionManager.KEY_USER_ID));
            param.put("latitude", String.valueOf(currentLocation.getLatitude()));
            param.put("longitude", String.valueOf(currentLocation.getLongitude()));
            common.makePostRequest(AppConstants.update_location, param, response -> {
                AppDebugLog.print("update location response : " + response);
                common.hideProgressRelativeLayout(loader);
                try {
                    JSONObject object = new JSONObject(response);
                    if (object.has("status") && object.getString("status").equalsIgnoreCase("success")
                            && object.has("errormessage")) {
                        Toast.makeText(getApplicationContext(), object.getString("errormessage"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    common.hideProgressRelativeLayout(loader);
                    e.printStackTrace();
                }
                //  pd.dismiss();
            }, error -> {
                common.hideProgressRelativeLayout(loader);
            });
        }
    }
}
