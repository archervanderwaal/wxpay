package me.stormma;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>Created on 2017/4/14.</p>
 *
 * @author stormma
 * @Descrption: <p>图片上传任务，上传 oss, 入库</p>
 */
@Component
public class Task {
    @Async(value = "myAsync")
    public void uploadImg() {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        String url = "http://xdkd.boxkj.com/admin/tree?id=7";
        CloseableHttpResponse response;
        Map<String, String> header = new HashMap<String, String>();

        header.put("Cookie", "JSESSIONID=32275C0772B399FF369E052D85D3FE95");
        XD.httpPost(url, null, header);


//        ArrayList
    }
}
