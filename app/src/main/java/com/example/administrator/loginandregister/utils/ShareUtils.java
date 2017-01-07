package com.example.administrator.loginandregister.utils;

/**
 * Created by JimCharles on 2016/11/27.
 */

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.widget.Toast;

import com.example.administrator.loginandregister.R;
import com.example.administrator.loginandregister.global.AppConstants;
import com.example.administrator.loginandregister.global.ShareDomain;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners;
import com.umeng.socialize.media.QZoneShareContent;
import com.umeng.socialize.media.SinaShareContent;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.sso.QZoneSsoHandler;
import com.umeng.socialize.sso.SinaSsoHandler;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.umeng.socialize.weixin.media.CircleShareContent;
import com.umeng.socialize.weixin.media.WeiXinShareContent;


public class ShareUtils {
    // 整个平台的Controller,负责管理整个SDK的配置、操作等处理
    private static UMSocialService mController = UMServiceFactory
            .getUMSocialService(AppConstants.DESCRIPTOR);

    /**
     * 配置分享平台参数
     */
    public static void configPlatforms(Context context) {
        // 添加新浪sso授权
        mController.getConfig().setSsoHandler(new SinaSsoHandler());

        // 添加QQ、QZone平台
        addQQQZonePlatform(context);

        // 添加微信、微信朋友圈平台
        addWXPlatform(context);

    }

    /**
     * @功能描述 : 添加微信平台分享
     * @return
     */
    private static void addWXPlatform(Context context) {
        // 注意：在微信授权的时候，必须传递appSecret
        // wx967daebe835fbeac是你在微信开发平台注册应用的AppID, 这里需要替换成你注册的AppID
        String appId = AppConstants.WEXIN_APPID;
        String appSecret = AppConstants.WEXIN_APPSECRET;
        // 添加微信平台
        UMWXHandler wxHandler = new UMWXHandler(context, appId, appSecret);
        wxHandler.addToSocialSDK();

        // 支持微信朋友圈
        UMWXHandler wxCircleHandler = new UMWXHandler(context, appId, appSecret);
        wxCircleHandler.setToCircle(true);
        wxCircleHandler.addToSocialSDK();
    }

    /**
     * @功能描述 : 添加QQ平台支持 QQ分享的内容， 包含四种类型， 即单纯的文字、图片、音乐、视频. 参数说明 : title, summary,
     *       image url中必须至少设置一个, targetUrl必须设置,网页地址必须以"http://"开头 . title :
     *       要分享标题 summary : 要分享的文字概述 image url : 图片地址 [以上三个参数至少填写一个] targetUrl
     *       : 用户点击该分享时跳转到的目标地址 [必填] ( 若不填写则默认设置为友盟主页 )
     * @return
     */
    private static void addQQQZonePlatform(Context context) {
        String appId = AppConstants.QQZONE_APPID;
        String appKey = AppConstants.QQZONE_APPKEY;
        // 添加QQ支持, 并且设置QQ分享内容的target url
        UMQQSsoHandler qqSsoHandler = new UMQQSsoHandler((Activity) context,
                appId, appKey);
        qqSsoHandler.setTargetUrl("http://www.umeng.com/social");
        qqSsoHandler.addToSocialSDK();

        // 添加QZone平台
        QZoneSsoHandler qZoneSsoHandler = new QZoneSsoHandler(
                (Activity) context, appId, appKey);
        qZoneSsoHandler.addToSocialSDK();
    }

    /**
     * 根据不同的平台设置不同的分享内容</br>
     */
    public static void setShareContent(Context context, ShareDomain shareDomain) {
        String articleIntro = shareDomain.getShareContent();
        String targetUrl = shareDomain.getTargetUrl();
        String articleTitle = shareDomain.getShareTitle();
        String temp = targetUrl + context.getString(R.string.jimcharles);

        Bitmap bitmap = shareDomain.getBitmap();
        articleIntro = articleIntro + temp;
        if (articleIntro.length() >= 140) {
            articleIntro = articleIntro.substring(0, 136 - temp.length())
                    + "..." + temp;
        }

        QZoneSsoHandler qZoneSsoHandler = new QZoneSsoHandler(
                (Activity) context, AppConstants.QQZONE_APPID,
                AppConstants.QQZONE_APPKEY);
        qZoneSsoHandler.addToSocialSDK();
        mController.setShareContent(articleIntro);
        UMImage urlImage = null;
        if (shareDomain.getImgUrl() != null
                && !"".equals(shareDomain.getImgUrl())) {
            urlImage = new UMImage(context, shareDomain.getImgUrl());
        } else {
            urlImage = new UMImage(context, bitmap);
        }

        WeiXinShareContent weixinContent = new WeiXinShareContent();
        weixinContent.setShareContent(articleIntro);
        weixinContent.setTitle(articleTitle);
        weixinContent.setTargetUrl(targetUrl);
        weixinContent.setShareMedia(urlImage);
        mController.setShareMedia(weixinContent);

        // 设置朋友圈分享的内容
        CircleShareContent circleMedia = new CircleShareContent();
        circleMedia.setShareContent(articleIntro);
        circleMedia.setTitle(articleTitle);
        circleMedia.setShareImage(urlImage);
        circleMedia.setTargetUrl(targetUrl);
        mController.setShareMedia(circleMedia);

        // 设置QQ空间分享内容
        QZoneShareContent qzone = new QZoneShareContent();
        qzone.setShareContent(articleIntro);
        qzone.setTargetUrl(targetUrl);
        qzone.setTitle(articleTitle);
        qzone.setShareImage(urlImage);
        mController.setShareMedia(qzone);

        StringBuffer sb = new StringBuffer();
        sb.append("【").append(articleTitle).append("】").append(articleIntro);

        // 设置新浪分享内容
        SinaShareContent sinaContent = new SinaShareContent(urlImage);
        sinaContent.setShareContent(sb.toString());
        mController.setShareMedia(sinaContent);

    }

    // 显示分享平台
    public static void showShareBoard(Context context) {
        mController.getConfig().setPlatforms(SHARE_MEDIA.SINA,
                SHARE_MEDIA.QZONE, SHARE_MEDIA.WEIXIN_CIRCLE,
                SHARE_MEDIA.WEIXIN);
        registerCallback(context);
        mController.openShare((Activity) context, false);

    }

    public static void showShareBoard(final Context context,
                                      final String link_id, final String hash) {
        mController.getConfig().setPlatforms(SHARE_MEDIA.SINA,
                SHARE_MEDIA.QZONE, SHARE_MEDIA.WEIXIN_CIRCLE,
                SHARE_MEDIA.WEIXIN);
        mController.getConfig().cleanListeners();
        mController.getConfig().registerListener(new SocializeListeners.SnsPostListener() {

            @Override
            public void onStart() {

            }

            @Override
            public void onComplete(SHARE_MEDIA platform, int stCode,
                                   SocializeEntity entity) {
                if (stCode == 200) {
                    // TODO: 这里可以进行分享数统计
                } else {
//					Toast.makeText(context, "分享失败", Toast.LENGTH_SHORT).show();

                }
            }
        });
        mController.openShare((Activity) context, false);

    }

    /**
     * 设置分享回调
     *
     * @param
     */
    public static void registerCallback(final Context context) {
        mController.getConfig().cleanListeners();
        mController.getConfig().registerListener(new SocializeListeners.SnsPostListener() {

            @Override
            public void onStart() {

            }

            @Override
            public void onComplete(SHARE_MEDIA platform, int stCode,
                                   SocializeEntity entity) {
                if (stCode == 200) {
                    Toast.makeText(context, "分享成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "分享失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
