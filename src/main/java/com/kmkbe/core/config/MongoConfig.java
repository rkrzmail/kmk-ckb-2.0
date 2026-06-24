package com.kmkbe.core.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfig {

  @Value("${spring.data.mongodb.database}")
  private String database;

  @Value("${custom.mongodb.collection}")
  private String collection;

  @Bean
  public MongoCollection<Document> provideMongoCollection(MongoClient mongoClient) {
    // Spring Boot automatically injects its own safely-managed mongoClient bean here
    MongoDatabase db = mongoClient.getDatabase(database);
    return db.getCollection(collection);
  }
}
