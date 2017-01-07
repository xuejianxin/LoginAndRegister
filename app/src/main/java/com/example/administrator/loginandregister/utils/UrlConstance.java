package com.example.administrator.loginandregister.utils;

/**
 * Created by JimCharles on 2016/11/27.
 */

public interface UrlConstance {

    public static final String ACCESSTOKEN_KEY ="accesstoken";


    //签约公钥，即客户端与服务器协商订的一个公钥
    public static final String PUBLIC_KEY ="*.cdz.itzhishiku.com";
    public static final String APP_URL = "http://cdz.itzhishiku.com/cdz/";

    //4.6注册用户接口
    public static final String KEY_REGIST_INFO ="user_register.php";

    //4.8登录用户接口
    public static final String KEY_LOGIN_INFO ="user_login.php";

    //4.9获取用户基本信息
    public static final String KEY_USER_BASE_INFO ="user_message.php";

    //5.0充值接口
    public static final String KEY_CHECKOUT_INFO = "user_checkout.php";

    //5.0扣费接口
    public static final String KEY_RECHARGE_INFO = "user_checkout.php";
}
