package backend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.session.data.mongo.config.annotation.web.reactive.EnableMongoWebSession;

import com.mongodb.reactivestreams.client.MongoClient;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableReactiveMongoRepositories(basePackages = "com.example.webflux.**.repository",
	reactiveMongoTemplateRef = "reactiveMongoTemplate")
// reactiveMongoTemplateRef = "getABeerReactiveMongoTemplate")
@RequiredArgsConstructor
@EnableMongoAuditing
@EnableMongoWebSession
public class ReactiveMongoDBConfig {
	private final MongoMappingContext mongoMappingContext;
	private final MongoClient mongoClient;

	@Bean
	public ReactiveMongoTemplate reactiveMongoTemplate() {
		return new ReactiveMongoTemplate(mongoClient, "getABeer");
	}

	// @Bean
	// public ReactiveMongoDatabaseFactory getABeerReactiveMongoDatabaseFactory(MongoClient mongoClient) {
	// 	return new SimpleReactiveMongoDatabaseFactory(mongoClient, "getABeer");
	// }
	//
	// @Bean
	// public MappingMongoConverter reactiveMappingMongoConverter() {
	// 	MappingMongoConverter mappingMongoConverter = new MappingMongoConverter(
	// 		ReactiveMongoTemplate.NO_OP_REF_RESOLVER,
	// 		mongoMappingContext);
	//
	// 	/*This setting removes the field of the _class*/
	// 	mappingMongoConverter.setTypeMapper(new DefaultMongoTypeMapper(null));
	// 	return mappingMongoConverter;
	// }

	// @Bean
	// public ReactiveMongoTemplate getABeerReactiveMongoTemplate(
	// 	ReactiveMongoDatabaseFactory reactiveMongoDatabaseFactory,
	// 	MappingMongoConverter reactiveMappingMongoConverter) {
	//
	// 	return new ReactiveMongoTemplate(reactiveMongoDatabaseFactory, reactiveMappingMongoConverter);
	// }
}
