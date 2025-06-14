<template>
	<view class="chat-top-message">
		<text class="icon iconfont icon-top-message"></text>
		<rich-text class="content" :nodes="content" @click.stop="onClickMessage"></rich-text>
		<view class="close" @click.stop="onClose">
			<uni-icons type="closeempty"></uni-icons>
		</view>
		<popup-modal ref="modal"></popup-modal>
	</view>
</template>
<script>
export default {
	name: "chat-top-message",
	props: {
		group: {
			type: Object,
		},
		groupMembers: {
			type: Array
		},
		msgInfo: {
			type: Object,
			required: true
		},
		headImage: {
			type: String,
			default: ''
		},
		showName: {
			type: String,
			required: true
		}
	},
	data() {
		return {}
	},
	methods: {
		onClose() {
			if (this.isOwner || this.isManager) {
				this.removeTopMessage();
			} else {
				this.hideTopMessage();
			}
		},
		onClickMessage() {
			// 定位消息
			this.$emit("locate", this.msgInfo);
		},
		removeTopMessage() {
			this.$refs.modal.open({
				title: '移除置顶',
				content: '将在当前群聊的所有成员中移除此置顶消息,确认移除?',
				confirmText: '移除',
				success: () => {
					this.$http({
						url: "/group/removeTopMessage/" + this.group.id,
						method: 'delete'
					})
				}
			});
		},
		hideTopMessage() {
			this.$http({
				url: "/group/hideTopMessage/" + this.group.id,
				method: 'delete'
			})
		}
	},
	computed: {
		content() {
			let content = "不支持的消息类型"
			if (this.msgInfo.type == this.$enums.MESSAGE_TYPE.IMAGE) {
				content = "[图片]";
			} else if (this.msgInfo.type == this.$enums.MESSAGE_TYPE.VIDEO) {
				content = "[视频]";
			} else if (this.msgInfo.type == this.$enums.MESSAGE_TYPE.FILE) {
				content = "[文件] " + JSON.parse(this.msgInfo.content).name;
			} else if (this.msgInfo.type == this.$enums.MESSAGE_TYPE.AUDIO) {
				content = "[语音] " + JSON.parse(this.msgInfo.content).duration + '"';
			} else if (this.msgInfo.type == this.$enums.MESSAGE_TYPE.USER_CARD) {
				content = "[个人名片] " + JSON.parse(this.msgInfo.content).nickName;
			} else if (this.msgInfo.type == this.$enums.MESSAGE_TYPE.GROUP_CARD) {
				content = "[群名片] " + JSON.parse(this.msgInfo.content).groupName;
			} else if (this.msgInfo.type == this.$enums.MESSAGE_TYPE.TEXT) {
				content = this.$emo.transform(this.msgInfo.content, 'emoji-normal');
			}
			return content;
		},
		isOwner() {
			return this.group.ownerId == this.userStore.userInfo.id;
		},
		isManager() {
			let userId = this.userStore.userInfo.id;
			let m = this.groupMembers.find((m) => m.userId == userId);
			return m && m.isManager;
		},
	}

}
</script>

<style scoped lang="scss">
.chat-top-message {
	display: flex;
	align-items: center;
	background: #DEEEFD;
	padding: 20rpx;
	border-radius: 20rpx;

	.icon {
		font-size: 40rpx;
		color: $im-color-primary;
	}

	.content {
		flex: 1;
		font-size: $im-font-size;
		width: 600rpx;
		white-space: nowrap;
		overflow: hidden;
		text-overflow: ellipsis;
		margin-left: 10rpx;
		line-height: 50rpx;
		color: $im-text-color-light;
	}


	.close {
		margin-left: 20rpx;
		width: 50rpx;
		text-align: center;
	}

}
</style>