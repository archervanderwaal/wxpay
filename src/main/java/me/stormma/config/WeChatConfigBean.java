package me.stormma.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created on 2017/3/13.
 *
 * @author StormMma
 *
 * @Description: 微信相关常量
 */
@Component
@ConfigurationProperties(locations = {"classpath:config/wechat.properties"}, prefix = "wechat")
public class WeChatConfigBean {

    /**
     * token
     */
    private String token;

    /**
     * app id
     */
    private String appId;

    /**
     * app secret
     */
    private String appSecret;

    /**
     * call back url
     */
    private String callBackUrl;

    /**
     * 静默授权回调地址
     */
    private String callBackSlientUrl;

    /**
     * 商户id
     */
    private String MCHID;

    /**
     * 异步回调地址
     */
    private String NOTIFYURL;

    /**
     * 微信同意下单地址
     */
    private String wxorder;

    /**
     * key
     */
    private String KEY;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public String getCallBackUrl() {
        return callBackUrl;
    }

    public void setCallBackUrl(String callBackUrl) {
        this.callBackUrl = callBackUrl;
    }

    public String getCallBackSlientUrl() {
        return callBackSlientUrl;
    }

    public void setCallBackSlientUrl(String callBackSlientUrl) {
        this.callBackSlientUrl = callBackSlientUrl;
    }

    public String getMCHID() {
        return MCHID;
    }

    public void setMCHID(String MCHID) {
        this.MCHID = MCHID;
    }

    public String getNOTIFYURL() {
        return NOTIFYURL;
    }

    public void setNOTIFYURL(String NOTIFYURL) {
        this.NOTIFYURL = NOTIFYURL;
    }

    public String getWxorder() {
        return wxorder;
    }

    public void setWxorder(String wxorder) {
        this.wxorder = wxorder;
    }

    public String getKEY() {
        return KEY;
    }

    public void setKEY(String KEY) {
        this.KEY = KEY;
    }

    @Override
    public String toString() {
        return "WeChatConfigBean{" +
                "token='" + token + '\'' +
                ", appId='" + appId + '\'' +
                ", appSecret='" + appSecret + '\'' +
                ", callBackUrl='" + callBackUrl + '\'' +
                ", callBackSlientUrl='" + callBackSlientUrl + '\'' +
                ", MCHID='" + MCHID + '\'' +
                ", NOTIFYURL='" + NOTIFYURL + '\'' +
                ", wxorder='" + wxorder + '\'' +
                ", KEY='" + KEY + '\'' +
                '}';
    }
}
