package cn.ruoshy.myorm.orm.register;

import cn.ruoshy.myorm.orm.annotation.Mapper;
import cn.ruoshy.myorm.orm.annotation.MapperScan;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//, ResourceLoaderAware, BeanFactoryAware
public class MapperScannerRegister implements ImportBeanDefinitionRegistrar {
    private static List<String> mappers = new ArrayList<>();

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        // 获得包路径
        Map<String, Object> attributes = importingClassMetadata.getAnnotationAttributes(MapperScan.class.getName());
        String[] basePackages = (String[]) attributes.get("basePackages");
        // 扫包
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry, false);
        scanner.addIncludeFilter((metadataReader, metadataReaderFactory) -> {
            boolean flag = metadataReader.getAnnotationMetadata().hasAnnotation(Mapper.class.getName());
            if (flag) {
                // 添加@Mapper注解的类位置
                mappers.add(metadataReader.getClassMetadata().getClassName());
            }
            return false;
        });
        // 开始扫包
        scanner.scan(basePackages);
    }

    public static List<String> getMappers() {
        return mappers;
    }
}