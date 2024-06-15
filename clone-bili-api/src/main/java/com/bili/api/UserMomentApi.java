package com.bili.api;

import Service.UserMomentsService;
import com.bili.api.support.UserSupport;
import domain.Annotation.ApiLimitedRole;
import domain.Annotation.DataLimited;
import domain.JsonResponse;
import domain.UserMoment;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static domain.Constant.Constants.*;

@RestController
public class UserMomentApi {

    @Autowired(required = false)
    private UserMomentsService userMomentsService;

    @Autowired
    private UserSupport userSupport;

    //发布动态
    @ApiLimitedRole(limitedRoleCodeList = {ROLE_CODE_LV0}) //lv0没有发布权限；角色变多可以改为角色组
    @DataLimited
    @PostMapping("/user-moments")
    public JsonResponse<String> addUserMoments(@RequestBody UserMoment userMoment) throws MQBrokerException, RemotingException, InterruptedException, MQClientException {
        Long userId = userSupport.getCurrentUserId();
        userMoment.setUserId(userId);
        userMomentsService.addUserMoments(userMoment);
        return new JsonResponse<>("Send succeed");
    }

    //查询关注动态
    @GetMapping("/user-subscribed-moments")
    public JsonResponse<List<UserMoment>> getUserSubscribedMoments() {
        Long userId = userSupport.getCurrentUserId();
        List<UserMoment> list = userMomentsService.getUserSubscribedMoments(userId);
        return new JsonResponse<>(list);
    }

}
