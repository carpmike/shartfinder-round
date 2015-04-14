package com.shartfinder.round

import org.springframework.beans.factory.annotation.Autowired;

import groovy.json.JsonSlurper

class RoundManager {
	private Map<String, Round> activeRounds
	
	@Autowired
	public RoundManager() {
		this.activeRounds = new HashMap<String, Round>()
	}
	
	public Round findRoundByEncounterId(String encounterId) {
		return this.activeRounds.get(encounterId)
	}
	
	public Round saveRoundFromInitiativeInfo(initiativeCreated) {
		def round = new Round()
		round.encounterId = initiativeCreated.encounterId
		// convert combatants to a list
		def combatants = []
		initiativeCreated.orderedCombatants.each { k,v ->
			combatants.add(v)
		}
		round.combatantsInTurnOrder = combatants
		
		this.activeRounds.put(round.encounterId, round)
		
		return round
	}
	
	public Map getCurrentTurnUserAndCombatant(String encounterId) {
		def round = this.activeRounds.get(encounterId)
		return round.combatantsInTurnOrder.get(round.currentTurnIndex)
	}
	
	public void finishTurn(String encounterId) {
		def round = this.findRoundByEncounterId(encounterId)
		// if at the end of the turns (and thus at the end of the round), set the turn index back to the beginning
		if (round.currentTurnIndex + 1 == round.combatantsInTurnOrder.size()) {
			round.currentTurnIndex = 0
		} else {
			round.currentTurnIndex++
		}
	}

}
