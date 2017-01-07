package com.example.administrator.loginandregister.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.loginandregister.R;
import com.example.administrator.loginandregister.global.AppConstants;
import com.example.administrator.loginandregister.utils.LogUtils;
import com.example.administrator.loginandregister.utils.ProgressDialogUtils;
import com.example.administrator.loginandregister.utils.RegexUtils;
import com.example.administrator.loginandregister.utils.ShareUtils;
import com.example.administrator.loginandregister.utils.SpUtils;
import com.example.administrator.loginandregister.utils.ToastUtils;
import com.example.administrator.loginandregister.utils.UrlConstance;
import com.example.administrator.loginandregister.utils.Utils;
import com.example.administrator.loginandregister.views.CleanEditText;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners.UMAuthListener;
import com.umeng.socialize.controller.listener.SocializeListeners.UMDataListener;
import com.umeng.socialize.exception.SocializeException;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static android.view.View.OnClickListener;

/**
 * Created by JimCharles on 2016/11/27.
 */

public class LoginActivity extends Activity implements OnClickListener,UrlConstance {

    private static final String TAG = "loginActivity";
    private static final int REQUEST_CODE_TO_REGISTER = 0x001;

    // 界面控件
    private CleanEditText accountEdit;
    private CleanEditText passwordEdit;
    private Button loginButton;

    // 第三方平台获取的访问token，有效时间，uid
    private String accessToken;
    private String expires_in;
    private String uid;
    private String sns;

    String result = "";
    // 整个平台的Controller，负责管理整个SDK的配置、操作等处理
    private UMSocialService mController = UMServiceFactory
            .getUMSocialService(AppConstants.DESCRIPTOR);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        // 配置分享平台
        ShareUtils.configPlatforms(this);
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        loginButton = (Button) findViewById(R.id.btn_login);
        loginButton.setOnClickListener(this);
        accountEdit = (CleanEditText) this.findViewById(R.id.et_email_phone);
        accountEdit.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        accountEdit.setTransformationMethod(HideReturnsTransformationMethod
                .getInstance());
        passwordEdit = (CleanEditText) this.findViewById(R.id.et_password);
        passwordEdit.setImeOptions(EditorInfo.IME_ACTION_DONE);
        passwordEdit.setImeOptions(EditorInfo.IME_ACTION_GO);
        passwordEdit.setTransformationMethod(PasswordTransformationMethod
                .getInstance());
        passwordEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE
                        || actionId == EditorInfo.IME_ACTION_GO) {
                    clickLogin();
                }
                return false;
            }
        });
    }

    private void clickLogin() {
        String account = accountEdit.getText().toString();
        String password = passwordEdit.getText().toString();

        if (checkInput(account, password)) {
            // TODO: 请求服务器登录账号
//                Login(account,password);
            loginButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    //android4.0后的新的特性，网络数据请求时不能放在主线程中。
                    //使用线程执行访问服务器，获取返回信息后通知主线程更新UI或者提示信息。
                    final Handler handler = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            if (msg.what == 1) {
                                //提示读取结果
                                Toast.makeText(LoginActivity.this, result, Toast.LENGTH_LONG).show();
                                ToastUtils.showShort(LoginActivity.this,
                                        result);
                                if (result.contains("！")) {
                                    Toast.makeText(LoginActivity.this, result, Toast.LENGTH_LONG).show();
                                    ToastUtils.showShort(LoginActivity.this,
                                            "密码错误......");
                                } else {
                                    final Intent it = new Intent(LoginActivity.this, WelcomActivity.class); //你要转向的Activity
                                    Timer timer = new Timer();
                                    TimerTask task = new TimerTask() {
                                        @Override
                                        public void run() {
                                            startActivity(it); //执行
                                        }
                                    };
                                    timer.schedule(task, 1000); //1秒后
                                }
                            }
                        }
                    };
                    // 启动线程来执行任务
                    new Thread() {
                        public void run() {
                            //请求网络
                            try {
                                Login(accountEdit.getText().toString(),passwordEdit.getText().toString());
                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                            }
                            Message m = new Message();
                            m.what = 1;
                            // 发送消息到Handler
                            handler.sendMessage(m);
                        }
                    }.start();
                }
            });
            }
    }

    public Boolean Login(String account, String passWord) throws IOException, JSONException {
        try {
            String httpUrl="http://cdz.ittun.cn/cdz/user_login.php";
            URL url = new URL(httpUrl);//创建一个URL
            HttpURLConnection connection  = (HttpURLConnection)url.openConnection();//通过该url获得与服务器的连接
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");//设置请求方式为post
            connection.setConnectTimeout(3000);//设置超时为3秒
            //设置传送类型
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Charset", "utf-8");
            //提交数据
            String data = "&passwd=" + URLEncoder.encode(passWord, "UTF-8")+ "&number=" + URLEncoder.encode(account, "UTF-8")+ "&cardid=";//传递的数据
            connection.setRequestProperty("Content-Length",String.valueOf(data.getBytes().length));
            ToastUtils.showShort(this,
                    "数据提交成功......");
            //获取输出流
            OutputStream os = connection.getOutputStream();
            os.write(data.getBytes());
            os.flush();
            //获取响应输入流对象
            InputStreamReader is = new InputStreamReader(connection.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(is);
            StringBuffer strBuffer = new StringBuffer();
            String line = null;
            //读取服务器返回信息
            while ((line = bufferedReader.readLine()) != null){
                strBuffer.append(line);
            }
            result = strBuffer.toString();
            is.close();
            connection.disconnect();
        } catch (Exception e) {
            return true;
        }
        return false;
    }

    /**
     * 检查输入
     *
     * @param account
     * @param password
     * @return
     */
    public boolean checkInput(String account, String password) {
        // 账号为空时提示
        if (account == null || account.trim().equals("")) {
            Toast.makeText(this, R.string.tip_account_empty, Toast.LENGTH_LONG)
                    .show();
        } else {
            // 账号不匹配手机号格式（11位数字且以1开头）
            if ( !RegexUtils.checkMobile(account)) {
                Toast.makeText(this, R.string.tip_account_regex_not_right,
                        Toast.LENGTH_LONG).show();
            } else if (password == null || password.trim().equals("")) {
                Toast.makeText(this, R.string.tip_password_can_not_be_empty,
                        Toast.LENGTH_LONG).show();
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.iv_cancel:
                finish();
                break;
            case R.id.btn_login:
                clickLogin();
                break;
            case R.id.iv_wechat:
                clickLoginWexin();
                break;
            case R.id.iv_qq:
                clickLoginQQ();
                break;
            case R.id.iv_sina:
                loginThirdPlatform(SHARE_MEDIA.SINA);
                break;
            case R.id.tv_create_account:
                enterRegister();
                break;
            case R.id.tv_forget_password:
                enterForgetPwd();
                break;
            default:
                break;
        }
    }

    /**
     * 点击使用QQ快速登录
     */
    private void clickLoginQQ() {
        if (!Utils.isQQClientAvailable(this)) {
            ToastUtils.showShort(LoginActivity.this,
                    getString(R.string.no_install_qq));
        } else {
            loginThirdPlatform(SHARE_MEDIA.QZONE);
        }
    }

    /**
     * 点击使用微信登录
     */
    private void clickLoginWexin() {
        if (!Utils.isWeixinAvilible(this)) {
            ToastUtils.showShort(LoginActivity.this,
                    getString(R.string.no_install_wechat));
        } else {
            loginThirdPlatform(SHARE_MEDIA.WEIXIN);
        }
    }

    /**
     * 跳转到忘记密码
     */
    private void enterForgetPwd() {
        Intent intent = new Intent(this, ForgetPasswordActivity.class);
        startActivity(intent);
    }

    /**
     * 跳转到注册页面
     */
    private void enterRegister() {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivityForResult(intent, REQUEST_CODE_TO_REGISTER);
    }

    /**
     * 授权。如果授权成功，则获取用户信息
     *
     * @param platform
     */
    private void loginThirdPlatform(final SHARE_MEDIA platform) {
        mController.doOauthVerify(LoginActivity.this, platform,
                new UMAuthListener() {

                    @Override
                    public void onStart(SHARE_MEDIA platform) {
                        LogUtils.i(TAG, "onStart------"
                                + Thread.currentThread().getId());
                        ProgressDialogUtils.getInstance().show(
                                LoginActivity.this,
                                getString(R.string.tip_begin_oauth));
                    }

                    @Override
                    public void onError(SocializeException e,
                                        SHARE_MEDIA platform) {
                        LogUtils.i(TAG, "onError------"
                                + Thread.currentThread().getId());
                        ToastUtils.showShort(LoginActivity.this,
                                getString(R.string.oauth_fail));
                        ProgressDialogUtils.getInstance().dismiss();
                    }

                    @Override
                    public void onComplete(Bundle value, SHARE_MEDIA platform) {
                        LogUtils.i(TAG, "onComplete------" + value.toString());
                        if (platform == SHARE_MEDIA.SINA) {
                            accessToken = value.getString("access_key");
                        } else {
                            accessToken = value.getString("access_token");
                        }
                        expires_in = value.getString("expires_in");
                        // 获取uid
                        uid = value.getString(AppConstants.UID);
                        if (!TextUtils.isEmpty(uid)) {
                            // uid不为空，获取用户信息
                            getUserInfo(platform);
                        } else {
                            ToastUtils.showShort(LoginActivity.this,
                                    getString(R.string.oauth_fail));
                        }
                    }

                    @Override
                    public void onCancel(SHARE_MEDIA platform) {
                        LogUtils.i(TAG, "onCancel------"
                                + Thread.currentThread().getId());
                        ToastUtils.showShort(LoginActivity.this,
                                getString(R.string.oauth_cancle));
                        ProgressDialogUtils.getInstance().dismiss();

                    }
                });
    }

    /**
     * 获取用户信息
     *
     * @param platform
     */
    private void getUserInfo(final SHARE_MEDIA platform) {
        mController.getPlatformInfo(LoginActivity.this, platform,
                new UMDataListener() {

                    @Override
                    public void onStart() {
                        // 开始获取
                        LogUtils.i("getUserInfo", "onStart------");
                        ProgressDialogUtils.getInstance().dismiss();
                        ProgressDialogUtils.getInstance().show(
                                LoginActivity.this, "正在请求...");
                    }

                    @Override
                    public void onComplete(int status, Map<String, Object> info) {

                        try {
                            String sns_id = "";
                            String sns_avatar = "";
                            String sns_loginname = "";
                            if (info != null && info.size() != 0) {
                                LogUtils.i("third login", info.toString());

                                if (platform == SHARE_MEDIA.SINA) { // 新浪微博
                                    sns = AppConstants.SINA;
                                    if (info.get(AppConstants.UID) != null) {
                                        sns_id = info.get(AppConstants.UID)
                                                .toString();
                                    }
                                    if (info.get(AppConstants.PROFILE_IMAGE_URL) != null) {
                                        sns_avatar = info
                                                .get(AppConstants.PROFILE_IMAGE_URL)
                                                .toString();
                                    }
                                    if (info.get(AppConstants.SCREEN_NAME) != null) {
                                        sns_loginname = info.get(
                                                AppConstants.SCREEN_NAME)
                                                .toString();
                                    }
                                } else if (platform == SHARE_MEDIA.QZONE) { // QQ
                                    sns = AppConstants.QQ;
                                    if (info.get(AppConstants.UID) == null) {
                                        ToastUtils
                                                .showShort(
                                                        LoginActivity.this,
                                                        getString(R.string.oauth_fail));
                                        return;
                                    }
                                    sns_id = info.get(AppConstants.UID)
                                            .toString();
                                    sns_avatar = info.get(
                                            AppConstants.PROFILE_IMAGE_URL)
                                            .toString();
                                    sns_loginname = info.get(
                                            AppConstants.SCREEN_NAME)
                                            .toString();
                                } else if (platform == SHARE_MEDIA.WEIXIN) { // 微信
                                    sns = AppConstants.WECHAT;
                                    sns_id = info.get(AppConstants.OPENID)
                                            .toString();
                                    sns_avatar = info.get(
                                            AppConstants.HEADIMG_URL)
                                            .toString();
                                    sns_loginname = info.get(
                                            AppConstants.NICKNAME).toString();
                                }

                                // 这里直接保存第三方返回来的用户信息
                                SpUtils.putBoolean(LoginActivity.this,
                                        AppConstants.THIRD_LOGIN, true);

                                LogUtils.e("info", sns + "," + sns_id + ","
                                        + sns_loginname);

                                // TODO: 这里执行第三方连接(绑定服务器账号）

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                });
    }
}
