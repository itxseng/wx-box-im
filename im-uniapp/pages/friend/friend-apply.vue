<template>
	<view class="page friend-apply">
		<nav-bar title="申请添加朋友" back></nav-bar>
		<view class="remark-item">
			<view class="remark-label">{{ "申请留言: "}}</view>
			<uni-easyinput type="textarea" v-model="remark"></uni-easyinput>
		</view>
		<bar-group>
			<button class="bottom-btn" type="primary" @click="onApply">发送</button>
		</bar-group>
	</view>
</template>

<script>
export default {
	data() {
		return {
			friendId: null,
			remark: ""
		}
	},
	methods: {
		onApply() {
			let formData = {
				friendId: this.friendId,
				remark: this.remark
			}
			this.$http({
				url: "/friend/request/apply",
				method: "POST",
				data: formData
			}).then((request) => {
				if (request.status == this.$enums.REQUEST_STATUS.APPROVED) {
					uni.showToast({
						title: "添加成功，对方已成为您的好友",
						icon: 'none'
					})
				} else if (request.status == this.$enums.REQUEST_STATUS.PENDING) {
					this.friendStore.addRequest(request)
					uni.showToast({
						title: "对方开启了好友验证,请等待对方通过您的好友申请",
						icon: 'none'
					})
				}
				// 返回
				setTimeout(() => uni.navigateBack(), 1500);
			})
		}

	},
	onLoad(options) {
		// 好友验证请求信息
		this.friendId = options.userId;
		this.remark = "我是" + this.userStore.userInfo.nickName
	}
}
</script>

<style lang="scss">
.friend-apply {

	.remark-item {
		background-color: white;
		margin-top: 3rpx;
		padding: 20rpx 40rpx;

		.remark-label {
			line-height: 100rpx;
			font-size: $im-font-size;
			color: $im-text-color-lighter;
		}

		.remark-text {
			line-height: 60rpx;
			font-size: $im-font-size-small;

		}
	}
}
</style>