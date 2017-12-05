我们都知道Android应用软件基本上都会用到登录注册功能，那么对一个一个好的登录注册模块进行封装就势在必行了。这里给大家介绍一下我的第一个项目中所用到的登录注册功能的，已经对其进行封装，希望能对大家有帮助，如果有什么错误或者改进的话希望各位可以指出。

我们都知道登录注册系列功能的实现有以下几步：

 - 注册账号 
 
 - 登录账号 （第三方账号登录）
 
 - 记住密码  
 
 - 自动登录
 
 - 修改密码

大体的流程如下

1. 对于需要获取用户登录状态的操作，先判断用户是否已经登录。 

2. 如果用户已经登录，则继续后面的操作，否则，跳转到登录页面进行登录。 

3. 如果已经有账号，则可以直接登录，或者可以直接选择第三方平台授权登录。 

4. 如果还没有账号，则需要先进行账号注册，注册成功后再登录；也可以不注册账号，通过第三方平台授权进行登录。 

5. 如果有账号，但忘记密码，可以重置密码，否则直接登录。

好了，一个登录注册系列的常用功能就是以上这五点了，大体流程也已经知道了，接下来让我们一个一个的实现它们。

---

## 注册功能的实现

注册时一般通过手机或者邮箱来注册，这里我选择利用手机号来注册；且注册时通常需要接收验证码，这里通过第三方的Mob平台的短信SDK来实现，第三方账号授权也是利用Mob的ShareSDK来实现的。注册完成后由客户端将注册信息提交至服务端进行注册，提交方式为HTTP的POST请求方式。

SignUpActivity.java

```
package com.example.administrator.loginandregister.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.example.administrator.loginandregister.R;
import com.example.administrator.loginandregister.utils.RegexUtils;
import com.example.administrator.loginandregister.utils.ToastUtils;
import com.example.administrator.loginandregister.utils.VerifyCodeManager;
import com.example.administrator.loginandregister.views.CleanEditText;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by JimCharles on 2016/11/27.
 */

public class SignUpActivity extends Activity implements OnClickListener {
    private static final String TAG = "SignupActivity";
    // 界面控件
    private CleanEditText phoneEdit;
    private CleanEditText passwordEdit;
    private CleanEditText verifyCodeEdit;
    private CleanEditText nicknameEdit;
    private Button getVerifiCodeButton;
    private Button createAccountButton;

    private VerifyCodeManager codeManager;
    String result = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        initViews();
        codeManager = new VerifyCodeManager(this, phoneEdit, getVerifiCodeButton);
    }

    /**
     * 通过findViewById,减少重复的类型转换
     *
     * @param id
     * @return
     */
    @SuppressWarnings("unchecked")
    public final <E extends View> E getView(int id) {
        try {
            return (E) findViewById(id);
        } catch (ClassCastException ex) {
            Log.e(TAG, "Could not cast View to concrete class.", ex);
            throw ex;
        }
    }

    private void initViews() {

        getVerifiCodeButton = getView(R.id.btn_send_verifi_code);
        getVerifiCodeButton.setOnClickListener(this);
        createAccountButton = getView(R.id.btn_create_account);
        createAccountButton.setOnClickListener(this);
        phoneEdit = getView(R.id.et_phone);
        phoneEdit.setImeOptions(EditorInfo.IME_ACTION_NEXT);// 下一步
        verifyCodeEdit = getView(R.id.et_verifiCode);
        verifyCodeEdit.setImeOptions(EditorInfo.IME_ACTION_NEXT);// 下一步
        nicknameEdit = getView(R.id.et_nickname);
        nicknameEdit.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        passwordEdit = getView(R.id.et_password);
        passwordEdit.setImeOptions(EditorInfo.IME_ACTION_DONE);
        passwordEdit.setImeOptions(EditorInfo.IME_ACTION_GO);
        passwordEdit.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                // 点击虚拟键盘的done
                if (actionId == EditorInfo.IME_ACTION_DONE
                        || actionId == EditorInfo.IME_ACTION_GO) {
                    try {
                        commit();
                    } catch (IOException | JSONException e1) {
                        e1.printStackTrace();
                    }
                }
                return false;
            }
        });
    }

    private void commit() throws IOException, JSONException {
        String phone = phoneEdit.getText().toString().trim();
        String password = passwordEdit.getText().toString().trim();

        if (checkInput(phone, password)) {
            // TODO:请求服务端注册账号
            createAccountButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    //android4.0后的新的特性，网络数据请求时不能放在主线程中。
                    //使用线程执行访问服务器，获取返回信息后通知主线程更新UI或者提示信息。
                    final Handler handler = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            if (msg.what == 1) {
                                //提示读取结果
                                Toast.makeText(SignUpActivity.this, result, Toast.LENGTH_LONG).show();
                                if (result.contains("成")){
                                    Toast.makeText(SignUpActivity.this, result, Toast.LENGTH_LONG).show();
                                    ToastUtils.showShort(SignUpActivity.this,
                                            "注册成功......");
                                }
                                else{
                                    final Intent it = new Intent(SignUpActivity.this, LoginActivity.class); //你要转向的Activity
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
                                Register(phoneEdit.getText().toString(),passwordEdit.getText().toString(),nicknameEdit.getText().toString());
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

    private boolean checkInput(String phone, String password) {
        if (TextUtils.isEmpty(phone)) { // 电话号码为空
            ToastUtils.showShort(this, R.string.tip_phone_can_not_be_empty);
        } else {
            if (!RegexUtils.checkMobile(phone)) { // 电话号码格式有误
                ToastUtils.showShort(this, R.string.tip_phone_regex_not_right);
            }  else if (password == null || password.trim().equals("")) {
                Toast.makeText(this, R.string.tip_password_can_not_be_empty,
                        Toast.LENGTH_LONG).show();
            }else if (password.length() < 6 || password.length() > 32
                    || TextUtils.isEmpty(password)) { // 密码格式
                ToastUtils.showShort(this,
                        R.string.tip_please_input_6_32_password);
            } else {
                return true;
            }
        }
        return false;
    }

    public Boolean Register(String account, String passWord, String niceName) throws IOException, JSONException {
        try {
            String httpUrl="http://cdz.ittun.cn/cdz/user_register.php";
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
            String data = "&name=" + URLEncoder.encode(niceName, "UTF-8")+"&cardid="
                    + "&passwd=" +passWord+ "&money=0"+ "&number=" + account;//传递的数据
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send_verifi_code:
                // TODO 请求接口发送验证码
                codeManager.getVerifyCode(VerifyCodeManager.REGISTER);
                break;
            case R.id.btn_create_account:
                try {
                    commit();
                    } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                break;

            default:
                break;
        }
    }
}

```

---

## 登录功能的实现

登录功能需要在完成注册以后才能进行，只要提交账号、密码等信息至服务器，请求登录即可，至于第三方登录功能利用Mob平台的ShareSDK来实现。

LoginActivity.java

```
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

```

---

## 忘记密码功能实现

到这一步其实大家都应该明白了，我用的都是最简单的方式去实现这些功能，就是通过提交数据给服务器，然后由服务器判断提交的数据是否正确，正确的话就返回注册信息，包含账号、密码等功能，然后对返回的数据进行操作，修改密码功能也是这样，修改返回数据中密码并重新提交给服务器，这个功能就完成了。

```
package com.example.administrator.loginandregister.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.loginandregister.R;
import com.example.administrator.loginandregister.utils.RegexUtils;
import com.example.administrator.loginandregister.utils.ToastUtils;
import com.example.administrator.loginandregister.utils.VerifyCodeManager;
import com.example.administrator.loginandregister.views.CleanEditText;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import static android.view.View.OnClickListener;

/**
 * Created by JimCharles on 2016/11/27.
 */

public class ForgetPasswordActivity extends Activity implements OnClickListener {
    private static final String TAG = "SignupActivity";
    // 界面控件
    private CleanEditText phoneEdit;
    private CleanEditText passwordEdit;
    private CleanEditText verifyCodeEdit;
    private Button getVerifiCodeButton;
    private Button resetButton;

    private VerifyCodeManager codeManager;

    String result = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frogetpwd);

        initViews();
        codeManager = new VerifyCodeManager(this, phoneEdit, getVerifiCodeButton);
    }

    /**
     * 通用findViewById,减少重复的类型转换
     *
     * @param id
     * @return
     */
    @SuppressWarnings("unchecked")
    public final <E extends View> E getView(int id) {
        try {
            return (E) findViewById(id);
        } catch (ClassCastException ex) {
            Log.e(TAG, "Could not cast View to concrete class.", ex);
            throw ex;
        }
    }


    private void initViews() {

        resetButton = getView(R.id.btn_create_account);
        resetButton.setOnClickListener(this);
        getVerifiCodeButton = getView(R.id.btn_send_verifi_code);
        getVerifiCodeButton.setOnClickListener(this);
        phoneEdit = getView(R.id.et_phone);
        phoneEdit.setImeOptions(EditorInfo.IME_ACTION_NEXT);// 下一步
        verifyCodeEdit = getView(R.id.et_verifiCode);
        verifyCodeEdit.setImeOptions(EditorInfo.IME_ACTION_NEXT);// 下一步
        passwordEdit = getView(R.id.et_password);
        passwordEdit.setImeOptions(EditorInfo.IME_ACTION_DONE);
        passwordEdit.setImeOptions(EditorInfo.IME_ACTION_GO);
        passwordEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                // 点击虚拟键盘的done
                if (actionId == EditorInfo.IME_ACTION_DONE
                        || actionId == EditorInfo.IME_ACTION_GO) {
                    try {
                        commit();
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });
    }

    private void commit() throws IOException, JSONException {
        String phone = phoneEdit.getText().toString().trim();
        String password = passwordEdit.getText().toString().trim();

        if (checkInput(phone, password)) {
            // TODO:请求服务端注册账号
            resetButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    //android4.0后的新的特性，网络数据请求时不能放在主线程中。
                    //使用线程执行访问服务器，获取返回信息后通知主线程更新UI或者提示信息。
                    final Handler handler = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            if (msg.what == 1) {
                                //提示读取结果
                                Toast.makeText(ForgetPasswordActivity.this, result, Toast.LENGTH_LONG).show();
                                if (result.contains("成")){
                                    Toast.makeText(ForgetPasswordActivity.this, result, Toast.LENGTH_LONG).show();
                                    ToastUtils.showShort(ForgetPasswordActivity.this,
                                            "注册成功......");
                                }
                                else{
                                    final Intent it = new Intent(ForgetPasswordActivity.this, LoginActivity.class); //你要转向的Activity
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
                                Register(phoneEdit.getText().toString(),passwordEdit.getText().toString());
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

    private boolean checkInput(String phone, String password) {
        if (TextUtils.isEmpty(phone)) { // 电话号码为空
            ToastUtils.showShort(this, R.string.tip_phone_can_not_be_empty);
        } else {
            if (!RegexUtils.checkMobile(phone)) { // 电话号码格式有误
                ToastUtils.showShort(this, R.string.tip_phone_regex_not_right);
            } else if (password.length() < 6 || password.length() > 32
                    || TextUtils.isEmpty(password)) { // 密码格式
                ToastUtils.showShort(this,
                        R.string.tip_please_input_6_32_password);
            } else {
                return true;
            }
        }
        return false;
    }

    public Boolean Register(String account, String passWord) throws IOException, JSONException {
        try {
            String httpUrl="http://cdz.ittun.cn/cdz/user_register.php";
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
            String data = "&cardid=" + "&passwd=" +passWord+ "&money=0"+ "&number=" + account;//传递的数据
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_cancel:
                finish();
                break;
            case R.id.btn_send_verifi_code:
                // TODO 请求接口发送验证码
                codeManager.getVerifyCode(VerifyCodeManager.RESET_PWD);
                break;

            default:
                break;
        }
    }
}

```

---

## 记住密码和自动登录功能的实现

记住密码和自动登录功能可以通过SharedPreferences来实现

 调用Context.getSharePreferences(String name, int mode)方法来得到SharePreferences接口，该方法的第一个参数是文件名称，第二个参数是操作模式。

操作模式有三种：MODE_PRIVATE(私有) ,MODE_WORLD_READABLE(可读),MODE_WORLD_WRITEABLE(可写)

SharePreference提供了获得数据的方法，如getString(String key,String defValue)等

调用SharePreferences的edit()方法返回 SharePreferences.Editor内部接口，该接口提供了保存数据的方法如： putString(String key,String value)等,调用该接口的commit()方法可以将数据保存。 

这一部分还没有进行封装，简单的给大家介绍一下利用SharePreferences来实现记住密码和自动登陆

代码

```
import android.content.Context;
import android.content.SharedPreferences;
 
 public class UserInfo {

     private String USER_INFO = "userInfo";
 
    private Context context;
    
     public UserInfo() {
     }
 
     public UserInfo(Context context) {

         this.context = context;
     }
     // 存放字符串型的值
     public void setUserInfo(String key, String value) {
         SharedPreferences sp = context.getSharedPreferences(USER_INFO,
                 Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
         editor.remove(key);
         editor.putString(key, value);
         editor.commit();
     }
 
     // 存放整形的值
     public void setUserInfo(String key, int value) {
         SharedPreferences sp = context.getSharedPreferences(USER_INFO,
                 Context.MODE_PRIVATE);
         SharedPreferences.Editor editor = sp.edit();
         editor.remove(key);
         editor.putInt(key, value);
         editor.commit();
    }
 
     // 存放长整形值
     public void setUserInfo(String key, Long value) {
         SharedPreferences sp = context.getSharedPreferences(USER_INFO,
                 Context.MODE_PRIVATE);
         SharedPreferences.Editor editor = sp.edit();
         editor.remove(key);
         editor.putLong(key, value);
         editor.commit();
     }

     // 存放布尔型值
     public void setUserInfo(String key, Boolean value) {
         SharedPreferences sp = context.getSharedPreferences(USER_INFO,
                 Context.MODE_PRIVATE);
         SharedPreferences.Editor editor = sp.edit();
         editor.remove(key);
         editor.putBoolean(key, value);
         editor.commit();
     }
 
    // 清空记录
     public void clear() {
         SharedPreferences sp = context.getSharedPreferences(USER_INFO,
                 Context.MODE_PRIVATE);
         SharedPreferences.Editor editor = sp.edit();
         editor.clear();
         editor.commit();
     }
 
     // 注销用户时清空用户名和密码
     // public void logOut() {
     // SharedPreferences sp = context.getSharedPreferences(USER_INFO,
     // Context.MODE_PRIVATE);
     // SharedPreferences.Editor editor = sp.edit();
     // editor.remove(ACCOUNT);
     // editor.remove(PASSWORD);
     // editor.commit();
     // }
 
     // 获得用户信息中某项字符串型的值
     public String getStringInfo(String key) {
         SharedPreferences sp = context.getSharedPreferences(USER_INFO,
                 Context.MODE_PRIVATE);
         return sp.getString(key, "");
     }
 
     // 获得用户息中某项整形参数的值
     public int getIntInfo(String key) {
         SharedPreferences sp = context.getSharedPreferences(USER_INFO,
                 Context.MODE_PRIVATE);
         return sp.getInt(key, -1);
    }
 
     // 获得用户信息中某项长整形参数的值
     public Long getLongInfo(String key) {
         SharedPreferences sp = context.getSharedPreferences(USER_INFO,
                 Context.MODE_PRIVATE);
         return sp.getLong(key, -1);
     }
 
     // 获得用户信息中某项布尔型参数的值
     public boolean getBooleanInfo(String key) {
         SharedPreferences sp = context.getSharedPreferences(USER_INFO,
                 Context.MODE_PRIVATE);
         return sp.getBoolean(key, false);
     }
 
 }
```

```
public class MainActivity extends Activity {
 
     private Button login, cancel;

    private EditText usernameEdit, passwordEdit;
 
    private CheckBox CK_save, CK_auto;
 
    private UserInfo userInfo;
 
    private static final String USER_NAME = "user_name";
    private static final String PASSWORD = "password";
    private static final String ISSAVEPASS = "savePassWord";

    private String username;
    private String password;

    @Override
   protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);

         userInfo = new UserInfo(this);
         login = (Button) findViewById(R.id.login_btn);
         cancel = (Button) findViewById(R.id.unlogin_btn);
         usernameEdit = (EditText) findViewById(R.id.username);
         passwordEdit = (EditText) findViewById(R.id.password);
         CK_save = (CheckBox) findViewById(R.id.savePassword);
         CK_auto = (CheckBox) findViewById(R.id.autoLogin);
         // 判断是否记住了密码的 初始默认是要记住密码的
         if (userInfo.getBooleanInfo(ISSAVEPASS)) {
             CK_save.setChecked(true);
             usernameEdit.setText(userInfo.getStringInfo(USER_NAME));
             passwordEdit.setText(userInfo.getStringInfo(PASSWORD));
             // 判断是否要自动登陆
             if (userInfo.getBooleanInfo(AUTOLOGIN)) {
                 // 默认是要自动登陆的
                 CK_auto.setChecked(true);
                 Intent i = new Intent();
                 i.setClass(MainActivity.this, SecondActivity.class);
                startActivity(i);
             }
 
         }
 
         login.setOnClickListener(new Button.OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 username = usernameEdit.getText().toString();
                 password = passwordEdit.getText().toString();
                 if (username.equals("liang") && password.equals("123")) {
                     if (CK_save.isChecked()) {
                         userInfo.setUserInfo(USER_NAME, username);
                         userInfo.setUserInfo(PASSWORD, password);
                         userInfo.setUserInfo(ISSAVEPASS, true);
                     }
                     if (CK_auto.isChecked()) {
                         userInfo.setUserInfo(AUTOLOGIN, true);
 
                     }
                     Intent i = new Intent();
                     i.setClass(MainActivity.this, SecondActivity.class);
                     startActivity(i);
                 }
 
             }
         });
 
     }
 }
```

可见要实现记住密码和自动登录功能并不难，只要在登陆的XML布局文件添加两个Checkbar并对其进行设置，然后在Activity中进行简单的处理，就可以实现了。

---
