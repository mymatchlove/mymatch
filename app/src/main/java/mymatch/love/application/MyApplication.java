package mymatch.love.application;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.firebase.FirebaseApp;

import mymatch.love.fcm.NotificationChannels;
import mymatch.love.utility.FontsOverride;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

public class MyApplication extends Application {

    public static final String TAG = MyApplication.class.getSimpleName();

    private RequestQueue mRequestQueue;

    private static MyApplication mInstance;
    private static JSONObject spinData;
    private static boolean isApprove = true;
    private static String isApproved = "";
    private static int isApprovedPos = 0;
    private static boolean is_plan = false;
    private Activity currentActivity;
    public static boolean isFromAddedReview = false;


    private static Context context;

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        mInstance = this;
        setupActivityListener();
        //TODO Fixed Picasso error
        setPicassoSingleInstance();

        FontsOverride.overrideFont(getApplicationContext(), "SERIF", "font/roboto_regular.ttf");

        FirebaseApp.initializeApp(context);

        NotificationChannels notificationChannels = new NotificationChannels();
        notificationChannels.createNotificationChannels(context);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.METHOD, "new event");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    public static Context getAppContext() {
        //AppDebugLog.print("context : " + context);
        return context;
    }

    public void setPicassoSingleInstance() {
        try {
            Picasso.setSingletonInstance(new Picasso.Builder(context).build());
        } catch (Exception e) {

        }
    }

    private void setupActivityListener() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                currentActivity = activity;
            }

            @Override
            public void onActivityStarted(Activity activity) {
                currentActivity = activity;
            }

            @Override
            public void onActivityResumed(Activity activity) {
                currentActivity = activity;
            }

            @Override
            public void onActivityPaused(Activity activity) {
                currentActivity = null;
            }

            @Override
            public void onActivityStopped(Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
            }
        });
    }

    public static Context getContext() {
        return context;
    }


    public ViewGroup getCurrentView() {
        // ActivityManager am = (ActivityManager)getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        //omponentName cn = am.getRunningTasks(1).get(0).topActivity;

        // Log.e("resp",cn.toString());
        return currentActivity.getWindow().getDecorView().findViewById(android.R.id.content);
    }

    public Activity getCurrentActivity() {
        return this.currentActivity;
    }

    public static boolean isApprove() {
        return isApprove;
    }

    public static void setApprove(boolean approve) {
        isApprove = approve;
    }

    public static String getIsApproved() {
        return isApproved;
    }

    public static void setIsApproved(String isApproved) {
        MyApplication.isApproved = isApproved;
    }

    public static int getIsApprovedPos() {
        return isApprovedPos;
    }

    public static void setIsApprovedPos(int isApprovedPos) {
        MyApplication.isApprovedPos = isApprovedPos;
    }

    public static JSONObject getSpinData() {
        return spinData;
    }

    public static void setSpinData(JSONObject spinData) {

        try {
            MyApplication.spinData = spinData.getJSONObject("data");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void setPlan(boolean is_plan) {
        MyApplication.is_plan = is_plan;
    }

    public static boolean getPlan() {
        return is_plan;
    }

    public static synchronized MyApplication getInstance() {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

}
