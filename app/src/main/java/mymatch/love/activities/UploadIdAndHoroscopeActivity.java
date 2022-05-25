package mymatch.love.activities;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;

import mymatch.love.R;
import mymatch.love.custom.TouchImageView;
import mymatch.love.network.ConnectionDetector;
import mymatch.love.retrofit.AppApiService;
import mymatch.love.retrofit.ProgressRequestBody;
import mymatch.love.retrofit.RetrofitClient;
import mymatch.love.utility.AppConstants;
import mymatch.love.utility.AppDebugLog;
import mymatch.love.utility.Common;
import mymatch.love.utility.PickDocument;
import mymatch.love.utility.SessionManager;

import com.rajat.pdfviewer.PdfViewerActivity;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import pl.aprilapps.easyphotopicker.ChooserType;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;
import pl.aprilapps.easyphotopicker.MediaFile;
import pl.aprilapps.easyphotopicker.MediaSource;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

import static mymatch.love.utility.AppConstants.MAX_IMAGE_SIZE_IN_MB_TO_UPLOAD;

public class UploadIdAndHoroscopeActivity extends AppCompatActivity implements View.OnClickListener, ProgressRequestBody.UploadCallbacks {
    private TextView tv_delete_id, tv_cancel, tv_gallary, tv_camera, tv_pdf;
    private Button btn_id, btn_horo;
    private LinearLayout lay_id, lay_horo;
    private TextView tvHoroscopeFileName, tvIdFileName;
    private Common common;
    private SessionManager session;
    private ProgressDialog pd;
    private RelativeLayout loader;
    private String finalpath = "";
    private boolean isPdfFileSelected = false;
    private File originalFile;
    private String page_name = "";
    private LinearLayout layoutBottomSheet;
    private BottomSheetBehavior sheetBehavior;
    ImageView img_show_id, img_edit_id, img_edit_horo, img_show_idH;
    private String horoscopeUrl = "", biodataUrl = "";
    private final int PERMISSION_REQUEST_CODE = 122;
    private EasyImage easyImage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_id_and_horoscope);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Upload");
        toolbar.setNavigationOnClickListener(view -> finish());

        session = new SessionManager(this);
        common = new Common(this);

        loader = findViewById(R.id.loader);
        img_edit_id = findViewById(R.id.img_edit_id);
        img_show_id = findViewById(R.id.img_show_id);
        img_show_idH = findViewById(R.id.img_show_idH);
        img_edit_id.setOnClickListener(this);
        img_show_idH.setOnClickListener(this);
        img_show_id.setOnClickListener(this);
        img_edit_horo = findViewById(R.id.img_edit_horo);
        img_edit_horo.setOnClickListener(this);
        tv_delete_id = findViewById(R.id.tv_delete_id);
        tv_delete_id.setOnClickListener(this);
        btn_id = findViewById(R.id.btn_id);
        btn_id.setOnClickListener(this);
        btn_horo = findViewById(R.id.btn_horo);
        btn_horo.setOnClickListener(this);
        lay_id = findViewById(R.id.lay_id);
        lay_horo = findViewById(R.id.lay_horo);
        tv_pdf = findViewById(R.id.tv_pdf);
        tv_pdf.setOnClickListener(this);
        tvHoroscopeFileName = findViewById(R.id.tvHoroscopeFileName);
        tvIdFileName = findViewById(R.id.tvIdFileName);
        btn_horo.setEnabled(false);
        btn_horo.setBackgroundColor(Color.parseColor("#808080"));
        btn_id.setEnabled(false);
        btn_id.setBackgroundColor(Color.parseColor("#808080"));

        Bundle b = getIntent().getExtras();
        if (b != null && b.containsKey(AppConstants.KEY_INTENT)) {
            page_name = b.getString(AppConstants.KEY_INTENT);
            Log.d("TAG", AppConstants.KEY_INTENT + page_name);
            if (page_name.equals("id")) {
                getSupportActionBar().setTitle("Upload ID");
                lay_id.setVisibility(View.VISIBLE);
                lay_horo.setVisibility(View.GONE);
            } else if (page_name.equals("horoscope")) {
                getSupportActionBar().setTitle("Upload Horoscope");
                lay_id.setVisibility(View.GONE);
                lay_horo.setVisibility(View.VISIBLE);
            }
        }

        layoutBottomSheet = findViewById(R.id.bottom_sheet);
        tv_cancel = findViewById(R.id.tv_cancel);
        tv_gallary = findViewById(R.id.tv_gallary);
        tv_camera = findViewById(R.id.tv_camera);
        tv_gallary.setOnClickListener(this);
        tv_camera.setOnClickListener(this);
        tv_cancel.setOnClickListener(this);
        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);
        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });
        setUpEasyImage();
        getMyprofile();
    }

    private void setUpEasyImage() {
        easyImage = new EasyImage.Builder(this)
                .setChooserTitle(getString(R.string.app_name))
                .setCopyImagesToPublicGalleryFolder(false)
                .setChooserType(ChooserType.CAMERA_AND_GALLERY)
                .setFolderName(AppConstants.DIRECTORY_NAME)
                .allowMultiple(false)
                .build();
    }

    private void pickImage(int pickFor) {
        switch (pickFor) {
            case 100:
                easyImage.openGallery(this);
                break;
            case 200:
                easyImage.openCameraForImage(this);
                break;
        }
    }

    private void openFileChooser() {
        requestPermission();
    }

    @AfterPermissionGranted(122)
    private void requestPermission() {
        if (!checkPermission()) {
            // Ask for one permission
            EasyPermissions.requestPermissions(
                    this,
                    "This needs permission to use feature. You can grant them in app settings.",
                    PERMISSION_REQUEST_CODE,
                    Manifest.permission.CAMERA);
        } else {
            // sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            pickImage(200);
        }
    }

    //TODO Permission related
    private boolean checkPermission() {
        String[] perms = {Manifest.permission.CAMERA};

        if (EasyPermissions.hasPermissions(this, perms)) {
            return true;
        } else {
            return false;
        }
    }

    private void getMyprofile() {
        common.showProgressRelativeLayout(loader);

        HashMap<String, String> param = new HashMap<>();
        param.put("member_id", session.getLoginData(SessionManager.KEY_USER_ID));
        common.makePostRequest(AppConstants.get_my_profile, param, response -> {
            AppDebugLog.print("MyProfile in upload horoscope ");
            common.hideProgressRelativeLayout(loader);
            try {
                JSONObject object = new JSONObject(response);
                JSONObject data = object.getJSONObject("data");

                if (page_name.equals("id")) {
                    if (!data.getString("id_proof").equals("") && !data.getString("id_proof").equals("null")) {
                        tvIdFileName.setText(Common.getFileNameFromfilePath(data.getString("id_proof")));
                        tv_delete_id.setVisibility(View.VISIBLE);
                        btn_id.setEnabled(false);
                        btn_id.setBackgroundColor(Color.parseColor("#808080"));
                    } else {
                        tv_delete_id.setVisibility(View.GONE);
                    }
                    if (data.has("id_proof") && !data.getString("id_proof").equals("") && !data.getString("id_proof").equals("null") && data.getString("id_proof").length() > 0) {
                        img_show_id.setVisibility(View.VISIBLE);
                        biodataUrl = data.getString("id_proof");
                    }
                } else if (page_name.equals("horoscope")) {
                    if (!data.getString("horoscope_photo").equals("") && !data.getString("horoscope_photo").equals("null")) {
                        AppDebugLog.print("file name : " + data.getString("horoscope_photo"));
                        tvHoroscopeFileName.setText(Common.getFileNameFromfilePath(data.getString("horoscope_photo")));
                        btn_horo.setEnabled(false);
                        btn_horo.setBackgroundColor(Color.parseColor("#808080"));
                    }
                    if (data.has("horoscope_photo") && !data.getString("horoscope_photo").equals("") && !data.getString("horoscope_photo").equals("null") && data.getString("horoscope_photo").length() > 0) {
                        img_show_idH.setVisibility(View.VISIBLE);
                        horoscopeUrl = data.getString("horoscope_photo");
                    }
                }
//                if (data.has("bio_data_url") && data.getString("bio_data_url").length() > 0) {
//                    biodataUrl = data.getString("bio_data_url");
//                }
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
                openFileChooser();
            } else if (requestCode == 5000) {
                getFile(data);
            } else {
                easyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
                    @Override
                    public void onMediaFilesPicked(MediaFile[] mediaFiles, MediaSource mediaSource) {
                        for (MediaFile mediaFile : mediaFiles) {
                            AppDebugLog.print("file : " + mediaFile.getFile().getAbsolutePath());
                            switch (mediaSource) {
                                case DOCUMENTS:
                                case CAMERA_IMAGE:
                                case GALLERY:
                                    isPdfFileSelected = false;
                                    originalFile = mediaFile.getFile();
                                    finalpath = mediaFile.getFile().getPath();
                                    if (page_name.equals("id")) {
                                        tvIdFileName.setText(Common.getFileNameFromfilePath(finalpath));
                                        btn_id.setEnabled(true);
                                        btn_id.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                                    } else {
                                        tvHoroscopeFileName.setText(Common.getFileNameFromfilePath(finalpath));
                                        btn_horo.setEnabled(true);
                                        btn_horo.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                                    }
                                    break;
                            }
                        }
                    }

                    @Override
                    public void onImagePickerError(Throwable error, MediaSource source) {
                        super.onImagePickerError(error, source);
                    }
                });
            }
        } else {
            if (page_name.equals("id")) {
                //btn_id.setText("Select Id Proof");
                finalpath = "";
            } else if (page_name.equals("horoscope")) {
                //btn_horo.setText("Select Horoscope");
                finalpath = "";
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadFileToServer(boolean isPdf) {
        if (!ConnectionDetector.isConnectingToInternet(this)) {
            Toast.makeText(this, getString(R.string.err_msg_no_intenet_connection), Toast.LENGTH_LONG).show();
            return;
        }

        if (pd != null && pd.isShowing()) {
            pd.dismiss();
        }
        // setting progress bar to zero
        pd = new ProgressDialog(UploadIdAndHoroscopeActivity.this);
        pd.setTitle("Uploading...");
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setProgress(0);
        pd.setCancelable(false);
        pd.show();

        AppDebugLog.print("File path : " + finalpath);
        MultipartBody.Part originalFilePart = null;
        File uploadFile = null;
        if (!isPdf) {
            uploadFile = Common.getCompressedImageFile(this, originalFile);
            AppDebugLog.print("profileOriginalImageCompressedFile Mime Type : : " + getMimeType(uploadFile.getAbsolutePath()));
            ProgressRequestBody originalFileBody = new ProgressRequestBody(uploadFile, getMimeType(finalpath), this);
            if (page_name.equals("id")) {
                originalFilePart = MultipartBody.Part.createFormData("id_proof", uploadFile.getName().replaceAll("[^a-zA-Z0-9.]", ""), originalFileBody);
            } else if (page_name.equals("horoscope")) {
                originalFilePart = MultipartBody.Part.createFormData("horoscope_photo", uploadFile.getName().replaceAll("[^a-zA-Z0-9.]", ""), originalFileBody);
            }
        } else {
            uploadFile = new File(finalpath);
            ProgressRequestBody originalFileBody = new ProgressRequestBody(uploadFile, "application/pdf", this);
            if (page_name.equals("id")) {
                originalFilePart = MultipartBody.Part.createFormData("id_proof", uploadFile.getName().replaceAll("[^a-zA-Z0-9.]", ""), originalFileBody);
            } else if (page_name.equals("horoscope")) {
                originalFilePart = MultipartBody.Part.createFormData("horoscope_photo", uploadFile.getName().replaceAll("[^a-zA-Z0-9.]", ""), originalFileBody);
            }
        }

        RequestBody partParam1 = RequestBody.create(MediaType.parse("text/plain"), session.getLoginData(SessionManager.KEY_USER_ID));
        RequestBody partParam2 = RequestBody.create(MediaType.parse("text/plain"), "NI-AAPP");
        RequestBody partParam3 = RequestBody.create(MediaType.parse("text/plain"), session.getLoginData(SessionManager.TOKEN));

        Retrofit retrofit = RetrofitClient.getClient();
        AppApiService appApiService = retrofit.create(AppApiService.class);

        Map<String, RequestBody> params = new HashMap<>();
        params.put("member_id", partParam1);
        params.put("user_agent", partParam2);
        params.put("csrf_new_matrimonial", partParam3);
        AppDebugLog.print("member_id:" + session.getLoginData(SessionManager.KEY_USER_ID) + " csrf_new_matrimonial:" + session.getLoginData(SessionManager.TOKEN));

        Call<JsonObject> call = null;
        if (page_name.equals("id")) {
            long fileSizeMB = common.getFIleSizeInMB(uploadFile);
            if (fileSizeMB > MAX_IMAGE_SIZE_IN_MB_TO_UPLOAD) {
                Toast.makeText(UploadIdAndHoroscopeActivity.this, "ID Image size more than " + MAX_IMAGE_SIZE_IN_MB_TO_UPLOAD + "MB", Toast.LENGTH_SHORT).show();
            } else {
                call = appApiService.uploadIdProof(originalFilePart, params);
            }

        } else {
            long fileSizeMB = common.getFIleSizeInMB(uploadFile);
            if (fileSizeMB > MAX_IMAGE_SIZE_IN_MB_TO_UPLOAD) {
                Toast.makeText(UploadIdAndHoroscopeActivity.this, "Image size more than " + MAX_IMAGE_SIZE_IN_MB_TO_UPLOAD + "MB", Toast.LENGTH_SHORT).show();
            } else {
                call = appApiService.uploadHoroscopePhoto(originalFilePart, params);
            }
        }

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }
                AppDebugLog.print("response in submitData : " + response);

                JsonObject data = response.body();
                if (data != null) {
                    common.showToast(data.get("errmessage").getAsString());
                } else {
                    Toast.makeText(UploadIdAndHoroscopeActivity.this, getString(R.string.err_msg_try_again_later), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(UploadIdAndHoroscopeActivity.this, getString(R.string.err_msg_try_again_later), Toast.LENGTH_LONG).show();
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }
            }
        });
    }

    // url = file path or whatever suitable URL you want.
    public String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        AppDebugLog.print("File Mime Type : " + type);
        return type;
    }

    @Override
    public void onProgressUpdate(int percentage) {
        // set current progress
        if (pd != null && pd.isShowing()) {
            pd.setProgress(percentage);
        }

    }

    @Override
    public void onError() {

    }

    @Override
    public void onFinish() {
        if (pd != null && pd.isShowing()) {
            pd.setProgress(100);
            pd.dismiss();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_delete_id:
                deleteAlert();
                break;
            case R.id.img_edit_id:
            case R.id.img_edit_horo:
                if (sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
                break;
            case R.id.img_show_idH:
                if (!horoscopeUrl.isEmpty()) {
                    viewFile(horoscopeUrl);
                }
                break;
            case R.id.img_show_id:
                if (!biodataUrl.isEmpty()) {
                    viewFile(biodataUrl);
                }
                break;
            case R.id.btn_id:
                if (finalpath.equals("")) {
                    common.showToast("Please Select Id proof First ");
                } else {
                    if (isPdfFileSelected) {
                        uploadFileToServer(true);
                    } else {
                        uploadFileToServer(false);
                    }
                }
                break;
            case R.id.btn_horo:
                if (finalpath.equals("")) {
                    common.showToast("Please Select horoscope First ");
                } else {
                    if (isPdfFileSelected) {
                        uploadFileToServer(true);
                    } else {
                        uploadFileToServer(false);
                    }
                }
                break;
            case R.id.tv_cancel:
                sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                break;
            case R.id.tv_camera:
                openFileChooser();
                break;
            case R.id.tv_gallary:
                sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                pickImage(100);
                break;
            case R.id.tv_pdf:
                sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                openPdfFile();
                break;
        }
    }

    private void deleteAlert() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage("Are you sure you want to delete id proof?");
        alert.setNegativeButton("Cancel", null);
        alert.setPositiveButton("Delete", (dialogInterface, i) -> deleteId_api());
        alert.show();
    }

    private void deleteId_api() {
        common.showProgressRelativeLayout(loader);

        HashMap<String, String> param = new HashMap<>();
        param.put("member_id", session.getLoginData(SessionManager.KEY_USER_ID));
        param.put("delete_id_proof_photo", "delete");

        common.makePostRequest(AppConstants.delete_id_proof_photo, param, response -> {
            common.hideProgressRelativeLayout(loader);
            try {
                JSONObject object = new JSONObject(response);
                common.showToast(object.getString("errmessage"));
                if (object.getString("status").equals("success")) {
                    getMyprofile();
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


    private void openPdfFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("application/pdf");

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        startActivityForResult(intent, 5000);
    }

    private void getFile(Intent data) {
        PickDocument pickDocument = new PickDocument();
        File file = pickDocument.tryOpenDocument(RESULT_OK, data);
        if (file != null) {
            isPdfFileSelected = true;
            finalpath = file.getAbsolutePath();
            if (page_name.equals("id")) {
                tvIdFileName.setText(Common.getFileNameFromfilePath(finalpath));
                btn_id.setEnabled(true);
                btn_id.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            } else {
                tvHoroscopeFileName.setText(Common.getFileNameFromfilePath(finalpath));
                btn_horo.setEnabled(true);
                btn_horo.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            }
        } else {
            Toast.makeText(this, "Pdf file not selected from your device because of android privacy restriction", Toast.LENGTH_LONG).show();
        }
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

}
