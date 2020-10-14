package com.yube.redis;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

public final class RedissonClientFactory {

    private RedissonClient client;
    private static RedissonClientFactory redissonClientFactory;

    private RedissonClientFactory() throws Exception {
        Config config = new Config();
        String address = "redis://host.docker.internal:6379";
        config.useSingleServer().setAddress(address);
        client = Redisson.create(config);
    }

    public static RedissonClientFactory getInstance() throws Exception {
        if (redissonClientFactory == null) {
            redissonClientFactory = new RedissonClientFactory();
        }
        return redissonClientFactory;
    }

    public RedissonClient getRedissonClient(){
        return client;
    }
}
