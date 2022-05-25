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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.loopeer.shadow.ShadowView;

import mymatch.love.activities.OtherUserProfileActivity;
import mymatch.love.activities.PlanListActivity;
import mymatch.love.application.MyApplication;
import mymatch.love.custom.TouchImageView;
import mymatch.love.model.ExpressItem;
import mymatch.love.R;
import mymatch.love.utility.AppConstants;
import mymatch.love.utility.AppDebugLog;
import mymatch.love.utility.Common;
import mymatch.love.utility.SessionManager;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExpressReceivedFragment extends Fragment {
    private ListView lv_exp_receive;
    private TextView tv_no_data;
    private Spinner spin_exp_recv;
    private Context context;
    private List<ExpressItem> list = new ArrayList<>();
    private Received_Adapter adapter;
    private Common common;
    private SessionManager session;
    private RelativeLayout loader;
    private boolean continue_request;
    private int page = 0;
    private String tag = "all_receive";
    private boolean isCreate = false;
    private int placeHolder, photoProtectPlaceHolder;


    public ExpressReceivedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        common = new Common(getActivity());
        session = new SessionManager(getActivity());
        context = getActivity();
        isCreate = true;

        if (session.getLoginData(SessionManager.KEY_GENDER).equals("Female")) {
            photoProtectPlaceHolder = R.drawable.photopassword_male;
            placeHolder = R.drawable.male;
        } else if (session.getLoginData(SessionManager.KEY_GENDER).equals("Male")) {
            photoProtectPlaceHolder = R.drawable.photopassword_female;
            placeHolder = R.drawable.female;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_express_received, container, false);
        context = getActivity();

        loader = view.findViewById(R.id.loader);
        lv_exp_receive = view.findViewById(R.id.lv_exp_receive);
        spin_exp_recv = view.findViewById(R.id.spin_exp_recv);
        tv_no_data = view.findViewById(R.id.tv_no_data);

        List<String> lst = new ArrayList<>();
        lst.add("All Received Interest");
        lst.add("Interest Received Accept");
        lst.add("Interest Received Reject");
        lst.add("Interest Received Pending");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, lst);
        spin_exp_recv.setAdapter(arrayAdapter);

        spin_exp_recv.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                page = 0;
                page = page + 1;
                continue_request = true;
                list.clear();
                switch (position) {
                    case 0:
                        getData(page, "all_receive");
                        tag = "all_receive";
                        break;
                    case 1:
                        getData(page, "accept_receive");
                        tag = "accept_receive";
                        break;
                    case 2:
                        getData(page, "reject_receive");
                        tag = "reject_receive";
                        break;
                    case 3:
                        getData(page, "pending_receive");
                        tag = "pending_receive";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        lv_exp_receive.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int currentVisibleItemCount;
            private int currentFirstVisibleItem;
            private int totalItem;

            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (totalItem - currentFirstVisibleItem == currentVisibleItemCount
                        && scrollState == SCROLL_STATE_TOUCH_SCROLL) {
                    if (continue_request) {
                        page = page + 1;
                        getData(page, tag);
                        common.hideProgressRelativeLayout(loader);
                    }
                }
            }

            public void onScroll(AbsListView view, int firstVisibleItemm, int visibleItemCountt, int totalItemCountt) {
                this.currentFirstVisibleItem = firstVisibleItemm;
                this.currentVisibleItemCount = visibleItemCountt;
                this.totalItem = totalItemCountt;
            }
        });

        adapter = new Received_Adapter(getActivity(), list);
        lv_exp_receive.setAdapter(adapter);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    private void getData(int page, String tag) {
        common.showProgressRelativeLayout(loader);

        HashMap<String, String> param = new HashMap<>();
        param.put("member_id", session.getLoginData(SessionManager.KEY_USER_ID));
        param.put("exp_status", tag);

        common.makePostRequest(AppConstants.express_interest + page, param, response -> {
            common.hideProgressRelativeLayout(loader);
            AppDebugLog.print("resp : " + response);
            try {
                JSONObject object = new JSONObject(response);
                int total_count = object.getInt("total_count");
                continue_request = object.getBoolean("continue_request");

                if (total_count != 0) {
                    tv_no_data.setVisibility(View.GONE);
                    lv_exp_receive.setVisibility(View.VISIBLE);
                    if (total_count != list.size()) {
                        JSONArray data = object.getJSONArray("data");
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject obj = data.getJSONObject(i);
                            ExpressItem item = new ExpressItem();
                            item.setId(obj.getString("id"));
                            item.setMatri_id(obj.getString("matri_id"));
                            item.setUser_id(obj.getString("user_id"));
                            item.setName(obj.getString("username"));

                            item.setAbout(obj.getString("message"));
                            item.setReceiver_response(obj.getString("receiver_response"));
                            item.setImage(obj.getString("photo1"));
                            item.setImage_approval(obj.getString("photo1_approve"));
                            item.setPhoto_view_status(obj.getString("photo_view_status"));
                            item.setPhoto_view_count(obj.getString("photo_view_count"));
                            item.setBadge(obj.getString("badge"));
                            item.setBadgeUrl(obj.getString("badgeUrl"));
                            item.setColor(obj.getString("color"));
                            item.setPhotoUrl(obj.getString("photoUrl"));
                            item.setId_proof_approve(obj.getString("id_proof_approve"));
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
                e.printStackTrace();
                common.showToast(getString(R.string.err_msg_try_again_later));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                common.hideProgressRelativeLayout(loader);
                if (error.networkResponse != null) {
                    common.showToast(Common.getErrorMessageFromErrorCode(error.networkResponse.statusCode));
                }
            }
        });
    }

    private class Received_Adapter extends ArrayAdapter<ExpressItem> {

        Context context;
        List<ExpressItem> list;

        public Received_Adapter(Context context, List<ExpressItem> list) {
            super(context, R.layout.express_item, list);
            this.context = context;
            this.list = list;
        }

        public View getView(final int position, View view, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.express_recieved_item, null, true);

            ShadowView cardView = rowView.findViewById(R.id.cardView);
            ImageView imgPLanStamp = rowView.findViewById(R.id.imgPLanStamp);
            ImageView img_profile = rowView.findViewById(R.id.img_profile);
            TextView tv_name = rowView.findViewById(R.id.tv_name);
            TextView tv_status = rowView.findViewById(R.id.tv_status);
            TextView tv_detail = rowView.findViewById(R.id.tv_detail);

            ImageView btnDelete = rowView.findViewById(R.id.btnDelete);
            ImageView btnInterest = rowView.findViewById(R.id.btnInterest);
            ImageView btnReject = rowView.findViewById(R.id.btnReject);
            ImageView img_more = rowView.findViewById(R.id.img_more);
            ImageView imgVerifiedBadge = rowView.findViewById(R.id.imgVerifiedBadge);

            final ExpressItem item = list.get(position);
            tv_name.setText(item.getName());
            tv_detail.setText(item.getAbout());
            img_more.setOnClickListener(Common::showFilterPopup);

            //if (!ApplicationData.isImageRatioSet) {
            common.setImage(item.getPhoto_view_count(), item.getPhoto_view_status(), item.getImage_approval(),
                    item.getPhotoUrl() + item.getImage(), img_profile, null, 0);
            //}

            switch (item.getReceiver_response()) {
                case "All":
                    tv_status.setText("Sent");
                    break;
                case "Accepted":
                    tv_status.setText("Accepted");
                    break;
                case "Rejected":
                    tv_status.setText("Rejected");
                    break;
                case "Pending":
                    tv_status.setText("Pending");
                    break;
            }

            if (item.getBadge().length() > 0) {
                Picasso.get().load(item.getBadgeUrl() + item.getBadge())
                        .placeholder(R.drawable.ic_transparent_placeholder)
                        .error(R.drawable.placeholder)
                        .into(imgPLanStamp);
                imgPLanStamp.setVisibility(View.VISIBLE);
            } else {
                imgPLanStamp.setVisibility(View.GONE);
            }

            if (item.getColor().length() > 0) {
                cardView.setShadowColor(Color.parseColor("" + item.getColor()));
            }

            img_profile.setOnClickListener(view1 -> {
                if (item.getPhoto_view_status().equals("0") && item.getPhoto_view_count().equals("0")) {
                    alertPhotoPassword(item.getMatri_id());
                } else if (item.getPhoto_view_status().equals("0") && item.getPhoto_view_count().equals("1") && item.getImage_approval().equals("APPROVED")) {
                    final Dialog dialog = new Dialog(context);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.setContentView(R.layout.show_image_alert);
                    TouchImageView img_url = dialog.findViewById(R.id.img_url);
                    Picasso.get().load(item.getImage()).placeholder(placeHolder).error(placeHolder).into(img_url);
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

            rowView.setOnClickListener(view12 -> {
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
            });

            AppDebugLog.print("response : " + item.getReceiver_response());

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
                if (!item.getReceiver_response().equals("Accepted"))
                    updateAction(item.getId(), "accept");
                else
                    common.showToast("You already accepted this user.");
            });

            btnReject.setOnClickListener(view14 -> {
                if (!item.getReceiver_response().equals("Rejected"))
                    updateAction(item.getId(), "reject");
                else
                    common.showToast("You already rejected this user.");
            });

            btnDelete.setOnClickListener(view15 -> {
                deleteInterestAlert(item.getId(), position);
            });

            if (item.getIdProofApprove().equalsIgnoreCase("APPROVED")) {
                imgVerifiedBadge.setVisibility(View.VISIBLE);
            } else {
                imgVerifiedBadge.setVisibility(View.GONE);
            }

            return rowView;
        }

        private void showFilterPopup(View v, final String id, final String receiver_response, final int pos) {
            PopupMenu popup = new PopupMenu(context, v);
            popup.inflate(R.menu.photo_password_more);

            if (receiver_response.equals("Accepted")) {
                popup.getMenu().findItem(R.id.accept).setTitle("Accepted");
                popup.getMenu().findItem(R.id.reject).setTitle("Reject");
            } else if (receiver_response.equals("Rejected")) {
                popup.getMenu().findItem(R.id.accept).setTitle("Accept");
                popup.getMenu().findItem(R.id.reject).setTitle("Rejected");
            }
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.accept:
                        if (item.getTitle().toString().equals("Accept"))
                            updateAction(id, "accept");
                        return true;
                    case R.id.reject:
                        if (item.getTitle().toString().equals("Reject"))
                            updateAction(id, "reject");
                        return true;
                    case R.id.delete:
                        deleteInterestAlert(id, pos);
                        return true;
                    default:
                        return false;
                }
            });
            popup.show();
        }

        private void deleteInterestAlert(final String id, final int position) {
            AlertDialog.Builder alert = new AlertDialog.Builder(context);
            alert.setTitle("Delete");
            alert.setMessage("Are you sure you want to delete this interest?");
            alert.setPositiveButton("Delete", (dialogInterface, i) -> deleteApi(id, position));
            alert.setNegativeButton("Cancel", null);
            alert.show();
        }

        private void deleteApi(String id, final int position) {
            common.showProgressRelativeLayout(loader);

            HashMap<String, String> param = new HashMap<>();
            param.put("user_id", session.getLoginData(SessionManager.KEY_USER_ID));
            param.put("exp_status", tag);
            param.put("id", id);
            param.put("status", "delete");

            common.makePostRequest(AppConstants.action_update_status, param, response -> {
                common.hideProgressRelativeLayout(loader);
                Log.d("resp", response);
                try {
                    JSONObject object = new JSONObject(response);
                    common.showToast(object.getString("errormessage"));
                    if (object.getString("status").equals("success")) {
                        list.remove(position);
                        if (list.size() == 0) {
                            lv_exp_receive.setVisibility(View.GONE);
                            tv_no_data.setVisibility(View.VISIBLE);
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

        private void alertPhotoPassword(final String matri_id) {
            final String[] arr = new String[]{"We found your profile to be a good match. Please accept photo password request to proceed further.",
                    "I am interested in your profile. I would like to view photo now, accept photo request."};
            final String[] selected = {"We found your profile to be a good match. Please accept photo password request to proceed further."};
            AlertDialog.Builder alt_bld = new AlertDialog.Builder(context);

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

    private void updateAction(String id, String status) {
        common.showProgressRelativeLayout(loader);

        HashMap<String, String> param = new HashMap<>();
        param.put("id", id);
        param.put("exp_status", tag);
        param.put("status", status);
        param.put("user_id", session.getLoginData(SessionManager.KEY_USER_ID));

        common.makePostRequest(AppConstants.action_update_status, param, response -> {
            common.hideProgressRelativeLayout(loader);
            try {
                JSONObject object = new JSONObject(response);
                common.showToast(object.getString("errormessage"));
                if (object.getString("status").equals("success")) {
                    page = 0;
                    page = page + 1;
                    continue_request = true;
                    list.clear();
                    getData(page, tag);
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


}
