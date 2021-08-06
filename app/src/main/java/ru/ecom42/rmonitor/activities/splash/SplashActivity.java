package ru.ecom42.rmonitor.activities.splash;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.databinding.DataBindingUtil;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ru.ecom42.rmonitor.BuildConfig;
import ru.ecom42.rmonitor.MainActivity;
import ru.ecom42.rmonitor.R;
import ru.ecom42.rmonitor.common.components.BaseActivity;
import ru.ecom42.rmonitor.common.events.BackgroundServiceStartedEvent;
import ru.ecom42.rmonitor.common.events.ConnectEvent;
import ru.ecom42.rmonitor.common.events.ConnectResultEvent;
import ru.ecom42.rmonitor.common.events.LoginEvent;
import ru.ecom42.rmonitor.common.events.LoginResultEvent;
import ru.ecom42.rmonitor.common.models.Techdir;
import ru.ecom42.rmonitor.common.utils.AlertDialogBuilder;
import ru.ecom42.rmonitor.common.utils.AlerterHelper;
import ru.ecom42.rmonitor.common.utils.CommonUtils;
import ru.ecom42.rmonitor.common.utils.MyPreferenceManager;
import ru.ecom42.rmonitor.databinding.ActivitySplashBinding;
import ru.ecom42.rmonitor.services.RiderService;

public class SplashActivity extends BaseActivity {
    MyPreferenceManager SP;
    ActivitySplashBinding binding;
    int RC_SIGN_IN = 123;
    Handler locationTimeoutHandler;

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
                @Override
                public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
                    onSignInResult(result);
                }
            }
    );


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
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.PhoneBuilder().build());


        // Create and launch sign-in intent
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build();
        signInLauncher.launch(signInIntent);
//        startActivityForResult(
//                    AuthUI.getInstance()
//                            .createSignInIntentBuilder()
//                            .setAvailableProviders(
//                                    Collections.singletonList(
//                                            new AuthUI.IdpConfig.PhoneBuilder().build())
//                                    )
//                            .setTheme(getCurrentTheme())
//                            .build(),
//                    RC_SIGN_IN);
    };

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
                    String phone;
                    if (response != null) {
                        phone = response.getPhoneNumber();
                        assert phone != null;
                        tryLogin(phone);
                    }
        } else {
            AlerterHelper.showError(SplashActivity.this, getString(R.string.login_failed));
        }
    }



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

    private void startMainActivity() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginResultEvent(LoginResultEvent event) {
        if (event.hasError()) {
            event.showError(SplashActivity.this, result -> {
                if (result == AlertDialogBuilder.DialogResult.RETRY)
                    binding.loginButton.callOnClick();
                else
                    finish();
            });
            return;
        }
        CommonUtils.user = event.rider;
        SP.putString("user", event.riderJson);
        SP.putString("token", event.jwtToken);
        tryConnect();
    }

    public void tryConnect() {
        String token = SP.getString("token", null);
        if (token != null && !token.isEmpty()) {
            eventBus.post(new ConnectEvent(token));
        } else {
            binding.loginButton.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectedResult(ConnectResultEvent event) {
        if (event.hasError()) {
            binding.progressBar.setVisibility(View.INVISIBLE);
            event.showError(SplashActivity.this, result -> {
                if (result == AlertDialogBuilder.DialogResult.RETRY) {
                    eventBus.post(new ConnectEvent(SP.getString("token", null)));
                    binding.progressBar.setVisibility(View.VISIBLE);
                } else {
                    binding.loginButton.setVisibility(View.VISIBLE);
                }
            });
            return;
        }
        locationTimeoutHandler = new Handler();
        locationTimeoutHandler.postDelayed(this::startMainActivity, 5000);

        CommonUtils.user = Techdir.fromJson(SP.getString("user", "{}"));
    }

    @Subscribe
    public void onServiceStarted(BackgroundServiceStartedEvent event) {
        tryConnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        tryConnect();
    }

    private void tryLogin(String phone) {
        binding.progressBar.setVisibility(View.VISIBLE);
        if (phone.charAt(0) == '+')
            phone = phone.substring(1);
        eventBus.post(new LoginEvent(Long.parseLong(phone), BuildConfig.VERSION_CODE));
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == RC_SIGN_IN) {
//            if (resultCode == RESULT_OK) {
//                if (getResources().getBoolean(R.bool.use_custom_login)) {
//                    tryLogin(data.getStringExtra("mobile"));
//                } else {
//                    IdpResponse idpResponse = IdpResponse.fromResultIntent(data);
//                    String phone;
//                    if (idpResponse != null) {
//                        phone = idpResponse.getPhoneNumber();
//                        assert phone != null;
//                        tryLogin(phone);
//                        return;
//                    }
//                }
//
//            }
//            AlerterHelper.showError(SplashActivity.this, getString(R.string.login_failed));
//        }
//    }




}
