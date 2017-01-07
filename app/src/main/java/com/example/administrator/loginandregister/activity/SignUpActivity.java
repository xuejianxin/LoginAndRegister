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
