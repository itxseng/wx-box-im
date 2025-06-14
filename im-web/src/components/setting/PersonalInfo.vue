<template>
    <div class="personal-info">
        <el-form :model="userInfo" label-width="80px" :rules="rules" ref="personalForm" size="small">
            <el-form-item label="头像" style="margin-bottom: 0 !important;">
                <file-upload class="avatar-uploader" action="/image/upload" :isPermanent="true" :showLoading="true"
                    :maxSize="maxSize" @success="onUploadSuccess"
                    :fileTypes="['image/jpeg', 'image/png', 'image/jpg', 'image/webp']">
                    <img v-if="userInfo.headImage" :src="userInfo.headImage" class="avatar">
                    <i v-else class="el-icon-plus avatar-uploader-icon"></i>
                </file-upload>
            </el-form-item>
            <el-form-item label="用户名">
                <el-input disabled v-model="userInfo.userName" autocomplete="off" size="small"></el-input>
            </el-form-item>
            <el-form-item prop="nickName" label="昵称">
                <el-input v-model="userInfo.nickName" autocomplete="off" size="small" maxlength="20"></el-input>
            </el-form-item>
            <el-form-item label="性别">
                <el-radio-group v-model="userInfo.sex">
                    <el-radio :label="0">男</el-radio>
                    <el-radio :label="1">女</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item v-if="userInfo.phone" label="手机号">
                <el-input disabled v-model="userInfo.phone" autocomplete="off" size="small"></el-input>
            </el-form-item>
            <el-form-item v-if="userInfo.email" label="邮箱">
                <el-input disabled v-model="userInfo.email" autocomplete="off" size="small"></el-input>
            </el-form-item>
            <el-form-item label="个性签名">
                <el-input type="textarea" v-model="userInfo.signature" :rows="3" maxlength="64"></el-input>
            </el-form-item>
        </el-form>
        <div class="btn-group">
            <el-button type="primary" @click="onSubmit()">提 交</el-button>
        </div>
    </div>
</template>

<script>
import FileUpload from "../common/FileUpload.vue";

export default {
    name: "personalInfo",
    components: {
        FileUpload
    },
    data() {
        return {
            userInfo: {},
            maxSize: 5 * 1024 * 1024,
            action: "/image/upload",
            rules: {
                nickName: [{
                    required: true,
                    message: '请输入昵称',
                    trigger: 'blur'
                }]
            }
        }
    },
    methods: {
        init() {
            let mine = this.userStore.userInfo;
            this.userInfo = JSON.parse(JSON.stringify(mine));
        },
        onSubmit() {
            this.$refs.personalForm.validate((valid) => {
                if (!valid) {
                    return false;
                }
                this.$http({
                    url: "/user/update",
                    method: "put",
                    data: this.userInfo
                }).then(() => {
                    this.usetStore.loadUser();
                    this.$message.success("修改成功");
                })
            });
        },
        onUploadSuccess(data, file) {
            this.userInfo.headImage = data.originUrl;
            this.userInfo.headImageThumb = data.thumbUrl;
        }
    }

}
</script>

<style lang="scss" scoped>
.personal-info {
    padding: 10px 0 0 10px;

    .avatar-uploader {
        --width: 112px;

        .el-upload {
            border: 1px dashed #d9d9d9 !important;
            border-radius: 6px;
            cursor: pointer;
            position: relative;
            overflow: hidden;
        }

        .el-upload:hover {
            border-color: #409EFF;
        }

        .avatar-uploader-icon {
            font-size: 24px;
            color: #8c939d;
            width: var(--width);
            height: var(--width);
            line-height: var(--width);
            text-align: center;
        }

        .avatar {
            width: var(--width);
            height: var(--width);
            display: block;
        }
    }

    .btn-group {
        text-align: center;
    }
}
</style>
