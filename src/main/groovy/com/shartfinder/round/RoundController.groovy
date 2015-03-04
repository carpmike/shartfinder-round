package com.shartfinder.round

import groovy.json.JsonSlurper
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
class RoundController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RoundController.class)
	
	private StringRedisTemplate redisTemplate
	
	@Autowired
	public RoundController(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate
	}

	@RequestMapping("/initiate")
	@ResponseBody
	ResponseEntity<String> initiativeCreatedTest() {
		LOGGER.info("Initiative created called for testing")
		redisTemplate.convertAndSend(Application.TOPIC_INITIATIVE_CREATED, '{"encounterId": 69, "orderedCombatantIds": [{"userId":"Jason", "combatantName":"SilverNoun"},{"userId":"Jim", "combatantName":"Goblin 1"},{"userId":"Jim", "combatantName":"Goblin 2"}]}'.toString())
		new ResponseEntity<String>("", HttpStatus.OK)
	}
	
	@RequestMapping("/end-turn")
	@ResponseBody
	ResponseEntity<String> endTurn() {
		LOGGER.info("End turn called for testing")
		redisTemplate.convertAndSend(Application.TOPIC_TURN_FINISHED, '{"encounterId":69, "userId":"Jason", "combatantName":"SilverNoun"}'.toString())
		new ResponseEntity<String>("", HttpStatus.OK)
	}

	@ExceptionHandler
	ResponseEntity<String> handle(RoundException infe) {
		new ResponseEntity<String>(infe.message, HttpStatus.NOT_FOUND)
	}
}
