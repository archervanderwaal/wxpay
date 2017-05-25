package me.stormma.util.wechat;

import com.alibaba.fastjson.JSONObject;
import me.stormma.config.WeChatConfigBean;
import me.stormma.model.wechat.WeChatOauthToken;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * <p>Created on 2017/3/14.</p>
 *
 * @author StormMa
 *
 * @Description: 请求相关的工具类，与微信服务器的交互。
 */
@Component
public class RequestUtil {

    @Autowired
    private WeChatConfigBean weChatConfigBean;

    private static final Logger logger = LoggerFactory.getLogger(RequestUtil.class);

    /**
     * 发送Get请求到url，获得response的json实体
     * @param url
     * @return
     * @throws IOException
     */
    private JSONObject doGetUrl(String url) {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response;
        String result = null;
        try {
            response = httpclient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity, "UTF-8");
            httpclient.close();
        } catch (IOException e) {
            logger.error("执行GET请求发生错误!", e);
        }
        return JSONObject.parseObject(result);
    }

    /**
     * 发送post请求
     * @param url
     * @param param
     * @return
     */
    private JSONObject doPostUrl(String url, String param) {
        final String CONTENT_TYPE_TEXT_JSON = "application/json";
        DefaultHttpClient httpClient = new DefaultHttpClient(new PoolingClientConnectionManager());
        HttpPost httpPost = new HttpPost(url);
        HttpResponse response;
        String result = null;
        try {
            StringEntity stringEntity = new StringEntity(param);
            stringEntity.setContentType(CONTENT_TYPE_TEXT_JSON);
            stringEntity.setContentEncoding("UTF-8");
            httpPost.setEntity(stringEntity);
            response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity, "UTF-8");
            httpClient.close();
        } catch (IOException e) {
            logger.error("执行POST请求发生错误!", e);
        }
        return JSONObject.parseObject(result);
    }
    /**
     * 获得网页凭证
     * @param code
     */
    public WeChatOauthToken getOAuthAccessToken(String code) {
        WeChatOauthToken weChatOauthToken = null;
        String getAccessTokenUrl = "https://api.weixin.qq.com/sns/oauth2/access_token?" +
                "appid=" + weChatConfigBean.getAppId() +
                "&secret=" + weChatConfigBean.getAppSecret() +
                "&code=" + code +
                "&grant_type=authorization_code";

        JSONObject jsonObject = doGetUrl(getAccessTokenUrl);
        if (jsonObject != null) {
            weChatOauthToken = new WeChatOauthToken();
            weChatOauthToken.setAccess_token(jsonObject.getString("access_token"));
            weChatOauthToken.setExpires_in(jsonObject.getInteger("expires_in"));
            weChatOauthToken.setOpenId(jsonObject.getString("openid"));
            weChatOauthToken.setRefresh_token(jsonObject.getString("refresh_token"));
            weChatOauthToken.setScode(jsonObject.getString("scope"));
        }
        return weChatOauthToken;
    }

    /**
     * 获取验证微信的url（拉取详细的用户信息）
     * @return
     */
    public String getWeChatAuthorizationUrl() {
         return "https://open.weixin.qq.com/connect/oauth2/authorize?" +
                "appid=" + weChatConfigBean.getAppId() +
                "&redirect_uri=" + URLEncoder.encode(weChatConfigBean.getCallBackUrl())+
                "&response_type=code" +
                "&scope=snsapi_userinfo" +
                "&state=STATE#wechat_redirect";
    }

    /**
     * <p>基础授权</p>
     * @return
     */
    public String getWeChatAuthorizationUrlSocpeIsBase() {
        return "https://open.weixin.qq.com/connect/oauth2/authorize?" +
                "appid=" + weChatConfigBean.getAppId() +
                "&redirect_uri=" + URLEncoder.encode(weChatConfigBean.getCallBackUrl())+
                "&response_type=code" +
                "&scope=snsapi_base" +
                "&state=STATE#wechat_redirect";
    }

    /**
     * 获取基础支持的access_token，给前端js使用
     * @return
     */
    public String getFrontAccessToken() {
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential" +
                "&appid=" + weChatConfigBean.getAppId() +
                "&secret=" + weChatConfigBean.getAppSecret();

        JSONObject jsonObject = doGetUrl(url);
        return jsonObject.getString("access_token");
    }


    /**
     * 根据access_token获得jsapi_ticket
     * @param access_token
     * @return
     */
    public String getJsApiTicket(String access_token) {
        String url = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=" + access_token +
                "&type=jsapi";

        JSONObject jsonObject = doGetUrl(url);
        return jsonObject.getString("ticket");
    }


    /**
     * 根据jsapi_ticket
     * @param jsapi_ticket
     * @param url
     * @return
     */
    public Map<String, String> sign(String jsapi_ticket, String url) {
        Map<String, String> ret = new HashMap<>();
        String nonce_str = create_nonce_str();
        String timestamp = create_timestamp();
        String string1;
        String signature = "";

        //注意这里参数名必须全部小写，且必须有序
        string1 = "jsapi_ticket=" + jsapi_ticket +
                "&noncestr=" + nonce_str +
                "&timestamp=" + timestamp +
                "&url=" + url;
        System.out.println(string1);

        try
        {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(string1.getBytes("UTF-8"));
            signature = byteToHex(crypt.digest());
        } catch (Exception e) {
            logger.error("获取加密方式失败");
        }
        ret.put("url", url);
        ret.put("jsapi_ticket", jsapi_ticket);
        ret.put("nonceStr", nonce_str);
        ret.put("timestamp", timestamp);
        ret.put("signature", signature);

        return ret;
    }

    /**
     * <p>获得静默授权的 url</p>
     * @return
     */
    public String getSlientUrl() {
        String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + weChatConfigBean.getAppId() +
                "&redirect_uri=" + URLEncoder.encode(weChatConfigBean.getCallBackSlientUrl()) +
                "&response_type=code" +
                "&scope=snsapi_base" +
                "&state=STATE#wechat_redirect";

        return url;
    }

    /**
     * <p>静默授权获得openId</p>
     * @param code
     * @return
     */
    public String getOpenIdBySlientAuthy(String code) {

        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + weChatConfigBean.getAppId() +
                "&secret=" + weChatConfigBean.getAppSecret() +
                "&code=" + code +
                "&grant_type=authorization_code";

        JSONObject jsonObject = doGetUrl(url);

        return jsonObject.getString("openid");
    }
    /**
     * <p>字节转换成十六进制</p>
     * @param hash
     * @return
     */
    private String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash)
        {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    private String create_nonce_str() {
        return UUID.randomUUID().toString();
    }

    private String create_timestamp() {
        return Long.toString(System.currentTimeMillis() / 1000);
    }
}
