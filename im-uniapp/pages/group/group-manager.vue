<template>
	<view class="page group-manager">
		<nav-bar title="群管理员" back></nav-bar>
		<view class="member-items">
			<view class="title-bar">群主</view>
			<view class="member-item" @click="onShowUserInfo(ownerMember.userId)">
				<head-image size="small" :name="ownerMember.showNickName" :online="ownerMember.online"
					:url="ownerMember.headImage"></head-image>
				<view class="member-name">{{ ownerMember.showNickName }}</view>
			</view>
			<view class="title-bar">管理员({{managerMembers.length}})</view>
			<view v-for="m in managerMembers">
				<view class="member-item" @click="onShowUserInfo(m.userId)">
					<head-image size="small" :name="m.showNickName" :online="m.online" :url="m.headImage"></head-image>
					<view class="member-name">{{ m.showNickName }}</view>
					<view class="remove" @click.stop="onRemove(m)">移除</view>
				</view>
			</view>
		</view>
		<view class="tools-bar" @click="onAddManager()">
			<view class="tools-btn">
				<uni-icons class="icon" type="plusempty"></uni-icons>
			</view>
			<view class="tools-text">添加管理员</view>
		</view>
		<group-member-selector ref="selector" :members="groupMembers" :group="group"
			@complete="onSelectComplete"></group-member-selector>
		<popup-modal ref="modal"></popup-modal>
	</view>
</template>

<script>
export default {
	data() {
		return {
			group: {},
			groupMembers: []
		}
	},
	methods: {
		loadGroupMembers() {
			this.$http({
				url: `/group/members/${this.group.id}`,
				method: "GET"
			}).then((members) => {
				this.groupMembers = members.filter(m => !m.quit);
			})
		},
		onRemove(m) {
			this.$refs.modal.open({
				title: "移除群管理员",
				content: `确定将'${m.showNickName}'从管理员列表中移除吗`,
				success: () => {
					let data = {
						groupId: this.group.id,
						userIds: [m.userId]
					}
					this.$http({
						url: "/group/manager/remove",
						method: 'DELETE',
						data: data
					}).then(() => {
						m.isManager = false;
						uni.showToast({
							title: `您移除了'${m.showNickName}'的管理员身份`,
							icon: 'none'
						})
					})
				}
			})
		},
		onShowUserInfo(userId) {
			uni.navigateTo({
				url: "/pages/common/user-info?id=" + userId
			})
		},
		onAddManager() {
			// 群主不显示
			let hideIds = [this.group.ownerId]
			let checkedIds = this.groupMembers.filter(m => m.isManager).map(m => m.userId);
			this.$refs.selector.init(checkedIds, checkedIds, hideIds);
			this.$refs.selector.open();
		},
		onSelectComplete(userIds) {
			// 过滤掉已经是管理员的成员
			let managerIds = this.groupMembers.filter(m => m.isManager).map(m => m.userId);
			userIds = userIds.filter(userId => managerIds.indexOf(userId) < 0);
			if (userIds.length > 0) {
				let data = {
					groupId: this.group.id,
					userIds: userIds
				}
				this.$http({
					url: "/group/manager/add",
					method: 'POST',
					data: data
				}).then(() => {
					this.loadGroupMembers();
					uni.showToast({
						title: `您添加了${userIds.length}名管理员`,
						icon: 'none'
					})
				})
			}
		}
	},
	computed: {
		managerMembers() {
			return this.groupMembers.filter(m => m.isManager);
		},
		ownerMember() {
			return this.groupMembers.find(m => m.userId == this.group.ownerId);
		}
	},
	onLoad(options) {
		this.group = this.groupStore.findGroup(options.id)
		// 查询群聊成员
		this.loadGroupMembers()
	}
}
</script>

<style lang="scss" scoped>
.group-manager {

	.explain-area {
		padding: 30rpx;
		background: white;

		.expain-text {
			font-size: $im-font-size;
		}
	}

	.member-items {
		position: relative;
		flex: 1;
		overflow: hidden;

		.title-bar {
			padding: 20rpx 30rpx 5rpx 30rpx;
			font-size: $im-font-size;
			color: $im-text-color-light;
		}

		.member-item {
			height: 120rpx;
			display: flex;
			margin-bottom: 3rpx;
			padding: 0 30rpx;
			position: relative;
			align-items: center;
			background-color: white;
			white-space: nowrap;

			.member-name {
				display: flex;
				align-items: center;
				flex: 1;
				padding-left: 20rpx;
				font-size: $im-font-size;
				line-height: $im-font-size * 2;
				white-space: nowrap;
				overflow: hidden;
			}

			.remove {
				color: darkblue;
				font-size: $im-font-size;
			}
		}
	}

	.tools-bar {
		display: flex;
		align-items: center;
		padding: 0 30rpx;
		height: 120rpx;
		background-color: white;

		.tools-btn {
			display: flex;
			justify-content: center;
			align-items: center;
			width: 86rpx;
			height: 86rpx;

			.icon {
				border: solid 3rpx;
				border-radius: 50%;
				width: 50rpx;
				height: 50rpx;
				line-height: 50rpx;
				font-size: 35rpx !important;
				text-align: center;
			}
		}

		.tools-text {
			flex: 1;
			padding-left: 20rpx;

		}
	}
}
</style>