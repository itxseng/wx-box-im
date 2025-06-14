<template>
    <div class="chat-user-card" @click.stop="onShowUserInfo">
        <div class="card-body">
            <head-image :url="cardInfo.headImage" :name="cardInfo.nickName" radius="10%"></head-image>
            <div class="nick-name">
                {{ cardInfo.nickName }}
            </div>
        </div>
        <div class="card-tip">个人名片</div>
    </div>
</template>
<script>
import HeadImage from '../common/HeadImage.vue';

export default {
    name: "chatUserCard",
    components: {
        HeadImage
    },
    props: {
        cardInfo: {
            type: Object
        }
    },
    data() {
        return {}
    },
    methods: {
        onShowUserInfo(e) {
            this.$http({
                url: `/user/find/${this.cardInfo.userId}`,
                method: 'get'
            }).then(user => {
                let pos = {
                    x: e.x + 30,
                    y: e.y
                }
                this.$eventBus.$emit("openUserInfo", user, pos);
            })
        }
    }

}
</script>
<style scoped lang="scss">
.chat-user-card {
    display: flex;
    flex-direction: column;
    flex-wrap: nowrap;
    box-shadow: var(--im-box-shadow-light);
    border-radius: 10px;
    padding: 8px 15px;
    height: 80px;
    width: 180px;
    cursor: pointer;

    .card-body {
        flex: 1;
        display: flex;
        align-items: center;
        border-bottom: 2px solid #eee;

        .nick-name {
            margin-left: 8px;
            font-weight: 600;
            font-size: var(--im-font-size);
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }
    }

    .card-tip {
        margin-top: 4px;
        color: var(--im-text-color-light);
        font-size: var(--im-font-size-smaller);
        text-align: left;
    }
}
</style>
