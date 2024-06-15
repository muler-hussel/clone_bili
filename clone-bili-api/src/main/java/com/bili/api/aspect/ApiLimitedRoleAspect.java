package com.bili.api.aspect;

import Service.UserRoleService;
import com.bili.api.support.UserSupport;
import domain.Annotation.ApiLimitedRole;
import domain.Auth.UserRole;
import domain.Exception.ConditionException;
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

@Order(1)
@Component
@Aspect

public class ApiLimitedRoleAspect {

    @Autowired
    private UserSupport userSupport;

    @Autowired(required = false)
    private UserRoleService userRoleService;

    @Pointcut("@annotation(domain.Annotation.ApiLimitedRole)")
    public void check() {
    }

    @Before("check() && @annotation(apiLimitedRole)")
    public void doBefore(JoinPoint joinPoint, ApiLimitedRole apiLimitedRole) {
        Long userId = userSupport.getCurrentUserId();
        //获取当前用户角色
        List<UserRole> userRoleList = userRoleService.getUserRoleByUserId(userId);
        //获取受限的角色
        String[] limitedRoleCodeList = apiLimitedRole.limitedRoleCodeList();
        Set<String> limitedRoleCode = Arrays.stream(limitedRoleCodeList).collect(Collectors.toSet());
        Set<String> userRoleCode = userRoleList.stream().map(UserRole :: getRoleCode).collect(Collectors.toSet());
        //取交集
        userRoleCode.retainAll(limitedRoleCode);
        if (userRoleCode.size() > 0) {
            throw new ConditionException("User limited");
        }
    }
}
