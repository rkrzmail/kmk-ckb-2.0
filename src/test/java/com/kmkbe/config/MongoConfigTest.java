package com.kmkbe.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test MongoConfig: bean MongoCollection diambil dari database
 * dan collection yang dikonfigurasi (kmklog / event di prod).
 */
class MongoConfigTest {

  private MongoConfig config;
  private MongoClient mongoClient;
  private MongoDatabase database;
  @SuppressWarnings("unchecked")
  private final MongoCollection<Document> collection = mock(MongoCollection.class);

  @BeforeEach
  void setUp() {
    config = new MongoConfig();
    ReflectionTestUtils.setField(config, "database", "kmklog");
    ReflectionTestUtils.setField(config, "collection", "event");
    mongoClient = mock(MongoClient.class);
    database = mock(MongoDatabase.class);
    when(mongoClient.getDatabase("kmklog")).thenReturn(database);
    when(database.getCollection("event")).thenReturn(collection);
  }

  @Test
  void provideMongoCollectionUsesConfiguredDatabaseAndCollection() {
    MongoCollection<Document> result = config.provideMongoCollection(mongoClient);

    assertThat(result).isSameAs(collection);
    verify(mongoClient).getDatabase("kmklog");
    verify(database).getCollection("event");
  }
}
