<template>
	<view class="page user-info">
		<nav-bar title="用户信息" back></nav-bar>
		<!-- 用户基本信息 -->
		<user-info-card :userInfo="userInfo"></user-info-card>
		<!-- 验证信息 -->
		<friend-request-info v-if="request" :request="request"></friend-request-info>
		<view v-else>
			<bar-group v-if="chatIdx>=0">
				<arrow-bar title="查找聊天记录" @tap="onChatHistory()"></arrow-bar>
			</bar-group>
			<bar-group v-if="isFriend">
				<arrow-bar title="设置备注" @tap="onRemark()"></arrow-bar>
				<arrow-bar title="推荐该联系人" @tap="onSendCard()"></arrow-bar>
			</bar-group>
			<bar-group v-if="chatIdx>=0">
				<arrow-bar title="清空聊天记录" @tap="onCleanMessage()"></arrow-bar>
			</bar-group>
			<bar-group>
				<arrow-bar title="投诉" @tap="onComplaint()"></arrow-bar>
				<switch-bar title="加入黑名单" :checked="userInfo.isInBlacklist" @change="onBlacklistChange"></switch-bar>
			</bar-group>
			<bar-group>
				<btn-bar v-show="isFriend" type="primary" title="发送消息" @tap="onSendMessage()"></btn-bar>
				<btn-bar v-show="isFriend" type="danger" title="删除好友" @tap="onDelFriend()"></btn-bar>
				<btn-bar v-show="!isFriend && !isWaitingApprove" type="primary" title="加为好友"
					@tap="onAddFriend()"></btn-bar>
				<btn-bar v-show="!isFriend && isWaitingApprove" title="等待对方验证"></btn-bar>
			</bar-group>
		</view>
		<popup-modal ref="modal">
			<switch-bar title="清除聊天记录" :checked="isCleanMessage" @change="onCleanMessageChange"></switch-bar>
		</popup-modal>
		<popup-modal ref="modal2"></popup-modal>
		<chat-selector ref="chatSel"></chat-selector>
	</view>
</template>

<script>
export default {
	data() {
		return {
			userInfo: {},
			requestId: null,
			isCleanMessage: true
		}
	},
	methods: {
		onSendMessage() {
			let chat = {
				type: 'PRIVATE',
				targetId: this.userInfo.id,
				showName: this.userInfo.nickName,
				headImage: this.userInfo.headImage,
			};
			this.chatStore.openChat(chat);
			let chatIdx = this.chatStore.findChatIdx(chat);
			uni.navigateTo({
				url: "/pages/chat/chat-box?chatIdx=" + chatIdx
			})
		},
		onAddFriend() {
			uni.navigateTo({
				url: "/pages/friend/friend-apply?userId=" + this.userInfo.id
			})
		},
		onDelFriend() {
			this.$refs.modal.open({
				title: '确认删除',
				content: `确认删除 '${this.userInfo.nickName}'的好友关系吗?`,
				confirmText: '确认',
				success: () => {
					this.$http({
						url: `/friend/delete/${this.userInfo.id}`,
						method: 'delete'
					}).then(() => {
						uni.showToast({
							title: `与 '${this.userInfo.nickName}'的好友关系已解除`,
							icon: 'none'
						})
						this.friendStore.removeFriend(this.userInfo.id);
						if (this.isCleanMessage) {
							this.chatStore.removePrivateChat(this.userInfo.id);
						}
					})
				}
			})
		},
		onComplaint() {
			uni.navigateTo({
				url: "/pages/common/complaint"
			})
		},
		onRemark() {
			uni.navigateTo({
				url: "/pages/friend/friend-remark?id=" + this.userInfo.id
			})
		},
		onSendCard() {
			this.$refs.chatSel.open(chats => {
				let idx = 0;
				chats.forEach(chat => {
					let cardData = {
						userId: this.friendInfo.id,
						nickName: this.friendInfo.nickName,
						headImage: this.friendInfo.headImage,
					}
					let msgInfo = {};
					msgInfo.type = this.$enums.MESSAGE_TYPE.USER_CARD;
					msgInfo.content = JSON.stringify(cardData);
					if (chat.type == 'PRIVATE') {
						msgInfo.recvId = chat.targetId;
					} else {
						msgInfo.groupId = chat.targetId;
					}
					let action = `/message/${chat.type.toLowerCase()}/send`;
					this.$http({
						url: action,
						method: 'POST',
						data: msgInfo
					}).then(m => {
						m.selfSend = true;
						this.chatStore.openChat(chat);
						this.chatStore.insertMessage(m, chat);
						if (++idx == chats.length) {
							uni.showToast({
								title: "发送成功",
								icon: 'none'
							})
						}
					})
				})
			})
		},
		onChatHistory() {
			uni.navigateTo({
				url: '/pages/chat/chat-history?chatIdx=' + this.chatIdx
			})
		},
		onCleanMessage() {
			this.$refs.modal2.open({
				title: '清空聊天记录',
				content: `确认删除与'${this.userInfo.nickName}'的聊天记录吗?`,
				confirmText: '确认',
				success: () => {
					this.chatStore.cleanMessage(this.chatIdx);
					uni.showToast({
						title: `您清空了'${this.userInfo.nickName}'的聊天记录`,
						icon: 'none'
					})
				}
			})
		},
		onBlacklistChange(e) {
			const isChecked = e.detail.value;
			this.userInfo.isInBlacklist = isChecked;
			if (isChecked) {
				uni.showModal({
					title: "加入黑名单",
					content: `您将不再收到对方的消息,确认将 '${this.userInfo.nickName}'加入黑名单吗?`,
					success: (res) => {
						if (res.cancel) {
							// 还原按钮
							this.userInfo.isInBlacklist = !isChecked;
							return;
						}
						this.$http({
							url: "/blacklist/add?userId=" + this.userInfo.id,
							method: "POST"
						}).then(() => {
							uni.showToast({
								title: `已将'${this.userInfo.nickName}'加入黑名单`,
								icon: 'none'
							})
						}).catch(() => {
							this.userInfo.isInBlacklist = !isChecked;
						})
					}
				})
			} else {
				this.$http({
					url: "/blacklist/remove?userId=" + this.userInfo.id,
					method: "DELETE"
				}).catch(() => {
					this.userInfo.isInBlacklist = !isChecked;
				})
			}
		},
		onCleanMessageChange(e) {
			this.isCleanMessage = e.detail.value;
		},
		updateFriendInfo() {
			// store的数据不能直接修改，深拷贝一份store的数据
			let friend = JSON.parse(JSON.stringify(this.friendInfo));
			friend.headImage = this.userInfo.headImageThumb;
			friend.nickName = this.userInfo.nickName;
			friend.showNickName = friend.remarkNickName ? friend.remarkNickName : friend.nickName;
			// 更新好友列表中的昵称和头像
			this.friendStore.updateFriend(friend);
			// 更新会话中的头像和昵称
			this.chatStore.updateChatFromFriend(friend);
		},
		loadUserInfo(id) {
			this.$http({
				url: "/user/find/" + id,
				method: 'GET'
			}).then((user) => {
				this.userInfo = user;
				// 好友的头像、昵称可以能改了，更新一下
				if (this.isFriend) {
					this.updateFriendInfo()
				}
			})
		}
	},
	computed: {
		isFriend() {
			return this.friendStore.isFriend(this.userInfo.id);
		},
		friendInfo() {
			return this.friendStore.findFriend(this.userInfo.id);
		},
		request() {
			return this.friendStore.findRequest(this.requestId)
		},
		isWaitingApprove(userId) {
			return this.friendStore.isInRecvRequest(this.userInfo.id);
		},
		chatIdx() {
			let chat = this.chatStore.findChatByFriend(this.userInfo.id);
			if (chat) {
				return this.chatStore.findChatIdx(chat);
			}
			return -1;
		}
	},
	onLoad(options) {
		// 好友验证请求信息
		this.requestId = options.requestId;
		// 查询用户信息
		this.loadUserInfo(options.id);
	}
}
</script>

<style lang="scss" scoped>
</style>