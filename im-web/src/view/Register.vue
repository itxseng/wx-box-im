<template>
	<el-container class="register">
		<div class="content">
			<el-form :model="dataForm" status-icon :rules="rules" ref="registerForm" label-width="80px"
				class="web-ruleForm">
				<div class="title">
					<div>注册账号</div>
				</div>
				<el-form-item v-if="dataForm.mode == 'phone'" label="手机号码" prop="phone">
					<el-input v-model="dataForm.phone" autocomplete="off" placeholder="手机号码"></el-input>
				</el-form-item>
				<el-form-item v-show="dataForm.mode == 'phone'" label="验证码" prop="code">
					<el-input v-model="dataForm.code" autocomplete="off" placeholder="验证码">
						<div v-if="phoneLockTime > 0" slot="append">{{ `${phoneLockTime}秒后重新获取` }}</div>
						<el-button v-else slot="append" style="color:#3f3fde"
							@click="onLoadSmsCaptcha">获取验证码</el-button>
					</el-input>
				</el-form-item>
				<el-form-item v-if="dataForm.mode == 'email'" label="邮箱" prop="email">
					<el-input v-model="dataForm.email" autocomplete="off" placeholder="邮箱"></el-input>
				</el-form-item>
				<el-form-item v-show="dataForm.mode == 'email'" label="验证码" prop="code">
					<el-input v-model="dataForm.code" autocomplete="off" placeholder="验证码">
						<div v-if="emailLockTime > 0" slot="append">{{ `${emailLockTime}秒后重新获取` }}</div>
						<el-button v-else slot="append" style="color:#3f3fde"
							@click="onLoadMailCaptcha">获取验证码</el-button>
					</el-input>
				</el-form-item>
				<el-form-item label="用户名" prop="userName">
					<el-input type="userName" v-model="dataForm.userName" autocomplete="off" maxlength="20"
						placeholder="用户名(登录使用)"></el-input>
				</el-form-item>
				<el-form-item label="昵称" prop="nickName">
					<el-input type="nickName" v-model="dataForm.nickName" autocomplete="off" maxlength="20"
						placeholder="昵称"></el-input>
				</el-form-item>
				<el-form-item label="密码" prop="password">
					<el-input type="password" v-model="dataForm.password" autocomplete="off" maxlength="20"
						placeholder="密码"></el-input>
				</el-form-item>
				<el-form-item label="确认密码" prop="confirmPassword">
					<el-input type="password" v-model="dataForm.confirmPassword" autocomplete="off"
						placeholder="确认密码"></el-input>
				</el-form-item>
				<el-form-item>
					<el-button type="primary" @click="submitForm()">注册</el-button>
					<el-button @click="resetForm()">清空</el-button>
					<div class="to-login">
						<router-link to="/login">已有账号? 去登录</router-link>
					</div>
				</el-form-item>
			</el-form>
			<el-divider>其他注册方式</el-divider>
			<div class="other-mode">
				<div v-for="(mode) in config.mode" :key="mode">
					<div class="reg-mode" :class="mode == dataForm.mode ? 'active' : ''" @click="switchMode(mode)">
						<span class="icon iconfont " :class="'icon-' + mode"></span>
						<span class="mode-text">{{ modeNameMap[mode]
						}}</span>
					</div>
				</div>
			</div>
		</div>
		<icp></icp>
		<captcha-image ref="captchaRef"></captcha-image>
	</el-container>
</template>

<script>
import CaptchaImage from '../components/common/CaptchaImage.vue';
import Icp from '../components/common/Icp.vue'
import useConfigStore from '../store/configStore';

export default {
	name: "login",
	components: {
		Icp,
		CaptchaImage
	},
	data() {
		var checkPhone = (rule, value, callback) => {
			if (!value) {
				return callback(new Error('请输入手机号码'));
			}
			const regex = /^1[3-9]\d{9}$/;
			if (!regex.test(value)) {
				return callback(new Error('手机号格式错误'));
			}
			callback();
		};
		var checkEmail = (rule, value, callback) => {
			if (!value) {
				return callback(new Error('请输入邮箱'));
			}
			const regex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
			if (!regex.test(value)) {
				return callback(new Error('邮箱格式错误'));
			}
			callback();
		};
		var checkUserName = (rule, value, callback) => {
			if (!value) {
				return callback(new Error('请输入用户名'));
			}
			callback();
		};
		var checkNickName = (rule, value, callback) => {
			if (!value) {
				return callback(new Error('请输入昵称'));
			}
			callback();
		};
		var checkPassword = (rule, value, callback) => {
			if (value === '') {
				return callback(new Error('请输入密码'));
			}
			callback();
		};
		var checkConfirmPassword = (rule, value, callback) => {
			if (value === '') {
				return callback(new Error('请输入密码'));
			}
			if (value != this.dataForm.password) {
				return callback(new Error('两次密码输入不一致'));
			}
			callback();
		};
		return {
			dataForm: {
				phone: '',
				email: '',
				code: '',
				mode: 'username',
				userName: '',
				nickName: '',
				password: '',
				confirmPassword: ''
			},
			rules: {
				phone: [{
					validator: checkPhone,
					trigger: 'blur'
				}],
				email: [{
					validator: checkEmail,
					trigger: 'blur'
				}],
				userName: [{
					validator: checkUserName,
					trigger: 'blur'
				}],
				nickName: [{
					validator: checkNickName,
					trigger: 'blur'
				}],
				password: [{
					validator: checkPassword,
					trigger: 'blur'
				}],
				confirmPassword: [{
					validator: checkConfirmPassword,
					trigger: 'blur'
				}]
			},
			showCaptchaImage: false,
			phoneLockTime: 0,
			phoneLockTimer: null,
			emailLockTime: 0,
			emailLockTimer: null,
			modeNameMap: {
				username: "用户名注册",
				phone: "手机号注册",
				email: "邮箱注册"
			}
		};
	},
	methods: {
		submitForm() {
			this.$refs.registerForm.validate((valid) => {
				if (valid) {
					this.$http({
						url: "/register",
						method: 'post',
						data: this.dataForm
					}).then(() => {
						this.$message.success("注册成功!");
					})
				}
			});
		},
		onLoadSmsCaptcha() {
			this.$refs.registerForm.validateField('phone', (valid) => {
				if (valid == '') {
					// 发短信前先验证验证码，防止盗刷
					this.$refs.captchaRef.open((id, code) => {
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
							this.$message.success("验证码已发送至您的手机，请注意查收")
						})
					});
				}
			})
		},
		onLoadMailCaptcha() {
			this.$refs.registerForm.validateField('email', (valid) => {
				if (valid == '') {
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
						email: this.dataForm.email,
					}
					this.$http({
						url: "/captcha/mail/code",
						method: 'post',
						data: data
					}).then(() => {
						this.$message.success("验证码已发送至您的邮箱，请注意查收")
					})

				}
			})
		},
		switchMode(mode) {
			this.dataForm.mode = mode;
			this.resetForm();
		},
		resetForm() {
			this.$refs.registerForm.resetFields();
		}
	},
	computed: {
		config() {
			return useConfigStore().registration;
		}
	},
	watch: {
		config: {
			handler() {
				this.switchMode(this.config.mode[0]);
			}
		}
	},
	mounted() {
		useConfigStore().loadConfig().then(() => {
			if (this.config.mode) {
				this.switchMode(this.config.mode[0]);
			}
		})
	}
}
</script>

<style scoped lang="scss">
.register {
	display: flex;
	justify-content: space-around;
	align-items: center;
	width: 100%;
	height: 100%;
	background: #E8F2FF;

	.content {
		width: 500px;
		padding: 20px;
		background: white;
		opacity: 0.9;
		box-shadow: 0px 0px 1px #ccc;
		border-radius: 3px;
		overflow: hidden;
		border-radius: 3%;

		.title {
			display: flex;
			justify-content: center;
			align-items: center;
			line-height: 50px;
			margin: 20px 0 30px 0;
			font-size: 22px;
			font-weight: 600;
			letter-spacing: 2px;
			text-transform: uppercase;

			.logo {
				width: 30px;
				height: 30px;
				margin-right: 10px;
			}
		}

		.to-login {
			display: flex;
			flex-direction: row-reverse;
			line-height: 40px;
			text-align: left;
			padding-left: 20px;
		}

		.other-mode {
			display: flex;
			justify-content: center;
			align-items: center;

			.reg-mode {
				display: flex;
				padding: 20px;
				cursor: pointer;

				.icon {
					color: var(--im-text-color);
					font-size: 20px;
					margin-right: 2px;
				}

				.mode-text {
					color: var(--im-text-color);
					font-size: var(--im-font-size);
				}

				&.active {
					.mode-text {
						color: var(--im-color-primary);
					}

					.icon {
						color: var(--im-color-primary);
					}
				}
			}
		}
	}
}
</style>