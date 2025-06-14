package com.bx.implatform.mongo.document;

import com.bx.implatform.entity.PrivateMessage;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "im_private_message")
public class PrivateMessageDoc {
    @Id
    private Long id;
    private Long sendId;
    private Long recvId;
    private String content;
    private Integer type;
    private Long quoteMessageId;
    private Integer status;
    private Date sendTime;

    public static PrivateMessageDoc fromEntity(PrivateMessage message) {
        PrivateMessageDoc doc = new PrivateMessageDoc();
        doc.setId(message.getId());
        doc.setSendId(message.getSendId());
        doc.setRecvId(message.getRecvId());
        doc.setContent(message.getContent());
        doc.setType(message.getType());
        doc.setQuoteMessageId(message.getQuoteMessageId());
        doc.setStatus(message.getStatus());
        doc.setSendTime(message.getSendTime());
        return doc;
    }
}
