package com.example.administrator.loginandregister.utils;

/**
 * Created by JimCharles on 2016/11/27.
 */

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public class ToastUtils {

    private static Toast shortToast = null;

    public static void makeShortText(String msg, Context context) {
        if (context == null) {
            return;
        }

        if (shortToast == null) {
            shortToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        } else {
            shortToast.setText(msg);
        }
        shortToast.show();
    }


    private static Toast longToast = null;

    public static void makeLongText(String msg, Context context) {
        if (context == null) {
            return;
        }

        if (longToast == null) {
            longToast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        } else {
            longToast.setText(msg);
        }
        longToast.show();
    }



    public static void showLong(Context context, String msg){
        makeLongText(msg, context);
    }

    public static void showLong(Context context, int id){
        makeLongText(context.getResources().getString(id), context);
    }

    public static void showShort(Context context, String msg){
        makeShortText(msg, context);
    }

    public static void showShort(Context context, int id){
        makeShortText(context.getResources().getString(id), context);
    }


    public static void showCenterToast(Context context, String msg) {
        Toast toast = Toast.makeText(context, msg,  Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
