<template>
    <div class="user-config">
        <el-switch v-model="userInfo.isManualApprove" inactive-text="加我为好友时需要验证" @change="onManualApproveChange"></el-switch>
    </div>
</template>
<script>
export default {
    name: "userConfig",
    data() {
        return {
            userInfo: {
                isManualApprove: false
            }
        }
    },
    methods: {
        init() {
            let mine = this.userStore.userInfo;
            this.userInfo = JSON.parse(JSON.stringify(mine));
        },
        onManualApproveChange() {
            this.$http({
                url: "/user/manualApprove?enabled=" + this.userInfo.isManualApprove,
                method: 'put'
            }).then(() => {
                this.usetStore.loadUser();
                this.$message.success(`您${this.userInfo.isManualApprove ? '开启' : '关闭'}了好友审核验证`);
            })
        }
    }
}
</script>
<style scoped lang="scss">
.user-config {
    padding: 20px;
}
</style>
