<template>
	<div class="home-page" @click="closeAllBox">
		<div class="app-container" :class="{ fullscreen: isFullscreen }">
			<div class="navi-bar">
				<div class="navi-bar-box">
					<div class="top">
						<div class="avater">
							<head-image :name="mine.nickName" :size="38" :url="mine.headImageThumb"
								@click.native="showSetting">
							</head-image>
						</div>
						<div class="menu">
							<router-link class="link" v-bind:to="'/home/chat'">
								<div class="menu-item">
									<span class="icon iconfont icon-chat"></span>
									<div v-show="unreadCount > 0" class="unread-text">{{ unreadCount }}</div>
								</div>
							</router-link>
							<router-link class="link" v-bind:to="'/home/friend'">
								<div class="menu-item">
									<span class="icon iconfont icon-friend"></span>
									<div v-show="recvCount > 0" class="unread-text">{{ recvCount }}</div>
								</div>
							</router-link>
							<router-link class="link" v-bind:to="'/home/group'">
								<div class="menu-item">
									<span class="icon iconfont icon-group" style="font-size: 28px"></span>
								</div>
							</router-link>
						</div>
					</div>

					<div class="bottom">
						<div class="bottom-item" @click="isFullscreen = !isFullscreen">
							<i class="el-icon-full-screen"></i>
						</div>
						<div class="bottom-item" @click="showSetting">
							<span class="icon iconfont icon-setting" style="font-size: 20px"></span>
						</div>
						<div class="bottom-item" @click="onExit()" title="退出">
							<span class="icon iconfont icon-exit"></span>
						</div>
					</div>
				</div>
			</div>
			<div class="content-box">
				<router-view></router-view>
			</div>
			<setting ref="setting"></setting>
			<user-info ref="userInfo"></user-info>
			<group-info ref="groupInfo"></group-info>
			<full-image ref="fullImage"></full-image>
			<rtc-private-video ref="rtcPrivateVideo"></rtc-private-video>
			<rtc-group-video ref="rtcGroupVideo"></rtc-group-video>
		</div>
	</div>
</template>

<script>
import HeadImage from '../components/common/HeadImage.vue';
import Setting from '../components/setting/Setting.vue';
import UserInfo from '../components/common/UserInfo.vue';
import GroupInfo from '../components/group/GroupInfo.vue';
import FullImage from '../components/common/FullImage.vue';
import RtcPrivateVideo from '../components/rtc/RtcPrivateVideo.vue';
import RtcPrivateAcceptor from '../components/rtc/RtcPrivateAcceptor.vue';
import RtcGroupVideo from '../components/rtc/RtcGroupVideo.vue';
export default {
	components: {
		HeadImage,
		Setting,
		UserInfo,
		GroupInfo,
		FullImage,
		RtcPrivateVideo,
		RtcPrivateAcceptor,
		RtcGroupVideo
	},
	data() {
		return {
			lastPlayAudioTime: new Date().getTime() - 1000,
			isFullscreen: true,
			reconnecting: false
		}
	},
	methods: {
		init() {
			this.$eventBus.$on('openPrivateVideo', (rctInfo) => {
				// 进入单人视频通话
				this.$refs.rtcPrivateVideo.open(rctInfo);
			});
			this.$eventBus.$on('openGroupVideo', (rctInfo) => {
				// 进入多人视频通话
				this.$refs.rtcGroupVideo.open(rctInfo);
			});
			this.$eventBus.$on('openUserInfo', (user, pos) => {
				// 打开用户卡片
				this.$refs.userInfo.open(user, pos);
			});
			this.$eventBus.$on('openGroupInfo', (group, pos) => {
				// 打开群卡片
				this.$refs.groupInfo.open(group, pos);
			});
			this.$eventBus.$on('openFullImage', url => {
				// 图片大图
				this.$refs.fullImage.open(url);
			});
			this.loadStore().then(() => {
				// ws初始化
				this.$wsApi.connect(process.env.VUE_APP_WS_URL, sessionStorage.getItem("accessToken"));
				this.$wsApi.onConnect(() => {
					if (this.reconnecting) {
						this.onReconnectWs();
					} else {
						// 加载离线消息
						this.pullPrivateOfflineMessage(this.chatStore.privateMsgMaxId);
						this.pullGroupOfflineMessage(this.chatStore.groupMsgMaxId);
						this.pullSystemOfflineMessage(this.chatStore.systemMsgMaxSeqNo);
					}

				});
				this.$wsApi.onMessage((cmd, msgInfo) => {
					if (cmd == 2) {
						// 关闭ws
						this.$wsApi.close(3000)
						// 异地登录，强制下线
						this.$alert("您已在其他地方登录，将被强制下线", "强制下线通知", {
							confirmButtonText: '确定',
							callback: action => {
								location.href = "/";
							}
						});
					} else if (cmd == 3) {
						// 插入私聊消息
						this.handlePrivateMessage(msgInfo);
					} else if (cmd == 4) {
						// 插入群聊消息
						this.handleGroupMessage(msgInfo);
					} else if (cmd == 5) {
						// 处理系统消息
						this.handleSystemMessage(msgInfo);
					}
				});
				this.$wsApi.onClose(e => {
					console.log(e);
					if (e.code != 3000) {
						// 断线重连
						this.reconnectWs();
					}
				});
			}).catch(e => {
				console.log(e)
				this.$message.error("初始化失败");
			})
		},
		reconnectWs() {
			// 记录标志
			this.reconnecting = true;
			// 重新加载一次个人信息，目的是为了保证网络已经正常且token有效
			this.userStore.loadUser().then(() => {
				// 断线重连
				this.$message.error("连接断开，正在尝试重新连接...");
				this.$wsApi.reconnect(process.env.VUE_APP_WS_URL, sessionStorage.getItem(
					"accessToken"));
			}).catch(() => {
				// 10s后重试
				setTimeout(() => this.reconnectWs(), 10000)
			})
		},
		onReconnectWs() {
			// 重连成功
			this.reconnecting = false;
			// 重新加载群和好友
			const promises = [];
			promises.push(this.friendStore.loadFriend());
			promises.push(this.groupStore.loadGroup());
			Promise.all(promises).then(() => {
				// 加载离线消息
				this.pullPrivateOfflineMessage(this.chatStore.privateMsgMaxId);
				this.pullGroupOfflineMessage(this.chatStore.groupMsgMaxId);
				this.pullSystemOfflineMessage(this.chatStore.systemMsgMaxSeqNo);
				this.$message.success("重新连接成功");
			}).catch(() => {
				this.$message.error("初始化失败");
				this.onExit();
			})
		},
		loadStore() {
			return this.userStore.loadUser().then(() => {
				const promises = [];
				promises.push(this.friendStore.loadFriend());
				promises.push(this.groupStore.loadGroup());
				promises.push(this.chatStore.loadChat());
				promises.push(this.configStore.loadConfig());
				return Promise.all(promises);
			})
		},
		unloadStore() {
			this.friendStore.clear();
			this.groupStore.clear();
			this.chatStore.clear();
			this.userStore.clear();
		},
		pullPrivateOfflineMessage(minId) {
			this.chatStore.setLoadingPrivateMsg(true)
			this.$http({
				url: "/message/private/pullOfflineMessage?minId=" + minId,
				method: 'GET'
			}).catch(() => {
				this.chatStore.setLoadingPrivateMsg(false)
			})
		},
		pullGroupOfflineMessage(minId) {
			this.chatStore.setLoadingGroupMsg(true)
			this.$http({
				url: "/message/group/pullOfflineMessage?minId=" + minId,
				method: 'GET'
			}).catch(() => {
				this.chatStore.setLoadingGroupMsg(false)
			})
		},
		pullSystemOfflineMessage(minSeqNo) {
			this.chatStore.setLoadingSystemMsg(true)
			this.$http({
				url: "/message/system/pullOfflineMessage?minSeqNo=" + minSeqNo,
				method: 'GET'
			}).catch(() => {
				this.chatStore.setLoadingSystemMsg(false)
			})
		},
		handlePrivateMessage(msg) {
			// 标记这条消息是不是自己发的
			msg.selfSend = msg.sendId == this.mine.id;
			// 好友id
			let friendId = msg.selfSend ? msg.recvId : msg.sendId;
			// 会话信息
			let chatInfo = {
				type: 'PRIVATE',
				targetId: friendId
			}
			// 消息加载标志
			if (msg.type == this.$enums.MESSAGE_TYPE.LOADING) {
				this.chatStore.setLoadingPrivateMsg(JSON.parse(msg.content))
				return;
			}
			// 消息已读处理，清空已读数量
			if (msg.type == this.$enums.MESSAGE_TYPE.READED) {
				this.chatStore.resetUnreadCount(chatInfo)
				return;
			}
			// 消息回执处理,改消息状态为已读
			if (msg.type == this.$enums.MESSAGE_TYPE.RECEIPT) {
				this.chatStore.readedMessage({
					friendId: msg.sendId
				})
				return;
			}
			// 消息撤回
			if (msg.type == this.$enums.MESSAGE_TYPE.RECALL) {
				this.chatStore.recallMessage(msg, chatInfo)
				return;
			}
			// 好友申请
			if (msg.type == this.$enums.MESSAGE_TYPE.FRIEND_REQ_APPLY) {
				this.friendStore.addRequest(JSON.parse(msg.content))
				return;
			}
			// 好友申请被同意、拒绝、撤回
			if (msg.type == this.$enums.MESSAGE_TYPE.FRIEND_REQ_APPROVE
				|| msg.type == this.$enums.MESSAGE_TYPE.FRIEND_REQ_REJECT
				|| msg.type == this.$enums.MESSAGE_TYPE.FRIEND_REQ_RECALL) {
				this.friendStore.removeRequest(JSON.parse(msg.content).id)
				return;
			}
			// 新增好友
			if (msg.type == this.$enums.MESSAGE_TYPE.FRIEND_NEW) {
				this.friendStore.addFriend(JSON.parse(msg.content));
				return;
			}
			// 删除好友
			if (msg.type == this.$enums.MESSAGE_TYPE.FRIEND_DEL) {
				this.friendStore.removeFriend(friendId);
				return;
			}
			// 好友在线状态
			if (msg.type == this.$enums.MESSAGE_TYPE.FRIEND_ONLINE) {
				this.friendStore.updateOnlineStatus(JSON.parse(msg.content));
				return;
			}
			// 单人webrtc 信令
			if (this.$msgType.isRtcPrivate(msg.type)) {
				this.$refs.rtcPrivateVideo.onRTCMessage(msg)
				return;
			}
			// 消息插入
			if (this.$msgType.isNormal(msg.type) || this.$msgType.isTip(msg.type) || this.$msgType.isAction(msg.type)) {
				let friend = this.loadFriendInfo(friendId);
				this.insertPrivateMessage(friend, msg);
			}
		},
		insertPrivateMessage(friend, msg) {
			let chatInfo = {
				type: 'PRIVATE',
				targetId: friend.id,
				showName: friend.showNickName,
				headImage: friend.headImage
			};
			// 打开会话
			this.chatStore.openChat(chatInfo);
			// 插入消息
			this.chatStore.insertMessage(msg, chatInfo);
			// 播放提示音
			if (!msg.selfSend && this.$msgType.isNormal(msg.type) &&
				msg.status != this.$enums.MESSAGE_STATUS.READED) {
				this.playAudioTip();
			}

		},
		handleGroupMessage(msg) {
			// 标记这条消息是不是自己发的
			msg.selfSend = msg.sendId == this.mine.id;
			let chatInfo = {
				type: 'GROUP',
				targetId: msg.groupId
			}
			// 消息加载标志
			if (msg.type == this.$enums.MESSAGE_TYPE.LOADING) {
				this.chatStore.setLoadingGroupMsg(JSON.parse(msg.content))
				return;
			}

			// 消息已读处理
			if (msg.type == this.$enums.MESSAGE_TYPE.READED) {
				// 我已读对方的消息，清空已读数量
				this.chatStore.resetUnreadCount(chatInfo)
				return;
			}
			// 消息回执处理
			if (msg.type == this.$enums.MESSAGE_TYPE.RECEIPT) {
				// 更新消息已读人数
				let msgInfo = {
					id: msg.id,
					groupId: msg.groupId,
					readedCount: msg.readedCount,
					receiptOk: msg.receiptOk
				};
				this.chatStore.updateMessage(msgInfo, chatInfo)
				return;
			}
			// 消息撤回
			if (msg.type == this.$enums.MESSAGE_TYPE.RECALL) {
				this.chatStore.recallMessage(msg, chatInfo)
				return;
			}
			// 新增群
			if (msg.type == this.$enums.MESSAGE_TYPE.GROUP_NEW) {
				this.groupStore.addGroup(JSON.parse(msg.content));
				return;
			}
			// 删除群
			if (msg.type == this.$enums.MESSAGE_TYPE.GROUP_DEL) {
				this.groupStore.removeGroup(msg.groupId);
				return;
			}
			// 群消息置顶
			if (msg.type == this.$enums.MESSAGE_TYPE.GROUP_TOP_MESSAGE) {
				let topMessage = msg.content ? JSON.parse(msg.content) : null;
				this.groupStore.updateTopMessage(msg.groupId, topMessage);
				return;
			}
			// 群视频信令
			if (this.$msgType.isRtcGroup(msg.type)) {
				this.$nextTick(() => {
					this.$refs.rtcGroupVideo.onRTCMessage(msg);
				})
				return;
			}
			// 插入群聊消息
			if (this.$msgType.isNormal(msg.type) || this.$msgType.isTip(msg.type) || this.$msgType.isAction(msg.type)) {
				let group = this.loadGroupInfo(msg.groupId);
				this.insertGroupMessage(group, msg);
			}
		},
		insertGroupMessage(group, msg) {
			let chatInfo = {
				type: 'GROUP',
				targetId: group.id,
				showName: group.showGroupName,
				headImage: group.headImageThumb
			};
			// 打开会话
			this.chatStore.openChat(chatInfo);
			// 插入消息
			this.chatStore.insertMessage(msg, chatInfo);
			// 播放提示音
			if (!msg.selfSend && msg.type <= this.$enums.MESSAGE_TYPE.VIDEO &&
				msg.status != this.$enums.MESSAGE_STATUS.READED) {
				this.playAudioTip();
			}
		},
		handleSystemMessage(msg) {
			// 消息加载标志
			if (msg.type == this.$enums.MESSAGE_TYPE.LOADING) {
				this.chatStore.setLoadingSystemMsg(JSON.parse(msg.content))
				return;
			}
			// 消息已读处理，清空已读数量
			if (msg.type == this.$enums.MESSAGE_TYPE.READED) {
				this.chatStore.resetUnreadCount({
					type: 'SYSTEM',
					targetId: 0
				})
				return;
			}
			// 用户被封禁
			if (msg.type == this.$enums.MESSAGE_TYPE.USER_BANNED) {
				this.$wsApi.close(3000);
				this.$alert("您的账号已被管理员封禁,原因:" + msg.content, "账号被封禁", {
					confirmButtonText: '确定',
					callback: action => {
						this.onExit();
					}
				});
				return;
			}
			// 用户账户注销
			if (msg.type == this.$enums.MESSAGE_TYPE.USER_UNREG) {
				this.$wsApi.close(3000);
				this.$alert("您的账号已注销", {
					confirmButtonText: '确定',
					callback: action => {
						this.onExit();
					}
				});
				return;
			}
			// 插入消息
			this.insertSystemMessage(msg);
		},
		insertSystemMessage(msg) {
			let headImage = require('@/assets/image/chat_system.png')
			let chatInfo = {
				type: 'SYSTEM',
				targetId: 0,
				showName: "系统通知",
				headImage: headImage
			};
			// 打开会话
			this.chatStore.openChat(chatInfo);
			// 插入消息
			this.chatStore.insertMessage(msg, chatInfo);
		},
		onExit() {
			this.unloadStore();
			this.$wsApi.close(3000);
			sessionStorage.removeItem("accessToken");
			location.href = "/";
		},
		closeAllBox() {
			this.$refs.groupInfo.close();
			this.$refs.userInfo.close();
		},
		playAudioTip() {
			// 离线消息不播放铃声
			if (this.chatStore.isLoading()) {
				return;
			}
			// 防止过于密集播放
			if (new Date().getTime() - this.lastPlayAudioTime > 1000) {
				this.lastPlayAudioTime = new Date().getTime();
				let audio = new Audio();
				let url = require(`@/assets/audio/tip.wav`);
				audio.src = url;
				audio.play();
			}
		},
		showSetting() {
			this.$refs.setting.open();
		},
		loadFriendInfo(id) {
			let friend = this.friendStore.findFriend(id);
			if (!friend) {
				friend = {
					id: id,
					showNickName: "未知用户",
					headImage: ""
				}
			}
			return friend;
		},
		loadGroupInfo(id) {
			let group = this.groupStore.findGroup(id);
			if (!group) {
				group = {
					id: id,
					showGroupName: "未知群聊",
					headImageThumb: ""
				}
			}
			return group;
		}
	},
	computed: {
		mine() {
			return this.userStore.userInfo;
		},
		unreadCount() {
			let unreadCount = 0;
			let chats = this.chatStore.chats;
			chats.forEach((chat) => {
				if (!chat.delete) {
					unreadCount += chat.unreadCount
				}
			});
			return unreadCount;
		},
		recvCount() {
			let requests = this.friendStore.requests;
			return requests.filter((req) => req.recvId == this.mine.id).length;
		},
	},
	watch: {
		unreadCount: {
			handler(newCount, oldCount) {
				let tip = newCount > 0 ? `${newCount}条未读` : "";
				this.$elm.setTitleTip(tip);
			},
			immediate: true
		}
	},
	mounted() {
		this.$wsApi.close();
		this.init();
	},
	unmounted() {
		this.$wsApi.close();
	}
}
</script>

<style scoped lang="scss">
.home-page {
	height: 100vh;
	width: 100vw;
	display: flex;
	justify-content: center;
	align-items: center;
	border-radius: 4px;
	overflow: hidden;
	background: var(--im-color-primary-light-9);

	.app-container {
		width: 62vw;
		height: 80vh;
		display: flex;
		min-height: 600px;
		min-width: 970px;
		position: absolute;
		border-radius: 4px;
		overflow: hidden;
		box-shadow: var(--im-box-shadow-dark);
		transition: 0.2s;

		&.fullscreen {
			transition: 0.2s;
			width: 100vw;
			height: 100vh;
		}
	}

	.navi-bar {
		--icon-font-size: 22px;
		--width: 60px;
		width: var(--width);
		background: var(--im-color-primary-light-1);
		padding-top: 20px;

		.navi-bar-box {
			height: 100%;
			display: flex;
			flex-direction: column;
			justify-content: space-between;

			.bottom {
				margin-bottom: 30px;
			}
		}

		.avater {
			display: flex;
			justify-content: center;
		}

		.menu {
			height: 200px;
			//margin-top: 10px;
			display: flex;
			flex-direction: column;
			justify-content: center;
			align-content: center;

			.link {
				text-decoration: none;
			}

			.router-link-active .menu-item {
				color: #fff;
				background: var(--im-color-primary-light-2);
			}

			.link:not(.router-link-active) .menu-item:hover {
				color: var(--im-color-primary-light-7);
			}

			.menu-item {
				position: relative;
				color: var(--im-color-primary-light-4);
				width: var(--width);
				height: 46px;
				display: flex;
				justify-content: center;
				align-items: center;
				margin-bottom: 12px;

				.icon {
					font-size: var(--icon-font-size)
				}

				.unread-text {
					position: absolute;
					background-color: var(--im-color-danger);
					left: 30px;
					top: 5px;
					color: white;
					border-radius: 30px;
					padding: 0 6px;
					font-size: var(--im-font-size-smaller);
					text-align: center;
					white-space: nowrap;
					border: 1px solid #f1e5e5;
				}
			}
		}

		.bottom-item {
			display: flex;
			justify-content: center;
			align-items: center;
			height: 50px;
			width: 100%;
			cursor: pointer;
			color: var(--im-color-primary-light-4);
			font-size: var(--icon-font-size);

			.icon {
				font-size: var(--icon-font-size)
			}

			&:hover {
				font-weight: 600;
				color: var(--im-color-primary-light-7);
			}
		}
	}

	.content-box {
		flex: 1;
		padding: 0;
		background-color: #fff;
		text-align: center;
	}
}
</style>
