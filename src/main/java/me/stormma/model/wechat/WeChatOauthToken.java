package me.stormma.model.wechat;

/**
 * <p>Created on 2017/3/14.</p>
 *
 * @author StormMa
 *
 * @Description: 网页授权代表对象
 */
public class WeChatOauthToken {

    /**
     * 调用接口的凭证
     */
    private String access_token;

    /**
     * 过期日期
     */
    private Integer expires_in;

    /**
     * 刷新token
     */
    private String refresh_token;

    /**
     * 用户的唯一openId
     */
    private String openId;

    /**
     * scope
     */
    private String scode;

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public Integer getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(Integer expires_in) {
        this.expires_in = expires_in;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getScode() {
        return scode;
    }

    public void setScode(String scode) {
        this.scode = scode;
    }

    @Override
    public String toString() {
        return "WeChatOauthToken{" +
                "access_token='" + access_token + '\'' +
                ", expires_in=" + expires_in +
                ", refresh_token='" + refresh_token + '\'' +
                ", openId='" + openId + '\'' +
                ", scode='" + scode + '\'' +
                '}';
    }
}
