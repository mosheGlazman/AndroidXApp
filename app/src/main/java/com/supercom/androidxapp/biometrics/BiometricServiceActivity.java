package com.supercom.androidxapp.biometrics;

import static com.supercom.androidxapp.biometrics.BiometricsManager.ACTION_Authenticate;
import static com.supercom.androidxapp.biometrics.BiometricsManager.ACTION_Authenticate_cancel;
import static com.supercom.androidxapp.biometrics.BiometricsManager.ACTION_Authenticate_failed;
import static com.supercom.androidxapp.biometrics.BiometricsManager.ACTION_Authenticate_success;
import static com.supercom.androidxapp.biometrics.BiometricsManager.ACTION_Enroll;
import static com.supercom.androidxapp.biometrics.BiometricsManager.ACTION_Status;
import static com.supercom.androidxapp.biometrics.BiometricsManager.ACTION_Status_Result;
import static com.supercom.androidxapp.biometrics.BiometricsManager.ACTION_error;
import static com.supercom.androidxapp.biometrics.BiometricsManager.ERROR_no_status;
import static com.supercom.androidxapp.biometrics.BiometricsManager.EXTRA_error_code;
import static com.supercom.androidxapp.biometrics.BiometricsManager.EXTRA_error_message;
import static com.supercom.androidxapp.biometrics.BiometricsManager.EXTRA_status;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.supercom.androidxapp.R;
import com.supercom.androidxapp.biometrics.BiometricsManager;

import java.util.Date;

public class BiometricServiceActivity extends AppCompatActivity {
    BroadcastReceiver biometricReceiver;
    String action;
    boolean doAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BiometricsManager.getInstance().init(this);
        registerToCancelBroadcasts();

        if(getIntent()!=null){
            action=getIntent().getStringExtra(BiometricsManager.EXTRA_action);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterToCancelBroadcasts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(getIntent()!=null){
            action=getIntent().getStringExtra(BiometricsManager.EXTRA_action);
        }
        if(!doAction){
            doAction=true;
            if(action==null || action.length()==0){
                finish();
                return;
            }

            if (action.equals(ACTION_Enroll)) {
                enroll();
            } else if (action.equals(ACTION_Authenticate)) {
                askAuthenticate();
            } else if (action.equals(ACTION_Status)) {
                askStatus();
            }else{
                finish();
            }
        }else{
            finish();
        }
    }

    private void registerToCancelBroadcasts() {
        biometricReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                BiometricsManager.getInstance().cancelAuthenticate();
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_Authenticate_cancel);
        registerReceiver(biometricReceiver, filter);
    }

    private void askStatus() {
        if (BiometricsManager.getInstance().getStatus() != null) {
            Intent intent = new Intent(ACTION_Status_Result);
            intent.putExtra(EXTRA_status, BiometricsManager.getInstance().getStatus().ordinal());
            sendBroadcast(intent);
        } else {
            Intent intent = new Intent(ACTION_error);
            intent.putExtra(EXTRA_error_message, "No status");
            intent.putExtra(EXTRA_error_code, ERROR_no_status);
            sendBroadcast(intent);
        }

        finish();
    }

    private void askAuthenticate() {
        BiometricsManager.getInstance().setListener(new BiometricsManager.BiometricsListener() {
            @Override
            public void onAuthenticate() {
                sendBroadcast(new Intent(ACTION_Authenticate_success));
                finish();
            }

            @Override
            public void onAuthenticateFailed() {
                Intent intent = new Intent(ACTION_Authenticate_failed);
                sendBroadcast(intent);
                finish();
            }

            @Override
            public void onError(String error, int errorCode) {
                Intent intent = new Intent(ACTION_error);
                intent.putExtra(EXTRA_error_message, error);
                intent.putExtra(EXTRA_error_code, errorCode);
                sendBroadcast(intent);
            }
        });
        BiometricsManager.getInstance().askAuthenticate();
    }

    private void enroll() {
        BiometricsManager.getInstance().enroll();
    }

    private void unregisterToCancelBroadcasts() {
        unregisterReceiver(biometricReceiver);
    }

}