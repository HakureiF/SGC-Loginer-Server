package com.seer.seerweb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;


@Configuration
public class RedisConfig {

  @Bean
  public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory redisConnectionFactory) {

    RedisTemplate<String,String> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(redisConnectionFactory);
    //设置序列化Key的实例化对象
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    //设置序列化Value的实例化对象
    redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
    //设置序列化HashMap的实例化对象
    redisTemplate.setHashKeySerializer(new StringRedisSerializer());
    redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
    return redisTemplate;
  }

  @Bean
  public RedisTemplate<String, Integer> redisTemplateInt(RedisConnectionFactory redisConnectionFactory) {

    RedisTemplate<String, Integer> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(redisConnectionFactory);
    //设置序列化Key的实例化对象
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    //设置序列化Value的实例化对象
    redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
    //设置序列化HashMap的实例化对象
    redisTemplate.setHashKeySerializer(new StringRedisSerializer());
    redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
    return redisTemplate;
  }
}
