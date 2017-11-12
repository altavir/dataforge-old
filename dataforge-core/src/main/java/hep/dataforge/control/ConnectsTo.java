package hep.dataforge.control;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface ConnectsTo {
    String type();
    String[] roles() default {};
    Class<? extends Connection> cl();
}
