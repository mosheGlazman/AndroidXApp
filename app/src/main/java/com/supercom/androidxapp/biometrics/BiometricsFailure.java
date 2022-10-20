package com.supercom.androidxapp.biometrics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BiometricsFailure {
    @NonNull
    private final FailureType failureType;
    private final int errorCode;
    @Nullable
    private final String errorMessage;

    /**
     * Holds the possible types of biometric auth failure:
     * 1. Error: The authentication started but threw an error in the process.
     * 2. Failure: Could not authenticate the user
     */
    public enum FailureType {
        ERROR,
        FAILURE,
    }

    public BiometricsFailure(@NonNull FailureType failureType, int errorCode,@Nullable String errorMessage) {
        this.failureType = failureType;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    @NonNull
    public FailureType getFailureType() {
        return failureType;
    }

    public int getErrorCode() {
        return errorCode;
    }

    @Nullable
    public String getErrorMessage() {
        return errorMessage;
    }
}
