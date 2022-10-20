package com.supercom.androidxapp.biometrics;

import static android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL;
import static android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static com.supercom.androidxapp.biometrics.BiometricsFailure.FailureType.*;
import static com.supercom.androidxapp.biometrics.BiometricsFailure.FailureType.ERROR;
import static com.supercom.androidxapp.biometrics.BiometricsFailure.FailureType.FAILURE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.concurrent.Executor;

public class BiometricsAuth {
    private final String TAG = "BiometricsAuth";
    private final FragmentActivity hostActivity;
    private final Context context;
    private BiometricPrompt biometricPrompt;

    public BiometricsAuth(FragmentActivity activity, Context context) {
        this.hostActivity = activity;
        this.context = context;
    }

    /**
     * Prompts the user to create credentials that your app accepts.
     */
    public boolean enroll(){
        final Intent enrollIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Log.d(TAG, "Trying to enroll user for biometrics.");
            enrollIntent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
            hostActivity.startActivity(enrollIntent);
            return true;
        }
        else{
            Log.d(TAG, "Cannot enroll user. SDK is lower than 30");
            return false;
        }
    }

    /**
     * This Function simplifies the biometrics check and returns a check result.
     * @return 5 possible results depending on the check result:
     * BIOMETRIC_SUCCESS,
     * BIOMETRIC_ERROR_NO_HARDWARE,
     * BIOMETRIC_ERROR_HW_UNAVAILABLE,
     * BIOMETRIC_UNKNOWN_ERROR
     *  and
     * BIOMETRIC_ERROR_NONE_ENROLLED.
     */
    //Suppressed because of default fallback.
    @SuppressLint("SwitchIntDef")
    public BiometricStatus checkForBiometrics() {
        BiometricManager biometricManager = BiometricManager.from(context);
        switch (biometricManager.canAuthenticate()) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Log.d(TAG, "App can authenticate using biometrics.");
                return BiometricStatus.BIOMETRIC_SUCCESS;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Log.e(TAG, "No biometric features available on this device.");
                return BiometricStatus.BIOMETRIC_ERROR_NO_HARDWARE;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Log.e(TAG, "Biometric features are currently unavailable.");
                return BiometricStatus.BIOMETRIC_ERROR_HW_UNAVAILABLE;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Log.e(TAG, "Biometric features are not set on this device.");
                return BiometricStatus.BIOMETRIC_ERROR_NONE_ENROLLED;
            default:
                //BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED
                //BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED
                //BiometricManager.BIOMETRIC_STATUS_UNKNOWN

                Log.e(TAG, "Unknown error in biometrics.");
                return BiometricStatus.BIOMETRIC_UNKNOWN_ERROR;
        }
    }

    /**
     * Holds the possible statuses of the device's biometrics
     */
    public enum BiometricStatus {
        BIOMETRIC_SUCCESS,
        BIOMETRIC_ERROR_NO_HARDWARE,
        BIOMETRIC_ERROR_HW_UNAVAILABLE,
        BIOMETRIC_UNKNOWN_ERROR,
        BIOMETRIC_ERROR_NONE_ENROLLED,
    }

    /**
     * Sets the callbacks for the authentication process.
     * @see BiometricsAuthenticationListener
     * @param callback The callback for the outcome of the authentication process.
     * @implNote MUST be called before <code>prompt</code>
     */
    private void setOnAuthenticationListener(BiometricsAuthenticationListener callback){
        Executor executor;

        executor = ContextCompat.getMainExecutor(context);
        biometricPrompt = new BiometricPrompt(hostActivity,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                BiometricsFailure failure = new BiometricsFailure(
                        ERROR,
                        errorCode,
                        errString.toString()
                );
                callback.onAuthenticationFailed(failure);
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                callback.onAuthenticationSucceeded(result);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                BiometricsFailure failure = new BiometricsFailure(
                        FAILURE,
                        -1,
                        null
                );
                callback.onAuthenticationFailed(failure);
            }
        });
    }

    /**
     * Prompts the user with the system UI of the authentication.
     * @param promptInfo The configuration of the UI and auth options.
     * @param callback The listener for the auth result
     * @see BiometricPrompt.PromptInfo
     * @see BiometricsAuthenticationListener
     * //@implNote MUST be called after <code>setOnAuthenticationListener</code>
     */
    public void prompt(BiometricPrompt.PromptInfo promptInfo, BiometricsAuthenticationListener callback){
        setOnAuthenticationListener(callback);
        biometricPrompt.authenticate(promptInfo);
    }

    public void cancelPrompt(){
          biometricPrompt.cancelAuthentication();
    }
}
