package org.spacelab.housingutilitiessystemadmin;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cm = new CaffeineCacheManager();
        cm.setCacheSpecification("maximumSize=10000,expireAfterWrite=30m");
        cm.setAsyncCacheMode(true);
        return cm;
    }
}
