package com.bx.implatform.mongo.service;

import com.bx.implatform.entity.GroupMessage;
import com.bx.implatform.entity.PrivateMessage;
import com.bx.implatform.mongo.document.GroupMessageDoc;
import com.bx.implatform.mongo.document.PrivateMessageDoc;
import com.bx.implatform.mongo.repository.GroupMessageRepository;
import com.bx.implatform.mongo.repository.PrivateMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MongoMessageService {
    private final PrivateMessageRepository privateRepo;
    private final GroupMessageRepository groupRepo;
    private final MongoTemplate mongoTemplate;

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

    public Optional<PrivateMessage> findPrivateMessageById(Long id) {
        return privateRepo.findById(id).map(PrivateMessageDoc::toEntity);
    }

    public List<PrivateMessage> findPrivateMessages(Long userId, Long minId, Date minDate) {
        Query query = new Query();
        Criteria c = Criteria.where("id").gt(minId)
                .and("sendTime").gte(minDate)
                .andOperator(
                        new Criteria().orOperator(
                                Criteria.where("sendId").is(userId),
                                Criteria.where("recvId").is(userId)
                        )
                );
        query.addCriteria(c);
        query.with(Sort.by(Sort.Direction.ASC, "id"));
        return mongoTemplate.find(query, PrivateMessageDoc.class).stream()
                .map(PrivateMessageDoc::toEntity).collect(Collectors.toList());
    }

    public List<PrivateMessage> findPrivateHistory(Long userId, Long friendId, long skip, long limit) {
        Query query = new Query();
        Criteria c = new Criteria().orOperator(
                new Criteria().andOperator(Criteria.where("sendId").is(userId), Criteria.where("recvId").is(friendId)),
                new Criteria().andOperator(Criteria.where("sendId").is(friendId), Criteria.where("recvId").is(userId))
        );
        query.addCriteria(c);
        query.addCriteria(Criteria.where("status").ne(2));
        query.with(Sort.by(Sort.Direction.DESC, "id"));
        query.skip(skip).limit((int) limit);
        return mongoTemplate.find(query, PrivateMessageDoc.class).stream()
                .map(PrivateMessageDoc::toEntity).collect(Collectors.toList());
    }

    public void markPrivateMessagesRead(Long friendId, Long myId, Integer sended, Integer readed) {
        Query query = new Query();
        query.addCriteria(Criteria.where("sendId").is(friendId).and("recvId").is(myId).and("status").is(sended));
        Update update = new Update().set("status", readed);
        mongoTemplate.updateMulti(query, update, PrivateMessageDoc.class);
    }

    public Long getMaxReadedId(Long userId, Long friendId, Integer readed) {
        Query query = new Query();
        query.addCriteria(Criteria.where("sendId").is(userId).and("recvId").is(friendId).and("status").is(readed));
        query.with(Sort.by(Sort.Direction.DESC, "id"));
        query.limit(1);
        PrivateMessageDoc doc = mongoTemplate.findOne(query, PrivateMessageDoc.class);
        return doc == null ? -1L : doc.getId();
    }

    public List<PrivateMessage> findPrivateMessagesByIds(Collection<Long> ids) {
        Query query = new Query(Criteria.where("id").in(ids));
        return mongoTemplate.find(query, PrivateMessageDoc.class).stream()
                .map(PrivateMessageDoc::toEntity).collect(Collectors.toList());
    }

    public Optional<GroupMessage> findGroupMessageById(Long id) {
        return groupRepo.findById(id).map(GroupMessageDoc::toEntity);
    }

    public List<GroupMessage> findGroupMessages(Long minId, Date minDate, Collection<Long> groupIds, Integer recall) {
        Query query = new Query();
        query.addCriteria(Criteria.where("id").gt(minId)
                .and("sendTime").gte(minDate)
                .and("groupId").in(groupIds)
                .and("status").ne(recall));
        query.with(Sort.by(Sort.Direction.ASC, "id"));
        return mongoTemplate.find(query, GroupMessageDoc.class).stream()
                .map(GroupMessageDoc::toEntity).collect(Collectors.toList());
    }

    public List<GroupMessage> findQuitGroupMessages(Long groupId, Date minDate, Date quitTime, Long minId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("id").gt(minId)
                .and("sendTime").gte(minDate)
                .and("sendTime").lte(quitTime)
                .and("groupId").is(groupId));
        query.with(Sort.by(Sort.Direction.ASC, "id"));
        return mongoTemplate.find(query, GroupMessageDoc.class).stream()
                .map(GroupMessageDoc::toEntity).collect(Collectors.toList());
    }

    public List<GroupMessage> findGroupMessagesByIds(Collection<Long> ids) {
        Query query = new Query(Criteria.where("id").in(ids));
        return mongoTemplate.find(query, GroupMessageDoc.class).stream()
                .map(GroupMessageDoc::toEntity).collect(Collectors.toList());
    }

    public List<GroupMessage> findGroupHistory(Long groupId, Date joinTime, long skip, long limit, Integer recall) {
        Query query = new Query();
        query.addCriteria(Criteria.where("groupId").is(groupId)
                .and("sendTime").gt(joinTime)
                .and("status").ne(recall));
        query.with(Sort.by(Sort.Direction.DESC, "id"));
        query.skip(skip).limit((int) limit);
        return mongoTemplate.find(query, GroupMessageDoc.class).stream()
                .map(GroupMessageDoc::toEntity).collect(Collectors.toList());
    }

    public GroupMessage findLastGroupMessage(Long groupId) {
        Query query = new Query(Criteria.where("groupId").is(groupId));
        query.with(Sort.by(Sort.Direction.DESC, "id"));
        query.limit(1);
        GroupMessageDoc doc = mongoTemplate.findOne(query, GroupMessageDoc.class);
        return doc == null ? null : doc.toEntity();
    }

    public List<GroupMessage> findReceiptMessages(Long groupId, Long startId, Long endId, Integer recall) {
        Query query = new Query();

        Criteria idCriteria = Criteria.where("id").gt(startId).lte(endId);
        Criteria otherCriteria = new Criteria().andOperator(
                Criteria.where("groupId").is(groupId),
                Criteria.where("status").ne(recall),
                Criteria.where("receipt").is(true)
        );

        query.addCriteria(new Criteria().andOperator(idCriteria, otherCriteria));
        return mongoTemplate.find(query, GroupMessageDoc.class).stream()
                .map(GroupMessageDoc::toEntity).collect(Collectors.toList());
    }

    public void updateGroupMessageReceiptOk(Long id, boolean ok) {
        Query query = new Query(Criteria.where("id").is(id));
        Update update = new Update().set("receiptOk", ok);
        mongoTemplate.updateFirst(query, update, GroupMessageDoc.class);
    }
}
