<template>
    <el-dialog v-dialogDrag class="captcha-image" title="验证" width="400px" :visible.sync="isShow" 
        :before-close="onClose"    :close-on-click-modal="false">
        <el-form :model="formData" :rules="rules" ref="captchaForm">
            <el-form-item prop="code">
                <img class="img" :src="captchaImage" @click="loadCaptchaImage">
                <el-input v-model="formData.code" placeholder="验证码,不区分大小写"></el-input>
            </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
            <el-button @click="onClose()">取 消</el-button>
            <el-button type="primary" @click="onOk()">确 定</el-button>
        </span>
    </el-dialog>
</template>

<script>
export default {
    name: "captchaImage",
    components: {
    },
    data() {
        return {
            isShow: false,
            id: '',
            captchaImage: '',
            callback: null,
            formData: {
                code: ''
            },
            rules: {
                code: [
                    { required: true, errorMessage: '请输入验证码' }
                ]
            }
        }
    },
    methods: {
        open(callback) {
            this.isShow = true;
            this.callback = callback;
            this.formData.code = ''; 
            this.loadCaptchaImage();
        },
        onClose() {
            this.isShow = false;
        },
        loadCaptchaImage() {
            this.$http({
                url: "/captcha/img/code",
                method: 'post'
            }).then((data) => {
                this.id = data.id;
                this.captchaImage = 'data:image/gif;base64,' + data.image;
            })
        },
        onOk() {
            this.$refs.captchaForm.validate((valid) => {
                if (valid) {
                    this.$http({
                        url: `/captcha/img/vertify?id=${this.id}&code=${this.formData.code}`,
                        method: 'get'
                    }).then((isPass) => {
                        if (!isPass) {
                            this.$message.error("验证码错误")
                        } else {
                            this.callback && this.callback( this.id, this.formData.code);
                            this.onClose();
                        }
                    })
                }
            });
        }
    }
}
</script>


<style lang="scss" scoped>
.captcha-image {
    .img {
        cursor: pointer;
    }
}
</style>