<template>
    <el-dialog class="friend-apply" title="申请添加好友" width="400px" :visible.sync="isShow" :before-close="onClose"
        append-to-body>
        <div>
            <el-input type="textarea" v-model="remark" maxlength="255" rows="5">
            </el-input>
            <div class="tip">提示: 对方开启了好友验证,等待对方同意后才能成为好友</div>
        </div>
        <span slot="footer" class="dialog-footer">
            <el-button @click="onClose()">取 消</el-button>
            <el-button type="primary" @click="onOk()">确 定</el-button>
        </span>
    </el-dialog>
</template>

<script>
export default {
    name: "friendApply",
    components: {},
    data() {
        return {
            isShow: false,
            remark: "",
            userInfo: {}
        }
    },
    methods: {
        open(userInfo) {
            this.isShow = true;
            this.userInfo = userInfo;
            this.remark = "我是" + this.userStore.userInfo.nickName;
            if (!this.userInfo.isManualApprove) {
                // 对方未开启好友验证,直接发起
                this.sendApplyRequest();
                this.isShow = false;
            }
        },
        onClose() {
            this.isShow = false;
        },
        onOk() {
            this.sendApplyRequest();
        },
        sendApplyRequest() {
            let formData = {
                friendId: this.userInfo.id,
                remark: this.remark
            }
            this.$http({
                url: "/friend/request/apply",
                method: "post",
                data: formData
            }).then((request) => {
                if (request.status == this.$enums.REQUEST_STATUS.APPROVED) {
                    this.$message.success(`添加成功，'${this.userInfo.nickName}'已成为您的好友`);
                    let friend = {
                        id: this.userInfo.id,
                        nickName: this.userInfo.nickName,
                        showNickName: this.userInfo.nickName,
                        headImage: this.userInfo.headImageThumb,
                        online: this.userInfo.online,
                        deleted: false
                    }
                    this.friendStore.addFriend(friend);
                } else if (request.status == this.$enums.REQUEST_STATUS.PENDING) {
                    this.$message.success(`您向'${this.userInfo.nickName}'发起了好友申请`);
                    this.friendStore.addRequest(request);
                }
                this.isShow = false;
            })
        }
    }

}
</script>


<style lang="scss" scoped>
.friend-apply {
    .tip {
        color: var(--im-text-color-light);
        font-size: var(--im-font-size-small);
        line-height: 35px;
    }
}
</style>