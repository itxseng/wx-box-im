<template>
	<view class="login">
		<view class="title">欢迎登录</view>
		<view class="form">
			<uni-forms  :modelValue="loginForm" :rules="rules" validate-trigger="bind">
				<uni-forms-item name="userName">
					<uni-easyinput type="text" v-model="loginForm.userName" prefix-icon="person" :placeholder="loginNamePlaceholder" />
				</uni-forms-item>
				<uni-forms-item name="password">
					<uni-easyinput type="password" v-model="loginForm.password" prefix-icon="locked" placeholder="密码" />
				</uni-forms-item>
				<view>
					<checkbox-group @change="onCheckChange">
						<checkbox value="agree" :checked="isAgree">
							<text>我已阅读并同意
								<text class="protocol" @click.stop="onShowProtocol()">《用户协议》</text>
								和
								<text class="protocol" @click.stop="onShowPrivacy()">《隐私政策》</text>
							</text>
						</checkbox>
					</checkbox-group>
				</view>
				<button class="btn-submit" @click="submit" type="primary">登录</button>
			</uni-forms>
		</view>
		<view class="nav-register">
			<navigator  url="/pages/register/register">
				没有账号,前往注册
			</navigator>
		</view>
		<!-- #ifdef APP-PLUS -->
		<policy></policy>
		<!-- #endif -->
	</view>
</template>

<script>
import UNI_APP from '@/.env.js';
export default {
	data() {
		return {
			isAgree: false,
			loginForm: {
				terminal: 1, // APP终端
				userName: '',
				password: ''
			},
			rules: {
				userName: {
					rules: [{
						required: true,
						errorMessage: '请输入用户名',
					}]
				},
				password: {
					rules: [{
						required: true,
						errorMessage: '请输入密码',
					}]
				}
			}
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
			this.$http({
				url: '/login',
				data: this.loginForm,
				method: 'POST'
			}).then(loginInfo => {
				console.log("登录成功,自动跳转到聊天页面...")
				uni.setStorageSync("userName", this.loginForm.userName);
				uni.setStorageSync("password", this.loginForm.password);
				uni.setStorageSync("isAgree", this.isAgree);
				uni.setStorageSync("loginInfo", loginInfo);
				// 调用App.vue的初始化方法
				getApp().$vm.init()
				// 跳转到聊天页面   
				uni.switchTab({
					url: "/pages/chat/chat"
				})
			}).catch((e) => {
				console.log("登录失败：", e);
			})
			// 清理数据，防止缓存未失效
			getApp().$vm.unloadStore();
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
		}
	},
	computed:{
		loginNamePlaceholder(){
			let mode = this.configStore.registration.mode;
			let strText = "用户名";
			if(mode && mode.includes("phone")){
				strText += "/手机号"
			}
			if(mode && mode.includes("email")){
				strText += "/邮箱"
			}
			return strText;
		}
	},
	onLoad() {
		this.loginForm.userName = uni.getStorageSync("userName");
		this.loginForm.password = uni.getStorageSync("password");
		this.isAgree = !!uni.getStorageSync("isAgree");
	}	
}
</script>

<style lang="scss" scoped>
.login {
	display: flex;
	flex-direction: column;
	height: 100vh;
	
	.title {
		padding-top: 250rpx;
		padding-bottom: 50rpx;
		color: $im-color-primary;
		text-align: center;
		font-size: 24px;
		font-weight: bold;
	}

	.form {
		padding: 50rpx;
		flex: 1;
		
		.protocol {
			color: #007BFF;
		}
		
		.btn-submit {
			margin-top: 80rpx;
			border-radius: 50rpx;
		}
	}

	.nav-register {
		height: 200rpx;
		color: $im-color-primary;
		text-align: center;
		font-size: $im-font-size-large;
	}
}
</style>