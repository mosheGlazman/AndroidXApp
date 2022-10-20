package com.supercom.androidxapp.biometrics;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;
import androidx.fragment.app.FragmentActivity;

import com.supercom.androidxapp.R;

public class BiometricsManager {
    private static BiometricsManager _instance;
    private BiometricsAuth biometricsAuth;

    public static final String ACTION_Status = "com.supercom.androidxapp.biometric.status";
    public static final String ACTION_Status_Result = "com.supercom.androidxapp.biometric.status.result";
    public static final String ACTION_Enroll = "com.supercom.androidxapp.biometric.enroll";
    public static final String ACTION_Authenticate = "com.supercom.androidxapp.biometric.Authenticate";
    public static final String ACTION_Authenticate_cancel = "com.supercom.androidxapp.biometric.Authenticate.cancel";
    public static final String ACTION_Authenticate_success = "com.supercom.androidxapp.biometric.Authenticate.success";
    public static final String ACTION_Authenticate_failed = "com.supercom.androidxapp.biometric.Authenticate.failed";
    public static final String ACTION_error = "com.supercom.androidxapp.biometric.Authenticate.error";
    public static final String EXTRA_error_message = "error_message";
    public static final String EXTRA_error_code = "error_code";
    public static final String EXTRA_status = "status";
    public static final String EXTRA_action = "action";
    public static final int ERROR_enroll = -3;
    public static final int ERROR_initialize = -4;
    public static final int ERROR_no_status = -5;

    public static BiometricsManager getInstance() {
        if (_instance == null) {
            _instance = new BiometricsManager();
        }

        return _instance;
    }

    BiometricPrompt.PromptInfo promptInfo;
    private FragmentActivity activity;
    private BiometricsListener listener;

    private BiometricsManager() {
    }

    public interface BiometricsListener {
        void onAuthenticate();

        void onAuthenticateFailed();

        void onError(String error, int errorCode);

    }

    public void setListener(BiometricsListener listener) {
        this.listener = listener;
    }

    public void init(FragmentActivity activity) {
        this.activity = activity;
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(activity.getString(R.string.biometrics_prompt_title))
                .setSubtitle(activity.getString(R.string.biometrics_prompt_description))
                .setNegativeButtonText(activity.getString(R.string.biometrics_prompt_negative))
                .build();

        biometricsAuth = new BiometricsAuth(activity, activity);
    }

    public boolean BiometricsOk() {
        if (!isInitialize()) {
            return false;
        }

        return biometricsAuth.checkForBiometrics() == BiometricsAuth.BiometricStatus.BIOMETRIC_SUCCESS;
    }

    public BiometricsAuth.BiometricStatus getStatus() {
        if (!isInitialize()) {
            return null;
        }

        return biometricsAuth.checkForBiometrics();
    }

    public boolean BiometricsNotSetUp() {
        if (!isInitialize()) {
            return false;
        }
        return biometricsAuth.checkForBiometrics() == BiometricsAuth.BiometricStatus.BIOMETRIC_ERROR_NONE_ENROLLED;
    }

    private boolean isInitialize() {
        if (biometricsAuth == null) {
            if (listener != null) {
                listener.onAuthenticateFailed();
                listener.onError("Biometrics manager initialize required. call init method with FragmentActivity", ERROR_initialize);
            }
            return false;
        }

        return true;
    }

    public boolean BiometricsNotSupported() {
        if (!isInitialize()) {
            return false;
        }

        return biometricsAuth.checkForBiometrics() == BiometricsAuth.BiometricStatus.BIOMETRIC_ERROR_NO_HARDWARE;
    }

    public void askAuthenticate() {
        if (!isInitialize()) {
            return;
        }

        authenticate(promptInfo);
    }

    public void cancelAuthenticate() {
        try {
            biometricsAuth.cancelPrompt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void enroll() {
        if (!isInitialize()) {
            return;
        }

        if (!biometricsAuth.enroll()) {
            if (listener != null) {
                listener.onAuthenticateFailed();
                listener.onError("Cannot enroll user. SDK is lower than 30", ERROR_enroll);
            }
        }
    }

    boolean isAuthenticateRunning;

    private void authenticate(BiometricPrompt.PromptInfo promptInfo) {
        isAuthenticateRunning = true;
        biometricsAuth.prompt(promptInfo, new BiometricsAuthenticationListener() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                isAuthenticateRunning = false;
                if (listener == null) {
                    return;
                }

                listener.onAuthenticate();
            }

            @Override
            public void onAuthenticationFailed(BiometricsFailure failure) {
                isAuthenticateRunning = false;

                if (listener == null) {
                    return;
                }

                String error = activity.getString(R.string.biometrics_result_failure);

                if (failure.getFailureType() == BiometricsFailure.FailureType.ERROR) {
                    error = failure.getErrorMessage();
                }

                listener.onAuthenticateFailed();
                listener.onError(error, failure.getErrorCode());
            }
        });
    }
}