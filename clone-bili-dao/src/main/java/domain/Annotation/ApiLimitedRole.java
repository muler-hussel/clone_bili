package domain.Annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
@Component

public @interface ApiLimitedRole {

    //接口控制
    String[] limitedRoleCodeList() default {};
}
