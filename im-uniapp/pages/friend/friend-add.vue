<template>
	<view class="page friend-add">
		<nav-bar title="添加好友" back></nav-bar>
		<view class="nav-bar">
			<view class="nav-search">
				<uni-search-bar v-model="searchText" radius="100" :focus="true" @confirm="onSearch()"
					@cancel="onCancel()" @input="onSearchTextChange" placeholder="用户名/昵称/手机号/邮箱"></uni-search-bar>
			</view>
		</view>
		<view class="user-items">
			<scroll-view v-if="!isNoData" class="scroll-bar" scroll-with-animation="true" scroll-y="true">
				<view v-for="(user) in users" :key="user.id" v-show="user.id != userStore.userInfo.id">
					<view class="user-item">
						<head-image :id="user.id" :name="user.nickName" :online="user.online"
							:url="user.headImage"></head-image>
						<view class="user-info">
							<view class="nick-name">
								<view>{{ user.nickName }}</view>
								<uni-tag v-if="user.status == 1" circle type="error" text="已注销" size="small"
									:inverted="true"></uni-tag>
								<uni-tag v-if="user.isBanned" circle type="error" text="已封禁" size="small"
									:inverted="true"></uni-tag>
							</view>
							<view v-if="searchText == user.phone" class="line-info">手机:{{user.phone}}</view>
							<view v-else-if="searchText == user.email" class="line-info">邮箱:{{user.email}}</view>
							<view v-else class="line-info">用户名:{{ user.userName}}</view>
						</view>
						<view>
							<text class="status-tip" v-if="isFriend(user.id)">已添加</text>
							<text class="status-tip" v-else-if="isWaitingApprove(user.id)">等待对方验证</text>
							<button type="primary" v-else size="mini" @click.stop="onAddFriend(user)">加为好友</button>
						</view>
					</view>
				</view>
			</scroll-view>
			<no-data-tip v-else class="no-data-tip" :tip="`未搜索到与'${searchText}'相关的用户`"></no-data-tip>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			searchText: "",
			isNoData: false,
			users: []
		}
	},
	methods: {
		onCancel() {
			uni.navigateBack();
		},
		onSearch() {
			this.$http({
				url: "/user/search?name=" + this.searchText,
				method: "GET"
			}).then((data) => {
				this.users = data;
				this.isNoData = this.users.length == 0;
			})
		},
		onSearchTextChange() {
			console.log("onSearchTextChange")
			this.isNoData = false;
		},
		onAddFriend(user) {
			uni.navigateTo({
				url: "/pages/friend/friend-apply?userId=" + user.id
			})
		},
		onShowUserInfo(user) {
			uni.navigateTo({
				url: "/pages/common/user-info?id=" + user.id
			})
		},
		isFriend(userId) {
			return this.friendStore.isFriend(userId);
		},
		isWaitingApprove(userId) {
			return this.friendStore.isInRecvRequest(userId);
		}
	}

}
</script>

<style scoped lang="scss">
.friend-add {
	position: relative;
	display: flex;
	flex-direction: column;

	.user-items {
		position: relative;
		flex: 1;
		overflow: hidden;

		.user-item {
			height: 100rpx;
			display: flex;
			margin-bottom: 1rpx;
			position: relative;
			padding: 18rpx 20rpx;
			align-items: center;
			background-color: white;
			white-space: nowrap;

			.user-info {
				flex: 1;
				display: flex;
				flex-direction: column;
				padding-left: 20rpx;
				font-size: $im-font-size;
				white-space: nowrap;
				overflow: hidden;

				.nick-name {
					display: flex;
					flex: 1;
					font-size: $im-font-size-large;
					white-space: nowrap;
					overflow: hidden;
					align-items: center;

					.uni-tag {
						text-align: center;
						margin-left: 5rpx;
						padding: 1px 5px;
					}
				}

				.line-info {
					display: flex;
					font-size: $im-font-size-smaller;
					color: $im-text-color-lighter;
					padding-top: 8rpx;
				}
			}

			.status-tip {
				font-size: $im-font-size-small;
				color: $im-text-color-lighter;
			}
		}

		.scroll-bar {
			height: 100%;
		}

		.no-data-tip {
			height: 100%;
		}
	}
}
</style>