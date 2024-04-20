package org.qiyu.live.account.provider.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import jakarta.annotation.Resource;
import org.idea.qiyu.live.framework.redis.starter.key.AccountProviderCacheKeyBuilder;
import org.qiyu.live.account.provider.service.IAccountTokenService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class IAccountTokenServiceImpl implements IAccountTokenService {
    @Resource
    AccountProviderCacheKeyBuilder accountProviderCacheKeyBuilder;

    @Resource
    RedisTemplate<String,Object> redisTemplate;

    @Override
    public String createAndSaveLoginToken(Long userId) {
        String token= UUID.randomUUID().toString();
        String key = accountProviderCacheKeyBuilder.buildUserLoginTokenKey(token);
        redisTemplate.opsForValue().set(key, String.valueOf(userId),30, TimeUnit.DAYS);
        return token;
    }

    @Override
    public Long getUserIdByToken(String tokenKey) {
        String key = accountProviderCacheKeyBuilder.buildUserLoginTokenKey(tokenKey);
        Integer s = (Integer) redisTemplate.opsForValue().get(key);
        if(s==null){
            return null;
        }
        return Long.valueOf(s);
    }

}
