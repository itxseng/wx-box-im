<template>
	<view class="page group-info">
		<nav-bar title="群聊信息" back></nav-bar>
		<view class="group-members">
			<view class="member-items">
				<view v-for="(member, idx) in groupMembers" :key="idx">
					<view class="member-item" v-if="idx < showMaxIdx">
						<head-image :id="member.userId" :name="member.showNickName" :url="member.headImage" size="small"
							:online="member.online"></head-image>
						<view class="member-name">
							<text>{{ member.showNickName }}</text>
						</view>
					</view>
				</view>
				<view v-if="!group.quit" class="member-item" @click="onInviteMember()">
					<view class="tools-btn">
						<uni-icons class="icon" type="plusempty" color="#888888"></uni-icons>
					</view>
					<view class="member-name">邀请</view>
				</view>
				<view v-if="isOwner||isManager" class="member-item" @click="onRemoveMember()">
					<view class="tools-btn">
						<text class="icon iconfont icon-remove"></text>
					</view>
					<view class="member-name">移除</view>
				</view>
				<view v-if="isOwner||isManager" class="member-item" @click="onMuted()">
					<view class="tools-btn">
						<text class="icon iconfont icon-chat-muted"></text>
					</view>
					<view class="member-name">禁言</view>
				</view>
				<view v-if="isOwner||isManager" class="member-item" @click="onUnmuted()">
					<view class="tools-btn">
						<text class="icon iconfont icon-chat-unmuted"></text>
					</view>
					<view class="member-name">取消禁言</view>
				</view>
			</view>
			<view class="member-more" @click="onShowMoreMmeber()">{{ `查看全部群成员${groupMembers.length}人` }}></view>
		</view>
		<view class="form">
			<view class="form-item">
				<view class="label">群聊名称</view>
				<view class="value">{{group.name}}</view>
			</view>
			<arrow-bar v-if="!group.quit" title="群二维码" @tap="onShowQrcode()">
				<text class="icon iconfont icon-qrcode"></text>
			</arrow-bar>
			<view class="form-item">
				<view class="label">群主</view>
				<view class="value">{{ownerName}}</view>
			</view>
			<view class="form-item" v-if="!group.quit && group.remarkGroupName">
				<view class="label">群名备注</view>
				<view class="value">{{group.remarkGroupName}}</view>
			</view>
			<view class="form-item" v-if="!group.quit">
				<view class="label">我在本群的昵称</view>
				<view class="value">{{group.showNickName}}</view>
			</view>
			<view v-if="group.notice" class="form-item">
				<view class="label">群公告</view>
			</view>
			<view v-if="group.notice" class="notice-text">
				{{group.notice}}
			</view>
			<view v-if="!group.quit" class="group-edit" @click="onEditGroup()">修改群聊资料 > </view>
		</view>
		<bar-group v-if="!group.quit && chatIdx>=0">
			<arrow-bar title="查找聊天记录" @tap="onChatHistory()"></arrow-bar>
		</bar-group>
		<bar-group v-if="!group.quit">
			<arrow-bar title="分享该群聊" @tap="onSendCard()"></arrow-bar>
		</bar-group>
		<bar-group v-if="isOwner||isManager">
			<switch-bar title="全员禁言" :checked="group.isMuted" @change="onGroupMutedChange"></switch-bar>
			<arrow-bar v-if="isOwner" title="群管理员" @tap="onSetManager()"></arrow-bar>
		</bar-group>
		<bar-group v-if="!group.quit && chatIdx>=0">
			<arrow-bar title="清空聊天记录" @tap="onCleanMessage()"></arrow-bar>
		</bar-group>
		<bar-group v-if="!group.quit">
			<btn-bar type="primary" title="发送消息" @tap="onSendMessage()"></btn-bar>
			<btn-bar v-if="!isOwner" type="danger" title="退出群聊" @tap="onQuitGroup()"></btn-bar>
			<btn-bar v-if="isOwner" type="danger" title="解散群聊" @tap="onDissolveGroup()"></btn-bar>
		</bar-group>
		<bar-group v-else>
			<btn-bar type="primary" title="加入群聊" @tap="onJoin()"></btn-bar>
		</bar-group>
		<group-member-selector ref="removeSelector" :members="groupMembers" :group="group"
			@complete="onRemoveComplete"></group-member-selector>
		<group-member-selector ref="mutedSelector" :members="groupMembers" :group="group"
			@complete="onMutedComplete"></group-member-selector>
		<group-member-selector ref="unmutedSelector" :members="groupMembers" :group="group"
			@complete="onUnmutedComplete"></group-member-selector>
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
			groupId: null,
			group: {},
			groupMembers: [],
			isCleanMessage: true
		}
	},
	methods: {
		onInviteMember() {
			uni.navigateTo({
				url: `/pages/group/group-invite?id=${this.groupId}`
			})
		},
		onShowMoreMmeber() {
			uni.navigateTo({
				url: `/pages/group/group-member?id=${this.groupId}`
			})
		},
		onEditGroup() {
			uni.navigateTo({
				url: `/pages/group/group-edit?id=${this.groupId}&isManager=${this.isManager}`
			})
		},
		onShowQrcode() {
			uni.navigateTo({
				url: '/pages/group/group-qrcode?id=' + this.groupId
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
				content: `确认删除群聊'${this.group.name}'的聊天记录吗?`,
				confirmText: '确认',
				success: () => {
					this.chatStore.cleanMessage(this.chatIdx);
					uni.showToast({
						title: `您清空了'${this.group.name}'的聊天记录`,
						icon: 'none'
					})
				}
			})
		},
		onSetManager() {
			uni.navigateTo({
				url: `/pages/group/group-manager?id=${this.groupId}`
			})
		},
		onSendMessage() {
			let chat = {
				type: 'GROUP',
				targetId: this.group.id,
				showName: this.group.showGroupName,
				headImage: this.group.headImage,
			};
			this.chatStore.openChat(chat);
			let chatIdx = this.chatStore.findChatIdx(chat);
			uni.navigateTo({
				url: "/pages/chat/chat-box?chatIdx=" + chatIdx
			})
		},
		onJoin() {
			this.$http({
				url: "/group/join/" + this.group.id,
				method: 'POST'
			}).then(() => {
				this.loadGroupInfo();
				this.loadGroupMembers();
				uni.showToast({
					title: `您加入了群聊'${this.group.name}'`,
					icon: 'none'
				})
			})
		},
		onRemoveMember() {
			// 群主和自己不显示
			let hideIds = [this.group.ownerId, this.mine.id];
			// 只有群主可以移除管理员
			if (!this.isOwner) {
				hideIds = hideIds.concat(this.managerIds);
			}
			this.$refs.removeSelector.init([], [], hideIds);
			this.$refs.removeSelector.open();
		},
		onRemoveComplete(userIds) {
			let data = {
				groupId: this.group.id,
				userIds: userIds
			}
			this.$http({
				url: "/group/members/remove",
				method: 'DELETE',
				data: data
			}).then(() => {
				this.loadGroupMembers();
				uni.showToast({
					title: `您移除了${userIds.length}位成员`,
					icon: 'none'
				})
			})
		},
		onMuted() {
			// 群主和自己不显示
			let hideIds = [this.group.ownerId, this.mine.id];
			// 只有群主可以禁言管理员
			if (!this.isOwner) {
				hideIds = hideIds.concat(this.managerIds);
			}
			// 不可选中已禁言的用户
			let mutedIds = this.groupMembers.filter(m => m.isMuted).map(m => m.userId);
			this.$refs.mutedSelector.init(mutedIds, mutedIds, hideIds);
			this.$refs.mutedSelector.open();
		},
		onMutedComplete(userIds) {
			// 过滤掉已禁言的用户
			let mutedIds = this.groupMembers.filter(m => m.isMuted).map(m => m.userId);
			userIds = userIds.filter(userId => mutedIds.indexOf(userId) < 0);
			if (userIds.length > 0) {
				let data = {
					groupId: this.group.id,
					userIds: userIds,
					isMuted: true
				}

				let tip = `您对${userIds.length}位成员进行了禁言`;
				this.sendMemberMuted(data, tip);
			}
		},
		onUnmuted() {
			// 过滤掉未禁言的用户
			let hideIds = this.groupMembers.filter(m => !m.isMuted).map(m => m.userId);
			// 只有群主可以解除管理员的禁言
			if (!this.isOwner) {
				hideIds = hideIds.concat(this.managerIds);
			}
			this.$refs.unmutedSelector.init([], [], hideIds);
			this.$refs.unmutedSelector.open();
		},
		onUnmutedComplete(userIds) {
			let data = {
				groupId: this.group.id,
				userIds: userIds,
				isMuted: false
			}
			let tip = `您解除了${userIds.length}位成员的禁言`;
			this.sendMemberMuted(data, tip);
		},
		onGroupMutedChange(e) {
			let data = {
				id: this.group.id,
				isMuted: e.detail.value
			}
			this.$http({
				url: `/group/muted`,
				method: 'PUT',
				data: data
			})
		},
		onQuitGroup() {
			this.$refs.modal.open({
				title: '确认退出',
				content: '退出群聊后将不再接受群里的消息，确认退出吗?',
				confirmText: '退出',
				success: () => {
					this.$http({
						url: `/group/quit/${this.groupId}`,
						method: 'DELETE'
					}).then(() => {
						uni.showToast({
							title: `您退出了群聊'${this.group.name}'`,
							icon: "none"
						})
						setTimeout(() => {
							uni.switchTab({
								url: "/pages/group/group"
							});
							this.groupStore.removeGroup(this.groupId);
							if (this.isCleanMessage) {
								this.chatStore.removeGroupChat(this.groupId);
							}
						}, 1500)
					});
				},
			});
		},
		onDissolveGroup() {
			this.$refs.modal.open({
				title: '确认解散',
				content: `确认要解散群聊'${this.group.name}'吗?`,
				confirmText: '解散',
				success: () => {
					this.$http({
						url: `/group/delete/${this.groupId}`,
						method: 'delete'
					}).then(() => {
						uni.showToast({
							title: `您解散了群聊'${this.group.name}'`,
							icon: "none"
						})
						setTimeout(() => {
							uni.switchTab({
								url: "/pages/group/group"
							});
							this.groupStore.removeGroup(this.groupId);
							if (this.isCleanMessage) {
								this.chatStore.removeGroupChat(this.groupId);
							}
						}, 1500)
					});
				}
			});
		},
		onCleanMessageChange(e) {
			this.isCleanMessage = e.detail.value;
		},
		onSendCard() {
			this.$refs.chatSel.open(chats => {
				let idx = 0;
				chats.forEach(chat => {
					let cardData = {
						groupId: this.group.id,
						groupName: this.group.name,
						headImage: this.group.headImageThumb,
					}
					let msgInfo = {};
					msgInfo.type = this.$enums.MESSAGE_TYPE.GROUP_CARD;
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
		sendMemberMuted(data, tip) {
			this.$http({
				url: "/group/members/muted",
				method: "PUT",
				data: data
			}).then(() => {
				this.loadGroupMembers();
				uni.showToast({
					title: tip,
					icon: 'none'
				})
			})
		},
		loadGroupInfo() {
			this.$http({
				url: `/group/find/${this.groupId}`,
				method: 'GET'
			}).then((group) => {
				this.group = group;
				// 更新聊天页面的群聊信息
				this.chatStore.updateChatFromGroup(group);
				// 更新聊天列表的群聊信息
				this.groupStore.updateGroup(group);
			});
		},
		loadGroupMembers() {
			this.$http({
				url: `/group/members/${this.groupId}`,
				method: "GET"
			}).then((members) => {
				this.groupMembers = members.filter(m => !m.quit);
			})
		}
	},
	computed: {
		mine() {
			return this.userStore.userInfo;
		},
		ownerName() {
			let member = this.groupMembers.find((m) => m.userId == this.group.ownerId);
			return member && member.showNickName;
		},
		isOwner() {
			return this.group.ownerId == this.mine.id;
		},
		isManager() {
			let userId = this.mine.id;
			let m = this.groupMembers.find((m) => m.userId == userId);
			return m && m.isManager;
		},
		managerIds() {
			return this.groupMembers.filter(m => m.isManager).map(m => m.userId)
		},
		showMaxIdx() {
			let idx = 10;
			if (!this.group.quit) {
				// 邀请按钮
				idx--;
			}
			if (this.isOwner || this.isManager) {
				// 开启禁言、解除禁言、移除成员
				idx -= 3;
			}
			return idx;
		},
		chatIdx() {
			let chat = this.chatStore.findChatByGroup(this.groupId);
			if (chat) {
				return this.chatStore.findChatIdx(chat);
			}
			return -1;
		}
	},
	onLoad(options) {
		this.groupId = options.id;
		// 查询群聊信息
		this.loadGroupInfo(options.id);
		// 查询群聊成员
		this.loadGroupMembers(options.id)
	}
}
</script>

<style lang="scss">
.group-info {
	.group-members {
		padding: 30rpx;
		background: white;

		.member-items {
			display: flex;
			flex-wrap: wrap;

			.member-item {
				width: 120rpx;
				display: flex;
				flex-direction: column;
				margin: 8rpx 2rpx;
				position: relative;
				align-items: center;
				padding-right: 5px;
				white-space: nowrap;

				.member-name {
					width: 100%;
					flex: 1;
					overflow: hidden;
					text-align: center;
					white-space: nowrap;
					padding-top: 8rpx;
					font-size: $im-font-size-smaller;
				}

				.tools-btn {
					display: flex;
					justify-content: center;
					align-items: center;
					border: $im-border solid 1rpx;
					border-radius: 10%;
					width: 80rpx;
					height: 80rpx;

					.icon {
						font-size: 40rpx !important;
						color: $im-text-color-lighter !important;
					}
				}
			}
		}

		.member-more {
			padding-top: 24rpx;
			text-align: center;
			font-size: $im-font-size-small;
			color: $im-text-color-lighter;
		}
	}

	.form {
		margin-top: 20rpx;

		.form-item {
			padding: 0 40rpx;
			display: flex;
			background: white;
			align-items: center;
			margin-top: 2rpx;

			.label {
				width: 220rpx;
				line-height: 100rpx;
				font-size: $im-font-size;
				white-space: nowrap;
			}

			.value {
				flex: 1;
				text-align: right;
				line-height: 100rpx;
				color: $im-text-color-lighter;
				font-size: $im-font-size-small;
				white-space: nowrap;
				overflow: hidden;
			}
		}

		.notice-text {
			padding: 0 40rpx;
			background: white;
			text-align: left;
			font-size: $im-font-size;
			color: $im-text-color-light;
			line-height: 50rpx;
		}

		.group-edit {
			padding: 10rpx 40rpx 30rpx 40rpx;
			text-align: center;
			background: white;
			font-size: $im-font-size-small;
			color: $im-text-color-lighter;
		}
	}

	.icon-qrcode {
		font-size: 36rpx;
		color: $im-text-color-lighter;
	}
}
</style>