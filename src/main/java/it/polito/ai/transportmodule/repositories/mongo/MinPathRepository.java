package it.polito.ai.transportmodule.repositories.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import it.polito.ai.transportmodule.model.mongo.MinPath;

public interface MinPathRepository extends MongoRepository<MinPath, org.bson.types.ObjectId> {
	
	public MinPath findOneByIdSourceAndIdDestination(String idSource, String idDestination); 

}
