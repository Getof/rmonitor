package ru.ecom42.rmonitor.common.utils;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import com.tapadoo.alerter.Alerter;

import ru.ecom42.rmonitor.R;

public class AlerterHelper {
    public static void showError(Context context, String message){
        Alerter.create((AppCompatActivity)context)
                .setTitle(R.string.error)
                .setText(message)
                .setIcon(R.drawable.ic_baseline_error_outline_24)
                .setBackgroundColorRes(R.color.accent_red)
                .show();
    }
    public static void showWarning(Context context,String message){
        Alerter.create((AppCompatActivity)context)
                .setTitle(R.string.warning)
                .setText(message)
                .setIcon(R.drawable.ic_baseline_warning_24)
                .setBackgroundColorRes(R.color.accent_orange)
                .show();
    }
    public static void showInfo(Context context,String message){
        Alerter.create((AppCompatActivity)context)
                .setTitle(R.string.info)
                .setText(message)
                .setIcon(R.drawable.ic_baseline_info_24)
                .setBackgroundColorRes(R.color.accent_cyan)
                .show();
    }

}
