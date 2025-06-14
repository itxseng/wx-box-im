<template>
	<view class="friend-request-item" @click="showRequestInfo()">
		<head-image :name="friend.nickName" :url="friend.headImage" size="small"></head-image>
		<view class="request-info">
			<view class="friend-name">{{ friend.nickName }}</view>
			<view class="info-text">
				<view v-if="request.remark">{{ request.remark }}</view>
				<view v-else-if="isSender">您请求添加对方为好友</view>
				<view v-else>对方请求添加您为好友</view>
			</view>
		</view>
		<view class="btn-group">
			<button type="warn" v-if="isSender" size="mini" @click.stop="onRecall">撤回</button>
			<button type="default" v-else  size="mini">查看</button>
		</view>
	</view>
</template>

<script>
export default {
	name: "frined-item",
	data() {
		return {}
	},
	methods: {
		showRequestInfo() {
			uni.navigateTo({
				url: `/pages/common/user-info?id=${this.friend.id}&requestId=${this.request.id}`
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
	props: {
		request: {
			type: Object
		}
	},
	computed: {
		mine() {
			return this.userStore.userInfo;
		},
		isSender() {
			// 我是否发起方
			return this.request.sendId == this.mine.id;
		},
		friend() {
			if (this.isSender) {
				return {
					id: this.request.recvId,
					nickName: this.request.recvNickName,
					HeadImage: this.request.recvHeadImage,
				}
			} else {
				return {
					id: this.request.sendId,
					nickName: this.request.sendNickName,
					HeadImage: this.request.sendHeadImage,
				}
			}
		}
	}
}
</script>

<style scope lang="scss">
.friend-request-item {
	height: 110rpx;
	display: flex;
	margin-bottom: 1rpx;
	position: relative;
	padding: 10rpx;
	padding-left: 20rpx;
	align-items: center;
	background-color: white;
	white-space: nowrap;

	&:hover {
		background-color: $im-bg;
	}

	.request-info {
		flex: 1;
		display: flex;
		flex-direction: column;
		padding-left: 20rpx;
		text-align: left;

		.friend-name {
			font-size: $im-font-size;
			white-space: nowrap;
			overflow: hidden;
		}
		
		.info-text {
			display: flex;
			font-size: $im-font-size-smaller;
			color: $im-text-color-lighter;
			padding-top: 8rpx;
			align-items: center;  
			white-space: nowrap;
		}
	}
	
	.btn-group {
		margin-right: 30rpx;
	}
}
</style>