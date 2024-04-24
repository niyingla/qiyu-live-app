package org.qiyu.live.web.starter.limit;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestLimit {
    /**
     * 请求允许的量
     * @return
     */
    int limit();

    /**
     * 限制的时长
     * @return
     */
    int second();

    /**
     * 限制之后提示的内容
     * @return
     */
    String msg() default "请求过于频繁";
}
