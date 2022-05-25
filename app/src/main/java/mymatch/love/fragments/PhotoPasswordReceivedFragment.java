package mymatch.love.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

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

import com.loopeer.shadow.ShadowView;

import mymatch.love.activities.OtherUserProfileActivity;
import mymatch.love.activities.PlanListActivity;
import mymatch.love.application.MyApplication;
import mymatch.love.custom.TouchImageView;
import mymatch.love.model.PhotoPasswordBean;
import mymatch.love.R;
import mymatch.love.utility.AppConstants;
import mymatch.love.utility.AppDebugLog;
import mymatch.love.utility.Common;
import mymatch.love.utility.SessionManager;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class PhotoPasswordReceivedFragment extends Fragment {
    private ListView lv_exp_receive;
    private Context context;
    private List<PhotoPasswordBean> list = new ArrayList<>();
    private Received_Adapter adapter;
    private Common common;
    private SessionManager session;
    private RelativeLayout loader;
    private boolean continue_request;
    private TextView tv_no_data;
    private int page = 0;
    private boolean isCreate = false;
    private int placeHolder, photoProtectPlaceHolder;

    public PhotoPasswordReceivedFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isCreate = true;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_password_received, container, false);

        context = getActivity();
        common = new Common(getActivity());
        session = new SessionManager(getActivity());

        if (session.getLoginData(SessionManager.KEY_GENDER).equals("Female")) {
            photoProtectPlaceHolder = R.drawable.photopassword_male;
            placeHolder = R.drawable.male;
        } else if (session.getLoginData(SessionManager.KEY_GENDER).equals("Male")) {
            photoProtectPlaceHolder = R.drawable.photopassword_female;
            placeHolder = R.drawable.female;
        }

        loader = view.findViewById(R.id.loader);

        lv_exp_receive = view.findViewById(R.id.lv_exp_receive);
        tv_no_data = view.findViewById(R.id.tv_no_data);

        page = page + 1;
        getListData(page);

        adapter = new Received_Adapter(getActivity(), list);
        lv_exp_receive.setAdapter(adapter);

        lv_exp_receive.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int currentVisibleItemCount;
            private int currentFirstVisibleItem;
            private int totalItem;

            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (totalItem - currentFirstVisibleItem == currentVisibleItemCount
                        && scrollState == SCROLL_STATE_IDLE) {
                    if (continue_request) {
                        page = page + 1;
                        getListData(page);
                    }
                }
            }

            public void onScroll(AbsListView view, int firstVisibleItemm, int visibleItemCountt, int totalItemCountt) {
                this.currentFirstVisibleItem = firstVisibleItemm;
                this.currentVisibleItemCount = visibleItemCountt;
                this.totalItem = totalItemCountt;
            }
        });

        return view;
    }

    private void getListData(int page) {
        common.showProgressRelativeLayout(loader);

        HashMap<String, String> param = new HashMap<>();
        param.put("matri_id", session.getLoginData(SessionManager.KEY_MATRI_ID));

        common.makePostRequest(AppConstants.photo_pass_request_received + page, param, response -> {
            common.hideProgressRelativeLayout(loader);
            AppDebugLog.print("resp : " + response);
            try {
                JSONObject object = new JSONObject(response);
                continue_request = object.getBoolean("continue_request");
                int total_count = object.getInt("total_count");
                if (total_count != 0) {
                    tv_no_data.setVisibility(View.GONE);
                    lv_exp_receive.setVisibility(View.VISIBLE);
                    if (total_count != list.size()) {
                        JSONArray data = object.getJSONArray("data");
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject obj = data.getJSONObject(i);
                            PhotoPasswordBean item = new PhotoPasswordBean();
                            item.setId(obj.getString("ph_reqid"));
                            item.setMatri_id(obj.getString("ph_requester_id"));
                            item.setAbout(obj.getString("ph_msg"));
                            item.setUser_id(obj.getString("user_id"));
                            item.setReceiver_response(obj.getString("receiver_response"));
                            item.setPh_receiver_id(obj.getString("ph_receiver_id"));
                            item.setPh_requester_id(obj.getString("ph_requester_id"));

                            item.setPhoto_view_count(obj.getString("photo_view_count"));
                            item.setPhoto_view_status(obj.getString("photo_view_status"));
                            item.setImage_approval(obj.getString("photo1_approve"));
                            item.setBadge(obj.getString("badge"));
                            item.setBadgeUrl(obj.getString("badgeUrl"));
                            item.setColor(obj.getString("color"));
                            item.setPhotoUrl(obj.getString("photoUrl"));
                            if (obj.has("photo1"))
                                item.setImage(obj.getString("photo1"));
                            item.setDate(getDate(obj.getString("ph_reqdate")));
                            list.add(item);
                        }
                        if (list.size() < 10) continue_request = false;
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    lv_exp_receive.setVisibility(View.GONE);
                    tv_no_data.setVisibility(View.VISIBLE);
                }
            } catch (JSONException e) {
                common.showToast(getString(R.string.err_msg_try_again_later));
                e.printStackTrace();
            }
        }, error -> {
            common.hideProgressRelativeLayout(loader);
            if (error.networkResponse != null) {
                common.showToast(Common.getErrorMessageFromErrorCode(error.networkResponse.statusCode));
            }
        });
    }

    private String getDate(String time) {
        String outputPattern = "MMM dd, yyyy";
        String inputPattern = "yyyy-MM-dd";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(time);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return str;
    }

    private class Received_Adapter extends ArrayAdapter<PhotoPasswordBean> {
        Context context;
        List<PhotoPasswordBean> list;

        public Received_Adapter(Context context, List<PhotoPasswordBean> list) {
            super(context, R.layout.photo_password, list);
            this.context = context;
            this.list = list;
        }

        public View getView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.photo_received_password, null, true);

            ShadowView cardView = rowView.findViewById(R.id.cardView);
            ImageView imgPLanStamp = rowView.findViewById(R.id.imgPLanStamp);
            ImageView img_profile = rowView.findViewById(R.id.img_profile);
            TextView tv_name = rowView.findViewById(R.id.tv_name);
            TextView tv_status = rowView.findViewById(R.id.tv_status);
            TextView tv_date = rowView.findViewById(R.id.tv_date);
            TextView tv_detail = rowView.findViewById(R.id.tv_detail);

            ImageView btnDelete = rowView.findViewById(R.id.btnDelete);
            ImageView btnInterest = rowView.findViewById(R.id.btnInterest);
            ImageView btnReject = rowView.findViewById(R.id.btnReject);

            PhotoPasswordBean item = list.get(position);
            tv_name.setText(item.getName());

            //if (!ApplicationData.isImageRatioSet) {
            common.setImage(item.getPhoto_view_count(), item.getPhoto_view_status(), item.getImage_approval(),
                    item.getPhotoUrl() + item.getImage(), img_profile, null, 0);
            //}

            tv_date.setText(item.getDate());
            tv_status.setText(item.getReceiver_response());

            tv_detail.setText(item.getAbout());

            if (item.getBadge().length() > 0) {
                Picasso.get().load(item.getBadgeUrl() + item.getBadge())
                        .placeholder(R.drawable.ic_transparent_placeholder)
                        .error(R.drawable.ic_transparent_placeholder)
                        .into(imgPLanStamp);
                imgPLanStamp.setVisibility(View.VISIBLE);
            } else {
                imgPLanStamp.setVisibility(View.GONE);
            }

            if (item.getColor().length() > 0) {
                cardView.setShadowColor(Color.parseColor("" + item.getColor()));
            }

            if (item.getReceiver_response().equalsIgnoreCase("Pending")) {
                btnDelete.setVisibility(View.VISIBLE);
            } else {
                btnDelete.setVisibility(View.GONE);
            }

            if (item.getReceiver_response().equalsIgnoreCase("Accepted")) {
                //btnInterest.setImageResource(R.drawable.check_fill_green);
                btnInterest.setColorFilter(getResources().getColor(R.color.colorPrimary));
            } else {
                btnInterest.setColorFilter(getResources().getColor(R.color.white));
                //btnInterest.setImageResource(R.drawable.check_gray_fill);
            }

            if (item.getReceiver_response().equalsIgnoreCase("Rejected")) {
                //btnReject.setImageResource(R.drawable.cancel_fill_red);
                btnReject.setColorFilter(getResources().getColor(R.color.colorPrimary));
            } else {
                //btnReject.setImageResource(R.drawable.cancel_fill_gray);
                btnReject.setColorFilter(getResources().getColor(R.color.white));
            }


            btnInterest.setOnClickListener(view13 -> {
                AppDebugLog.print("response : " + item.getReceiver_response());
                if (item.getReceiver_response().equalsIgnoreCase("Accepted")) {
                    common.showToast("You already Accepted Photo request of this user !!!");
                } else if (item.getReceiver_response().equalsIgnoreCase("Rejected")) {
                    statusChange("Accepted", item.getId());
                } else if (item.getReceiver_response().equalsIgnoreCase("Pending")) {
                    statusChange("Accepted", item.getId());
                }
            });

            btnReject.setOnClickListener(view14 -> {
                AppDebugLog.print("response : " + item.getReceiver_response());
                if (item.getReceiver_response().equalsIgnoreCase("Rejected")) {
                    common.showToast("You already Rejected Photo request of this user !!!");
                } else if (item.getReceiver_response().equalsIgnoreCase("Accepted")) {
                    statusChange("Rejected", item.getId());
                } else if (item.getReceiver_response().equalsIgnoreCase("Pending")) {
                    statusChange("Rejected", item.getId());
                }
            });

            btnDelete.setOnClickListener(view15 -> {
                deleteAlert(item.getId());
            });

            img_profile.setOnClickListener(view1 -> {
                if (item.getPhoto_view_status().equals("0") && item.getPhoto_view_count().equals("0")) {
                    alertPhotoPassword(item.getImage(), item.getMatri_id());
                } else if (item.getPhoto_view_status().equals("0") && item.getPhoto_view_count().equals("1") && item.getImage_approval().equals("APPROVED")) {
                    final Dialog dialog = new Dialog(context);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.setContentView(R.layout.show_image_alert);
                    TouchImageView img_url = dialog.findViewById(R.id.img_url);
                    Picasso.get().load(item.getImage()).into(img_url);
                    dialog.show();
                } else {
                    if (!MyApplication.getPlan()) {
                        common.showToast("Please upgrade your membership to view this profile.");
                        context.startActivity(new Intent(context, PlanListActivity.class));
                    } else if (!MyApplication.getIsApproved().equalsIgnoreCase("APPROVED")) {
                        common.showDialog(context, MyApplication.getIsApproved(), MyApplication.getIsApprovedPos());
                    } else {
                        Intent i = new Intent(context, OtherUserProfileActivity.class);
                        i.putExtra("other_id", item.getUser_id());
                        context.startActivity(i);
                    }
                }
            });

            return rowView;
        }

        private void alertPhotoPassword(final String url, final String matri_id) {
            final String[] arr = new String[]{"We found your profile to be a good match. Please accept photo password request to proceed further.",
                    "I am interested in your profile. I would like to view photo now, accept photo request."};
            final String[] selected = {"We found your profile to be a good match. Please accept photo password request to proceed further."};
            AlertDialog.Builder alt_bld = new AlertDialog.Builder(context);

            alt_bld.setTitle("Photos View Request");
            alt_bld.setSingleChoiceItems(arr, 0, (dialog, item) -> {
                // dismiss the alertbox after chose option
                selected[0] = arr[item];
            });
            alt_bld.setPositiveButton("Send", (dialogInterface, i) -> sendRequest(selected[0], matri_id));
            alt_bld.setNegativeButton("Cancel", (dialogInterface, i) -> {
                //alertpassword(password,url);
            });
            AlertDialog alert = alt_bld.create();
            alert.show();

        }

        private void sendRequest(String int_msg, String matri_id) {
            common.showProgressRelativeLayout(loader);

            HashMap<String, String> param = new HashMap<>();
            param.put("interest_message", int_msg);
            param.put("receiver_id", matri_id);
            param.put("requester_id", session.getLoginData(SessionManager.KEY_MATRI_ID));

            common.makePostRequest(AppConstants.photo_password_request, param, response -> {
                common.hideProgressRelativeLayout(loader);
                try {
                    JSONObject object = new JSONObject(response);
                    Toast.makeText(context, object.getString("errmessage"), Toast.LENGTH_LONG).show();

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

    }

    private void statusChange(String tag, String id) {
        common.showProgressRelativeLayout(loader);

        HashMap<String, String> param = new HashMap<>();
        param.put("requester_id", id);
        param.put("status", tag);

        common.makePostRequest(AppConstants.reject_request, param, response -> {
            common.hideProgressRelativeLayout(loader);
            try {
                AppDebugLog.print("response : " + response);
                JSONObject object = new JSONObject(response);
                common.showToast(object.getString("response"));
                if (object.getString("status").equals("success")) {
                    list.clear();
                    page = 0;
                    page = page + 1;
                    getListData(page);
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

    private void deleteAlert(final String id) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle("Delete");
        alert.setMessage("Are you sure you want to delete this photo request?");
        alert.setNegativeButton("No", null);
        alert.setPositiveButton("Yes", (dialogInterface, i) -> {
            common.showProgressRelativeLayout(loader);

            HashMap<String, String> param = new HashMap<>();
            param.put("requester_id", id);
            param.put("status", "receiver");

            common.makePostRequest(AppConstants.delete_request, param, response -> {
                common.hideProgressRelativeLayout(loader);
                Log.d("resp", response);
                try {
                    JSONObject object = new JSONObject(response);
                    common.showToast(object.getString("response"));
                    if (object.getString("status").equals("success")) {
                        list.clear();
                        page = 0;
                        page = page + 1;
                        getListData(page);
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

        });
        alert.show();
    }

}

