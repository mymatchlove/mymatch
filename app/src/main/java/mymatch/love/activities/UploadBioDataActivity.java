package mymatch.love.activities;

import static mymatch.love.utility.AppConstants.MAX_IMAGE_SIZE_IN_MB_TO_UPLOAD;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
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

public class UploadBioDataActivity extends AppCompatActivity implements View.OnClickListener, ProgressRequestBody.UploadCallbacks {
    private TextView tv_cancel, tv_gallary, tv_camera, tv_pdf;
    private Button btn_horo;
    private TextView tvBioDataFileName, btnViewBioData;
    private ImageView btnBioDataChoose, img_show_idB;
    private Common common;
    private SessionManager session;
    private ProgressDialog pd;
    private RelativeLayout loader;
    private String finalpath = "";
    private File originalFile;
    private String page_name = "";
    private LinearLayout layoutBottomSheet;
    private BottomSheetBehavior sheetBehavior;

    private final int PERMISSION_REQUEST_CODE = 122;
    private EasyImage easyImage = null;

    private String fileUrl = "", bioDataUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_biodata);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Upload");
        toolbar.setNavigationOnClickListener(view -> finish());

        session = new SessionManager(this);
        common = new Common(this);

        loader = findViewById(R.id.loader);
        tvBioDataFileName = findViewById(R.id.tvBioDataFileName);
        btnViewBioData = findViewById(R.id.btnViewBioData);
        btnBioDataChoose = findViewById(R.id.btnBioDataChoose);
        img_show_idB = findViewById(R.id.img_show_idB);
        btnBioDataChoose.setOnClickListener(this);
        btn_horo = findViewById(R.id.btn_horo);
        btn_horo.setOnClickListener(this);
        btnViewBioData.setOnClickListener(this);
        img_show_idB.setOnClickListener(this);
        btn_horo.setEnabled(false);
        btn_horo.setBackgroundColor(Color.parseColor("#808080"));
        getSupportActionBar().setTitle("Upload Bio Data");

        layoutBottomSheet = findViewById(R.id.bottom_sheet);
        tv_cancel = findViewById(R.id.tv_cancel);
        tv_gallary = findViewById(R.id.tv_gallary);
        tv_camera = findViewById(R.id.tv_camera);
        tv_pdf = findViewById(R.id.tv_pdf);
        tv_gallary.setOnClickListener(this);
        tv_camera.setOnClickListener(this);
        tv_cancel.setOnClickListener(this);
        tv_pdf.setOnClickListener(this);
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

                if (data.has("bio_data") && !data.getString("bio_data").equals("") && !data.getString("bio_data").equals("null")) {
                    tvBioDataFileName.setText(data.getString("bio_data"));
                    fileUrl = "https://www.mymatch.love/assets/bio_data/" + data.getString("bio_data");
                    AppDebugLog.print("MyProfile in upload horoscope " + data.getString("bio_data"));
                    data.getString("bio_data");
                    if (data.has("bio_data") && !data.getString("bio_data").equals("") && !data.getString("bio_data").equals("null") && data.getString("bio_data").length() > 0) {
                        bioDataUrl = "https://www.mymatch.love/assets/bio_data/" + data.getString("bio_data");
                        img_show_idB.setVisibility(View.VISIBLE);
                        btn_horo.setEnabled(false);
                        btn_horo.setBackgroundColor(Color.parseColor("#808080"));
                    }
                    //btnViewBioData.setVisibility(View.VISIBLE);
                } else {
                    //btnViewBioData.setVisibility(View.GONE);
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
                                    originalFile = mediaFile.getFile();
                                    finalpath = mediaFile.getFile().getPath();
                                    tvBioDataFileName.setText(Common.getFileNameFromfilePath(originalFile.getAbsolutePath()));
                                    btn_horo.setEnabled(true);
                                    btn_horo.setBackgroundColor(getResources().getColor(R.color.colorAccent));
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
        pd = new ProgressDialog(UploadBioDataActivity.this);
        pd.setTitle("Uploading...");
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setProgress(0);
        pd.setCancelable(false);
        pd.show();

        AppDebugLog.print("File path : " + finalpath);
        AppDebugLog.print("finalpath Mime Type : : " + getMimeType(finalpath));
        MultipartBody.Part originalFilePart = null;
        File uploadFile = null;
        if (!isPdf) {
            uploadFile = Common.getCompressedImageFile(this, originalFile);
            AppDebugLog.print("profileOriginalImageCompressedFile Mime Type : : " + getMimeType(uploadFile.getAbsolutePath()));
            ProgressRequestBody originalFileBody = new ProgressRequestBody(uploadFile, getMimeType(finalpath), this);

            originalFilePart = MultipartBody.Part.createFormData("bio_data", uploadFile.getName().replaceAll("[^a-zA-Z0-9.]", ""), originalFileBody);
        } else {
            uploadFile = new File(finalpath);
            ProgressRequestBody originalFileBody = new ProgressRequestBody(uploadFile, "application/pdf", this);
            originalFilePart = MultipartBody.Part.createFormData("bio_data", uploadFile.getName().replaceAll("[^a-zA-Z0-9.]", ""), originalFileBody);
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
        long fileSizeMB = common.getFIleSizeInMB(uploadFile);
        if (fileSizeMB > MAX_IMAGE_SIZE_IN_MB_TO_UPLOAD) {
            Toast.makeText(UploadBioDataActivity.this, "Image size more than " + MAX_IMAGE_SIZE_IN_MB_TO_UPLOAD + "MB", Toast.LENGTH_SHORT).show();
        } else {
            call = appApiService.uploadBiodata(originalFilePart, params);

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
                        Toast.makeText(UploadBioDataActivity.this, getString(R.string.err_msg_try_again_later), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Toast.makeText(UploadBioDataActivity.this, getString(R.string.err_msg_try_again_later), Toast.LENGTH_LONG).show();
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
            case R.id.tv_delete_id:
                deleteAlert();
                break;
            case R.id.img_show_idB:
                viewFile(bioDataUrl);
                break;
            case R.id.btnBioDataChoose:
                if (sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
                break;
            case R.id.btn_horo:
                if (finalpath.equals("")) {
                    common.showToast("Please Select bio data First ");
                } else {
                    uploadFileToServer(false);
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
            case R.id.btnViewBioData:
                PdfViewerActivity.Companion.launchPdfFromUrl(
                        this,
                        fileUrl,
                        Common.getFileNameFromfilePath(fileUrl),
                        "",
                        false
                );
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
            finalpath = file.getAbsolutePath();
            tvBioDataFileName.setText(Common.getFileNameFromfilePath(finalpath));
            uploadFileToServer(true);
        } else {
            Toast.makeText(this, "Pdf file not selected from your device beacause of android privacy restriction", Toast.LENGTH_LONG).show();
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
