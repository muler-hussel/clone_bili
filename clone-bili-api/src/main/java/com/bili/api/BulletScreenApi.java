package com.bili.api;

import Service.BulletScreenService;
import com.bili.api.support.UserSupport;
import domain.BulletScreen;
import domain.JsonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.List;

@RestController
public class BulletScreenApi {

    @Autowired
    private UserSupport userSupport;

    @Autowired(required = false)
    private BulletScreenService bulletScreenService;

    @GetMapping("/bulletscreen")
    public JsonResponse<List<BulletScreen>> getBulletScreen(@RequestParam Long videoId,
                                                            String startTime, //按时间筛选弹幕时需要
                                                            String endTime) throws ParseException {
        List<BulletScreen> list;
        try {
            userSupport.getCurrentUserId();
            list = bulletScreenService.getBulletScreen(videoId, startTime, endTime);
        } catch (Exception e) {
            //游客模式，不能进行时间筛选
            list = bulletScreenService.getBulletScreen(videoId, null, null);
        }
        return new JsonResponse<>(list);
    }
}
