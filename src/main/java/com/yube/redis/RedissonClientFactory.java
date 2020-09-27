package com.yube.redis;

import com.yube.exceptions.ConfigurationException;
import com.yube.utils.PropertiesHandler;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.Properties;

public final class RedissonClientFactory {

    private RedissonClient client;
    private static RedissonClientFactory redissonClientFactory;

    private RedissonClientFactory() throws Exception {
        Config config = new Config();
        String address = "redis://" + System.getenv("REDIS_ADDR");
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
