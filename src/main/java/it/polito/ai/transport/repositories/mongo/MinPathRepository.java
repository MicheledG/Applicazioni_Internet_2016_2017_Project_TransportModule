package it.polito.ai.transport.repositories.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import it.polito.ai.transport.model.mongo.MinPath;

@Repository
public interface MinPathRepository extends MongoRepository<MinPath, org.bson.types.ObjectId> {
	
	public MinPath findOneByIdSourceAndIdDestination(String idSource, String idDestination); 

}
