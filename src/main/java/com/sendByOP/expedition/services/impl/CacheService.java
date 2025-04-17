package com.sendByOP.expedition.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {

    private final CacheManager cacheManager;

    /**
     * Vide un cache spécifique
     * @param cacheName le nom du cache à vider
     */
    public void evictCache(String cacheName) {
        log.info("Evicting cache: {}", cacheName);
        cacheManager.getCache(cacheName).clear();
    }

    /**
     * Vide tous les caches
     */
    public void evictAllCaches() {
        log.info("Evicting all caches");
        cacheManager.getCacheNames().forEach(cacheName -> {
            cacheManager.getCache(cacheName).clear();
        });
    }
}