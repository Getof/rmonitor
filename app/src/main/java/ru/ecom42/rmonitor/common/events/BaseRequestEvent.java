package ru.ecom42.rmonitor.common.events;

import android.os.CountDownTimer;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import ru.ecom42.rmonitor.common.utils.CommonUtils;

public class BaseRequestEvent {
    final BaseResultEvent resultEvent;
    CountDownTimer timer;
    public BaseRequestEvent(BaseResultEvent _resultEvent){
        resultEvent = _resultEvent;
        timer = new CountDownTimer(15000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.e("Time Passing","1");
            }

            @Override
            public void onFinish() {
                EventBus.getDefault().post(resultEvent);
            }
        }.start();
        CommonUtils.currentTimer = timer;
    }
}
