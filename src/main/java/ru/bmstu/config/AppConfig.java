package ru.bmstu.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;
import ru.bmstu.service.LogService;
import ru.bmstu.service.StudentService;
import ru.bmstu.service.impl.LogServiceImpl;
import ru.bmstu.service.impl.StudentServiceImpl;

@Configuration //java-конфигурация(регистрируют классы как Spring-бины)
@ComponentScan("ru.bmstu")
@EnableAspectJAutoProxy(proxyTargetClass = true)
@PropertySource("classpath:application.properties") 
public class AppConfig {
    
    @Value("${csv.file.path}")
    private String csvFilePath;
    
    @Value("${log.file.path}")
    private String logFilePath;

    //значит, что такие методы будут возвращать бины (объекты), которые он уже поместит себе в контекст.
    @Bean
    public LogService logService() {
        return new LogServiceImpl(logFilePath);
    }
    
    @Bean
    public StudentService studentService(LogService logService) {
        return new StudentServiceImpl(csvFilePath, logService);
    }
}