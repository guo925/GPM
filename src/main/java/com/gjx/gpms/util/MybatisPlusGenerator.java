package com.gjx.gpms.util;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collections;

public class MybatisPlusGenerator {

    public static void generator3() {
        FastAutoGenerator.create("jdbc:mysql://127.0.0.1:3306/graduation_project?useUnicode=true&useSSL=false&characterEncoding=utf8","root","123456")
                .globalConfig(builder -> {
                    builder.outputDir((System.getProperty("user.dir")+"/src/main/java"))
                            .disableOpenDir()
                            .author("gpms");
                })
                .packageConfig(builder -> {
                    builder.parent("com.gjx.gpms")
                            .pathInfo(Collections.singletonMap(OutputFile.xml, System.getProperty("user.dir")+"/src/main/resources/mapper"));
                })
                .strategyConfig(builder -> {
                    builder.enableSkipView()
                            .entityBuilder().enableLombok()
                            .mapperBuilder().mapperAnnotation(Mapper.class);
                })
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }

    public static void main(String[] args) {
        generator3();
    }
}
