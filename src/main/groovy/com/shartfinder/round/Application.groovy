package com.shartfinder.round

import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EnableAutoConfiguration
@ComponentScan("com.shartfinder.round")
public class Application {

	private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
	
	public static String TOPIC_INITIATIVE_CREATED = "initiative-created"
	public static String TOPIC_ROUND_STARTED = "round-started"
	public static String TOPIC_ROUND_FINISHED = "round-ended"
	public static String TOPIC_TURN_STARTED = "turn-started"
	public static String TOPIC_TURN_FINISHED = "end-turn"
	
	@Bean
	RedisConnectionFactory connectionFactory() {
		JedisConnectionFactory factory = new JedisConnectionFactory();
		factory.setHostName("pub-redis-18240.us-east-1-3.1.ec2.garantiadata.com");
		factory.setPort(18240);
		factory.setPassword("abc123");
		
		return factory;
	}
	
	@Bean
	RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
			MessageListenerAdapter listenerAdapter) {

		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.addMessageListener(listenerAdapter, new PatternTopic(Application.TOPIC_INITIATIVE_CREATED));
		container.addMessageListener(listenerAdapter, new PatternTopic(Application.TOPIC_TURN_FINISHED));
		
		return container;
	}

	@Bean
	MessageListenerAdapter listenerAdapter(Receiver receiver) {
		return new MessageListenerAdapter(receiver, "receiveMessage");
	}

	@Bean
	Receiver receiver(StringRedisTemplate redisTemplate, RoundManager roundManager, CountDownLatch latch) {
		return new Receiver(redisTemplate, roundManager, latch);
	}
	
	@Bean
	RoundManager roundManager() {
		return new RoundManager()
	}

	@Bean
	CountDownLatch latch() {
		return new CountDownLatch(1);
	}
	
	@Bean
	StringRedisTemplate redisTemplate(RedisConnectionFactory connectionFactory) {
		return new StringRedisTemplate(connectionFactory);
	}
	
	@Bean
	RedisTemplate<String, Round> redisTemplateRound(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, Round> redisTemplate = new RedisTemplate<String, Round>();
		redisTemplate.setConnectionFactory(connectionFactory);
		redisTemplate.setKeySerializer(new StringRedisSerializer());

		Jackson2JsonRedisSerializer<Round> valueSerializer = new Jackson2JsonRedisSerializer<Round>(
				Round.class);
			
		// load Java 8 temporal classes	
		valueSerializer.setObjectMapper(new ObjectMapper().findAndRegisterModules());

		redisTemplate.setValueSerializer(valueSerializer);

		return redisTemplate;
	}

	public static void main(String[] args) throws InterruptedException {

		ApplicationContext ctx = SpringApplication.run(Application.class, args);

	}
}
