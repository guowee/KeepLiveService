package com.uowee.keeplive.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.view.View;

import com.missile.daemon.DaemonEnv;
import com.missile.daemon.IntentWrapper;
import com.uowee.keeplive.service.SlaveService;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    AppCompatButton startBtn;
    AppCompatButton whiteBtn;
    AppCompatButton stopBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startBtn = findViewById(R.id.start_btn);
        whiteBtn = findViewById(R.id.white_btn);
        stopBtn = findViewById(R.id.stop_btn);

        startBtn.setOnClickListener(this);
        whiteBtn.setOnClickListener(this);
        stopBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_btn:
                SlaveService.sShouldStopService = false;
                DaemonEnv.startServiceMayBind(SlaveService.class);
                break;
            case R.id.white_btn:
                IntentWrapper.whiteListMatters(this, "轨迹跟踪服务的持续运行");
                break;
            case R.id.stop_btn:
                SlaveService.stopService();
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        IntentWrapper.onBackPressed(this);
    }
}
