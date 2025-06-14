<template>
    <el-dialog v-dialogDrag :title="title" :visible.sync="show" width="700px">
        <div class="chat-selector">
            <div class="left-box">
                <el-input placeholder="搜索" v-model="searchText" size="small">
                    <i class="el-icon-search el-input__icon" slot="suffix"> </i>
                </el-input>
                <tabs :items="tabItems" :current="tabIdx" @change="onTabChange"></tabs>
                <virtual-scroller v-if="tabIdx == 0" class="left-scroll-box" :items="showChats">
                    <template v-slot="{ item }">
                        <div class="chat-item" @click="onClickChatItem(item)">
                            <head-image :url="item.headImage" :name="item.showName" :size="42"></head-image>
                            <div class="chat-info">
                                <div class="chat-name">{{ item.showName }}
                                    <el-tag v-if="item.type == 'GROUP'" size="mini">群</el-tag>
                                </div>
                                <div class="chat-content" v-html="$emo.transform(item.lastContent, 'emoji-small')">
                                </div>
                            </div>
                            <el-checkbox v-model="item.checked" class="check-box" @click.native.stop=""
                                @change="onChatChange(item)"></el-checkbox>
                        </div>
                    </template>
                </virtual-scroller>
                <virtual-scroller v-if="tabIdx == 1" class="left-scroll-box" :items="showFriends">
                    <template v-slot="{ item }">
                        <friend-item :menu="false" :friend="item" size="small" @click.native="onClickFriendItem(item)">
                            <el-checkbox v-model="item.checked" class="check-box" @click.native.stop=""
                                @change="onFriendChange(item)"></el-checkbox>
                        </friend-item>
                    </template>
                </virtual-scroller>
                <virtual-scroller v-if="tabIdx == 2" class="left-scroll-box" :items="showGroups">
                    <template v-slot="{ item }">
                        <group-item :group="item" :menu="false" size="small" @click.native="onClickGroupItem(item)">
                            <el-checkbox v-model="item.checked" class="check-box" @click.native.stop=""
                                @change="onGroupChange(item)"></el-checkbox>
                        </group-item>
                    </template>
                </virtual-scroller>
            </div>
            <div class="arrow el-icon-d-arrow-right"></div>
            <div class="right-box">
                <div class="tip">已勾选{{ checkedItems.length }}个联系人</div>
                <el-scrollbar class="right-scroll-box">
                    <div v-for="(item, idx) in checkedItems" :key="idx">
                        <div class="checked-item">
                            <head-image :url="item.headImage" :name="item.showName" :size="36"></head-image>
                            <div class="name">{{ item.showName }}</div>
                            <div class="close" title="移除" @click.stop="onRemoveItem(item)"><i class="el-icon-close"></i>
                            </div>
                        </div>
                    </div>
                </el-scrollbar>
            </div>
        </div>
        <span slot="footer" class="dialog-footer">
            <el-button @click="close()">取 消</el-button>
            <el-button type="primary" @click="ok()">发 送</el-button>
        </span>
    </el-dialog>
</template>
<script>
import HeadImage from '../common/HeadImage.vue';
import Tabs from '../common/Tabs.vue';
import VirtualScroller from '../common/VirtualScroller.vue';
import FriendItem from '../friend/FriendItem.vue';
import GroupItem from '../group/GroupItem.vue';

export default {
    components: { VirtualScroller, HeadImage, Tabs, FriendItem, GroupItem },
    name: "chatSelector",
    props: {
        title: {
            type: String,
            default: ""
        }
    },
    data() {
        return {
            show: false,
            searchText: "",
            callback: null,
            tabIdx: 0,
            tabItems: ['最近聊天', '好友', '群聊'],
            chats: [],
            friends: [],
            groups: [],
            checkedItems: []
        }
    },
    methods: {
        open(callback) {
            this.tabIdx = 0;
            this.searchText = "";
            this.show = true;
            this.callback = callback;
            this.initData();
        },
        initData() {
            this.checkedItems = []
            this.chats = [];
            this.friends = [];
            this.groups = []
            this.chatStore.chats.forEach(chat => {
                // 过滤掉掉已经删除好友关系或者退群的会话
                if ((chat.type == 'PRIVATE' && this.friendStore.isFriend(chat.targetId))
                    || (chat.type == 'GROUP' && this.groupStore.isGroup(chat.targetId))) {
                    this.chats.push({
                        type: chat.type,
                        targetId: chat.targetId,
                        headImage: chat.headImage,
                        showName: chat.showName,
                        lastContent: chat.lastContent,
                        checked: false
                    })
                }
            })
            this.friendStore.friends.forEach(friend => {
                if (!friend.deleted) {
                    this.friends.push({
                        id: friend.id,
                        headImage: friend.headImage,
                        showNickName: friend.showNickName,
                        checked: false
                    })
                }
            })
            this.groupStore.groups.forEach(group => {
                if (!group.quit) {
                    this.groups.push({
                        id: group.id,
                        headImage: group.headImage,
                        headImageThumb: group.headImageThumb,
                        showGroupName: group.showGroupName,
                        checked: false
                    })
                }
            })
        },
        close() {
            this.show = false;
        },
        ok() {
            this.close();
            this.callback(this.checkedItems);
        },
        onTabChange(idx) {
            this.tabIdx = idx;
            // 更新选中状态
            this.chats.forEach(chat => {
                chat.checked = this.checkedItems.some(chatItem => chat.type == chatItem.type && chat.targetId == chatItem.targetId);
            })
            this.friends.forEach(f => {
                f.checked = this.checkedItems.some(chatItem => chatItem.type == 'PRIVATE' && chatItem.targetId == f.id);
            })
            this.groups.forEach(g => {
                g.checked = this.checkedItems.some(chatItem => chatItem.type == 'GROUP' && chatItem.targetId == g.id);
            })
        },
        onClickChatItem(chatItem) {
            chatItem.checked = !chatItem.checked;
            this.onChatChange(chatItem);
        },
        onChatChange(chatItem) {
            let idx = this.checkedItems.findIndex(chat => chat.type == chatItem.type && chat.targetId == chatItem.targetId);
            if (idx >= 0) {
                this.checkedItems.splice(idx, 1)
            } else {
                this.checkedItems.push(chatItem)
            }
        },
        onClickFriendItem(friend) {
            friend.checked = !friend.checked;
            this.onFriendChange(friend);
        },
        onFriendChange(friend) {
            let idx = this.checkedItems.findIndex(chat => chat.type == 'PRIVATE' && chat.targetId == friend.id);
            if (idx >= 0) {
                this.checkedItems.splice(idx, 1)
            } else {
                let chatItem = {
                    targetId: friend.id,
                    type: 'PRIVATE',
                    showName: friend.showNickName,
                    headImage: friend.headImage
                }
                this.checkedItems.push(chatItem)
            }
        },
        onClickGroupItem(group) {
            group.checked = !group.checked;
            this.onGroupChange(group);
        },
        onGroupChange(group) {
            let idx = this.checkedItems.findIndex(chat => chat.type == 'GROUP' && chat.targetId == group.id);
            if (idx >= 0) {
                this.checkedItems.splice(idx, 1)
            } else {
                let chatItem = {
                    targetId: group.id,
                    type: 'GROUP',
                    showName: group.showGroupName,
                    headImage: group.headImageThumb
                }
                this.checkedItems.push(chatItem)
            }
        },
        onRemoveItem(chatItem) {
            let idx = this.checkedItems.findIndex(chat => chat.type == chatItem.type && chat.targetId == chatItem.targetId);
            this.checkedItems.splice(idx, 1);
            // 取消选中状态
            let chat = this.chats.find(chat => chat.type == chatItem.type && chat.targetId == chatItem.targetId);
            if (chat) {
                chat.checked = false;
            }
            if (chatItem.type == 'PRIVATE') {
                let friend = this.friends.find(f => f.id == chat.targetId);
                if (friend) {
                    friend.checked = false;
                }
            }
            if (chatItem.type == 'GROUP') {
                let group = this.groups.find(g => g.id == chat.targetId);
                if (group) {
                    group.checked = false;
                }
            }
        }
    },
    computed: {
        showChats() {
            return this.chats.filter(chat => chat.showName.includes(this.searchText))
        },
        showFriends() {
            return this.friends.filter(f => f.showNickName.includes(this.searchText))
        },
        showGroups() {
            return this.groups.filter(g => g.showGroupName.includes(this.searchText))
        },
    }
}
</script>

<style scoped lang="scss">
.chat-selector {
    display: flex;

    .left-box {
        width: 48%;
        overflow: hidden;
        border: var(--im-border);

        .left-scroll-box {
            height: 400px;

            .check-box {
                margin-right: 10px;
            }

            .chat-item {
                display: flex;
                align-items: center;
                padding: 5px 10px;
                cursor: pointer;

                .chat-info {
                    flex: 1;
                    display: flex;
                    flex-direction: column;
                    padding-left: 10px;
                    text-align: left;
                    overflow: hidden;

                    .chat-name {
                        flex: 1;
                        white-space: nowrap;
                        overflow: hidden;
                        display: flex;
                        align-items: center;

                        .el-tag {
                            min-width: 20px;
                            text-align: center;
                            border-radius: 10px;
                            border: 0;
                            margin-left: 3px;
                        }
                    }

                    .chat-content {
                        white-space: nowrap;
                        overflow: hidden;
                        text-overflow: ellipsis;
                        font-size: var(--im-font-size-small);
                        color: var(--im-text-color-light);
                    }
                }

                &:hover {
                    background-color: var(--im-background-active);
                }
            }
        }
    }

    .arrow {
        display: flex;
        align-items: center;
        font-size: 20px;
        padding: 10px;
        color: var(--im-color-primary);
    }

    .right-box {
        width: 48%;
        border: var(--im-border);

        .tip {
            text-align: left;
            height: 32px;
            line-height: 32px;
            text-indent: 10px;
            color: var(--im-text-color-light)
        }

        .right-scroll-box {
            height: 460px;

            .checked-item {
                display: flex;
                align-items: center;
                padding: 3px 10px;
                white-space: nowrap;
                cursor: pointer;

                .name {
                    margin-left: 10px;
                    flex: 1;
                    text-align: left;
                    overflow: hidden;
                }

                .close {
                    width: 30px;
                    display: none;
                }

                &:hover {
                    background-color: var(--im-background-active);
                }

                &:hover .close {
                    display: block;
                }
            }
        }
    }
}
</style>
