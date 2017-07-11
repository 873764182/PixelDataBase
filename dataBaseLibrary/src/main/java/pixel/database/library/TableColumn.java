package pixel.database.library;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by pixel on 2017/6/5.
 * <p>
 * 代表映射到数据库 与 MapField一样的效果 只是名称更合理
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)  // 只能注解属性
@Documented
public @interface TableColumn {

    /* 默认内容 */
    String value() default "TableColumn";

    /* 字段描述 */
    String des() default "";

    /* 最大长度 */
    int maxLength() default Integer.MAX_VALUE;

    /* 是否启用 默认启用 */
    boolean enable() default true;

    /* 默认值 即:插入数据库时对应的字段要是为空则插入该默认值到数据库字段 */
    String defValue() default "";
}
