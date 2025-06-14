<template>
	<view class="friend-request-info">
		<bar-group>
			<view class="info-item">
				<view v-if="isSender" class="label">您请求添加对方为好友</view>
				<view v-else class="label">对方请求添加您为好友</view>
				<view class="value"> {{ $date.formatDateTime(request.applyTime)}}</view>
			</view>
			<view class="remark-item" v-if="request.remark">
				<view class="remark-text"> {{isSender ?  '我' : request.sendNickName}}: {{request.remark}}</view>
			</view>
		</bar-group>
		<bar-group>
			<btn-bar v-if="isSender" type="danger" title="撤回" @tap="onRecall()"></btn-bar>
			<btn-bar v-if="!isSender" type="primary" title="同意" @tap="onApprove()"></btn-bar>
			<btn-bar v-if="!isSender" type="danger" title="拒绝" @tap="onReject()"></btn-bar>
		</bar-group>
	</view>
</template>

<script>
export default {
	name: "friend-request-info",
	props: {
		request: {
			type: Object
		}
	},
	data() {
		return {}
	},
	methods: {
		onApprove() {
			this.$http({
				url: "/friend/request/approve?id=" + this.request.id,
				method: 'POST'
			}).then(() => {
				this.friendStore.removeRequest(this.request.id);
				uni.showToast({
					title: `${this.request.sendNickName} 已成为您的好友`,
					icon: 'none'
				})
			})
		},
		onReject() {
			this.$http({
				url: "/friend/request/reject?id=" + this.request.id,
				method: 'POST'
			}).then(() => {
				this.friendStore.removeRequest(this.request.id);
				uni.showToast({
					title: `您拒绝了 ${this.request.sendNickName} 的好友请求`,
					icon: 'none'
				})
			})
		},
		onRecall() {
			this.$http({
				url: "/friend/request/recall?id=" + this.request.id,
				method: 'POST'
			}).then(() => {
				this.friendStore.removeRequest(this.request.id);
				uni.showToast({
					title: `您撤回了 ${this.request.recvNickName} 的添加好友请求`,
					icon: 'none'
				})
			})
		}
	},
	computed: {
		mine() {
			return this.userStore.userInfo;
		},
		isSender() {
			// 我是否发起方
			return this.request.sendId == this.mine.id;
		}
	}
}
</script>

<style lang="scss" scoped>
.friend-request-info {
	.info-item {
		display: flex;
		background-color: white;
		padding: 0rpx 40rpx;

		.label {
			line-height: 100rpx;
			font-size: $im-font-size;
		}

		.value {
			flex: 1;
			text-align: right;
			line-height: 100rpx;
			color: $im-text-color-lighter;
			font-size: $im-font-size-small;
		}
	}

	.remark-item {
		background-color: white;
		padding: 0rpx 40rpx;

		.remark-text {
			flex: 1;
			line-height: 60rpx;
			color: $im-text-color-lighter;
			font-size: $im-font-size;
		}
	}
}
</style>