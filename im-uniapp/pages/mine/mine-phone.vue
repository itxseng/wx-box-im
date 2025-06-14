<template>
	<view class="page mine-phone">
		<nav-bar title="绑定手机" back></nav-bar>
		<uni-card :is-shadow="false" is-full :border="false">
			<uni-forms class="form" ref="form" :modelValue="dataForm" :rules="rules" validate-trigger="bind"
				label-position="top" label-width="100%">
				<uni-forms-item label="手机号码" name="phone">
					<uni-easyinput v-model="dataForm.phone" autocomplete="off">
					</uni-easyinput>
				</uni-forms-item>
				<uni-forms-item label="验证码" name="code">
					<uni-easyinput v-model="dataForm.code">
						<template #right>
							<text class="lock-text" v-if="lockTime > 0">{{ `${lockTime}秒后重新获取` }}</text>
							<text class="captcha-btn" v-else @click="onLoadCaptcha">获取验证码</text>
						</template>
					</uni-easyinput>
				</uni-forms-item>
				<button class="btn-submit" @click="submit" type="primary">绑定</button>
			</uni-forms>
		</uni-card>
		<captcha-image ref="captcha"></captcha-image>
		<popup-modal ref="modal"></popup-modal>
	</view>
</template>

<script>
export default {
	data() {
		return {
			dataForm: {
				phone: '',
				code: ''
			},
			rules: {
				phone: {
					rules: [{
						required: true,
						errorMessage: '请输入手机',
					}, {
						validateFunction: (rule, value, data, callback) => {
							const regex = /^1[3-9]\d{9}$/;
							if (!regex.test(value)) {
								return callback('手机格式错误');
							}
							return true;
						}
					}],
				},
				code: {
					rules: [{
						required: true,
						errorMessage: '请输入验证码',
					}]
				},
			},
			lockTime: 0,
			lockTimer: null
		}
	},
	methods: {
		submit() {
			this.$refs.form.validate().then(() => {
				this.$refs.modal.open({
					title: '确认绑定？',
					confirmText: '确认',
					content: '手机号码绑定后将无法更改，是否绑定?',
					success: () => {
						this.$http({
							url: '/user/bindPhone',
							method: 'PUT',
							data: this.dataForm
						}).then(() => {
							this.userStore.loadUser();
							uni.showToast({
								title: "手机号码绑定成功",
								icon: 'none'
							})
							setTimeout(() => uni.navigateBack(), 1500)
						})
					}
				});
			})
		},
		onLoadCaptcha() {
			this.$refs.form.validateField('phone', (valid) => {
				// 通过校验时，v值为null，所以要取反
				if (!valid) {
					// 发短信前先验证验证码，防止盗刷
					this.$refs.captcha.open((id, code) => {
						this.resetTimer();
						// 发送短信
						let data = {
							phone: this.dataForm.phone,
							id: id,
							code: code
						}
						this.$http({
							url: "/captcha/sms/code",
							method: 'post',
							data: data
						}).then(() => {
							uni.showToast({
								title: "验证码已发送至您的手机，请注意查收",
								icon: 'none'
							})
						})
					});
				}
			})
		},
		resetTimer() {
			// 60s内不允许再次发送
			this.lockTime = 60;
			this.lockTimer && clearInterval(this.lockTimer);
			this.lockTimer = setInterval(() => {
				this.lockTime -= 1;
				if (this.lockTime <= 0) {
					this.lockTimer && clearInterval(this.lockTimer);
				}
			}, 1000)
		}
	}

}
</script>

<style lang="scss">
.mine-phone {
	.uni-forms {
		padding: 10rpx;

		.lock-text {
			margin-right: 10px;
		}

		.captcha-btn {
			color: $im-color-primary;
			margin-right: 10px;
		}

		.btn-submit {
			margin: 40rpx 0;
			border-radius: 50rpx;
		}
	}
}
</style>