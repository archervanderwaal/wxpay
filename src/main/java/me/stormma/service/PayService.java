package me.stormma.service;

import me.stormma.config.WeChatConfigBean;
import me.stormma.util.pay.SignatureUtils;
import me.stormma.util.pay.WXUtil;
import me.stormma.util.request.HttpClientUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;

/**
 * Created on 2017/5/18.
 *
 * @author stormma
 */
@Service
public class PayService {

    @Autowired
    private WeChatConfigBean weChatConfigBean;

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
        datas.put("device_info", "WEB");
        datas.put("body", body);
        datas.put("trade_type", "JSAPI");
        datas.put("nonce_str", WXUtil.getNonceStr());
        datas.put("notify_url", weChatConfigBean.getNOTIFYURL());
        datas.put("out_trade_no", createOutTradeNO());
        datas.put("total_fee", totalFee);
        datas.put("openid", openId);
        datas.put("spbill_create_ip", ip);
        //datas.put("sign", SignatureUtils.signature(datas))
        String sign = SignatureUtils.signature(datas, weChatConfigBean.getKEY());
        datas.put("sign", sign);
        return this.getRequestXml(datas);
    }

    /**
     * 创建订单
     *
     * @return
     */
    public String createOutTradeNO() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 32);
    }

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

    /**
     * 请求处理
     * @param param
     * @return
     * @throws Exception
     */
    public Map<String, String> requestWechatPayServer(String param) throws Exception {

        String response = HttpClientUtil.doPostHttpsXMLParam(weChatConfigBean.getWxorder(), param);
        return WXUtil.parseXml(response);
    }

    /**
     * 回调参数解析
     * @param request
     * @return
     * @throws Exception
     */
    public Map<String, String> getCallbackParams(HttpServletRequest request)
            throws Exception {
        InputStream inStream = request.getInputStream();
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outSteam.write(buffer, 0, len);
        }
        System.out.println("~~~~~~~~~~~~~~~~付款成功~~~~~~~~~");
        outSteam.close();
        inStream.close();
        String result = new String(outSteam.toByteArray(), "utf-8");
        return WXUtil.parseXml(result);
    }
}
