package mymatch.love.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Response;

import mymatch.love.R;
import mymatch.love.activities.OtherUserProfileActivity;
import mymatch.love.activities.ShortlistedProfileActivity;
import mymatch.love.adapter.ShortListAdapter;
import mymatch.love.application.MyApplication;
import mymatch.love.custom.TouchImageView;
import mymatch.love.model.DashboardItem;
import mymatch.love.utility.AppConstants;
import mymatch.love.utility.ApplicationData;
import mymatch.love.utility.Common;
import mymatch.love.utility.SessionManager;

import com.like.LikeButton;
import com.like.OnLikeListener;

import mymatch.love.model.DashboardItem;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ShortlistedFragment extends Fragment implements ShortListAdapter.ItemListener {

    private View view;
    private RecyclerView recyclerView;
    private List<DashboardItem> list;
    private Common common;
    private SessionManager session;
    private RelativeLayout loader;
    private boolean continue_request;
    private TextView tv_no_data;
    private ShortListAdapter adapter;
    private int page = 1;

    private int currentVisibleItemCount;
    private int currentFirstVisibleItem;
    private int totalItem;

    public ShortlistedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_shortlisted, container, false);
        list = new ArrayList<>();
        init();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            if (list != null)
                list.clear();
            getListData(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() {
        common = new Common(getActivity());
        session = new SessionManager(getActivity());

        loader = view.findViewById(R.id.loader);
        recyclerView = view.findViewById(R.id.recyclerView);
        tv_no_data = view.findViewById(R.id.tv_no_data);

        initializeRecyclerView();
        try {
            if (list != null)
                list.clear();
            getListData(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeRecyclerView() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(requireActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        adapter = new ShortListAdapter(requireActivity(), list);
        adapter.setListener(this);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) { //check for scroll down
                    currentVisibleItemCount = mLayoutManager.getChildCount();
                    totalItem = mLayoutManager.getItemCount();
                    currentFirstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

                    if (continue_request) {
                        if ((currentVisibleItemCount + currentFirstVisibleItem) >= totalItem) {
                            continue_request = false;
                            if (loader != null)
                                common.hideProgressRelativeLayout(loader);
                            page = page + 1;
                            getListData(page);
                        }
                    }
                }
            }
        });
    }

    private void getListData(int page) {
        common.showProgressRelativeLayout(loader);

        HashMap<String, String> param = new HashMap<>();
        param.put("matri_id", session.getLoginData(SessionManager.KEY_MATRI_ID));
        param.put("member_id", session.getLoginData(SessionManager.KEY_USER_ID));

        Log.d("resp", "" + list.size());

        common.makePostRequestTime(AppConstants.shortlist_profile + page, param, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                common.hideProgressRelativeLayout(loader);
                Log.d("resp Short list", response);
                try {
                    JSONObject object = new JSONObject(response);
                    int total_count = object.getInt("total_count");
                    if (total_count != 0) {
                        tv_no_data.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        continue_request = object.getBoolean("continue_request");
                        if (list.size() != total_count) {
                            JSONArray data = object.getJSONArray("data");
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject obj = data.getJSONObject(i);

                                DashboardItem item = new DashboardItem();
                                item.setId(obj.getString("id"));
                                item.setMatri_id(obj.getString("matri_id"));
                                item.setName(obj.getString("username"));
                                item.setUserName(obj.getString("username"));
//                                item.setOccupation_name(obj.getString("occupation_name"));
                                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                                String description = Common.getDetailsFromValue(obj.getString("profileby"), common.getAge(obj.getString("birthdate"), sdf) + " Years ", obj.getString("height"),
                                        "", obj.getString("caste_name"), obj.getString("religion_name"),
                                        obj.getString("state_name"), obj.getString("country_name"), obj.getString("education_name"));

                                item.setAbout(description);
                                item.setImage_approval(obj.getString("photo1_approve"));
                                item.setImage(obj.getString("photo1"));
                                item.setUser_id(obj.getString("user_id"));
                                item.setPhoto_view_status(obj.getString("photo_view_status"));
                                //item.setPhoto_view_count(obj.getString("photo_view_count"));
                                item.setBadge(obj.getString("badge"));
                                item.setBadgeUrl(obj.getString("badgeUrl"));
                                item.setColor(obj.getString("color"));
                                item.setPhotoUrl(obj.getString("photoUrl"));
                                item.setId_proof_approve(obj.getString("id_proof_approve"));
                                Log.d("resp", obj.getString("photo_view_status") + "  " + obj.getString("matri_id") +
                                        "    " + obj.getString("photo1") + "   " + obj.getString("photo1_approve"));
                                list.add(item);
                            }
                            if (list.size() < 10) continue_request = false;
                            adapter.notifyDataSetChanged();
                        }

                    } else {
                        tv_no_data.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    common.showToast(getString(R.string.err_msg_try_again_later));
                }
            }
        }, error -> {
            common.hideProgressRelativeLayout(loader);
            if (error.networkResponse != null) {
                common.showToast(Common.getErrorMessageFromErrorCode(error.networkResponse.statusCode));
            }
        });

    }

    private void sendRequest(String int_msg, String matri_id) {
        common.showProgressRelativeLayout(loader);

        HashMap<String, String> param = new HashMap<>();
        param.put("interest_message", int_msg);
        param.put("receiver_id", matri_id);
        param.put("requester_id", session.getLoginData(SessionManager.KEY_MATRI_ID));

        common.makePostRequestTime(AppConstants.photo_password_request, param, response -> {
            common.hideProgressRelativeLayout(loader);
            try {
                JSONObject object = new JSONObject(response);
                Toast.makeText(requireActivity(), object.getString("errmessage"), Toast.LENGTH_LONG).show();

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
    public void itemClicked(DashboardItem object, int position) {

    }

    @Override
    public void alertPhotoPassword(final String matri_id) {
        final String[] arr = new String[]{"We found your profile to be a good match. Please accept photo password request to proceed further.",
                "I am interested in your profile. I would like to view photo now, accept photo request."};
        final String[] selected = {"We found your profile to be a good match. Please accept photo password request to proceed further."};
        AlertDialog.Builder alt_bld = new AlertDialog.Builder(requireActivity());

        alt_bld.setTitle("Photos View Request");
        alt_bld.setSingleChoiceItems(arr, 0, (dialog, item) -> {

            //dialog.dismiss();// dismiss the alertbox after chose option
            selected[0] = arr[item];
        });
        alt_bld.setPositiveButton("Send", (dialogInterface, i) -> sendRequest(selected[0], matri_id));
        alt_bld.setNegativeButton("Cancel", (dialogInterface, i) -> {
            //alertpassword(password,url);
        });
        AlertDialog alert = alt_bld.create();
        alert.show();

    }

    @Override
    public void removeShortlist(final int position, String id) {
        common.showProgressRelativeLayout(loader);

        HashMap<String, String> param = new HashMap<>();
        param.put("matri_id", session.getLoginData(SessionManager.KEY_MATRI_ID));
        param.put("shortlist_action", "remove");
        param.put("shortlisteduserid", id);

        common.makePostRequestTime(AppConstants.shortlist_user, param, response -> {
            common.hideProgressRelativeLayout(loader);
            try {
                JSONObject object = new JSONObject(response);

                if (object.getString("status").equals("success")) {
                    ApplicationData.getSharedInstance().isProfileChanged = true;
                    common.showAlert("Remove From Shortlist", object.getString("errmessage"), R.drawable.star_gray_fill);
                    list.remove(position);
                    if (list.size() == 0) {
                        tv_no_data.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                    adapter.notifyDataSetChanged();
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
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            onResume();
        }
    }
}