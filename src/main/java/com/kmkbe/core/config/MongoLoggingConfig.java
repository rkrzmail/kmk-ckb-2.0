package com.kmkbe.core.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoLoggingConfig {
    @Value("${spring.data.mongodb.database}")
    private String database;

    @Value("${spring.data.mongodb.collection}")
    private String collection;

    @Bean
    public MongoCollection<Document> provideMongoCollection() {
        MongoClient mongoClient = MongoClients.create();
        MongoDatabase db = mongoClient.getDatabase(database);
        return db.getCollection(collection);
    }
}
