package com.example.examapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.infowarelab.conference.ui.activity.LogoActivity;
import com.infowarelab.conference.ui.activity.preconf.ActHome;
import com.infowarelab.hongshantongphone.ConfAPI;
import com.infowarelabsdk.conference.domain.LoginBean;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, EasyPermissions.PermissionCallbacks {
    private static final int RC_PERMISSIONS = 0;
    public EditText et_site, et_username, et_pwd, et_confid, et_confpwd, et_nickname, et_confTopic;
    private CheckBox cb_create;

    private AlertDialog alertDialog = null;

    private Handler notifyHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            switch (msg.what) {
                case ConfAPI.WM_SUCCESS:
                    dismissLoadingDialog();
                    showToast("加会成功。");
                    break;
                case ConfAPI.WM_ERROR:
                    dismissLoadingDialog();
                    showToast("加会失败。错误码：" + msg.arg1);
                    break;
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //setContentView(R.layout.activity_main_private);

        et_site = (EditText) findViewById(R.id.et_site);
        et_username = (EditText) findViewById(R.id.et_username);
        et_pwd = (EditText) findViewById(R.id.et_pwd);
        et_confid = (EditText) findViewById(R.id.et_confid);
        et_confpwd = (EditText) findViewById(R.id.et_confpwd);
        et_confTopic = (EditText) findViewById(R.id.et_confTopic);
        et_nickname = (EditText) findViewById(R.id.et_nickname);

        cb_create = (CheckBox) findViewById(R.id.cb_create);
        cb_create.setChecked(false);

        cb_create.setOnCheckedChangeListener(this);

        String[] perms = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            // Already have permission, do the thing
            // ...
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, getString(R.string.permission_rationale),
                    RC_PERMISSIONS, perms);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (b) {
            et_confid.setEnabled(false);
        } else {
            et_confid.setEnabled(true);
        }
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void showToast(final String msg) {

        if (alertDialog != null && alertDialog.isShowing()){
            ((TextView)alertDialog.findViewById(R.id.tv_message)).setText(msg);
        }
        else
            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
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

        alertDialog.setContentView(R.layout.loading_alert);
        alertDialog.setCanceledOnTouchOutside(false);

    }


    public void dismissLoadingDialog() {
        if (null != alertDialog && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }

    public void btnJoinClick(View view) {

        String site = et_site.getText().toString();
        String confId = et_confid.getText().toString();
        String confPwd = et_confpwd.getText().toString();
        String nickName = et_nickname.getText().toString();

        showLoadingDialog();

        new Handler().post(new Runnable() {
            @Override
            public void run() {

                ConfAPI.getInstance().setNotifyHandler(notifyHandler);

                if (!ConfAPI.getInstance().initSite(site, MainActivity.this)) {
                    dismissLoadingDialog();
                    showToast("连接服务器失败。");
                    return;
                } else {
                    showToast("连接服务器成功,正在加入会议...");
                }

                ConfAPI.getInstance().joinConf(0, confId, confPwd, nickName);

            }
        });

    }

    public void btnJoin2Click(View view) {

        String site = et_site.getText().toString();
        String confId = et_confid.getText().toString();
        String confPwd = et_confpwd.getText().toString();
        String userName = et_username.getText().toString();
        String pwd = et_pwd.getText().toString();

        showLoadingDialog();

        new Handler().post(new Runnable() {

            @Override
            public void run() {
                ConfAPI.getInstance().setNotifyHandler(notifyHandler);

                long start_time = System.currentTimeMillis();

                if (!ConfAPI.getInstance().initSite(site, MainActivity.this)) {
                    dismissLoadingDialog();
                    showToast("连接服务器失败。");
                    return;
                } else {
                    showToast("连接服务器成功。");
                }

                long end_time = System.currentTimeMillis();

                long spend_time = end_time - start_time;
                Log.d("InfowareLab.Perform","initSite: spend_time = " + spend_time);

                ConfAPI.getInstance().clearLoginCookie();

                start_time = System.currentTimeMillis();

                LoginBean loginBean = ConfAPI.getInstance().login(userName, pwd);
                if (loginBean == null) {
                    dismissLoadingDialog();
                    showToast("登录失败。");
                    return;
                } else {
                    showToast("登录成功,正在加入会议...");
                }

                end_time = System.currentTimeMillis();

                spend_time = end_time - start_time;

                Log.d("InfowareLab.Perform","login: spend_time = " + spend_time);

                start_time = System.currentTimeMillis();

                ConfAPI.getInstance().joinConf(loginBean.getUid(), confId, confPwd, loginBean.getUsername());

                end_time = System.currentTimeMillis();

                spend_time = end_time - start_time;

                Log.d("InfowareLab.Perform","joinConf: spend_time = " + spend_time);

            }
        });
    }

    public void btnCreateClick(View view) {
        String site = et_site.getText().toString();
        String confPwd = et_confpwd.getText().toString();
        String userName = et_username.getText().toString();
        String pwd = et_pwd.getText().toString();
        String confTopic = et_confTopic.getText().toString();

        showLoadingDialog();

        new Handler().post(new Runnable() {

            @Override
            public void run() {
                ConfAPI.getInstance().setNotifyHandler(notifyHandler);

                if (!ConfAPI.getInstance().initSite(site, MainActivity.this)) {
                    dismissLoadingDialog();
                    showToast("连接服务器失败。");
                    return;
                } else {
                    showToast("连接服务器成功。");
                }

                ConfAPI.getInstance().clearLoginCookie();

                LoginBean loginBean = ConfAPI.getInstance().login(userName, pwd);
                if (loginBean == null) {
                    dismissLoadingDialog();
                    showToast("登录失败。");
                    return;
                } else {
                    showToast("登录成功。");
                }

                String newConfId = ConfAPI.getInstance().createConf(confTopic, confPwd, 30);

                if (newConfId != null) {
                    showToast("创建会议成功,正在加入会议：" + newConfId);
                    et_confid.setText(newConfId);
                    et_confid.setTextColor(Color.RED);
                } else {
                    dismissLoadingDialog();
                    showToast("创建会议失败");
                }
            }
        });
    }

    public void btnChatClick(View view) {

        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
        startActivity(intent);
    }


    public void btnEnterConfListClick(View view) {

        String site = et_site.getText().toString();
        String userName = et_username.getText().toString();
        String pwd = et_pwd.getText().toString();
        String nickName = et_nickname.getText().toString();

        Intent intent = new Intent(MainActivity.this, ConfActivity.class);

        intent.putExtra("site",site);
        intent.putExtra("userName",userName);
        intent.putExtra("password",pwd);
        intent.putExtra("joinName",nickName);

        startActivity(intent);

        //ConfAPI.getInstance().launchConfListUI(MainActivity.this, site, userName, pwd, nickName);
    }
}
