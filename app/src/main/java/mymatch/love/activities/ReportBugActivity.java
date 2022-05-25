package mymatch.love.activities;

import static mymatch.love.utility.AppConstants.MAX_IMAGE_SIZE_IN_MB_TO_UPLOAD;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.gson.JsonObject;
import mymatch.love.R;
import mymatch.love.network.ConnectionDetector;
import mymatch.love.retrofit.AppApiService;
import mymatch.love.retrofit.ProgressRequestBody;
import mymatch.love.retrofit.RetrofitClient;
import mymatch.love.utility.AppConstants;
import mymatch.love.utility.AppDebugLog;
import mymatch.love.utility.Common;
import mymatch.love.utility.SessionManager;
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

public class ReportBugActivity extends AppCompatActivity implements View.OnClickListener, ProgressRequestBody.UploadCallbacks {

    private EditText et_about;
    private Button btn_submit;
    private Common common;
    private SessionManager session;
    private RelativeLayout loader;

    private ImageView img_ss,img_edit_ss;
    private TextView tv_cancel, tv_gallery, tv_camera;
    private String finalpath = "";
    private File originalFile;
    private String page_name = "";
    private LinearLayout layoutBottomSheet;
    private BottomSheetBehavior sheetBehavior;

    private final int PERMISSION_REQUEST_CODE = 122;
    private EasyImage easyImage = null;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_bug);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Report Bug/Error");

        common = new Common(this);
        session = new SessionManager(this);

        loader = findViewById(R.id.loader);
        et_about = (EditText) findViewById(R.id.et_about);
        btn_submit = (Button) findViewById(R.id.btn_submit);

        btn_submit.setOnClickListener(this);

        img_ss = findViewById(R.id.img_ss);
        img_edit_ss = findViewById(R.id.img_edit_ss);
        layoutBottomSheet = findViewById(R.id.bottom_sheet);
        tv_cancel = findViewById(R.id.tv_cancel);
        tv_gallery = findViewById(R.id.tv_gallary);
        tv_camera = findViewById(R.id.tv_camera);
        tv_gallery.setOnClickListener(this);
        tv_camera.setOnClickListener(this);
        tv_cancel.setOnClickListener(this);
        img_edit_ss.setOnClickListener(this);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
                openFileChooser();
            } else {
                easyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
                    @Override
                    public void onMediaFilesPicked(MediaFile[] mediaFiles, MediaSource mediaSource) {
                        for (MediaFile mediaFile : mediaFiles) {
                            AppDebugLog.print("file : " + mediaFile.getFile());
                            AppDebugLog.print("mediaSource : " + mediaSource);
                            switch (mediaSource) {
                                case DOCUMENTS:
                                case CAMERA_IMAGE:
                                case GALLERY:
                                    AppDebugLog.print("file 1: " + mediaFile.getFile());
                                    originalFile = mediaFile.getFile();
                                    finalpath = mediaFile.getFile().getPath();
                                    int widthAndHeight = Common.convertDpToPixels(400, ReportBugActivity.this);
                                    Picasso.get().load(originalFile).resize(0,widthAndHeight).error(R.drawable.ic_placeholder).placeholder(R.drawable.ic_placeholder).into(img_ss);
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
            finalpath = "";
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadFileToServer() {
        if (!ConnectionDetector.isConnectingToInternet(this)) {
            Toast.makeText(this, getString(R.string.err_msg_no_intenet_connection), Toast.LENGTH_LONG).show();
            return;
        }

        if (pd != null && pd.isShowing()) {
            pd.dismiss();
        }
        // setting progress bar to zero
        pd = new ProgressDialog(ReportBugActivity.this);
        pd.setTitle("Uploading...");
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setProgress(0);
        pd.setCancelable(false);
        pd.show();

        AppDebugLog.print("File path : " + finalpath);
        File profileOriginalImageCompressedFile = Common.getCompressedImageFile(this, originalFile);
        AppDebugLog.print("finalpath Mime Type : : " + getMimeType(finalpath));
        AppDebugLog.print("profileOriginalImageCompressedFile Mime Type : : " + getMimeType(profileOriginalImageCompressedFile.getAbsolutePath()));
        ProgressRequestBody originalFileBody = new ProgressRequestBody(profileOriginalImageCompressedFile, getMimeType(finalpath), this);
        MultipartBody.Part originalFilePart = null;

        originalFilePart = MultipartBody.Part.createFormData("report_file", profileOriginalImageCompressedFile.getName().replaceAll("[^a-zA-Z0-9.]", ""), originalFileBody);

        RequestBody partParam1 = RequestBody.create(MediaType.parse("text/plain"), session.getLoginData(SessionManager.KEY_USER_ID));
        RequestBody partParam2 = RequestBody.create(MediaType.parse("text/plain"), "NI-AAPP");
        RequestBody partParam3 = RequestBody.create(MediaType.parse("text/plain"), session.getLoginData(SessionManager.TOKEN));
        RequestBody partParam4 = RequestBody.create(MediaType.parse("text/plain"), et_about.getText().toString());

        Retrofit retrofit = RetrofitClient.getClient();
        AppApiService appApiService = retrofit.create(AppApiService.class);

        Map<String, RequestBody> params = new HashMap<>();
        params.put("member_id", partParam1);
        params.put("user_agent", partParam2);
        params.put("csrf_new_matrimonial", partParam3);
        params.put("message", partParam4);
        AppDebugLog.print("member_id:" + session.getLoginData(SessionManager.KEY_USER_ID) + " csrf_new_matrimonial:" + session.getLoginData(SessionManager.TOKEN));

        Call<JsonObject> call = null;

        long fileSizeMB = common.getFIleSizeInMB(profileOriginalImageCompressedFile);
        if (fileSizeMB > MAX_IMAGE_SIZE_IN_MB_TO_UPLOAD) {
            Toast.makeText(ReportBugActivity.this, "Image size more than " + MAX_IMAGE_SIZE_IN_MB_TO_UPLOAD + "MB", Toast.LENGTH_SHORT).show();
        } else {
            call = appApiService.uploadReportBugToAdmin(originalFilePart, params);
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
                        finish();
                    } else {
                        Toast.makeText(ReportBugActivity.this, getString(R.string.err_msg_try_again_later), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Toast.makeText(ReportBugActivity.this, getString(R.string.err_msg_try_again_later), Toast.LENGTH_LONG).show();
                    if (pd != null && pd.isShowing()) {
                        pd.dismiss();
                    }
                }
            });
        }
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
            case R.id.img_edit_ss:
                if (sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
                break;
            case R.id.btn_horo:
                if (finalpath.equals("")) {
                    common.showToast("Please select screen shot");
                } else {
                    uploadFileToServer();
                }
                break;
            case R.id.tv_cancel:
                sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                break;
            case R.id.tv_camera:
                sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                openFileChooser();
                break;
            case R.id.tv_gallary:
                sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                pickImage(100);
                break;
            case R.id.btn_submit:
                String msg = et_about.getText().toString().trim();
                if (TextUtils.isEmpty(msg)) {
                    et_about.setError("Please enter issue description");
                    return;
                }
                uploadFileToServer();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
