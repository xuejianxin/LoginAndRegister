package com.example.administrator.loginandregister.global;

/**
 * Created by JimCharles on 2016/12/23.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ReturnJSON {

    @SerializedName("t_id")
    @Expose
    private String tId;
    @SerializedName("user_name")
    @Expose
    private String userName;
    @SerializedName("user_number")
    @Expose
    private String userNumber;
    @SerializedName("user_cardid")
    @Expose
    private String userCardid;
    @SerializedName("user_passwd")
    @Expose
    private String userPasswd;
    @SerializedName("user_money")
    @Expose
    private String userMoney;

    public String getTId() {
        return tId;
    }

    public void setTId(String tId) {
        this.tId = tId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserNumber() {
        return userNumber;
    }

    public void setUserNumber(String userNumber) {
        this.userNumber = userNumber;
    }

    public String getUserCardid() {
        return userCardid;
    }

    public void setUserCardid(String userCardid) {
        this.userCardid = userCardid;
    }

    public String getUserPasswd() {
        return userPasswd;
    }

    public void setUserPasswd(String userPasswd) {
        this.userPasswd = userPasswd;
    }

    public String getUserMoney() {
        return userMoney;
    }

    public void setUserMoney(String userMoney) {
        this.userMoney = userMoney;
    }

}
