package me.stormma.task;

import me.stormma.service.WechatService;
import me.stormma.util.wechat.RequestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * <p>Created on 2017/4/14.</p>
 *
 * @author stormma
 *
 * @Description: 定时刷新 access_token的定时任务
 */
@Component
public class ScheduledUpdateAccessTokenTask {

    @Autowired
    private WechatService wechatService;

    @Autowired
    private RequestUtil requestUtil;

    private static final Logger logger = LoggerFactory.getLogger(ScheduledUpdateAccessTokenTask.class);

    /**
     * <p>刷新 access_token</p>
     */
    @Scheduled(fixedDelay = 1000 * 60 * 110, initialDelay = 0)
    public void updateAccessToken() {
        wechatService.updateAccessToken();
    }

    /**
     * <p>定时刷新 jsApi_ticet，延时启动两秒，保证 access_token 已经缓存好</p>
     */
    @Scheduled(fixedDelay = 1000 * 60 * 110, initialDelay = 1000)
    public void updateJsApiTicket() {
        wechatService.updateJsApiTicket();
    }
}
