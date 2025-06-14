<template>
	<uni-card :is-shadow="false" is-full :border="false">
		<view class="user-info-card">
			<head-image :name="userInfo.nickName" :url="userInfo.headImageThumb" :size="160"
				@click="onShowFullImage()"></head-image>
			<view class="info-item">
				<view class="info-primary">
					<text class="info-username">{{ userInfo.nickName }} </text>
					<text v-show="userInfo.sex == 0" class="iconfont icon-man" color="darkblue"></text>
					<text v-show="userInfo.sex == 1" class="iconfont icon-girl" color="darkred"></text>
					<uni-tag v-if="userInfo.status == 1" circle type="error" text="已注销" size="small" :inverted="true">
					</uni-tag>
					<uni-tag v-if="userInfo.isBanned" circle type="error" text="已封禁" size="small" :inverted="true">
					</uni-tag>
				</view>
				<view class="info-text">
					<text class="label-text">用户名:</text>
					<text class="content-text"> {{ userInfo.userName }}</text>
				</view>
				<view class="info-text" v-if="isFriend">
					<text class="label-text">备注:</text>
					<text class="content-text"> {{ friendInfo.showNickName }}</text>
				</view>
				<view class="info-text" v-if="userInfo.signature">
					<view>
						<text class="label-text"> 签名:</text>
						<text class="content-text"> {{ userInfo.signature }}</text>
					</view>
				</view>
			</view>
		</view>
	</uni-card>
</template>

<script>
export default {
	name: "user-info-card",
	props: {
		userInfo: {
			type: Object
		}
	},
	data() {
		return {

		}
	},
	methods: {
		onShowFullImage() {
			let imageUrl = this.userInfo.headImage;
			if (imageUrl) {
				uni.previewImage({
					urls: [imageUrl]
				})
			}
		}
	},
	computed: {
		isFriend() {
			return this.friendStore.isFriend(this.userInfo.id);
		},
		friendInfo() {
			return this.friendStore.findFriend(this.userInfo.id);
		}
	},

}
</script>

<style lang="scss" scoped>
.user-info-card {
	height: 200rpx;
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 20rpx;

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

			.info-username {
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

			.uni-tag {
				margin-left: 5rpx;
			}
		}
	}
}
</style>