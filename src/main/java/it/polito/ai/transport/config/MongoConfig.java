package it.polito.ai.transport.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import com.mongodb.MongoClient;

@Configuration
@PropertySource("classpath:mongo.properties")
public class MongoConfig {

	@Bean
	public MongoDbFactory mongoDbFactory(Environment env){
		MongoClient mongoClient = new MongoClient(
				env.getRequiredProperty("mongo.url"),
				Integer.parseInt(env.getRequiredProperty("mongo.port")));
		
		MongoDbFactory mongoDbFactory = new SimpleMongoDbFactory(mongoClient, env.getRequiredProperty("mongo.db"));
		return mongoDbFactory;
	}
	
	@Bean
	public MongoTemplate mongoTemplate(MongoDbFactory mongoDbFactory){
		MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory);
		return mongoTemplate;
	}
	
}
