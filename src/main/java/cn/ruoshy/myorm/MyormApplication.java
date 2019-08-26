package cn.ruoshy.myorm;

import cn.ruoshy.myorm.orm.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan(basePackages = {"cn.ruoshy.myorm.mapper"})
@SpringBootApplication
public class MyormApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyormApplication.class, args);
    }

}
