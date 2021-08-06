package ru.ecom42.rmonitor.common.components;

import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.transition.Fade;
import androidx.transition.TransitionInflater;

import org.greenrobot.eventbus.EventBus;

import ru.ecom42.rmonitor.R;

public class BaseActivity extends AppCompatActivity {
    public ActionBar toolbar;
    public float screenDensity;
    private boolean isImmersive = false;
    public EventBus eventBus;
    public boolean registerEventBus = true;
    public boolean isInForeground = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(registerEventBus)
            eventBus = EventBus.getDefault();
//        setupWindowAnimations();
        screenDensity = getApplicationContext().getResources().getDisplayMetrics().density;
        setActivityTheme(BaseActivity.this);
    }

//    public void initializeToolbar(String title) {
//        Toolbar toolbarView = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbarView);
//        toolbar = getSupportActionBar();
//        if (toolbar != null) {
//            toolbar.setDisplayHomeAsUpEnabled(true);
//            toolbar.setTitle(title);
//            toolbarView.setNavigationOnClickListener(v -> onBackPressed());
//        }
//    }

    public int getAccentColor() {
        TypedValue typedValue = new TypedValue();

        TypedArray a = this.obtainStyledAttributes(typedValue.data, new int[] { R.attr.colorAccent });
        int color = a.getColor(0, 0);

        a.recycle();

        return color;
    }

    public int getPrimaryColor() {
        TypedValue typedValue = new TypedValue();

        TypedArray a = this.obtainStyledAttributes(typedValue.data, new int[] { R.attr.colorPrimary });
        int color = a.getColor(0, 0);

        a.recycle();

        return color;
    }

//    public void setupWindowAnimations() {
//        getWindow().setAllowEnterTransitionOverlap(false);
//        getWindow().setAllowReturnTransitionOverlap(false);
//        Fade fade = (Fade) TransitionInflater.from(this).inflateTransition(R.transition.fade);
//        getWindow().setEnterTransition(fade);
//        getWindow().setExitTransition(fade);
//    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus & isImmersive) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }
    public int getCurrentTheme() {
        switch (getString(R.string.theme)) {
            case ("Amaranth"):
                return R.style.AppTheme_Amaranth;
            case ("Red"):
                return R.style.AppTheme_Red;
            case ("Amber"):
                return R.style.AppTheme_Amber;
            case ("Blue"):
                return R.style.AppTheme_Blue;
            case ("Brown"):
                return R.style.AppTheme_Brown;
            case ("BlueGrey"):
                return R.style.AppTheme_BlueGrey;
            case ("Indigo"):
                return R.style.AppTheme_Indigo;
            case ("Cyan"):
                return R.style.AppTheme_Cyan;
            default:
                return R.style.AppTheme_Amaranth;
        }
    }

    private void setActivityTheme(AppCompatActivity activity) {
        activity.setTheme(getCurrentTheme());
    }
    @Override
    public void onStart() {
        super.onStart();
        if(registerEventBus)
            eventBus.register(this);
    }

    @Override
    public void onStop() {
        if(registerEventBus)
            eventBus.unregister(this);
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isInForeground = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isInForeground = false;

    }

    public int convertDPToPixel(int dp) {
        return (int) (dp * (screenDensity));
    }

    @Override
    public void setImmersive(boolean immersive) {
        isImmersive = immersive;
    }
}
