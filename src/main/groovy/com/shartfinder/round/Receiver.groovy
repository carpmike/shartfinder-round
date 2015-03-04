package com.shartfinder.round

import groovy.json.JsonSlurper;

import java.util.concurrent.CountDownLatch

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

public class Receiver {
    private static final Logger LOGGER = LoggerFactory.getLogger(Receiver.class)

    private CountDownLatch latch
	private StringRedisTemplate redisTemplate
	private RoundManager roundManager

    @Autowired
    public Receiver(StringRedisTemplate redisTemplateRound, RoundManager roundManager, CountDownLatch latch) {
		this.redisTemplate = redisTemplateRound
		this.roundManager = roundManager
        this.latch = latch
    }

    public void receiveMessage(String message, String pattern) {
        LOGGER.info("Received <" + message + "> on pattern <" + pattern + ">")
		switch(pattern) {
			case Application.TOPIC_INITIATIVE_CREATED:
				handleInitiativeCreatedActions(message)
				break
			case Application.TOPIC_TURN_FINISHED:
				// finish the turn
				def turnFinished = reifyJson(message)
				this.roundManager.finishTurn(turnFinished.encounterId)
				def round = this.roundManager.findRoundByEncounterId(turnFinished.encounterId)
				LOGGER.info("Next round: " + round)
				if (round.isNewRound()) {
					redisTemplate.convertAndSend(Application.TOPIC_ROUND_STARTED, "{\"encounterId\":${round.encounterId},\"roundNumber\":${round.roundNumber}}".toString())
				}
				// then start the next turn (turn started event)
				def combatantAndUser = roundManager.getCurrentTurnUserAndCombatant(turnFinished.encounterId)
				redisTemplate.convertAndSend(Application.TOPIC_TURN_STARTED, "{\"encounterId\":${round.encounterId},\"userId\":\"${combatantAndUser.userId}\",\"combatantName\":\"${combatantAndUser.combatantName}\"}".toString())
				LOGGER.info("Turn finished action")
				break
		}
        latch.countDown()
    }

	private handleInitiativeCreatedActions(String message) {
		def round = this.roundManager.saveRoundFromInitiativeInfo(reifyJson(message))
		// fire a round started event
		redisTemplate.convertAndSend(Application.TOPIC_ROUND_STARTED, "{\"encounterId\":${round.encounterId},\"roundNumber\":${round.roundNumber}}".toString())
		// fire a turn started event
		def combatantAndUser = roundManager.getCurrentTurnUserAndCombatant(round.encounterId)
		redisTemplate.convertAndSend(Application.TOPIC_TURN_STARTED, "{\"encounterId\":${round.encounterId},\"userId\":\"${combatantAndUser.userId}\",\"combatantName\":\"${combatantAndUser.combatantName}\"}".toString())

		LOGGER.info("Initiative created action")
	}
	
	private reifyJson(String message) {
		def jsonSlurper = new JsonSlurper() //jsonslurper is not thread safe so create a new one each time since this is a singleton
		def jsonObject = jsonSlurper.parseText(message)
		assert jsonObject != null
		return jsonObject
	}
}
