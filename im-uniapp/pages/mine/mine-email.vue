<template>
	<view class="page mine-email">
		<nav-bar title="绑定邮箱" back></nav-bar>
		<uni-card :is-shadow="false" is-full :border="false">
			<uni-forms class="form" ref="form" :modelValue="dataForm" :rules="rules" validate-trigger="bind"
				label-position="top" label-width="100%">
				<uni-forms-item label="邮箱" name="email">
					<uni-easyinput v-model="dataForm.email" autocomplete="off">
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
		<popup-modal ref="modal"></popup-modal>
	</view>
</template>

<script>
export default {
	data() {
		return {
			dataForm: {
				email: '',
				code: ''
			},
			rules: {
				email: {
					rules: [{
						required: true,
						errorMessage: '请输入邮箱',
					}, {
						validateFunction: (rule, value, data, callback) => {
							const regex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
							if (!regex.test(value)) {
								return callback('邮箱格式错误');
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
					content: '邮箱绑定后将无法更改，是否绑定?',
					success: () => {
						this.$http({
							url: '/user/bindEmail',
							method: 'PUT',
							data: this.dataForm
						}).then(() => {
							this.userStore.loadUser();
							uni.showToast({
								title: "邮箱绑定成功",
								icon: 'none'
							})
							setTimeout(() => uni.navigateBack(), 1500)
						})
					}
				});
			})
		},
		onLoadCaptcha() {
			this.$refs.form.validateField('email', (valid) => {
				// 通过校验时，valid值为null，所以要取反
				if (!valid) {
					this.resetTimer();
					// 发送短信
					let data = {
						email: this.dataForm.email,
					}
					this.$http({
						url: "/captcha/mail/code",
						method: 'post',
						data: data
					}).then(() => {
						uni.showToast({
							title: "验证码已发送至您的邮箱，请注意查收",
							icon: 'none'
						})
					})
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
.mine-email {
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