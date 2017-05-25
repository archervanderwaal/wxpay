package me.stormma.controller;

import me.stormma.config.WeChatConfigBean;
import me.stormma.service.PayService;
import me.stormma.service.WechatService;
import me.stormma.util.pay.SignatureUtils;
import me.stormma.util.pay.WXUtil;
import me.stormma.util.result.Request;
import me.stormma.util.result.ResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created on 2017/5/25.
 *
 * @author stormma
 */
@RestController
@RequestMapping(value = "/api/wechat")
public class WechatController extends BaseController {

    @Autowired
    private PayService payService;

    @Autowired
    private WeChatConfigBean weChatConfigBean;

    @Autowired
    private WechatService wechatService;

    private static final Logger logger = LoggerFactory.getLogger(WechatController.class);

    /**
     * 支付接口获得预支付id,然后封装请求参数获得拉取支付的响应参数
     * @param body
     * @param totalFee
     * @param openId
     * @param response
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/pay")
    public Request<Map<String, String>> order(@RequestParam("body") String body,
                                              @RequestParam("totalFee") String totalFee,
                                              @SessionAttribute(name = "openId") String openId,
                                              HttpServletResponse response) throws Exception {

        String ip = this.getIpAddress();
        String requestParam = payService.getPayParam(openId, totalFee, ip, body);
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
            return ResultBuilder.success(datas);
        }
        return ResultBuilder.fail();
    }


    @ResponseBody
    @RequestMapping(value = "/pay/notification")
    public String notification(HttpServletRequest request) {

        try {
            Map<String, String> map = payService.getCallbackParams(request);
            if (map.get("result_code").toString().equalsIgnoreCase("SUCCESS")) {
                String orderId = map.get("out_trade_no");

                String openId = map.get("openid");
                //支付成功之后的逻辑
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "<xml>\n" +
                "  <return_code><![CDATA[SUCCESS]]></return_code>\n" +
                "  <return_msg><![CDATA[OK]]></return_msg>\n" +
                "</xml>";
    }


    /**
     * 处理微信用户的授权请求主入口，最先处理的控制器
     *
     * @param response
     */
    @RequestMapping(value = "/check")
    public void weChatAuthy(HttpServletResponse response) throws IOException {

        //1. 先进行静默授权, 获得openId，判断是不是已登录过的用户，如果是直接查信息，反之进行详细的信息拉取
        //得到静默授权的 url
        String url = wechatService.getSlientUrl();
        if (!StringUtils.isEmpty(url)) {

            //slient/check
            response.sendRedirect(url);
        } else {
            response.sendRedirect("/wechat/check");
        }
    }

    /**
     * 获得openId,静默授权
     * @param code
     * @param session
     * @param response
     */
    @RequestMapping(value = "/slient/check")
    public void callBackBase(@RequestParam(value = "code", required = false) String code, HttpSession session,
                             HttpServletResponse response) throws IOException {

        if (!StringUtils.isEmpty(code)) {
            //获取openId， 可以存到session中，后面pay会用到
            String openId = wechatService.getOpenIdBySlientAuthy(code);

            session.setAttribute("openId", openId);

            //可以重定向到你要拉取支付的页面，省略...
        }
    }
}
