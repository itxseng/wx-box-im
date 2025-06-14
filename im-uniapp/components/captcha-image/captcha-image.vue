<template>
	<popup-modal ref="modal" :autoClose="false">
		<view class="captcha-image">
			<uni-forms ref="captchaForm" class="form" :modelValue="formData" :rules="rules" validate-trigger="bind">
				<uni-forms-item name="code">
					<image class="img" :src="captchaImage" @click="loadCaptchaImage"></image>
					<uni-easyinput type="text" v-model="formData.code" placeholder="验证码,不区分大小写"  focus/>
				</uni-forms-item>
			</uni-forms>
		</view>
	</popup-modal>
</template>

<script>
export default {
	name: "captcha-image",
	data() {
		return {
			id: '',
			captchaImage: '',
			callback: null,
			formData: {
				code: ''
			},
			rules: {
				code: {
					rules: [{
						required: true,
						errorMessage: '请输入验证码',
					}]
				}
			}
		};
	},
	methods: {
		open(callback) {
			this.callback = callback;
			this.formData.code = '';
			this.loadCaptchaImage();
			this.$refs.modal.open({
				title: '图形验证码',
				confirmText: '确定',
				success: () => {
					this.onOk();
				}
			});
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
			this.$refs.captchaForm.validate().then((res) => {
				this.$http({
					url: `/captcha/img/vertify?id=${this.id}&code=${this.formData.code}`,
					method: 'get'
				}).then((isPass) => {
					if (!isPass) {
						uni.showToast({
							title: "验证码错误",
							icon: "none"
						})
					} else {
						this.callback && this.callback(this.id, this.formData.code);
						this.$refs.modal.close();
					}
				})
			})
		}
	}
}
</script>

<style scoped lang="scss">
.captcha-image {
	width: 400rpx;
	
	
	.uni-forms-item {
		margin-bottom: 0 ;
	}
	
	.img {
		width: 200rpx;
		height: 100rpx;
	}
	

}
</style>