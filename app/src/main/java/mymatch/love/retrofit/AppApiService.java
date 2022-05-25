package mymatch.love.retrofit;

import com.google.gson.JsonObject;

import java.util.Map;

import mymatch.love.model.OtpOnCallModel;
import mymatch.love.utility.AppConstants;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;

/**
 * Use for api call and get your response in your activity or fragment
 * Created by Nasirali on 02-02-2019.
 */

public interface AppApiService {

    @FormUrlEncoded
    @POST(AppConstants.get_token)
    Call<JsonObject> getToken(@FieldMap Map<String, String> params);


    /**
     * @param file1  Crop image
     * @param file2  Original image
     * @param params All params in multipart request body
     * @return JsonObject
     */
    @Multipart
    @POST(AppConstants.register_upload_profile_image)
    Call<JsonObject> uploadPhoto(@Part MultipartBody.Part file1, @Part MultipartBody.Part file2, @PartMap() Map<String, RequestBody> params);

    @Multipart
    @POST(AppConstants.upload_horoscope)
    Call<JsonObject> uploadHoroscopePhoto(@Part MultipartBody.Part file1, @PartMap() Map<String, RequestBody> params);

    @Multipart
    @POST(AppConstants.upload_biodata)
    Call<JsonObject> uploadBiodata(@Part MultipartBody.Part file1, @PartMap() Map<String, RequestBody> params);

    @Multipart
    @POST(AppConstants.report_bug_to_admin)
    Call<JsonObject> uploadReportBugToAdmin(@Part MultipartBody.Part file1, @PartMap() Map<String, RequestBody> params);

    @Multipart
    @POST(AppConstants.upload_id_proof_photo)
    Call<JsonObject> uploadIdProof(@Part MultipartBody.Part file1, @PartMap() Map<String, RequestBody> params);

    @Multipart
    @POST(AppConstants.upload_photo_new)
    Call<JsonObject> uploadMyPhotoWithCrop(@Part MultipartBody.Part file1, @Part MultipartBody.Part file2, @PartMap() Map<String, RequestBody> params);

    @Multipart
    @POST(AppConstants.upload_photo_new)
    Call<JsonObject> uploadMyPhotoWithoutCrop(@Part MultipartBody.Part file1, @PartMap() Map<String, RequestBody> params);

    @FormUrlEncoded
    @POST(AppConstants.success_story)
    Call<JsonObject> getSuccessStoryListRequest(@FieldMap() Map<String, String> params);

    @FormUrlEncoded
    @POST(AppConstants.GET_VENDOR_CATEGORY_LIST)
    Call<JsonObject> getVendorCategoryList(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST(AppConstants.GET_VENDOR_LIST)
    Call<JsonObject> getVendorList(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST(AppConstants.GET_VENDOR_DETAILS)
    Call<JsonObject> getVendorDetails(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST(AppConstants.SEND_VENDOR_INQUIRY)
    Call<JsonObject> sendVendorInquiry(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST(AppConstants.SEND_VENDOR_REVIEW)
    Call<JsonObject> sendVendorReview(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST(AppConstants.get_notification_list)
    Call<JsonObject> getNotificationList(@FieldMap Map<String, String> params);

    @GET("8a2d5d32-4ea3-11ec-b710-0200cd936042/VOICE/{number}/{otp}")
    Call<OtpOnCallModel> getOTPonCall(@Path("number") String number, @Path("otp") String otp);
}
