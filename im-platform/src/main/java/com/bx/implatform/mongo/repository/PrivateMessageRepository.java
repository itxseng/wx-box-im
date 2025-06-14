package com.bx.implatform.mongo.repository;

import com.bx.implatform.mongo.document.PrivateMessageDoc;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PrivateMessageRepository extends MongoRepository<PrivateMessageDoc, Long> {
}
