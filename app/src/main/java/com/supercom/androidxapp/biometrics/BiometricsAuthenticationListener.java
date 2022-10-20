package com.supercom.androidxapp.biometrics;
import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;

public interface BiometricsAuthenticationListener {
    /**
     * Gets called when the biometric authentication was successful.
     *
     * @param result The result of the authentication which contains the type, crypto etc.
     *               of the auth process.
     * @see BiometricPrompt.AuthenticationResult
     */
    void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result);

    /**
     * Gets called when the authentication failed or threw an error.
     * @param failure Holds the failure information.
     * @see BiometricsFailure
     */
    void onAuthenticationFailed(BiometricsFailure failure);
}
