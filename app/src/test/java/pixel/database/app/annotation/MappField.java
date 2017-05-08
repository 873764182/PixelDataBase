package pixel.database.app.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by pixel on 2017/5/8.
 * <p>
 * 是否映射到数据库
 * <p>
 * http://blog.csdn.net/bao19901210/article/details/17201173/
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)  // 只能注解属性
@Documented
public @interface MappField {

    /* 默认属性,可以不用属性名赋值. */
    String value() default "MapField";

    /* 是否启用 */
    boolean enable() default true;

    /* 描述信息 */
    String description() default "";

    /* 字段长度 */
    int length() default -1;

}
