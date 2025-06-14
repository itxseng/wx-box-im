<template>
	<view class="page mine">
		<nav-bar title="我的"></nav-bar>
		<uni-card :is-shadow="false" is-full :border="false">
			<view class="content" @click="onModifyInfo()">
				<head-image :name="userInfo.nickName" :url="userInfo.headImage" :size="160"></head-image>
				<view class="info-item">
					<view class="info-primary">
						<text class="nick-name">{{ userInfo.nickName }}</text>
						<text v-show="userInfo.sex == 0" class="iconfont icon-man" color="darkblue"></text>
						<text v-show="userInfo.sex == 1" class="iconfont icon-girl" color="darkred"></text>
					</view>
					<view class="info-text">
						<text class="label-text">用户名:</text>
						<text class="content-text"> {{ userInfo.userName }}</text>
					</view>
					<view class="info-text">
						<text class="label-text">签名:</text>
						<text class="content-text"> {{ userInfo.signature }}</text>
					</view>
				</view>
				<view class="btn-wrap">
					<text class="icon iconfont icon-qrcode" @click.stop="onShowQrcode()"></text>
					<uni-icons class="info-arrow" type="right" size="16"></uni-icons>
				</view>
			</view>
		</uni-card>
		<bar-group>
			<arrow-bar title="修改密码" icon="icon-modify-pwd" @tap="onModifyPassword()" icon-color="#5daa31"></arrow-bar>
			<arrow-bar title="注销账号" icon="icon-un-register" @tap="onUnregister()" icon-color="#e43d33"></arrow-bar>
		</bar-group>
		<bar-group>
			<arrow-bar title="用户协议" icon="icon-user-protocol" @tap="onShowProtocol()" icon-color="#ef975d"></arrow-bar>
			<arrow-bar title="隐私政策" icon="icon-privacy-protocol" @tap="onShowPrivacy()"
				icon-color="#8575e8"></arrow-bar>
		</bar-group>
		<bar-group>
			<arrow-bar title="通用设置" icon="icon-setting" @tap="onSetting()" icon-color="#3d13c3"></arrow-bar>
		</bar-group>
		<bar-group>
			<btn-bar title="退出登录" type="danger" @tap="onQuit()"></btn-bar>
		</bar-group>
		<popup-modal ref="modal"></popup-modal>
	</view>
</template>

<script>
import UNI_APP from '@/.env.js';
export default {
	data() {
		return {}
	},
	methods: {
		onModifyInfo() {
			uni.navigateTo({
				url: "/pages/mine/mine-edit"
			})
		},
		onModifyPassword() {
			uni.navigateTo({
				url: "/pages/mine/mine-password"
			})
		},
		onUnregister() {
			this.$refs.modal.open({
				title: '注销账号',
				content: '账号注销后将无法恢复，确认注销吗?',
				success: () => {
					this.$http({
						url: '/unregister',
						method: 'DELETE'
					})
				}
			});
		},
		onShowQrcode() {
			uni.navigateTo({
				url: '/pages/mine/mine-qrcode'
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
		onSetting() {
			uni.navigateTo({
				url: "/pages/mine/mine-setting"
			})
		},
		onQuit() {
			this.$refs.modal.open({
				title: '确认退出?',
				success: () => {
					// 主动退出后不再接收离线通知
					getApp().$vm.removeCid();
					getApp().$vm.exit()
				}
			});
		}
	},
	computed: {
		userInfo() {
			return this.userStore.userInfo;
		}
	},
	onShow() {
		// 每个tab页都要更新一下角标数量
		this.$badge.refreshFriendBadge();
		this.$badge.refreshChatBadge();
	}
}
</script>

<style scoped lang="scss">
.mine {
	.content {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 10rpx;
		padding-right: 30rpx;
		overflow: hidden;

		.info-item {
			display: flex;
			align-items: flex-start;
			flex-direction: column;
			padding-left: 40rpx;
			flex: 1;

			.info-text {
				line-height: 1.5;
			}

			.label-text {
				font-size: $im-font-size-small;
				color: $im-text-color-light;

			}

			.content-text {
				font-size: $im-font-size-small;
				color: $im-text-color-light;
				margin-left: 10rpx;
				word-break: break-all;
				overflow: hidden;
			}

			.info-primary {
				display: flex;
				align-items: center;
				margin-bottom: 10rpx;

				.nick-name {
					font-size: $im-font-size-large;
					font-weight: 600;
				}

				.iconfont {
					font-size: $im-font-size;
					padding-left: 5rpx;
				}

				.icon-man {
					color: darkblue;
				}

				.icon-girl {
					color: darkred;
				}
			}
		}

		.btn-wrap {
			display: flex;
			flex-direction: column;
			width: 50rpx;
			position: relative;
			left: 20rpx;

			.icon-qrcode {
				font-size: 35rpx;
			}

			.info-arrow {
				font-size: 30rpx;
				margin-top: 30rpx;
			}

		}

	}
}
</style>