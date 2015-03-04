package com.shartfinder.round

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
class Round {
	@Id BigInteger id
	BigInteger encounterId
	Integer roundNumber = 1
	Integer currentTurnIndex = 0
	List combatantsInTurnOrder
	
	def isNewRound() {
		return this.currentTurnIndex == 0
	}
}
