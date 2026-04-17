package xiaozhi.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * datafilterannotation
 * Copyright (c) Renren Opensource All rights reserved.
 * Website: https://www.renren.io
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataFilter {
    /**
     * table aliasname
     */
    String tableAlias() default "";

    /**
     * User ID
     */
    String userId() default "creator";

    /**
     * departmentID
     */
    String deptId() default "dept_id";

}