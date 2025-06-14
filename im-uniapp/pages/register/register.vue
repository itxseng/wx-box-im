<template>
	<view class="register">
		<view class="title">用户注册</view>
		<view class="form">
			<uni-forms ref="form" :modelValue="dataForm" :rules="rules" validate-trigger="bind">
				<uni-forms-item name="phone" v-if="dataForm.mode == 'phone'">
					<uni-easyinput v-model="dataForm.phone" autocomplete="off" placeholder="手机号码"></uni-easyinput>
				</uni-forms-item>
				<uni-forms-item name="code" v-if="dataForm.mode == 'phone'">
					<uni-easyinput v-model="dataForm.code" placeholder="验证码">
						<template #right>
							<text class="lock-text" v-if="phoneLockTime > 0">{{ `${phoneLockTime}秒后重新获取` }}</text>
							<text class="captcha-btn" v-else @click="onLoadSmsCaptcha">获取验证码</text>
						</template>
					</uni-easyinput>
				</uni-forms-item>
				<uni-forms-item name="email" v-if="dataForm.mode == 'email'">
					<uni-easyinput v-model="dataForm.email" autocomplete="off" placeholder="邮箱">
					</uni-easyinput>
				</uni-forms-item>
				<uni-forms-item name="code" v-if="dataForm.mode == 'email'">
					<uni-easyinput v-model="dataForm.code" placeholder="验证码">
						<template #right>
							<text class="lock-text" v-if="emailLockTime > 0">{{ `${emailLockTime}秒后重新获取` }}</text>
							<text class="captcha-btn" v-else @click="onLoadMailCaptcha">获取验证码</text>
						</template>
					</uni-easyinput>
				</uni-forms-item>
				<uni-forms-item name="userName">
					<uni-easyinput type="text" v-model="dataForm.userName" placeholder="用户名" maxlength="20" />
				</uni-forms-item>
				<uni-forms-item name="nickName">
					<uni-easyinput type="text" v-model="dataForm.nickName" placeholder="昵称" maxlength="20"/>
				</uni-forms-item>
				<uni-forms-item name="password">
					<uni-easyinput type="password" v-model="dataForm.password" placeholder="密码" maxlength="20"/>
				</uni-forms-item>
				<uni-forms-item name="corfirmPassword">
					<uni-easyinput type="password" v-model="dataForm.corfirmPassword" placeholder="确认密码" maxlength="20"/>
				</uni-forms-item>
				<view>
					<checkbox-group @change="onCheckChange">
						<checkbox value="agree" :checked="false">
							<text>我已阅读并同意
								<text class="protocol" @click="onShowProtocol()">《用户协议》</text>
								和
								<text class="protocol" @click="onShowPrivacy()">《隐私政策》</text>
							</text>
						</checkbox>
					</checkbox-group>
				</view>
				<button class="btn-submit" @click="submit" type="warn">注册并登录</button>
			</uni-forms>
		</view>
		<up-divider config class="divider" text="其他注册方式" textColor="#2979ff" lineColor="#2979ffC"
			textSize="14"></up-divider>
		<view class="other-mode">
			<view v-for="(mode) in config.mode" :key="mode">
				<view class="reg-mode" :class="mode == dataForm.mode ? 'active' : ''" @click="switchMode(mode)">
					<text class="icon iconfont " :class="'icon-' + mode"></text>
					<text class="mode-text">
						{{ modeNameMap[mode]}}</text>
				</view>
			</view>
		</view>
		<view class="nav-login">
			<navigator url="/pages/login/login">
				返回登录页面
			</navigator>
		</view>
		<captcha-image ref="captcha"></captcha-image>
	</view>
</template>

<script>
import UNI_APP from '@/.env.js';

export default {
	data() {
		return {
			isAgree: false,
			dataForm: {
				phone: '',
				email: '',
				code: '',
				mode: 'username',
				userName: '',
				nickName: '',
				password: '',
				corfirmPassword: ''
			},
			rules: {
				phone: {
					rules: [{
						required: true,
						errorMessage: '请输入手机号',
					}, {
						validateFunction: (rule, value, data, callback) => {
							const regex = /^1[3-9]\d{9}$/;
							if (!regex.test(value)) {
								return callback('手机号格式错误');
							}
							return true;
						}
					}],
				},
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
				userName: {
					rules: [{
						required: true,
						errorMessage: '请输入用户名',
					}]
				},
				nickName: {
					rules: [{
						required: true,
						errorMessage: '请输入昵称',
					}]
				},
				password: {
					rules: [{
						required: true,
						errorMessage: '请输入密码',
					}]
				},
				corfirmPassword: {
					rules: [{
						required: true,
						errorMessage: '请输入确认密码',
					}, {
						validateFunction: (rule, value, data, callback) => {
							if (data.password != value) {
								callback('两次密码输入不一致')
							}
							return true;
						}
					}]
				}
			},
			phoneLockTime: 0,
			phoneLockTimer: null,
			emailLockTime: 0,
			emailLockTimer: null,
			modeNameMap: {
				username: "用户名注册",
				phone: "手机注册",
				email: "邮箱注册"
			},
			modeColorMap: {
				username: "#123456",
				phone: "#121296",
				email: "#113d56"
			},
		}
	},
	methods: {
		submit() {
			if (!this.isAgree) {
				uni.showToast({
					title: "请勾选同意用户协议",
					icon: "none"
				})
				return;
			}
			this.$refs.form.validate().then(() => {
				this.$http({
					url: '/register',
					data: this.dataForm,
					method: 'POST'
				}).then(() => {
					uni.showToast({
						title: "注册成功,您已成为盒子IM的用户",
						icon: 'none'
					})
					this.login();
				})
			})
		},
		onShowProtocol() {
			const linkUrl = encodeURIComponent(UNI_APP.PROTOCOL_URL);
			uni.navigateTo({
				url: '/pages/common/external-link?url=' + linkUrl
			});
		},
		onShowPrivacy() {
			const linkUrl = encodeURIComponent(UNI_APP.PRIVACY_URL);
			uni.navigateTo({
				url: '/pages/common/external-link?url=' + linkUrl
			});
		},
		onCheckChange(e) {
			this.isAgree = e.detail.value.includes("agree")
		},
		onLoadSmsCaptcha() {
			this.$refs.form.validateField('phone', (valid) => {
				// 通过校验时，v值为null，所以要取反
				if (!valid) {
					// 发短信前先验证验证码，防止盗刷
					this.$refs.captcha.open((id, code) => {
						// 60s内不允许再次发送
						this.phoneLockTime = 60;
						this.phoneLockTimer && clearInterval(this.phoneLockTimer);
						this.phoneLockTimer = setInterval(() => {
							this.phoneLockTime -= 1;
							if (this.phoneLockTime <= 0) {
								this.phoneLockTimer && clearInterval(this.phoneLockTimer);
							}
						}, 1000)
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
		onLoadMailCaptcha() {
			this.$refs.form.validateField('email', (valid) => {
				// 通过校验时，v值为null，所以要取反
				if (!valid) {
					// 60s内不允许再次发送
					this.emailLockTime = 60;
					this.emailLockTimer && clearInterval(this.emailLockTimer);
					this.emailLockTimer = setInterval(() => {
						this.emailLockTime -= 1;
						if (this.emailLockTime <= 0) {
							this.emailLockTimer && clearInterval(this.emailLockTimer);
						}
					}, 1000)
					// 发送短信
					let data = {
						email: this.dataForm.email
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
		login() {
			const loginForm = {
				terminal: this.$enums.TERMINAL_TYPE.APP,
				userName: this.dataForm.userName,
				password: this.dataForm.password
			}
			this.$http({
				url: '/login',
				data: loginForm,
				method: 'POST'
			}).then((loginInfo) => {
				console.log("登录成功,自动跳转到聊天页面...")
				uni.setStorageSync("userName", loginForm.userName);
				uni.setStorageSync("password", loginForm.password);
				uni.setStorageSync("loginInfo", loginInfo);
				// 调用App.vue的初始化方法
				getApp().init()
				// 跳转到聊天页面   
				uni.switchTab({
					url: "/pages/chat/chat"
				})
			})
			// 清理数据，防止缓存未失效
			getApp().$vm.unloadStore();
		},
		switchMode(mode) {
			this.dataForm.mode = mode;
		}
	},
	computed: {
		config() {
			return this.configStore.registration;
		}
	},
	watch: {
		config: {
			handler() {
				this.switchMode(this.config.mode[0]);
			}
		}
	},
	onLoad() {
		this.config.mode && this.switchMode(this.config.mode[0]);
	}
}
</script>

<style lang="scss" scoped>
.register {
	display: flex;
	flex-direction: column;
	height: 100vh;

	.title {
		padding-top: 100rpx;
		padding-bottom: 50rpx;
		color: $im-color-primary;
		text-align: center;
		font-size: 24px;
		font-weight: 600;
	}

	.form {
		padding: 10rpx 50rpx;
		flex: 1;

		.lock-text {
			margin-right: 10px;
		}

		.captcha-btn {
			color: $im-color-primary;
			margin-right: 10px;
		}

		.protocol {
			color: #007BFF;
		}

		.btn-submit {
			margin: 40rpx 0;
			border-radius: 50rpx;
		}
	}

	.divider {
		padding: 0 40rpx;
	}

	.other-mode {
		display: flex;
		justify-content: center;
		align-items: center;

		.reg-mode {
			display: flex;
			flex-direction: column;
			align-items: center;
			padding: 40rpx;
			cursor: pointer;

			.icon {
				font-size: 50rpx;
				color: $im-text-color-light;
			}

			.mode-text {
				color: $im-text-color-light;
				font-size: $im-font-size-smaller;
			}

			&.active {
				.mode-text {
					color: $im-color-primary;
				}

				.icon {
					color: $im-color-primary;
				}
			}
		}
	}

	.nav-login {
		padding-top: 50rpx;
		height: 150rpx;
		color: $im-color-primary;
		text-align: center;
		font-size: $im-font-size-large;
	}
}
</style>