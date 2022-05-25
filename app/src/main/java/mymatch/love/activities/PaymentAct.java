package mymatch.love.activities;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
//
//import com.payu.base.models.ErrorResponse;
//import com.payu.base.models.PayUPaymentParams;
//import com.payu.base.models.PaymentMode;
//import com.payu.base.models.PaymentType;
//import com.payu.checkoutpro.PayUCheckoutPro;
//import com.payu.checkoutpro.models.PayUCheckoutProConfig;
//import com.payu.checkoutpro.utils.PayUCheckoutProConstants;
//import com.payu.ui.model.listeners.PayUCheckoutProListener;
//import com.payu.ui.model.listeners.PayUHashGenerationListener;

import java.util.ArrayList;
import java.util.HashMap;

import mymatch.love.R;

public class PaymentAct extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_act);

//        PayUCheckoutProConfig payUCheckoutProConfig = new PayUCheckoutProConfig();
//        payUCheckoutProConfig.setMerchantName("MyMatchLove");
//        payUCheckoutProConfig.setMerchantLogo(R.mipmap.ic_launcher_round);
//        payUCheckoutProConfig.setShowCbToolbar(false); //hide toolbar
//        payUCheckoutProConfig.setAutoApprove(true);
//        payUCheckoutProConfig.setMerchantResponseTimeout(15000); // for 15 seconds timeout
//
//        ArrayList<PaymentMode> checkoutOrderList = new ArrayList<>();
//        checkoutOrderList.add(new PaymentMode(PaymentType.UPI, PayUCheckoutProConstants.CP_GOOGLE_PAY));
//        checkoutOrderList.add(new PaymentMode(PaymentType.WALLET, PayUCheckoutProConstants.CP_PHONEPE));
//        checkoutOrderList.add(new PaymentMode(PaymentType.WALLET, PayUCheckoutProConstants.CP_PAYTM));
//        checkoutOrderList.add(new PaymentMode(PaymentType.CARD));
//        checkoutOrderList.add(new PaymentMode(PaymentType.UPI));
//
////        PayUCheckoutProConfig payUCheckoutProConfig = new PayUCheckoutProConfig ();
//        payUCheckoutProConfig.setPaymentModesOrder(checkoutOrderList);
//
//        PayUPaymentParams.Builder builder = new PayUPaymentParams.Builder();
//        builder.setAmount("1")
//                .setIsProduction(false)
//                .setProductInfo("Testing Production")
//                .setKey("VpjwyO")
//                .setPhone("+918019822298")
//                .setTransactionId(String.valueOf(System.currentTimeMillis()))
//                .setFirstName("Praveen")
//                .setEmail("impraveen.avula@gmail.com")
//                .setSurl("")
//                .setFurl("");
//        PayUPaymentParams payUPaymentParams = builder.build();
//
//        HashMap<String, Object> additionalParams = new HashMap<>();
//        additionalParams.put(PayUCheckoutProConstants.CP_VAS_FOR_MOBILE_SDK, "");
//        additionalParams.put(PayUCheckoutProConstants.CP_PAYMENT_RELATED_DETAILS_FOR_MOBILE_SDK, "");
//
//        PayUCheckoutPro.open(
//                this,
//                payUPaymentParams,
//                new PayUCheckoutProListener() {
//
//                    @Override
//                    public void generateHash(@NonNull HashMap<String, String> hashMap, @NonNull com.payu.ui.model.listeners.PayUHashGenerationListener payUHashGenerationListener) {
//                        String hashName = hashMap.get(PayUCheckoutProConstants.CP_HASH_NAME);
//                        String hashData = hashMap.get(PayUCheckoutProConstants.CP_HASH_STRING);
//                        if (!TextUtils.isEmpty(hashName) && !TextUtils.isEmpty(hashData)) {
//                            //Do not generate hash from local, it needs to be calculated from server side only. Here, hashString contains hash created from your server side.
////                            String hash = hashString;
//                            HashMap<String, String> dataMap = new HashMap<>();
////                            dataMap.put(hashName, hash);
//                            payUHashGenerationListener.onHashGenerated(dataMap);
//                        }
//                    }
//
//                    @Override
//                    public void onPaymentSuccess(Object response) {
//                        //Cast response object to HashMap
//                        HashMap<String,Object> result = (HashMap<String, Object>) response;
//                        String payuResponse = (String)result.get(PayUCheckoutProConstants.CP_PAYU_RESPONSE);
//                        String merchantResponse = (String) result.get(PayUCheckoutProConstants.CP_MERCHANT_RESPONSE);
//                    }
//
//                    @Override
//                    public void onPaymentFailure(Object response) {
//                        //Cast response object to HashMap
//                        HashMap<String,Object> result = (HashMap<String, Object>) response;
//                        String payuResponse = (String)result.get(PayUCheckoutProConstants.CP_PAYU_RESPONSE);
//                        String merchantResponse = (String) result.get(PayUCheckoutProConstants.CP_MERCHANT_RESPONSE);
//                    }
//
//                    @Override
//                    public void onPaymentCancel(boolean isTxnInitiated) {
//                    }
//
//                    @Override
//                    public void onError(ErrorResponse errorResponse) {
//                        String errorMessage = errorResponse.getErrorMessage();
//                    }
//
//                    @Override
//                    public void setWebViewProperties(@Nullable WebView webView, @Nullable Object o) {
//                        //For setting webview properties, if any. Check Customized Integration section for more details on this
//                    }
//
//                }
//        );
    }

    interface PayUHashGenerationListener {
        void onHashGenerated(HashMap<String, String> map);
    }


}
