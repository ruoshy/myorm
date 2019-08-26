package cn.ruoshy.myorm.orm.annotation;

import cn.ruoshy.myorm.orm.register.MapperScannerRegister;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(MapperScannerRegister.class)
public @interface MapperScan {
    String[] basePackages();
}
