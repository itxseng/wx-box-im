<template>
    <div class="chat-group-member">
        <div class="header">
            <span class="back el-icon-back" @click="onBack()" title="返回"></span>
            <span class="title">群聊成员({{ groupMembers.length }})</span>
        </div>
        <div v-show="!group.quit" class="group-side-search">
            <el-input placeholder="搜索群成员" v-model="searchText" size="small">
                <i class="el-icon-search el-input__icon" slot="prefix"> </i>
            </el-input>
        </div>
        <virtual-scroller class="scroll-box" :items="showMembers">
            <template v-slot="{ item }">
                <group-member-item :member="item" :group="group" :groupMembers="groupMembers"></group-member-item>
            </template>
        </virtual-scroller>
    </div>
</template>
<script>
import VirtualScroller from '../common/VirtualScroller.vue';
import GroupMemberItem from '../group/GroupMemberItem.vue';

export default {
    name: "chatGroupMember",
    components: { VirtualScroller, GroupMemberItem },
    props: {
        group: {
            type: Object
        },
        groupMembers: {
            type: Array
        }
    },
    data() {
        return {
            searchText: ""
        }
    },
    methods: {
        onBack() {
            this.$emit('back')
        },
    },
    computed: {
        showMembers(){
            return  this.groupMembers.filter(m => m.showNickName.includes(this.searchText))
        }
    }
}
</script>
<style lang="scss" scoped>
.chat-group-member {
    display: flex;
    flex-direction: column;
    height: 100%;
    .header {

        .back {
            position: absolute;
            left: 10px;
            line-height: 40px;
            font-size: 20px;
            cursor: pointer;
        }

        .title {
            text-align: center;
            line-height: 40px;
            font-size: var(--im-font-size-large);

        }
    }

    .scroll-box {
        flex: 1;
    }
}
</style>
