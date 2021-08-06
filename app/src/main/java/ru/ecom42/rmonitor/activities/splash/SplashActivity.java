package ru.ecom42.rmonitor.activities.splash;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.firebase.ui.auth.AuthUI;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ru.ecom42.rmonitor.R;
import ru.ecom42.rmonitor.common.components.BaseActivity;
import ru.ecom42.rmonitor.common.utils.AlertDialogBuilder;
import ru.ecom42.rmonitor.common.utils.CommonUtils;
import ru.ecom42.rmonitor.common.utils.MyPreferenceManager;
import ru.ecom42.rmonitor.databinding.ActivitySplashBinding;
import ru.ecom42.rmonitor.services.RiderService;

public class SplashActivity extends BaseActivity {
    MyPreferenceManager SP;
    ActivitySplashBinding binding;
    int RC_SIGN_IN = 123;
    Handler locationTimeoutHandler;

    final PermissionListener permissionlistener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            boolean isServiceRunning = isMyServiceRunning();
            if (!isServiceRunning)
                startService(new Intent(SplashActivity.this, RiderService.class));
        }

        @Override
        public void onPermissionDenied(List<String> deniedPermissions) {
            boolean isServiceRunning = isMyServiceRunning();
            if (!isServiceRunning)
                startService(new Intent(SplashActivity.this, RiderService.class));
        }
    };

    final View.OnClickListener onLoginButtonClicked = v -> {
        startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(
                                    Collections.singletonList(
                                            new AuthUI.IdpConfig.PhoneBuilder().build())
                                    )
                            .setTheme(getCurrentTheme())
                            .build(),
                    RC_SIGN_IN);
    };

    private boolean isMyServiceRunning() {
        try {
            ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            if (manager != null) {
                for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                    if (RiderService.class.getName().equals(service.service.getClassName()))
                        return true;
                }
            }
        } catch (Exception exception) {
            AlertDialogBuilder.show(SplashActivity.this, exception.getMessage());
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setImmersive(true);
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = DataBindingUtil.setContentView(SplashActivity.this, R.layout.activity_splash);
        binding.loginButton.setOnClickListener(onLoginButtonClicked);
        SP = MyPreferenceManager.getInstance(getApplicationContext());
        checkPermissions();
    }

    private void checkPermissions() {
        if (!CommonUtils.isInternetEnabled(this)) {
            AlertDialogBuilder.show(this, getString(R.string.message_enable_wifi), AlertDialogBuilder.DialogButton.CANCEL_RETRY, result -> {
                if (result == AlertDialogBuilder.DialogResult.RETRY) {
                    checkPermissions();
                } else {
                    finishAffinity();
                }
            });
            return;
        }
        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage(getString(R.string.message_permission_denied))
                .setPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                .check();
    }


}
