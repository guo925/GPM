package com.gjx.gpms.util;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.baomidou.mybatisplus.generator.model.ClassAnnotationAttributes;
import org.apache.ibatis.annotations.Mapper;


import java.util.Collections;

public class MybatisPlusGenerator {

    /**
     * 实际使用版
     * Spring Boot项目生成全部表示例，有调整可以自己修改
     */
    public static void generator3() {
        FastAutoGenerator.create("jdbc:mysql://127.0.0.1:3306/graduation_project?useUnicode=true&useSSL=false&characterEncoding=utf8","root","000000")
                .globalConfig(builder -> {
                    builder.outputDir((System.getProperty("user.dir")+"/src/main/java"))
                            .disableOpenDir()
                            .author("gpms")
                            .enableSwagger();
                })
                .packageConfig(builder -> {
                    builder.parent("com.gjx.gpms")
                            .pathInfo(Collections.singletonMap(OutputFile.xml, System.getProperty("user.dir")+"/src/main/resources/mapper"));
                })
                .strategyConfig(builder -> {
                    builder.enableSkipView()
                            .entityBuilder().enableLombok(new ClassAnnotationAttributes("@Data","lombok.Data"))
                            .mapperBuilder().mapperAnnotation(Mapper.class);
                })
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();

    }

    public static void main(String[] args) {
        // generator1(); // 全部配置清单版
        // generator2(); // 常用配置清单版
        generator3(); // 实际使用版
    }
}
