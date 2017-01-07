package com.example.administrator.loginandregister.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.PasswordTransformationMethod;
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
import com.example.administrator.loginandregister.views.CleanEditText;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by JimCharles on 2016/12/9.
 */

public class UserActivity extends Activity implements View.OnClickListener{

    private final String TAG = "UserActivity";

    private CleanEditText phoneEdit;
    private CleanEditText passwordEdit;

    private TextView numberEdit;
    private TextView passwdsEdit;
    private TextView moneyEdit;
    private TextView cardidEdit;
    private TextView nicknameEdit;

    private Button CheckButton;

    String result = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState) ;
        setContentView(R.layout.activity_user);

        initViews();
    }

    @SuppressWarnings("unchecked")
    public final <E extends View> E getView(int id) {
        try {
            return (E) findViewById(id);
        } catch (ClassCastException ex) {
            Log.e(TAG, "Could not cast View to concrete class.", ex);
            throw ex;
        }
    }

    private void initViews(){
        CheckButton = (Button) findViewById(R.id.btn_check);
        CheckButton.setOnClickListener(this);

        phoneEdit = getView(R.id.et_cardid);
        phoneEdit.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        passwordEdit = getView(R.id.et_name);
        passwordEdit.setTransformationMethod(PasswordTransformationMethod
                .getInstance());

        numberEdit = (TextView) findViewById(R.id.et_number);
        numberEdit.setOnClickListener(this);
        passwdsEdit = (TextView) findViewById(R.id.et_passwds) ;
        passwdsEdit.setOnClickListener(this);
        cardidEdit =  (TextView) findViewById(R.id.et_passwd);
        cardidEdit.setOnClickListener(this);
        nicknameEdit = (TextView) findViewById(R.id.et_account);
        nicknameEdit.setOnClickListener(this);
        moneyEdit = (TextView) findViewById(R.id.et_money);
        moneyEdit.setOnClickListener(this);

        passwordEdit.setImeOptions(EditorInfo.IME_ACTION_DONE);
        passwordEdit.setImeOptions(EditorInfo.IME_ACTION_GO);
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
        String account = phoneEdit.getText().toString();
        String password = passwordEdit.getText().toString();

        if (checkInput(account, password)) {
            // TODO: 请求服务器登录账号
            CheckButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    //android4.0后的新的特性，网络数据请求时不能放在主线程中。
                    //使用线程执行访问服务器，获取返回信息后通知主线程更新UI或者提示信息。
                    final Handler handler = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            numberEdit.setText(result);
                            passwdsEdit.setText(result);
                            nicknameEdit.setText(result);
                            cardidEdit.setText(result);
                            moneyEdit.setText(result);

                            if (msg.what == 1) {
                                //提示读取结果
                                Toast.makeText(UserActivity.this, result, Toast.LENGTH_LONG).show();
                                ToastUtils.showShort(UserActivity.this,
                                        result);
                                if (result.contains("！")) {
                                    Toast.makeText(UserActivity.this, result, Toast.LENGTH_LONG).show();
                                    ToastUtils.showShort(UserActivity.this,
                                            "密码错误......");
                                }
                            }
                        }
                    };
                    // 启动线程来执行任务
                    new Thread() {
                        public void run() {
                            //请求网络
                            try {
                                Login(phoneEdit.getText().toString(),passwordEdit.getText().toString());

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
            Toast.makeText(this, R.string.input, Toast.LENGTH_LONG)
                    .show();
        } else {
            // 账号不匹配手机号格式（11位数字且以1开头）
            if ( !RegexUtils.checkMobile(account)) {
                Toast.makeText(this, R.string.tip_account_regex_not_right,
                        Toast.LENGTH_LONG).show();
            } else if (password == null || password.trim().equals("")) {
                Toast.makeText(this, R.string.input,
                        Toast.LENGTH_LONG).show();
            } else {
                return true;
            }
        }
        return false;
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

    @Override
    public void onClick(final View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.iv_cancel:
                finish();
                break;
            case R.id.btn_check:
                clickLogin();
                break;
            default:
                break;
        }
    }
}
