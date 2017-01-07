package com.example.administrator.loginandregister.utils;

/**
 * Created by JimCharles on 2016/12/7.
 */

public interface OnSendMessageHandler {

    //#if def{lang} == cn
    /**
     * 此方法在发送验证短信前被调用，传入参数为接收者号码
     * 返回true表示此号码无须实际接收短信
     */
    //#elif def{lang} == en
    /**
     * This method will be called before verification message being to sent,
     * input params are the message receiver
     * return true means this number won't actually receive the message
     */
    //#endif
    public boolean onSendMessage(String country, String phone);

}
