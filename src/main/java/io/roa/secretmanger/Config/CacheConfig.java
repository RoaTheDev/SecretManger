package io.roa.secretmanger.Config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String MEMBERSHIP      = "membership";
    public static final String APPROVAL        = "approval-status";
    public static final String CREDENTIAL      = "credential-detail";
    public static final String USER_ROLE       = "user-role";
    public static final String DELETION_VOTES  = "deletion-votes";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();

        manager.registerCustomCache(MEMBERSHIP,
                Caffeine.newBuilder()
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .maximumSize(500)
                        .recordStats()
                        .build());

        manager.registerCustomCache(APPROVAL,
                Caffeine.newBuilder()
                        .expireAfterWrite(30, TimeUnit.SECONDS)
                        .maximumSize(200)
                        .recordStats()
                        .build());

        manager.registerCustomCache(CREDENTIAL,
                Caffeine.newBuilder()
                        .expireAfterWrite(10, TimeUnit.MINUTES)
                        .maximumSize(300)
                        .recordStats()
                        .build());

        manager.registerCustomCache(USER_ROLE,
                Caffeine.newBuilder()
                        .expireAfterWrite(10, TimeUnit.MINUTES)
                        .maximumSize(200)
                        .recordStats()
                        .build());

        return manager;
    }
}