<template>
    <div class="chat-top-message">
        <div class="title">置顶消息: </div>
        <div class="content" v-html="content" @click.stop="onClickMessage"> </div>
        <div class="close" title="移除" @click.stop="onClose"><i class="el-icon-close"></i></div>
    </div>
</template>
<script>
import HeadImage from '../common/HeadImage.vue';


export default {
    name: "chatTopMessage",
    components: { HeadImage },
    props: {
        group: {
            type: Object
        },
        groupMembers: {
            type: Array
        },
        msgInfo: {
            type: Object,
            required: true
        },
        headImage: {
            type: String,
            default: ''
        },
        showName: {
            type: String,
            required: true
        }
    },
    data() {
        return {

        }
    },
    methods: {
        onClose() {
            if (this.isOwner || this.isManager) {
                this.removeTopMessage();
            } else {
                this.hideTopMessage();
            }
        },
        onClickMessage() {
            // 定位消息
            this.$emit("locate", this.msgInfo);
        },
        removeTopMessage() {
            this.$confirm('将在当前群聊的所有成员中移除此置顶消息,确认移除？', '移除置顶', {
                confirmButtonText: '移除',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                this.$http({
                    url: "/group/removeTopMessage/" + this.group.id,
                    method: 'delete'
                })
            });
        },
        hideTopMessage() {
            this.$http({
                url: "/group/hideTopMessage/" + this.group.id,
                method: 'delete'
            })
        }
    },
    computed: {
        content() {
            let content = "不支持的消息类型"
            if (this.msgInfo.type == this.$enums.MESSAGE_TYPE.IMAGE) {
                content = "[图片]";
            } else if (this.msgInfo.type == this.$enums.MESSAGE_TYPE.VIDEO) {
                content = "[视频]";
            } else if (this.msgInfo.type == this.$enums.MESSAGE_TYPE.FILE) {
                content = "[文件] " + JSON.parse(this.msgInfo.content).name;
            } else if (this.msgInfo.type == this.$enums.MESSAGE_TYPE.AUDIO) {
                content = "[语音] " + JSON.parse(this.msgInfo.content).duration + '"';
            } else if (this.msgInfo.type == this.$enums.MESSAGE_TYPE.USER_CARD) {
                content = "[个人名片] " + JSON.parse(this.msgInfo.content).nickName;
            } else if (this.msgInfo.type == this.$enums.MESSAGE_TYPE.GROUP_CARD) {
                content = "[群名片] " + JSON.parse(this.msgInfo.content).groupName;
            }else if (this.msgInfo.type == this.$enums.MESSAGE_TYPE.TEXT) {
                content = this.$emo.transform(this.msgInfo.content, 'emoji-normal');
            }
            return content;
        },
        isOwner() {
            return this.group.ownerId == this.userStore.userInfo.id;
        },
        isManager(){
			let userId = this.userStore.userInfo.id;
			let m = this.groupMembers.find((m) => m.userId == userId);
			return m && m.isManager;
		}
    }

}
</script>

<style scoped lang="scss">
.chat-top-message {
    margin: 5px;
    display: flex;
    background: #DCEBFD;
    padding: 10px 15px;
    align-items: center;
    border-radius: 8px;

    .title {
        font-size: var(--im-font-size-large);
        color: #666;
    }

    .content {
        flex: 1;
        font-size: var(--im-font-size-large);
        color: #666;
        height: 25px;
        word-break: break-all;
        overflow: hidden;
        cursor: pointer;
        line-height: 25px;
        text-align: left;
        margin-left: 10px;
    }

    .close {
        margin-left: 10px;
        width: 30px;
        cursor: pointer;
    }
}
</style>