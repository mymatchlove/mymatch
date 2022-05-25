package mymatch.love.retrofit;


import mymatch.love.application.MyApplication;
import mymatch.love.utility.AppConstants;
import mymatch.love.utility.AppDebugLog;
import mymatch.love.utility.ApplicationData;
import mymatch.love.utility.Common;
import mymatch.love.utility.SessionManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Initialize retrofit object
 * Created by Nasirali on 02-02-2019.
 */

public class RetrofitClient {
    private static Retrofit retrofit = null;
    private static Retrofit retrofitWithHeader = null;
    private static Retrofit retrofitWithHeaderAfterLogin = null;

    //TODO Retrofit Use for post request before login..
    public static Retrofit getClientWithHeader() {
        AppDebugLog.print("RetrofitClient headertoken: " + ApplicationData.getSession().getLoginData(SessionManager.HEADER_TOKEN));
        AppDebugLog.print("RetrofitClient devicetoken: " + ApplicationData.getSession().getLoginData(SessionManager.KEY_DEVICE_TOKEN));
        AppDebugLog.print("RetrofitClient useragent: " + AppConstants.USER_AGENT);
        AppDebugLog.print("RetrofitClient appversion: " + Common.getAppVersionName(MyApplication.getAppContext()));

        if (retrofit == null) {
            //TODO Retrofit Add headers in api call
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.addInterceptor(chain -> {
                Request request = chain.request().newBuilder()
                        .addHeader("useragent", AppConstants.USER_AGENT)
                        .addHeader("devicetoken", ApplicationData.getSession().getLoginData(SessionManager.KEY_DEVICE_TOKEN))
                        .addHeader("appversion", Common.getAppVersionName(MyApplication.getAppContext()))
                        .addHeader("headertoken", ApplicationData.getSession().getLoginData(SessionManager.HEADER_TOKEN))
                        .build();
                return chain.proceed(request);
            });

            //TODO Retrofit End add headers in api call

            retrofit = new Retrofit.Builder()
                    .baseUrl(AppConstants.BASE_URL)
                    .client(httpClient.build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }

    //TODO Retrofit Use for simple get request
    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(AppConstants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }

    //TODO Retrofit Use for post request after login because after login token will change..
    public static Retrofit getClientWithHeaderAfterLogin() {
        if (retrofitWithHeaderAfterLogin == null) {
            AppDebugLog.print("csrf_new_matrimonial in RetrofitClient after login: " + ApplicationData.getSession().getLoginData(SessionManager.TOKEN));
            AppDebugLog.print("header_token in RetrofitClient after login: " + ApplicationData.getSession().getLoginData(SessionManager.HEADER_TOKEN));
            AppDebugLog.print("fcm token in RetrofitClient: " + ApplicationData.getSession().getLoginData(SessionManager.KEY_DEVICE_TOKEN));

            //TODO Retrofit Getting all api related logs using below interceptor
            //HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            //loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            //TODO Retrofit End getting all api related logs using below interceptor

            //TODO Retrofit Add headers in api call
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            //.addInterceptor(loggingInterceptor);
            httpClient.addInterceptor(chain -> {
                Request request = chain.request().newBuilder()
                        .addHeader("useragent", AppConstants.USER_AGENT)
                        //.addHeader("csrf_new_matrimonial", ApplicationData.getSharedInstance().sessionManager.getUserData(SessionManager.CSRF_TOKEN))
                        .addHeader("headertoken", ApplicationData.getSession().getLoginData(SessionManager.HEADER_TOKEN))
                        .build();

                return chain.proceed(request);
            });
            //TODO Retrofit End add headers in api call

            //TODO Retrofit Create retrofit object
            retrofitWithHeaderAfterLogin = new Retrofit.Builder()
                    .baseUrl(AppConstants.BASE_URL)
                    .client(httpClient.build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitWithHeaderAfterLogin;
        //TODO Retrofit End create retrofit object
    }

    //TODO Retrofit use for retrofit object reset after logout..
    public static void resetAfterLoginRetrofitObject() {
        retrofit = null;
    }
}
