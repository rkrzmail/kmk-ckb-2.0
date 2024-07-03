package com.kmkbe.core.service;

import com.kmkbe.core.model.HttpLoggerPayload;
import com.mongodb.client.MongoCollection;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoggingService {
    private final MongoCollection<Document> collection;

    @Async
    public void create(HttpLoggerPayload payload) {
        collection.insertOne(payload.toCollection());
    }
}
