package me.stormma.service;

import com.alibaba.fastjson.JSON;
import me.stormma.model.wechat.WeChatOauthToken;
import me.stormma.util.wechat.RequestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * <p>Created on 2017/3/15.</p>
 *
 * @author StormMa
 *
 * @Description: 微信相关的服务，用于授权已经拉取用户信息操作
 */
@Service
public class WechatService {

    @Autowired
    private RequestUtil requestUtil;

    private static final Logger logger = LoggerFactory.getLogger(WechatService.class);

    private static String frontAccessToken;

    private static String jsapi_ticket;

    /**
     * 获取微信的授权请求地址
     * @return
     */
    public String getWeChatAuthorizationUrl() {
        return requestUtil.getWeChatAuthorizationUrl();
    }

    /**
     * <p>基础授权</p>
     *
     * @return
     */
    public String getWeChatAuthorizationUrlScopeIsBase() {
        return requestUtil.getWeChatAuthorizationUrlSocpeIsBase();
    }

    /**
     * 获取微信网页凭证
     * @param code
     * @return
     */
    public WeChatOauthToken getOAuthAccessToken(String code) {
        return requestUtil.getOAuthAccessToken(code);
    }

    /**
     * 判断code的有效性
     * @param code
     * @return
     */
    private boolean isOkCode(String code) {
        return !(code == null || code.equals(""));
    }

    /**
     * 获取基础支持的access_token，给前端js使用
     * @return
     */
    public String getFrontAccessToken() {
        return frontAccessToken;
    }

    /**
     * 获得jsapi_ticket
     *
     * @return
     */
    public String getJsApiTicket() {
        return jsapi_ticket;
    }

    /**
     * <p>生成签名</p>
     * @param jsapi_ticket
     * @param url
     * @return
     */
    public Map<String, String> sign(String jsapi_ticket, String url) {
        return requestUtil.sign(jsapi_ticket, url);
    }

    /**
     * <p>刷新基础支持的 access_token</p>
     */
    public void updateAccessToken() {
        frontAccessToken = requestUtil.getFrontAccessToken();
    }

    /**
     * <p>获得静默授权的 url</p>
     *
     * @return
     */
    public String getSlientUrl() {
        return requestUtil.getSlientUrl();
    }

    /**
     * <p>静默授权获得 code</p>
     *
     * @param code
     * @return
     */
    public String getOpenIdBySlientAuthy(String code) {
        return requestUtil.getOpenIdBySlientAuthy(code);
    }

    /**
     * 更新前端需要的jsapi_ticket
     */
    public void updateJsApiTicket() {
        jsapi_ticket = requestUtil.getJsApiTicket(frontAccessToken);
    }
}