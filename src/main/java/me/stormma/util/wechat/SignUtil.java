/*
package me.stormma.util.wechat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.yueneng.config.WeChatConfigBean;
import pro.yueneng.exception.ParamNullException;
import pro.yueneng.exception.ServerSystemException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

*/
/**
 * <p>Created on 2017/3/12.</p>
 *
 * @author StormMa
 * @Description: 微信验证的相关工具类
 *//*

@Component
public class SignUtil {

    @Autowired
    private WeChatConfigBean weChatConfigBean;

    private static final Logger logger = LoggerFactory.getLogger(SignUtil.class);

    */
/**
     * @Description: 检验是否是微信客户端过来的消息
     * @param signature
     * @param timestamp
     * @param nonce
     * @return
     *//*

    public boolean checkSignature(String signature, String timestamp, String nonce) throws ServerSystemException, ParamNullException {
        String[] arr = new String[]{weChatConfigBean.getToken(), timestamp, nonce};
        if (arr.length == 0) {
            throw new ParamNullException("arr参数为空");
        }
        */
/*
         * 字典序排序
         *//*

        Arrays.sort(arr);

        StringBuilder content = new StringBuilder();
        for (String str : arr) {
            content.append(str);
        }
        MessageDigest messageDigest;
        String tmpStr = null;

        try {
            messageDigest = MessageDigest.getInstance("SHA-1");
            byte[] digestInformation = messageDigest.digest(content.toString().getBytes());
            tmpStr = byteArrToString(digestInformation);
        } catch (NoSuchAlgorithmException e) {
            logger.error("get SHA-1 MessageDigest an error occurred. the error message is: " + e.toString());
            throw new ServerSystemException("服务器内部错误!");
        }
        return tmpStr != null && tmpStr.equals(signature.toUpperCase());
    }

    */
/**
     * @Description: 字节转换成字符
     * @param mByte
     * @return
     *//*

    private static String byteToHexString(byte mByte) {
        char[] Digit = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        char[] tempArr = new char[2];
        tempArr[0] = Digit[(mByte >>> 4) & 0X0F];
        tempArr[1] = Digit[mByte & 0X0F];
        return new String(tempArr);
    }

    */
/**
     * @Description: 字节数组转换成字符串
     * @param byteArray
     * @return
     *//*

    private static String byteArrToString(byte[] byteArray) {
        String strDigest = "";
        for (byte aByteArray : byteArray) {
            strDigest += byteToHexString(aByteArray);
        }
        return strDigest;
    }
}
*/
