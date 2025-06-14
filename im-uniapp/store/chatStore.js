import { defineStore } from 'pinia';
import { MESSAGE_TYPE, MESSAGE_STATUS } from '@/common/enums.js';
import useUserStore from './userStore';

/**
 * 优化1(冷热消息分区):
 * 热消息：登录后的消息
 * 冷消息: 登录前的消息
 * 每个会话的冷热消息分别用一个key进行存储，当有新的消息时，只更新热消息key，冷消息key保持不变
 * 由于热消息数量不会很大，所以uni.setStorageSync耗时很低，可以防止消息过多时出现卡顿的情况
 *
 * 优化2(延迟渲染):
 * 拉取离线消息时，如果直接用state.chats接收，页面就开始渲染，一边渲染页面一边大量接消息会导致很严重的卡顿
 * 为了加速拉取效率，拉取时消息暂时存储到cacheChats,等待所有离线消息拉取完成后，再统一放至state中进行渲染
 * 
 * 优化3(pinia代替vuex)
 * 实测pinia的远超vuex,且语法更简洁清晰
 * 
 * */

let cacheChats = [];
export default defineStore('chatStore', {
	state: () => {
		return {
			chats: [],
			privateMsgMaxId: 0,
			groupMsgMaxId: 0,
			systemMsgMaxSeqNo: 0,
			loadingPrivateMsg: false,
			loadingSystemMsg: false,
			loadingGroupMsg: false
		}
	},
	actions: {
		initChats(chatsData) {
			cacheChats = [];
			this.chats = [];
			for (let chat of chatsData.chats) {
				chat.stored = false;
				// 暂存至缓冲区
				cacheChats.push(JSON.parse(JSON.stringify(chat)));
				// 加载期间显示只前15个会话做做样子,一切都为了加快初始化时间
				if (this.chats.length < 15) {
					this.chats.push(chat);
				}
			}
			this.privateMsgMaxId = chatsData.privateMsgMaxId || 0;
			this.groupMsgMaxId = chatsData.groupMsgMaxId || 0;
			this.systemMsgMaxSeqNo = chatsData.systemMsgMaxSeqNo || 0;
			// 防止图片一直处在加载中状态
			cacheChats.forEach((chat) => {
				chat.messages.forEach((msg) => {
					if (msg.loadStatus == "loading") {
						msg.loadStatus = "fail"
					}
				})
			})
		},
		openChat(chatInfo) {
			let chats = this.curChats;
			let chat = null;
			for (let idx in chats) {
				if (chats[idx].type == chatInfo.type &&
					chats[idx].targetId === chatInfo.targetId) {
					chat = chats[idx];
					// 放置头部
					this.moveTop(idx)
					break;
				}
			}
			// 创建会话
			if (chat == null) {
				chat = {
					targetId: chatInfo.targetId,
					type: chatInfo.type,
					showName: chatInfo.showName,
					headImage: chatInfo.headImage,
					lastContent: "",
					lastSendTime: new Date().getTime(),
					unreadCount: 0,
					hotMinIdx: 0,
					messages: [],
					atMe: false,
					atAll: false,
					stored: false
				};
				chats.unshift(chat);
				this.saveToStorage();
			}
		},
		activeChat(idx) {
			let chats = this.curChats;
			if (idx >= 0) {
				chats[idx].unreadCount = 0;
			}
		},
		resetUnreadCount(chatInfo) {
			let chats = this.curChats;
			for (let idx in chats) {
				if (chats[idx].type == chatInfo.type &&
					chats[idx].targetId == chatInfo.targetId) {
					chats[idx].unreadCount = 0;
					chats[idx].atMe = false;
					chats[idx].atAll = false;
					chats[idx].stored = false;
					this.saveToStorage();
				}
			}

		},
		readedMessage(pos) {
			let chat = this.findChatByFriend(pos.friendId);
			if (!chat) return;
			chat.messages.forEach((m) => {
				if (m.id && m.selfSend && m.status < MESSAGE_STATUS.RECALL) {
					// pos.maxId为空表示整个会话已读
					if (!pos.maxId || m.id <= pos.maxId) {
						m.status = MESSAGE_STATUS.READED
						chat.stored = false;
					}
				}
			})
			if (!chat.stored) {
				this.saveToStorage();
			}
		},
		cleanMessage(idx) {
			let chat = this.curChats[idx];
			chat.lastContent = '';
			chat.hotMinIdx = 0;
			chat.unreadCount = 0;
			chat.atMe = false;
			chat.atAll = false;
			chat.stored = false
			chat.messages = [];
			this.saveToStorage(true);
		},
		removeChat(idx) {
			let chats = this.curChats;
			chats[idx].delete = true;
			chats[idx].stored = false;
			this.saveToStorage();
		},
		removePrivateChat(userId) {
			let chats = this.curChats;
			for (let idx in chats) {
				if (chats[idx].type == 'PRIVATE' &&
					chats[idx].targetId == userId) {
					this.removeChat(idx);
				}
			}
		},
		removeGroupChat(groupId) {
			let chats = this.curChats;
			for (let idx in chats) {
				if (chats[idx].type == 'GROUP' &&
					chats[idx].targetId == groupId) {
					this.removeChat(idx);
				}
			}
		},
		moveTop(idx) {
			if (this.isLoading()) {
				return;
			}
			let chats = this.curChats;
			if (idx > 0) {
				let chat = chats[idx];
				chats.splice(idx, 1);
				chats.unshift(chat);
				chat.lastSendTime = new Date().getTime();
				chat.stored = false;
				this.saveToStorage();
			}
		},
		insertMessage(msgInfo, chatInfo) {
			// 获取对方id或群id
			let type = chatInfo.type;
			// 记录消息的最大id
			if (msgInfo.id && type == "PRIVATE" && msgInfo.id > this.privateMsgMaxId) {
				this.privateMsgMaxId = msgInfo.id;
			}
			if (msgInfo.id && type == "GROUP" && msgInfo.id > this.groupMsgMaxId) {
				this.groupMsgMaxId = msgInfo.id;
			}
			if (msgInfo.seqNo && type == "SYSTEM" && msgInfo.seqNo > this.systemMsgMaxSeqNo) {
				this.systemMsgMaxSeqNo = msgInfo.seqNo;
			}
			// 如果是已存在消息，则覆盖旧的消息数据
			let chat = this.findChat(chatInfo);
			let message = this.findMessage(chat, msgInfo);
			if (message) {
				Object.assign(message, msgInfo);
				chat.stored = false;
				this.saveToStorage();
				return;
			}
			// 会话列表内容
			if (msgInfo.type == MESSAGE_TYPE.IMAGE) {
				chat.lastContent = "[图片]";
			} else if (msgInfo.type == MESSAGE_TYPE.VIDEO) {
				chat.lastContent = "[视频]";
			} else if (msgInfo.type == MESSAGE_TYPE.FILE) {
				chat.lastContent = "[文件]";
			} else if (msgInfo.type == MESSAGE_TYPE.AUDIO) {
				chat.lastContent = "[语音]";
			} else if (msgInfo.type == MESSAGE_TYPE.USER_CARD) {
				chat.lastContent = "[个人名片] " + JSON.parse(msgInfo.content).nickName;
			} else if (msgInfo.type == MESSAGE_TYPE.GROUP_CARD) {
				chat.lastContent = "[群名片] " + JSON.parse(msgInfo.content).groupName;
			} else if (msgInfo.type == MESSAGE_TYPE.ACT_RT_VOICE) {
				chat.lastContent = "[语音通话]";
			} else if (msgInfo.type == MESSAGE_TYPE.ACT_RT_VIDEO) {
				chat.lastContent = "[视频通话]";
			} else if (msgInfo.type == MESSAGE_TYPE.SYSTEM_MESSAGE) {
				chat.lastContent = msgInfo.title;
			} else if (msgInfo.type == MESSAGE_TYPE.TEXT ||
				msgInfo.type == MESSAGE_TYPE.RECALL ||
				msgInfo.type == MESSAGE_TYPE.TIP_TEXT) {
				chat.lastContent = msgInfo.content;
			}
			chat.lastSendTime = msgInfo.sendTime;
			chat.sendNickName = msgInfo.sendNickName;
			// 未读加1
			if (!msgInfo.selfSend && msgInfo.status != MESSAGE_STATUS.READED &&
				msgInfo.status != MESSAGE_STATUS.RECALL && msgInfo.type != MESSAGE_TYPE.TIP_TEXT) {
				chat.unreadCount++;
			}
			// 是否有人@我
			if (!msgInfo.selfSend && chat.type == "GROUP" && msgInfo.atUserIds &&
				msgInfo.status != MESSAGE_STATUS.READED) {
				const userStore = useUserStore();
				let userId = userStore.userInfo.id;
				if (msgInfo.atUserIds.indexOf(userId) >= 0) {
					chat.atMe = true;
				}
				if (msgInfo.atUserIds.indexOf(-1) >= 0) {
					chat.atAll = true;
				}
			}
			// 间隔大于10分钟插入时间显示
			if (!chat.lastTimeTip || (chat.lastTimeTip < msgInfo.sendTime - 600 * 1000)) {
				chat.messages.push({
					sendTime: msgInfo.sendTime,
					type: MESSAGE_TYPE.TIP_TIME,
				});
				chat.lastTimeTip = msgInfo.sendTime;
			}
			// 根据id顺序插入，防止消息乱序
			let insertPos = chat.messages.length;
			// 防止 图片、文件 在发送方 显示 在顶端  因为还没存库，id=0
			if (type != "SYSTEM" && msgInfo.id && msgInfo.id > 0) {
				for (let idx in chat.messages) {
					if (chat.messages[idx].id && msgInfo.id < chat.messages[idx].id) {
						insertPos = idx;
						console.log(`消息出现乱序,位置:${chat.messages.length},修正至:${insertPos}`);
						break;
					}
				}
			}
			// 系统消息是按序列号发送
			if (type == "SYSTEM" && msgInfo.seqNo && msgInfo.seqNo > 0) {
				for (let idx in chat.messages) {
					if (chat.messages[idx].seqNo && msgInfo.seqNo < chat.messages[idx].seqNo) {
						insertPos = idx;
						console.log(`消息出现乱序,位置:${chat.messages.length},修正至:${insertPos}`);
						break;
					}
				}
			}
			chat.messages.splice(insertPos, 0, msgInfo);
			chat.stored = false;
			this.saveToStorage();
		},
		updateMessage(msgInfo, chatInfo) {
			// 获取对方id或群id
			let chat = this.findChat(chatInfo);
			let message = this.findMessage(chat, msgInfo);
			if (message) {
				// 属性拷贝
				Object.assign(message, msgInfo);
				chat.stored = false;
				this.saveToStorage();
			}
		},
		deleteMessage(msgInfo, chatInfo) {
			// 获取对方id或群id
			let chat = this.findChat(chatInfo);
			let isColdMessage = false;
			for (let idx in chat.messages) {
				// 已经发送成功的，根据id删除
				if (chat.messages[idx].id && chat.messages[idx].id == msgInfo.id) {
					chat.messages.splice(idx, 1);
					isColdMessage = idx < chat.hotMinIdx;
					break;
				}
				// 正在发送中的消息可能没有id，只有临时id
				if (chat.messages[idx].tmpId && chat.messages[idx].tmpId == msgInfo.tmpId) {
					chat.messages.splice(idx, 1);
					isColdMessage = idx < chat.hotMinIdx;
					break;
				}
			}
			chat.stored = false;
			this.saveToStorage(isColdMessage);
		},
		recallMessage(msgInfo, chatInfo) {
			let chat = this.findChat(chatInfo);
			if (!chat) return;
			// 要撤回的消息id
			let id = msgInfo.content;
			let name = msgInfo.selfSend ? '你' : chat.type == 'PRIVATE' ? '对方' : msgInfo.sendNickName;
			let isColdMessage = false;
			for (let idx in chat.messages) {
				let m = chat.messages[idx];
				if (m.id && m.id == id) {
					// 改造成一条提示消息
					m.status = MESSAGE_STATUS.RECALL;
					m.content = name + "撤回了一条消息";
					m.type = MESSAGE_TYPE.TIP_TEXT
					// 会话列表
					chat.lastContent = m.content;
					chat.lastSendTime = msgInfo.sendTime;
					chat.sendNickName = '';
					if (!msgInfo.selfSend && msgInfo.status != MESSAGE_STATUS.READED) {
						chat.unreadCount++;
					}
					isColdMessage = idx < chat.hotMinIdx;
				}
				// 被引用的消息也要撤回
				if (m.quoteMessage && m.quoteMessage.id == msgInfo.id) {
					m.quoteMessage.content = "引用内容已撤回";
					m.quoteMessage.status = MESSAGE_STATUS.RECALL;
					m.quoteMessage.type = MESSAGE_TYPE.TIP_TEXT
				}
			}
			chat.stored = false;
			this.saveToStorage(isColdMessage);
		},
		updateChatFromFriend(friend) {
			let chat = this.findChatByFriend(friend.id)
			if (chat && (chat.headImage != friend.headImage ||
					chat.showName != friend.showNickName)) {
				// 更新会话中的群名和头像
				chat.headImage = friend.headImage;
				chat.showName = friend.showNickName;
				chat.stored = false;
				this.saveToStorage();
			}
		},
		updateChatFromGroup(group) {
			let chat = this.findChatByGroup(group.id);
			if (chat && (chat.headImage != group.headImageThumb ||
					chat.showName != group.showGroupName)) {
				// 更新会话中的群名称和头像
				chat.headImage = group.headImageThumb;
				chat.showName = group.showGroupName;
				chat.stored = false;
				this.saveToStorage();
			}
		},
		updateChatFromUser(user) {
			let chat = this.findChatByFriend(user.id);
			// 更新会话中的昵称和头像
			if (chat && (chat.headImage != user.headImageThumb ||
					chat.showName != user.nickName)) {
				chat.headImage = user.headImageThumb;
				chat.showName = user.nickName;
				chat.stored = false;
				this.saveToStorage();
			}
		},
		setLoadingPrivateMsg(loading) {
			this.loadingPrivateMsg = loading;
			if (!this.isLoading()) {
				this.refreshChats()
			}
		},
		setLoadingGroupMsg(loading) {
			this.loadingGroupMsg = loading;
			if (!this.isLoading()) {
				this.refreshChats()
			}
		},
		setLoadingSystemMsg(loading) {
			this.loadingSystemMsg = loading;
			if (!this.isLoading()) {
				this.refreshChats()
			}
		},
		refreshChats() {
			if (!cacheChats) return;
			// 排序
			cacheChats.sort((chat1, chat2) => chat2.lastSendTime - chat1.lastSendTime);
			// #ifndef APP-PLUS
			/**
			 * 由于h5和小程序的stroge只有5m,大约只能存储2w条消息，
			 * 所以这里每个会话只保留1000条消息，防止溢出
			 */
			cacheChats.forEach(chat => {
				if (chat.messages.length > 1000) {
					let idx = chat.messages.length - 1000;
					chat.messages = chat.messages.slice(idx);
				}
			})
			// #endif
			// 记录热数据索引位置
			cacheChats.forEach(chat => chat.hotMinIdx = chat.messages.length);
			// 将消息一次性装载回来
			this.chats = cacheChats;
			// 清空缓存,不再使用
			cacheChats = null;
			// 消息持久化
			this.saveToStorage(true);
		},
		saveToStorage(withColdMessage) {
			// 加载中不保存，防止卡顿
			if (this.isLoading()) {
				return;
			}
			const userStore = useUserStore();
			let userId = userStore.userInfo.id;
			let key = "chats-app-" + userId;
			let chatKeys = [];
			// 按会话为单位存储，只存储有改动的会话
			this.chats.forEach((chat) => {
				let chatKey = `${key}-${chat.type}-${chat.targetId}`
				if (!chat.stored) {
					if (chat.delete) {
						uni.removeStorageSync(chatKey);
					} else {
						// 存储冷数据
						if (withColdMessage) {
							let coldChat = Object.assign({}, chat);
							coldChat.messages = chat.messages.slice(0, chat.hotMinIdx);
							uni.setStorageSync(chatKey, coldChat)
						}
						// 存储热消息
						let hotKey = chatKey + '-hot';
						let hotChat = Object.assign({}, chat);
						hotChat.messages = chat.messages.slice(chat.hotMinIdx)
						uni.setStorageSync(hotKey, hotChat);
					}
					chat.stored = true;
				}
				if (!chat.delete) {
					chatKeys.push(chatKey);
				}
			})
			// 会话核心信息
			let chatsData = {
				privateMsgMaxId: this.privateMsgMaxId,
				groupMsgMaxId: this.groupMsgMaxId,
				systemMsgMaxSeqNo: this.systemMsgMaxSeqNo,
				chatKeys: chatKeys
			}
			uni.setStorageSync(key, chatsData)
			// 清理已删除的会话
			this.chats = this.chats.filter(chat => !chat.delete)
		},
		clear() {
			cacheChats = [];
			this.chats = [];
			this.privateMsgMaxId = 0;
			this.groupMsgMaxId = 0;
			this.systemMsgMaxSeqNo = 0;
			this.loadingPrivateMsg = false;
			this.loadingGroupMsg = false;
			this.loadingSystemMsg = false;
		},
		loadChat() {
			return new Promise((resolve, reject) => {
				let userStore = useUserStore();
				let userId = userStore.userInfo.id;
				let chatsData = uni.getStorageSync("chats-app-" + userId)
				if (chatsData) {
					if (chatsData.chatKeys) {
						chatsData.chats = [];
						chatsData.chatKeys.forEach(key => {
							let coldChat = uni.getStorageSync(key);
							let hotChat = uni.getStorageSync(key + '-hot');
							if (!coldChat && !hotChat) {
								return;
							}
							// 冷热消息合并
							let chat = Object.assign({}, coldChat, hotChat);
							if (hotChat && coldChat) {
								chat.messages = coldChat.messages.concat(hotChat.messages)
							}
							chatsData.chats.push(chat);
						})
					}
					this.initChats(chatsData);
				}
				resolve()
			})
		}
	},
	getters: {
		isLoading: (state) => () => {
			return state.loadingPrivateMsg || state.loadingGroupMsg || state.loadingSystemMsg
		},
		curChats: (state) => {
			if (cacheChats && state.isLoading()) {
				return cacheChats;
			}
			return state.chats;
		},
		findChatIdx: (state) => (chat) => {
			let chats = state.curChats;
			for (let idx in chats) {
				if (chats[idx].type == chat.type &&
					chats[idx].targetId === chat.targetId) {
					chat = state.chats[idx];
					return idx;
				}
			}
		},
		findChat: (state) => (chat) => {
			let chats = state.curChats;
			let idx = state.findChatIdx(chat);
			return chats[idx];
		},
		findChatByFriend: (state) => (fid) => {
			return state.curChats.find(chat => chat.type == 'PRIVATE' &&
				chat.targetId == fid)
		},
		findChatByGroup: (state) => (gid) => {
			return state.curChats.find(chat => chat.type == 'GROUP' &&
				chat.targetId == gid)
		},
		findMessage: (state) => (chat, msgInfo) => {
			let idx = state.findMessageIdx(chat, msgInfo);
			return idx >= 0 ? chat.messages[idx] : null;
		},
		findMessageIdx: (state) => (chat, msgInfo) => {
			if (!chat) {
				return -1;
			}
			if (chat.type == 'SYSTEM') {
				// 系统消息通过seqNo判断
				for (let idx in chat.messages) {
					if (msgInfo.seqNo && chat.messages[idx].seqNo == msgInfo.seqNo) {
						return idx;
					}
				}
			} else {
				for (let idx in chat.messages) {
					// 通过id判断
					if (msgInfo.id && chat.messages[idx].id == msgInfo.id) {
						return idx;
					}
					// 正在发送中的消息可能没有id,只有tmpId
					if (msgInfo.tmpId && chat.messages[idx].tmpId &&
						chat.messages[idx].tmpId == msgInfo.tmpId) {
						return idx;
					}
				}
			}
			return -1;
		}
	}
});