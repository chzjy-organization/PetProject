package com.punuo.pet.compat.splash;

import android.os.Bundle;

import com.alibaba.android.arouter.launcher.ARouter;
import com.punuo.pet.compat.R;
import com.punuo.pet.compat.process.ProcessTasks;
import com.punuo.pet.router.CompatRouter;
import com.punuo.pet.router.MemberRouter;
import com.punuo.sys.sdk.account.AccountManager;
import com.punuo.sys.sdk.activity.BaseActivity;
import com.punuo.sys.sdk.util.MMKVUtil;

/**
 * Created by han.chen.
 * Date on 2019-06-15.
 **/
public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_layout);
        ProcessTasks.commonLaunchTasks(getApplication());
        init();
    }

    private void init() {
        boolean isFirstRun = MMKVUtil.getBoolean("is_first_run", true);
        if (isFirstRun) {
            MMKVUtil.setBoolean("is_first_run", false);
            //TODO 第一次启动，可以做一些自定义的东西
        }
        getWindow().getDecorView().postDelayed(new Runnable() {//postDelayed:定时器，此处是2秒后执行;getDecorView():获取整个屏幕
            @Override
            public void run() {
                if (AccountManager.isLoginned()) {
                    ARouter.getInstance().build(CompatRouter.ROUTER_HOME_ACTIVITY).navigation();
                } else {
                    ARouter.getInstance().build(MemberRouter.ROUTER_LOGIN_ACTIVITY).navigation();
                }
                finish();
            }
        }, 2 * 1000);
    }
}
