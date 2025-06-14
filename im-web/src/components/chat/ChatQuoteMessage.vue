<template>
    <div class="chat-quote-message">
        <div v-if="isRecall" class="recall">{{ msgInfo.content }}</div>
        <div v-if="!isRecall" class="send-user">
            {{ showName + ":" }}
        </div>
        <div v-if="!isRecall" class="quote-content">
            <span v-if="msgInfo.type == $enums.MESSAGE_TYPE.TEXT" class="quote-text"
                v-html="$emo.transform(msgInfo.content, 'emoji-small')"></span>
            <div v-if="msgInfo.type == $enums.MESSAGE_TYPE.IMAGE">
                <img class="quote-image" :src="contentData.thumbUrl" @click.stop="showFullImageBox" />
            </div>
            <div v-if="msgInfo.type == $enums.MESSAGE_TYPE.VIDEO">
                <video ref="videoPlayer" class="quote-video" controls preload="none" :poster="contentData.coverUrl"
                    :src="contentData.videoUrl" />
            </div>
            <div v-if="msgInfo.type == $enums.MESSAGE_TYPE.AUDIO">
                <audio class="quote-audio" controls :src="contentData.url"></audio>
            </div>
            <div class="quote-file" v-if="msgInfo.type == $enums.MESSAGE_TYPE.FILE">
                <div class="file-info">
                    <el-link class="file-name" :underline="true" target="_blank" type="primary" :href="contentData.url"
                        :download="contentData.name">{{ contentData.name }}</el-link>
                    <div class="file-size">{{ fileSize }}</div>
                </div>
                <div class="file-icon">
                    <span type="primary" class="el-icon-document"></span>
                </div>
            </div>
            <div class="quote-card" v-if="msgInfo.type == $enums.MESSAGE_TYPE.USER_CARD">
                <div class="card-body">
                    <head-image :url="contentData.headImage" :name="contentData.nickName" radius="10%"
                        :size="32"></head-image>
                    <div class="name">
                        {{ contentData.nickName }}
                    </div>
                </div>
                <div class="card-tip">个人名片</div>
            </div>
            <div class="quote-card" v-if="msgInfo.type == $enums.MESSAGE_TYPE.GROUP_CARD">
                <div class="card-body">
                    <head-image :url="contentData.headImage" :name="contentData.groupName" radius="10%"
                        :size="32"></head-image>
                    <div class="name">
                        {{ contentData.groupName }}
                    </div>
                </div>
                <div class="card-tip">群名片</div>
            </div>
        </div>
    </div>
</template>

<script>
import HeadImage from '../common/HeadImage.vue';
export default {
    name: "chatQuoteMessage",
    components: { HeadImage },
    props: {
        showName: {
            type: String,
            required: true
        },
        msgInfo: {
            type: Object,
            required: true
        }
    },
    data() {
        return {

        }
    },
    methods: {
        showFullImageBox() {
            let imageUrl = this.contentData.originUrl;
            if (imageUrl) {
                this.$eventBus.$emit("openFullImage", imageUrl);
            }
        },
    },
    computed: {
        quoteMessageText() {
            return this.msgInfo.content + "hello";
        },
        contentData() {
            return JSON.parse(this.msgInfo.content)
        },
        fileSize() {
            let size = this.contentData.size;
            if (size > 1024 * 1024) {
                return Math.round(size / 1024 / 1024) + "M";
            }
            if (size > 1024) {
                return Math.round(size / 1024) + "KB";
            }
            return size + "B";
        },
        isRecall() {
            return this.msgInfo.status == this.$enums.MESSAGE_STATUS.RECALL
        }
    }
}
</script>

<style lang="scss" scoped>
.chat-quote-message {
    background: var(--im-background);
    padding: 5px;
    display: inline-flex;
    align-items: center;
    border-radius: 8px;
    font-size: var(--im-font-size-smaller);
    color: var(--im-text-color-light);

    .send-user {
        margin-right: 10px;
        font-weight: 600;
        white-space: nowrap;
    }

    .quote-content {

        .quote-text {
            display: inline-block;
            text-align: left;
            white-space: pre-wrap;
            word-break: break-all;
        }

        .quote-image {
            min-width: 40px;
            min-height: 30px;
            max-width: 80px;
            max-height: 60px;
            cursor: pointer;
        }

        .quote-video {
            min-width: 80px;
            min-height: 60px;
            max-width: 160px;
            max-height: 120px;
            cursor: pointer;
        }

        .quote-audio {
            height: 38px;
        }


        .quote-file {
            display: flex;
            flex-wrap: nowrap;
            flex-direction: row;
            align-items: center;
            cursor: pointer;
            padding: 2px 15px;
            background: white;
            box-shadow: var(--im-box-shadow-light);
            border-radius: 4px;

            .file-info {
                flex: 1;
                height: 100%;
                text-align: left;
                font-size: 14px;
                margin-right: 10px;

                .file-name {
                    display: inline-block;
                    min-width: 120px;
                    max-width: 180px;
                    font-size: 14px;
                    margin-bottom: 4px;
                    white-space: pre-wrap;
                    word-break: break-all;
                }

                .file-size {
                    font-size: var(--im-font-size-smaller);
                    color: var(--im-text-color-light);
                }
            }

            .file-icon {
                font-size: 32px;
                color: #d42e07;
            }
        }
    }

    .quote-card {
        display: flex;
        flex-direction: column;
        flex-wrap: nowrap;
        box-shadow: var(--im-box-shadow-light);
        border-radius: 10px;
        padding: 5px 15px;
        height: 60px;
        width: 150px;
        cursor: pointer;
        background: white;

        .card-body {
            flex: 1;
            display: flex;
            align-items: center;
            border-bottom: 2px solid #eee;

            .name {
                margin-left: 8px;
                font-weight: 600;
                font-size: var(--im-font-size-smaller);
                color: var(--im-text-color);
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
            }
        }

        .card-tip {
            margin-top: 4px;
            color: var(--im-text-color-light);
            font-size: 10px;
            text-align: left;
        }
    }
}
</style>