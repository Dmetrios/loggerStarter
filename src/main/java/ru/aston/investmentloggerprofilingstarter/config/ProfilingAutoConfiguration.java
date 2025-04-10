package ru.aston.investmentloggerprofilingstarter.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import ru.aston.investmentloggerprofilingstarter.annotation.processor.ProfilingAnnotationBeanPostProcessor;

@AutoConfiguration
public class ProfilingAutoConfiguration {

    @Bean
    public ProfilingAnnotationBeanPostProcessor profilingAnnotationBeanPostProcessor() throws Exception {
        return new ProfilingAnnotationBeanPostProcessor();
    }
}
