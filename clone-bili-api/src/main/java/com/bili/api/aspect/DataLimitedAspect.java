package com.bili.api.aspect;

import Service.UserRoleService;
import com.bili.api.support.UserSupport;
import domain.Auth.UserRole;
import domain.Exception.ConditionException;
import domain.UserMoment;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static domain.Constant.Constants.ROLE_CODE_LV0;

@Order(1)
@Component
@Aspect

public class DataLimitedAspect {

    @Autowired
    private UserSupport userSupport;

    @Autowired(required = false)
    private UserRoleService userRoleService;

    @Pointcut("@annotation(domain.Annotation.DataLimited)")
    public void check() {
    }

    @Before("check()")
    public void doBefore(JoinPoint joinPoint) {
        Long userId = userSupport.getCurrentUserId();
        //获取当前用户角色
        List<UserRole> userRoleList = userRoleService.getUserRoleByUserId(userId);
        Set<String> userRoleCode = userRoleList.stream().map(UserRole :: getRoleCode).collect(Collectors.toSet());
        //获取当前方法的所有参数
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof UserMoment) {
                UserMoment userMoment = (UserMoment) arg;
                String type = userMoment.getType();
                if (userRoleCode.contains(ROLE_CODE_LV0) && !"0".equals(type)) {
                    throw new ConditionException("User Limited");
                }
            }
        }
    }
}
