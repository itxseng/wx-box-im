package com.bx.implatform.mongo.repository;

import com.bx.implatform.mongo.document.GroupMessageDoc;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GroupMessageRepository extends MongoRepository<GroupMessageDoc, Long> {
}
