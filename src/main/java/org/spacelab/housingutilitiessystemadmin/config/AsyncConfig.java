package org.spacelab.housingutilitiessystemadmin.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//@Configuration
//@EnableAsync
//@RequiredArgsConstructor
public class AsyncConfig implements WebMvcConfigurer {

//    private final SecurityContextCallableInterceptor securityContextInterceptor;
//
//    @Bean(name = "securityExecutor")
//    public AsyncTaskExecutor securityExecutor() {
//        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//        executor.setCorePoolSize(5);
//        executor.setMaxPoolSize(10);
//        executor.setQueueCapacity(100);
//        executor.setThreadNamePrefix("async-");
//        executor.initialize();
//
//        return new DelegatingSecurityContextAsyncTaskExecutor(executor);
//    }
//
//    @Override
//    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
//        configurer.setTaskExecutor(securityExecutor());
//        configurer.setDefaultTimeout(30000);
//
//        // ✅ Регистрируем перехватчик для передачи SecurityContext
//        configurer.registerCallableInterceptors(securityContextInterceptor);
//    }
}
