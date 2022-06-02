package com.example.examapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.infowarelab.hongshantongphone.ChatAPI;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class ChatActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, EasyPermissions.PermissionCallbacks{
    private static final int RC_CAMERA_AND_LOCATION = 0;
    public EditText et_site,et_username, et_pwd, et_confid, et_confpwd, et_nickname, et_confTopic;
    private CheckBox cb_create;
    private TextView tv_message;

    private boolean inited = false;
    private CheckBox cb_conf_type;

    private ListView lv_friendList;
    private AlertDialog alertDialog = null;

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
        setContentView(R.layout.activity_chat);

        et_site =(EditText)findViewById(R.id.et_site);
        et_username = (EditText)findViewById(R.id.et_username);
        et_pwd = (EditText)findViewById(R.id.et_pwd);
        et_confid = (EditText)findViewById(R.id.et_confid);
        et_confpwd = (EditText)findViewById(R.id.et_confpwd);
        et_confTopic = (EditText)findViewById(R.id.et_confTopic);
        et_nickname = (EditText)findViewById(R.id.et_nickname);

        et_site.clearFocus();
        et_username.clearFocus();
        et_pwd.clearFocus();

        tv_message = (TextView) findViewById(R.id.tv_message);

        et_username.addTextChangedListener(new TextWatcher() {
                                               @Override
                                               public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                               }

                                               @Override
                                               public void onTextChanged(CharSequence s, int start, int before, int count) {
                                                    ChatAPI.getInstance().clearLoginCookie();
                                               }

                                               @Override
                                               public void afterTextChanged(Editable s) {
                                                   ChatAPI.getInstance().clearLoginCookie();
                                               }
                                           });

        et_pwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ChatAPI.getInstance().clearLoginCookie();
            }

            @Override
            public void afterTextChanged(Editable s) {
                ChatAPI.getInstance().clearLoginCookie();
            }
        });


        cb_create = (CheckBox)findViewById(R.id.cb_create);
        cb_create.setChecked(false);

        cb_create.setOnCheckedChangeListener(this);

        cb_conf_type = (CheckBox)findViewById(R.id.cb_conf_type);
        cb_conf_type.setChecked(false);

        cb_conf_type.setOnCheckedChangeListener(this);

        lv_friendList = findViewById(R.id.lv_friendlist);

        String[] perms = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_CONTACTS,Manifest.permission.CALL_PHONE,Manifest.permission.ACCESS_NETWORK_STATE,Manifest.permission.ACCESS_WIFI_STATE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            // Already have permission, do the thing
            // ...

        }
        else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, getString(R.string.permission_rationale),
                    RC_CAMERA_AND_LOCATION, perms);
        }
    }
    
    private void showMessage(String message){

        if (alertDialog != null && alertDialog.isShowing()){
            ((TextView)alertDialog.findViewById(R.id.tv_message)).setText(message);
        }
        else
            Toast.makeText(ChatActivity.this, message, Toast.LENGTH_SHORT).show();

    }

    private void showToast(String message){

       Toast.makeText(ChatActivity.this, message, Toast.LENGTH_SHORT).show();

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

        alertDialog = null;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

        if (compoundButton.getId() == R.id.cb_create) {
            if (b) {
                et_confid.setEnabled(false);
            } else {
                et_confid.setEnabled(true);
            }
        }
        else if (compoundButton.getId() == R.id.cb_conf_type)
        {
            ChatAPI.getInstance().setConfType(b? 1:0);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void btnInviteClick(View view) {

        showLoadingDialog();

        showMessage("正在启动视频聊天，请稍候...");

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                //初始化服务器地址
                if (!ChatAPI.getInstance().initSite(et_site.getText().toString(), ChatActivity.this)) {
                    dismissLoadingDialog();
                    showMessage("连接服务器失败...");
                    return;
                }

                showMessage("连接服务器成功。");

                showMessage("正在登录...");

                if (null == ChatAPI.getInstance().login(et_username.getText().toString(), et_pwd.getText().toString(), et_username.getText().toString())) {
                    dismissLoadingDialog();
                    showMessage("登录失败。");
                    return;
                }

                showMessage("登录成功。");

                //演示呼叫Fred
                String inviteeName = "Fred";

                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.callee_face);

                ChatAPI.getInstance().setConfType(cb_conf_type.isChecked() ? 1 : 0);

                String confId;
                if (cb_conf_type.isChecked())
                    confId = ChatAPI.getInstance().launchCreateConfUI("IM视频通话", et_confpwd.getText().toString(), 120, 1, inviteeName, bitmap);
                else
                    confId = ChatAPI.getInstance().launchCreateConfUI("IM视频通话", et_confpwd.getText().toString(), 120, 0, inviteeName, bitmap);

                if (confId != null) {
                    et_confid.setText(confId);
                    et_confid.setTextColor(Color.RED);
                }

                dismissLoadingDialog();
            }
        });
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        if (!inited){
            inited = true;
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }

    public void btnAcceptClick(View view) {

        showLoadingDialog();

        showMessage("正在启动视频聊天，请稍候...");

        new Handler().post(new Runnable() {
            @Override
            public void run() {

                //初始化服务器地址
                if (!ChatAPI.getInstance().initSite(et_site.getText().toString(), ChatActivity.this)) {
                    dismissLoadingDialog();
                    showMessage("连接服务器失败...");
                    return;
                }

                showMessage("连接服务器成功。");

                showMessage("正在登录...");

                if (null == ChatAPI.getInstance() .login(et_username.getText().toString(), et_pwd.getText().toString(), et_username.getText().toString())) {
                    dismissLoadingDialog();
                    showMessage("登录失败。");
                    return;
                }

                showMessage("登录成功。");

                //演示接受Frank的呼叫

                String inviterName = "Frank";

                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.callee_face);

                ChatAPI.getInstance().setConfType(cb_conf_type.isChecked() ? 1 : 0);

                if (cb_conf_type.isChecked())
                    ChatAPI.getInstance().launchJoinConfUI(et_confid.getText().toString(), et_confpwd.getText().toString(), 1, inviterName, bitmap);
                else
                    ChatAPI.getInstance().launchJoinConfUI(et_confid.getText().toString(), et_confpwd.getText().toString(), 0, inviterName, bitmap);

                dismissLoadingDialog();

            }
        });

    }

    public void btnInviteByConfIdClick(View view) {

        showLoadingDialog();

        showMessage("正在启动视频聊天，请稍候...");

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                //初始化服务器地址
                if (!ChatAPI.getInstance().initSite(et_site.getText().toString(), ChatActivity.this)) {
                    dismissLoadingDialog();
                    showToast("连接服务器失败...");
                    return;
                }

                showToast("连接服务器成功。");

                showToast("正在登录...");

                if (null == ChatAPI.getInstance().login(et_username.getText().toString(), et_pwd.getText().toString(),et_username.getText().toString())) {
                    dismissLoadingDialog();
                    showMessage("登录失败。");
                    return;
                }

                showToast("登录成功。");

                //演示呼叫Fred
                String inviteeName = "Fred";

                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.callee_face);

                ChatAPI.getInstance().setConfType(cb_conf_type.isChecked() ? 1 : 0);

                boolean result = false;
                if (cb_conf_type.isChecked())
                    result = ChatAPI.getInstance().launchStartConfUI(et_confid.getText().toString(), et_confpwd.getText().toString(), 1, inviteeName, bitmap);
                else
                    result = ChatAPI.getInstance().launchStartConfUI(et_confid.getText().toString(), et_confpwd.getText().toString(), 0, inviteeName, bitmap);

                if (result != true) {
                    showToast("发起聊天失败。");
                }

                dismissLoadingDialog();
            }
        });

    }
}
