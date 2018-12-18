# wxpay demo

#### 用户认证获取openId
> 如果你知识关注支付流程，这块可以跳过，因为我知道这些你已经做过了，在开始所有的流程之前，我觉得你应该把所有微信相关的配置放到一个properties文件中去，这样不仅显得更规范，而且会避免犯很多错误，真是一个完美的选择！

```properties
######## 配置文件
######## 公众号开发配置中的token(自定义)
wechat.token=
######## 应用id
wechat.appId=
######## 密钥(同token查看地址)
wechat.appSecret=
######## 静默授权微信回调url
wechat.callBackSlientUrl=
######## 商户Id(支付相关)
wechat.MCHID=
######## 微信下单地址
wechat.wxorder=https://api.mch.weixin.qq.com/pay/unifiedorder
######## 支付api密钥
wechat.KEY=
######## 支付结果回调地址
wechat.NOTIFYURL=
```
> 接着你可以考虑把这个properties注入到一个bean中，使用更方便，当然你还可以选择使用java来读取properties的配置，对比这两个方法，我更喜欢第一个，我就使用第一种方法来演示一下(这里使用spring boot框架，spring mvc类似)

```java
/**
 * <p>Created on 2017/3/13.</p>
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
     * 微信统一下单地址
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
```


##### 封装请求工具(这次我选择使用HttpClient, 此处的json工具我选择了ali的fastjson)

> RequestUtil.java

```java
	/**
     * 发送Get请求到url，获得response的json实体
     * @param url
     * @return
     * @throws IOException
     */
    private JSONObject doGetUrl(String url) throws WechatException, ServerSystemException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response;
        String result;
        try {
            response = httpclient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity, "UTF-8");
            httpclient.close();
        } catch (IOException e) {
            logger.error("执行GET请求发生错误!", e);
            throw new ServerSystemException("执行GET请求发生错误！{}", e);
        }
        return JSONObject.parseObject(result);
    }


    /**
     * 发送post请求
     * @param url
     * @param param
     * @return
     * @throws ServerSystemException
     */
    private JSONObject doPostUrl(String url, String param) throws ServerSystemException {
        final String CONTENT_TYPE_TEXT_JSON = "application/json";
        DefaultHttpClient httpClient = new DefaultHttpClient(new PoolingClientConnectionManager());
        HttpPost httpPost = new HttpPost(url);
        HttpResponse response;
        String result;
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
            throw new ServerSystemException("执行POST请求发生错误！{}", e);
        }
        return JSONObject.parseObject(result);
    }

```

##### 获取code
> 在此之前，我想我们应该抽出一个微信工具类，专门来封装各种请求和RequestUtil来结合使用，是的，这是一个很好的选择。

---

> WxRequestUtil.java

```java
public calss WxRequestUtil {

	@AutoWired
	private WechatConfigBean config;

	/**
     * <p>获得静默授权的url</p>
     * @return
     */
    public String getSlientUrl() {
        String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + config.getAppId() +
                "&redirect_uri=" + URLEncoder.encode(config.getCallBackSlientUrl()) +
                "&response_type=code" +
                "&scope=snsapi_base" +
                "&state=STATE#wechat_redirect";

        return url;
    }
}
```
> 接着我想我们应该参照开发文档来重定向到这个url，然后微信服务器会检查参数接着重定向到我们的回调地址，嗯嗯，你猜对了，就是参数带的那个redirect_uri，那么我们应该补充一下回调接口

---

##### 获取openId

> WechatController.java

```java
	/**
     * 获得openId,静默授权
     * @param code
     * @param session
     * @param response
     */
    @RequestMapping(value = "/slient/check")
    public RequestResult<String> callBackBase(@RequestParam(value = "code", required = false) String code, HttpServletResponse response) {

        String openId = wechatService.getOpenIdBySlientAuthy(
        return ResultUtil.success(openId);
    }
```
> 我想我应该解释一下，控制器层我用的都是规范化的请求响应，不知道的可以参考我前面的博文。另外一点我需要说明的就是我们还需要一个service来处理获取openId的逻辑。

---

> WechatService.java

```java
	/**
     * 静默授权获得openId
     * @param code
     * @return
     */
    public String getOpenIdBySlientAuthy(String code) {

        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + config.getAppId() +
                "&secret=" + config.getAppSecret() +
                "&code=" + code +
                "&grant_type=authorization_code";

		//为了代码简便，此处省略异常处理
        JSONObject jsonObject = doGetUrl(url);

        return jsonObject.getString("openid");
    }

```

> 至此，我们获得了openId，那么接着我们回到支付的话题上

#### 微信支付
> 首先，我需要说明的是，微信支付的一个流程，至于为什么呢，我的目的很明确就是要描述清楚微信支付。我做支付的时候看过很多资料，有一个很深的体会就是代码复制来复制去，一大片一大片的代码看着心碎。在这里，我就不贴微信官方的流程图了，我相信你看着流程图会吓一跳，所以我选择不残害你。回到正题，微信支付最重要的就是三个步骤。

1. 统一下单，得到预支付id, 次数需要你提供商户的信息以及商品的信息，然后得到一个预支付id(请相信我，其他返回的数据并没有什么实际的意义)
2. 组装调起支付参数(我不知道叫什么名字更贴切，索性就这么叫吧，这个步骤其实就是使用预支付id，和其他的配置信息签名生成请求数据，返回至前台调用)
3. 调起支付(使用jssdk或者h5接口调起支付)

> 其他的步骤就不是那么重要了，比如支付接口通知接口，可以根据自己的需求进行改写，这里我就不多说了。

##### 统一下单
> PayService.java

```java
	/**
     * 获得统一下单参数
     * @param openId
     * @param totalFee
     * @param ip
     * @param body
     * @return
     */
public String getPayParam(String openId, String totalFee, String ip, String body) {

        Map<String, String> datas = new TreeMap<>();
        datas.put("appid", weChatConfigBean.getAppId());
        datas.put("mch_id", weChatConfigBean.getMCHID());
        //设备
        datas.put("device_info", "WEB");
        //商品描述
        datas.put("body", body);
        //支付类型，这里使用公众号支付，所以是JSAPI
        datas.put("trade_type", "JSAPI");
        //随机字符串，32字符以内
        datas.put("nonce_str", WXUtil.getNonceStr());
        //支付结果通知地址
        datas.put("notify_url", config.getNOTIFYURL());
        //订单号，自己生成一个唯一的订单号就行
        datas.put("out_trade_no", createOutTradeNO());
        //支付金额，以分为单位
        datas.put("total_fee", totalFee);
        //用户openId
        datas.put("openid", openId);
        //ip
        datas.put("spbill_create_ip", ip);
        String sign = SignatureUtils.signature(datas, config.getKEY());
        datas.put("sign", sign);
```
> 看到这里，你可能有点懵逼，我想我需要解释一下，开始之前我们用Map把所有的参数封装起来，至于为什么用TreeMapp，因为我们后面的签名要将Map的参数转换成一个字符串的形式(字段名=字段值&字段名=字段值)并且字段名字典序排序，这样，我们就只需要关注签名算法的实现，官方文档有解释签名算法，就像我前面说的，我们需要把Map转换成字符串的形式，并且后面要追加一个&key=#{key}(注意：#{key}是你的字段值)的参数，然后进行加密。我想此处我应该给出我的签名:

> SignatureUtils.java

```java
	/**
	 * 微信支付加密工具
	 * @param key
	 * @param map
	 */
	public static String signature(Map<String, String> map, String key) {
		Set<String> keySet = map.keySet();
		String[] str = new String[map.size()];
		StringBuilder tmp = new StringBuilder();
		str = keySet.toArray(str);
		for (int i = 0; i < str.length; i++) {
			String t = str[i] + "=" + map.get(str[i]) + "&";
			tmp.append(t);
		}
		if (StringUtils.isNotBlank(key)) {
			tmp.append("key=" + key);
		}
		String tosend = tmp.toString();
		MessageDigest md = null;
		byte[] bytes = null;
		try {

			md = MessageDigest.getInstance("MD5");
			bytes = md.digest(tosend.getBytes("utf-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		String singe = byteToStr(bytes);
		return singe.toUpperCase();

	}

	/**
	 * 字节数组转换为字符串
	 * @param byteArray
	 * @return
	 */
	public static String byteToStr(byte[] byteArray) {
		String strDigest = "";
		for (int i = 0; i < byteArray.length; i++) {
			strDigest += byteToHexStr(byteArray[i]);
		}
		return strDigest;
	}

	/**
	 * 字节转换为字符串
	 * @param mByte
	 * @return
	 */
	public static String byteToHexStr(byte mByte) {
		char[] Digit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A',
				'B', 'C', 'D', 'E', 'F' };
		char[] tempArr = new char[2];
		tempArr[0] = Digit[(mByte >>> 4) & 0X0F];
		tempArr[1] = Digit[mByte & 0X0F];

		String s = new String(tempArr);
		return s;
	}
```
> 这个工具类，具体我就不多介绍了，可以查看一下官方文档，了解一下签名算法，然后回来看代码，我相信你可以看懂。

---

```java
	/**
     * 得到统一下单参数的xml形式
     *
     * @param parameters
     * @return
     */
    public static String getRequestXml(Map<String, String> parameters) {
        StringBuffer sb = new StringBuffer();
        sb.append("<xml>");
        Set es = parameters.entrySet();
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String k = (String) entry.getKey();
            String v = (String) entry.getValue();
            if ("attach".equalsIgnoreCase(k) || "body".equalsIgnoreCase(k) || "sign".equalsIgnoreCase(k)) {
                sb.append("<" + k + ">" + "<![CDATA[" + v + "]]></" + k + ">");
            } else {
                sb.append("<" + k + ">" + v + "</" + k + ">");
            }
        }
        sb.append("</xml>");
        return sb.toString();
    }
```
> 于是，我们得到了统一下单的参数，接下来就是去请求微信服务器了。

```java
/**
     * 支付接口
     * @param body
     * @param totalFee
     * @param user
     * @param response
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/pay")
    public RequestResult<Map<String, String>> order(@RequestParam("body")String body,
                                                    @RequestParam("totalFee")String totalFee,
                                                    @SessionAttribute(name = "user", required = false)User user,
                                                    HttpServletResponse response) throws Exception {

		//之前我们获得了openId，这里我使用假数据测试
        String openId = "oxxjlv1dWSkielTGFfWQGNK-RHSc";
        String ip = this.getIpAddress();
        String requestParam = payService.getPayParam(openId, totalFee, ip, body);

		//stop here ,下面我会讲
        Map<String, String> result = payService.requestWechatPayServer(requestParam);
        Map<String, String> datas = new TreeMap<>();
        if (result.get("return_code").equals("SUCCESS")) {
            String prepayId = result.get("prepay_id");
            datas.put("appId", weChatConfigBean.getAppId());
            datas.put("package", "prepay_id=" + prepayId);
            datas.put("signType", "MD5");
            datas.put("timeStamp", Long.toString(new Date().getTime()).substring(0, 10));
            datas.put("nonceStr", WXUtil.getNonceStr());
            String sign = SignatureUtils.signature(datas, weChatConfigBean.getKEY());
            datas.put("paySign", sign);
            return ResultUtil.success(datas);
        }
        return ResultUtil.fail();
    }
```

##### 组装调起支付参数
> 继续上面的控制器，我们已经得到了预支付id,那么我们离成功不远了 请相信我，我没有骗你。然后我们要封装调起支付参数，我们先看一下jssdk调起支付需要的参数。

```javascript
wx.chooseWXPay({
    timestamp: 0, // 支付签名时间戳，注意微信jssdk中的所有使用timestamp字段均为小写。但最新版的支付后台生成签名使用的timeStamp字段名需大写其中的S字符
    nonceStr: '', // 支付签名随机串，不长于 32 位
    package: '', // 统一支付接口返回的prepay_id参数值，提交格式如：prepay_id=***）
    signType: '', // 签名方式，默认为'SHA1'，使用新版支付需传入'MD5'
    paySign: '', // 支付签名
    success: function (res) {
        // 支付成功后的回调函数
    }
});
```
> 那么我们就根据这个参数列表来生成参数，不过我很好奇，为什么timeStamp一阵大写一阵小写的，我想估计脑子抽了吧。现在我们看看上面的控制器剩余的代码，其实就是组装这个参数到Map，我想这个应该没有疑惑的地方吧。说到这，微信开发基本结束了，剩下的就是js调起支付，输入密码，微信服务器判断，给你返回结果的过程，处理结果的接口我就不贴了，简单到不行。

#### 结束
> 在做微信支付的时候，我有时候真的很无奈，没有好的官方文档，更没有好的博文，这篇博客旨在能讲清楚微信支付的步骤，我知道在这么短的时间讲清楚显然不可能，希望各位多多指正，有问题的可以发邮件给我，StormMaybin@gmail.com。哦对了，最后别忘记配置支付目录，不然会显示url未注册。应部分人的要求，最后写了一个demo，附上链接:https://github.com/StormMaybin/wxpay.git
