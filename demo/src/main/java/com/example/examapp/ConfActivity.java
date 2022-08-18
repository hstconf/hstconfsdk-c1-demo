package com.example.examapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.infowarelab.conference.ConferenceApplication;
import com.infowarelab.conference.ui.activity.inconf.ConferenceActivity;
import com.infowarelab.conference.ui.activity.preconf.BaseFragment;
import com.infowarelab.conference.ui.activity.preconf.fragment.FragCreate;
import com.infowarelab.conference.ui.activity.preconf.fragment.FragHistory;
import com.infowarelab.conference.ui.activity.preconf.fragment.FragHistory;
import com.infowarelab.conference.ui.activity.preconf.fragment.FragJoin;
import com.infowarelab.conference.ui.activity.preconf.fragment.FragJoinById;
import com.infowarelab.conference.ui.activity.preconf.fragment.FragSignIn;
import com.infowarelab.hongshantongphone.ConfAPI;
import com.infowarelabsdk.conference.callback.CallbackManager;
import com.infowarelabsdk.conference.domain.LoginBean;
import com.infowarelabsdk.conference.transfer.Config;
import com.infowarelabsdk.conference.util.Constants;
import com.infowarelabsdk.conference.util.FileUtil;

public class ConfActivity extends AppCompatActivity implements BaseFragment.onSwitchPageListener {
    private static final String TAG = "InfowareLab.ConfActivity";
    private FragmentManager fragManager;
    private FragCreate fragCreate;
    private FragJoin fragJoin;
    private FragJoinById fragJoinById;
    private FragHistory fragHistory;
    private FragSignIn fragSignIn;

    private LinearLayout llBottomBar;
    private RelativeLayout rlRoot;

    private int currentFrag = 0;
    private boolean isSwitching;

    private boolean mReturnFromConf = false;
    private AlertDialog alertDialog = null;
    private boolean landscape = false;
    private int screenWidth = 0;
    private int screenHeight = 0;
    private final float screenRatio = 1.6f;

    @Override
    public void finish() {
        super.finish();
    }

    private void initOrientation() {

        DisplayMetrics dm = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getWindowManager().getDefaultDisplay().getRealMetrics(dm);
        }else{
            getWindowManager().getDefaultDisplay().getMetrics(dm);
        }

        int orientation = getOrientationState();

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            screenWidth = dm.widthPixels;
            screenHeight = dm.heightPixels;
        }
        else
        {
            screenWidth = dm.heightPixels;
            screenHeight = dm.widthPixels;
        }

        if (screenHeight/screenWidth > screenRatio){
            //竖屏
            if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }

            landscape = false;
        }
        else
        {
            if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }

            landscape = true;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent != null)
            mReturnFromConf = intent.getBooleanExtra("returnFromConf", false);

        initOrientation();

        if (landscape)
            setContentView(R.layout.activity_conf_land);
        else
            setContentView(R.layout.activity_conf);

        getSupportActionBar().hide();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ConferenceApplication.setStatusBarColor(this, getColor(R.color.app_main_hue));
        }

        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
        }

        initView();
    }

    @Override
    protected void onResume() {

        super.onResume();
    }

    private int getOrientationState() {
        Configuration mConfiguration = this.getResources().getConfiguration();
        return mConfiguration.orientation;
    }


    private void initView() {

        showLoadingDialog();

        fragManager = getSupportFragmentManager();

        rlRoot = (RelativeLayout)findViewById(R.id.rl_root);
        llBottomBar = (LinearLayout)findViewById(R.id.ll_bottom);

        if (!landscape) {

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) llBottomBar.getLayoutParams();
            params.width = screenWidth;
            params.height = (int) (params.width * 0.172f);
            llBottomBar.setLayoutParams(params);
        }
        else {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) llBottomBar.getLayoutParams();
            params.height = screenWidth;
            params.width = (int) (params.height * 0.1208333f);
            llBottomBar.setLayoutParams(params);
        }

        rlRoot.post(new Runnable() {
            @Override
            public void run() {
                initConfEngine();
                switchFrag(2);

                dismissLoadingDialog();
            }
        });
    }


    private void initConfEngine() {

        if (mReturnFromConf) return;

        Config.Site_URL = FileUtil.readSharedPreferences(ConfActivity.this,
                Constants.SHARED_PREFERENCES, Constants.SITE);
        Config.SiteName = FileUtil.readSharedPreferences(ConfActivity.this, Constants.SHARED_PREFERENCES, Constants.SITE_NAME);
        Config.HAS_LIVE_SERVER = FileUtil.readSharedPreferences(ConfActivity.this, Constants.SHARED_PREFERENCES, Constants.HAS_LIVE_SERVER).equals("true");

        //如果会议还在进行中
        if (!CallbackManager.IS_LEAVED) {
            Intent intent = new Intent(ConfActivity.this, ConferenceActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        //检查输入服务器地址和账号信息
        if (!checkSiteAndAccount()) {
            finish();
            return;
        }
    }

    private boolean checkSiteAndAccount() {

        Intent intent = getIntent();

        if (intent != null) {

            final String site = intent.getStringExtra("site");
            final String loginPass = intent.getStringExtra("password");
            final String loginName = intent.getStringExtra("userName");
            String joinName = intent.getStringExtra("joinName");

            if (site == null || site.isEmpty()) return false;

            if (loginName == null || loginName.isEmpty()) return false;

            if (joinName == null || joinName.isEmpty()) joinName = loginName;

            if (!ConfAPI.getInstance().initSite(site, ConfActivity.this)) {
                dismissLoadingDialog();
                showToast("连接服务器失败。");
                return false;
            } else {
                showToast("连接服务器成功。");
            }

            ConfAPI.getInstance().clearLoginCookie();

            LoginBean loginBean = ConfAPI.getInstance().login(loginName, loginPass);
            if (loginBean == null) {
                dismissLoadingDialog();
                showToast("登录失败。");
                return false;
            } else {
                showToast("登录成功。");
            }
        }
        else {
            showToast("非法参数！");
            finish();
            return false;
        }

        return true;
    }

    private void switchFrag(int which) {
        if (currentFrag == which)
            return;

        if (which == 4)
            return;

        isSwitching = true;
        FragmentTransaction ft;
        ft = fragManager.beginTransaction();

        if (fragCreate != null && fragCreate.isAdded())
            ft.hide(fragCreate);

        if (fragJoin != null && fragJoin.isAdded())
            ft.hide(fragJoin);

        if (fragJoinById != null && fragJoinById.isAdded())
            ft.hide(fragJoinById);

        if (fragHistory != null && fragHistory.isAdded())
            ft.hide(fragHistory);

        if (fragSignIn != null && fragSignIn.isAdded())
            ft.hide(fragSignIn);

        switch (which) {
            case 1:
                //发起会议
                if (fragCreate == null) {
                    fragCreate = new FragCreate();
                    fragCreate.setOnSwitchPageListener(this);
                    //fragCreate.setBaseFragmentActivity(this);
                    ft.add(R.id.act_home_fl_container, fragCreate, "Create");
                } else {
                    ft.show(fragCreate);
                }
                currentFrag = 1;
                break;
            case 2:
                //以会议列表的方式加入会议
                //llTitleJoin.setVisibility(View.VISIBLE);
                if (fragJoin == null) {
                    fragJoin = new FragJoin();
                    fragJoin.setOnSwitchPageListener(this);
                    //fragJoin.setBaseFragmentActivity(this);
                    ft.add(R.id.act_home_fl_container, fragJoin, "Join");
                } else {
                    ft.show(fragJoin);
                }
                currentFrag = 2;
                break;
            case 3:
                //输入会议号的方式加入会议
                if (fragJoinById == null) {
                    fragJoinById = new FragJoinById();
                    fragJoinById.setOnSwitchPageListener(this);
                    //fragJoin.setBaseFragmentActivity(this);
                    ft.add(R.id.act_home_fl_container, fragJoinById, "JoinById");
                } else {
                    ft.show(fragJoinById);
                }
                currentFrag = 3;
                break;
            case 5:
                //历史会议
                if (fragHistory == null) {
                    fragHistory = new FragHistory();
                    fragHistory.setOnSwitchPageListener(this);
                    //fragJoin.setBaseFragmentActivity(this);
                    ft.add(R.id.act_home_fl_container, fragHistory, "FragHistory");
                } else {
                    ft.show(fragHistory);
                }
                currentFrag = 5;
                break;
            case 6:
                //人脸签到
                if (fragSignIn == null) {
                    fragSignIn = new FragSignIn();
                    fragSignIn.setOnSwitchPageListener(this);
                    ft.add(R.id.act_home_fl_container, fragSignIn, "FragSignIn");
                } else {
                    ft.show(fragSignIn);
                }
                currentFrag = 6;
                break;
            default:
                break;
        }
        ft.commit();
        isSwitching = false;
    }

    @Override
    public void onBackPressed() {

//        if (fragJoin != null && fragJoin.getCurrentItem() == 1 && fragJoin.isEnterFromItem()) {
//            fragJoin.setCurrentItem(0);
//            fragJoin.setEnterFromItem(false);
//            return;
//        }

        if (currentFrag == 1 || currentFrag == 3 || currentFrag == 4 || currentFrag == 6) {
            switchFrag(2);
            return;
        }

        super.onBackPressed();
    }

    @Override
    public void doSelect(int position) {
        switchFrag(position);
    }

    public void showLoadingDialog() {

        alertDialog = new AlertDialog.Builder(this).create();

        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable());
        alertDialog.setCancelable(false);
        alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_SEARCH || keyCode == KeyEvent.KEYCODE_BACK)
                    return true;
                return false;
            }
        });

        alertDialog.show();

        alertDialog.setContentView(com.example.examapp.R.layout.loading_alert);
        alertDialog.setCanceledOnTouchOutside(false);

    }


    public void dismissLoadingDialog() {
        if (null != alertDialog && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }

    private void showToast(final String msg) {

        if (alertDialog != null && alertDialog.isShowing()){
            ((TextView)alertDialog.findViewById(com.example.examapp.R.id.tv_message)).setText(msg);
        }
        else
            Toast.makeText(ConfActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

}
