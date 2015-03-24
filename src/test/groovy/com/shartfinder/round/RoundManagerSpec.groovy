package com.shartfinder.round

import groovy.json.JsonSlurper;
import spock.lang.Specification


class RoundManagerSpec extends Specification {
	def RoundManager rm = new RoundManager()
	def jsonSlurper = new JsonSlurper()
	def eid = 69
	def user1id = "Jason"
	def initString = "{\"encounterId\": $eid, \"orderedCombatants\":{\"$user1id\":{\"initiative\":93,\"diceRoll\":92,\"combatantName\":\"$user1id\",\"user\":\"\"},\"screw you jason\":{\"initiative\":21,\"diceRoll\":20,\"combatantName\":\"screw you jason\",\"user\":\"\"}}}".toString()
		
	def "save initiative info"() {
		given:
		def init = reifyJson(initString)
		println init
		
		when:
		def round = rm.saveRoundFromInitiativeInfo(init)
		
		then:
		round.encounterId == eid
		round.combatantsInTurnOrder != null
		round.combatantsInTurnOrder.get(0).combatantName == user1id
	}
	
	def "find encounter"() {
		given:
		def init = reifyJson(initString)
		rm.saveRoundFromInitiativeInfo(init)
		
		when:
		def round = rm.findRoundByEncounterId(eid)
		
		then:
		round.encounterId == eid
		round.combatantsInTurnOrder != null
		round.combatantsInTurnOrder.get(0).combatantName == user1id	
	}
	
	def reifyJson(String message) {
		def jsonObject = jsonSlurper.parseText(message)
		assert jsonObject != null
		return jsonObject
	}

}
