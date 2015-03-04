package com.shartfinder.round

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
class Turn {
	@Id BigInteger id
	BigInteger encounterId
	BigInteger characterId
	BigInteger roundId
}
