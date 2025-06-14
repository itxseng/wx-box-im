<template>
  <el-container class="chat-page">
    <el-aside width="260px" class="aside">
      <div class="header">
        <el-input class="search-text" size="small" placeholder="搜索" v-model="searchText">
          <i class="el-icon-search el-input__icon" slot="prefix"> </i>
        </el-input>
      </div>
      <div class="chat-loading" v-if="loading" v-loading="true" element-loading-text="消息接收中..."
        element-loading-spinner="el-icon-loading" element-loading-background="#F9F9F9" element-loading-size="24">
      </div>
      <el-scrollbar class="chat-items" v-else>
        <div v-for="(chat, index) in chatStore.chats" :key="index">
          <chat-item v-show="!chat.delete && chat.showName && chat.showName.includes(searchText)" :chat="chat"
            :index="index" @click.native="onActiveItem(index)" @delete="onDelItem(index)" @top="onTop(index)"
            @info="onShowInfo(chat)" :active="chat === chatStore.activeChat"></chat-item>
        </div>
      </el-scrollbar>
    </el-aside>
    <el-container class="chat-box">
      <chat-box v-if="activeChat && activeChat.type != 'SYSTEM'" :chat="activeChat"></chat-box>
      <chat-system-box v-if="activeChat && activeChat.type == 'SYSTEM'" :chat="activeChat"></chat-system-box>
    </el-container>
  </el-container>
</template>

<script>
import ChatItem from "../components/chat/ChatItem.vue";
import ChatBox from "../components/chat/ChatBox.vue";
import ChatSystemBox from "../components/chat/ChatSystemBox.vue";
export default {
  name: "chat",
  components: {
    ChatItem,
    ChatBox,
    ChatSystemBox
  },
  data() {
    return {
      searchText: "",
      messageContent: "",
      group: {},
      groupMembers: []
    }
  },
  methods: {
    onActiveItem(index) {
      this.chatStore.setActiveChat(index);
    },
    onDelItem(index) {
       this.chatStore.removeChat(index);
    },
    onTop(chatIdx) {
       this.chatStore.moveTop(chatIdx);
    },
    onShowInfo(chat) {
      if (chat.type == 'PRIVATE') {
        this.$router.push("/home/friend?id=" + chat.targetId);
      } else if (chat.type == 'GROUP') {
        if (!this.groupStore.isGroup(chat.targetId)) {
          this.$message.error("您已不在群聊中，无法查看群资料")
          return;
        }
        this.$router.push("/home/group?id=" + chat.targetId);
      }
    }
  },
  computed: {
    activeChat() {
      return this.chatStore.activeChat;
    },
    loading() {
      return this.chatStore.loadingGroupMsg || this.chatStore.loadingPrivateMsg
    }
  }
}
</script>

<style lang="scss">
.chat-page {
  .aside {
    display: flex;
    flex-direction: column;
    background: var(--im-background);

    .header {
      height: 50px;
      display: flex;
      align-items: center;
      padding: 0 8px;
    }

    .chat-loading {
      height: 50px;
      background-color: #eee;

      .el-icon-loading {
        font-size: 24px;
        color: var(--im-text-color-light);
      }

      .el-loading-text {
        color: var(--im-text-color-light);
      }

      .chat-loading-box {
        height: 100%;
      }
    }

    .chat-items {
      flex: 1;
    }
  }
}
</style>
