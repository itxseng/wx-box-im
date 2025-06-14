package com.bx.implatform.mongo.service;

import com.bx.implatform.entity.GroupMessage;
import com.bx.implatform.entity.PrivateMessage;
import com.bx.implatform.mongo.document.GroupMessageDoc;
import com.bx.implatform.mongo.document.PrivateMessageDoc;
import com.bx.implatform.mongo.repository.GroupMessageRepository;
import com.bx.implatform.mongo.repository.PrivateMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MongoMessageService {
    private final PrivateMessageRepository privateRepo;
    private final GroupMessageRepository groupRepo;

    public void savePrivateMessage(PrivateMessage message) {
        privateRepo.save(PrivateMessageDoc.fromEntity(message));
    }

    public void updatePrivateMessageStatus(Long id, Integer status) {
        privateRepo.findById(id).ifPresent(doc -> {
            doc.setStatus(status);
            privateRepo.save(doc);
        });
    }

    public void saveGroupMessage(GroupMessage message) {
        groupRepo.save(GroupMessageDoc.fromEntity(message));
    }

    public void updateGroupMessageStatus(Long id, Integer status) {
        groupRepo.findById(id).ifPresent(doc -> {
            doc.setStatus(status);
            groupRepo.save(doc);
        });
    }
}
