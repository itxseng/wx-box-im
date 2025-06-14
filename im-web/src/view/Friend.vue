<template>
	<el-container class="friend-page">
		<el-aside width="260px" class="aside">
			<div class="header">
				<el-input class="search-text" size="small" placeholder="搜索" v-model="searchText">
					<i class="el-icon-search el-input__icon" slot="prefix"> </i>
				</el-input>
				<el-button plain class="add-btn" icon="el-icon-plus" title="添加好友"
					@click="onShowAddFriend()"></el-button>
				<add-friend :dialogVisible="showAddFriend" @close="onCloseAddFriend"></add-friend>
			</div>
			<el-scrollbar class="friend-items">
				<!-- 顶部列表: 新的朋友、ai机器人等 -->
				<div class="top-item" :class="showFriendReuqest ? 'active' : ''" @click="onClickNewFriend">
					<div class="top-item-avatar">
						<head-image :size="42" :url="require('@/assets/image/new_friend.png')">
							<div v-show="recvCount" class="unread-text">{{ recvCount }} </div>
						</head-image>
					</div>
					<div class="top-item-info">
						<div class="top-item-name">新的朋友</div>
					</div>
				</div>
				<div class="divider"></div>
				<div v-for="(friends, i) in friendValues" :key="i">
					<div class="letter">{{ friendKeys[i] }}</div>
					<div v-for="(friend) in friends" :key="friend.id">
						<friend-item :id="friend.id" :friend="friend" :active="friend.id === activeFriend.id"
							@chat="onSendMessage(friend)" @card="onSendCard(friend)" @delete="onDelFriend(friend)"
							@click.native="onActiveItem(friend)">
						</friend-item>
					</div>
					<div v-if="i < friendValues.length - 1" class="divider"></div>
				</div>
			</el-scrollbar>
		</el-aside>
		<el-container v-if="showFriendReuqest" class="container">
			<div class="header"> 新的朋友</div>
			<friend-request class="request-box"></friend-request>
		</el-container>
		<el-container v-else class="container" v-show="userInfo.id">
			<div class="header">{{ userInfo.nickName }}</div>
			<div class="friend-info">
				<head-image :size="160" :name="userInfo.nickName" :url="userInfo.headImage" :radius="'10%'"
					@click.native="showFullImage()"></head-image>
				<div>
					<div class="info-items">
						<el-descriptions :title="activeFriend.showNickName" class="description" :column="1">
							<template slot="extra">
								<div v-if="isFriend" title="更多" class="more el-icon-more" @click="onClickMore">
								</div>
							</template>
							<el-descriptions-item label="用户名">{{ userInfo.userName }}
								<el-tag v-if="userInfo.status == 1" size="mini" type="danger">已注销</el-tag>
							</el-descriptions-item>
							<el-descriptions-item label="昵称">{{ userInfo.nickName }}
							</el-descriptions-item>
							<el-descriptions-item v-if="isFriend" class="remark" label="备注"
								:contentStyle="activeFriend.remarkNickName ? '' : 'color:#888;'">{{
									activeFriend.showNickName }}
								<span class="icon iconfont icon-modify" @click="onEditRemark"></span>
							</el-descriptions-item>
							<el-descriptions-item label="性别">{{ userInfo.sex == 0 ? "男" : "女"
							}}</el-descriptions-item>
							<el-descriptions-item label="签名">{{ userInfo.signature }}</el-descriptions-item>
						</el-descriptions>
					</div>
					<div class="btn-group">
						<el-button v-show="isFriend" icon="el-icon-position" type="primary"
							@click="onSendMessage(activeFriend)">发消息</el-button>
						<el-button v-show="!isFriend" icon="el-icon-plus" type="primary"
							@click="onAddFriend(userInfo)">加为好友</el-button>
						<el-button v-show="isFriend" icon="el-icon-delete" type="danger"
							@click="onDelFriend(userInfo)">删除好友</el-button>
					</div>
				</div>
			</div>

		</el-container>
		<friend-apply ref="applyRef"></friend-apply>
		<chat-selector ref="chatSel" title="分享名片"></chat-selector>
		<right-menu ref="rightMenu" @select="onSelectMenu"></right-menu>
	</el-container>
</template>

<script>
import FriendItem from "../components/friend/FriendItem.vue";
import FriendRequestItem from "../components/friend/FriendRequestItem.vue";
import AddFriend from "../components/friend/AddFriend.vue";
import HeadImage from "../components/common/HeadImage.vue";
import NoDataTip from '../components/common/NoDataTip.vue';
import FriendApply from '../components/friend/FriendApply.vue';
import FriendRequest from '../components/friend/FriendRequest.vue';
import ChatSelector from "../components/chat/ChatSelector.vue";
import RightMenu from "../components/common/RightMenu.vue";
import { pinyin } from 'pinyin-pro';

export default {
	name: "friend",
	components: {
		FriendItem,
		FriendRequestItem,
		AddFriend,
		HeadImage,
		NoDataTip,
		FriendApply,
		FriendRequest,
		ChatSelector,
		RightMenu
	},
	data() {
		return {
			searchText: "",
			showAddFriend: false,
			activeFriend: {},
			userInfo: {},
			showFriendReuqest: false,
			menuItems: [{
				key: 'CHAT',
				name: '发送消息',
			}, {
				key: 'CARD',
				name: '分享名片',
			}, {
				key: 'REMARK',
				name: '修改备注',
			}, {
				key: 'DELETE',
				name: '删除好友',
				color: '#F56C6C'
			}]
		}
	},
	methods: {
		onShowAddFriend() {
			this.showAddFriend = true;
		},
		onCloseAddFriend() {
			this.showAddFriend = false;
		},
		onActiveItem(friend) {
			this.activeFriend = friend;
			this.showFriendReuqest = false;
			this.loadUserInfo(friend.id);
		},
		onDelFriend(friend) {
			this.$confirm(`确认删除'${friend.nickName}',并清空聊天记录吗?`, '确认解除?', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			}).then(() => {
				this.$http({
					url: `/friend/delete/${friend.id}`,
					method: 'delete'
				}).then(() => {
					this.$message.success("删除好友成功");
					this.friendStore.removeFriend(friend.id);
					this.chatStore.removePrivateChat(friend.id);
				})
			})
		},
		onAddFriend() {
			this.$refs.applyRef.open(this.userInfo)
		},
		onSendMessage(friend) {
			let chat = {
				type: 'PRIVATE',
				targetId: friend.id,
				showName: friend.showNickName,
				headImage: friend.headImage,
			};
			this.chatStore.openChat(chat);
			this.chatStore.setActiveChat(0);
			this.$router.push("/home/chat");
		},
		onSendCard(friend) {
			this.$refs.chatSel.open(chats => {
				let idx = 0;
				chats.forEach(chat => {
					let cardData = {
						userId: friend.id,
						nickName: friend.nickName,
						headImage: friend.headImage,
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
						method: 'post',
						data: msgInfo
					}).then(m => {
						m.selfSend = true;
						this.chatStore.openChat(chat);
						this.chatStore.insertMessage(m, chat);
						if (++idx == chats.length) {
							this.$message.success("发送成功")
						}
					})
				})
			})
		},
		onEditRemark() {
			this.$prompt('请输入好友备注', '修改备注', {
				inputValue: this.activeFriend.showNickName,
				confirmButtonText: '确定',
				cancelButtonText: '取消'
			}).then((o) => {
				let data = {
					friendId: this.activeFriend.id,
					remarkNickName: o.value
				}
				this.$http({
					url: '/friend/update/remark',
					method: 'put',
					data: data
				}).then((friend) => {
					this.activeFriend = friend;
					this.chatStore.updateChatFromFriend(friend);
					this.friendStore.updateFriend(friend);
				})
			})
		},
		onClickNewFriend() {
			this.activeFriend = {};
			this.showFriendReuqest = true;
		},
		onClickMore(e) {
			this.$refs.rightMenu.open(e, this.menuItems);
		},
		onSelectMenu(item) {
			switch (item.key) {
				case 'CHAT':
					this.onSendMessage(this.activeFriend);
					break;
				case 'CARD':
					this.onSendCard(this.activeFriend);
					break;
				case 'REMARK':
					this.onEditRemark(this.activeFriend);
					break;
				case 'DELETE':
					this.onDelFriend(this.activeFriend);
					break;
			}
		},
		locateItem(id) {
			if (this.isFriend) {
				document.getElementById(id).scrollIntoView({ behavior: 'smooth' });
			}
		},
		showFullImage() {
			if (this.userInfo.headImage) {
				this.$eventBus.$emit("openFullImage", this.userInfo.headImage);

			}
		},
		updateFriendInfo() {
			if (this.isFriend) {
				// store的数据不能直接修改，深拷贝一份store的数据
				let friend = JSON.parse(JSON.stringify(this.activeFriend));
				friend.headImage = this.userInfo.headImageThumb;
				friend.nickName = this.userInfo.nickName;
				friend.showNickName = friend.remarkNickName ? friend.remarkNickName : friend.nickName;
				this.chatStore.updateChatFromFriend(friend);
				this.friendStore.updateFriend(friend);
			}
		},
		loadUserInfo(id) {
			// 获取好友用户信息
			this.$http({
				url: `/user/find/${id}`,
				method: 'GET'
			}).then((userInfo) => {
				this.userInfo = userInfo;
				this.updateFriendInfo();
			})
		},
		firstLetter(strText) {
			// 使用pinyin-pro库将中文转换为拼音
			let pinyinOptions = {
				toneType: 'none', // 无声调
				type: 'normal' // 普通拼音
			};
			let pyText = pinyin(strText, pinyinOptions);
			return pyText[0];
		},
		isEnglish(character) {
			return /^[A-Za-z]+$/.test(character);
		}
	},
	computed: {
		mine() {
			return this.userStore.userInfo;
		},
		friendMap() {
			// 按首字母分组
			let map = new Map();
			this.friendStore.friends.forEach((f) => {
				if (f.deleted || (this.searchText && !f.showNickName.includes(this.searchText))) {
					return;
				}
				let letter = this.firstLetter(f.showNickName).toUpperCase();
				// 非英文一律为#组
				if (!this.isEnglish(letter)) {
					letter = "#"
				}
				if (f.online) {
					letter = '在线'
				}
				if (map.has(letter)) {
					map.get(letter).push(f);
				} else {
					map.set(letter, [f]);
				}
			})
			// 排序
			let arrayObj = Array.from(map);
			arrayObj.sort((a, b) => {
				// #组在最后面
				if (a[0] == '#' || b[0] == '#') {
					return b[0].localeCompare(a[0])
				}
				return a[0].localeCompare(b[0])
			})
			map = new Map(arrayObj.map(i => [i[0], i[1]]));
			return map;
		},
		friendKeys() {
			return Array.from(this.friendMap.keys());
		},
		friendValues() {
			return Array.from(this.friendMap.values());
		},
		recvCount() {
			let requests = this.friendStore.requests;
			return requests.filter((req) => req.recvId == this.mine.id).length;
		},
		isFriend() {
			return this.friendStore.isFriend(this.userInfo.id);
		}
	},
	mounted() {
		let userId = this.$route.query.id;
		if (userId) {
			let friend = this.friendStore.findFriend(userId);
			this.onActiveItem(friend);
			this.locateItem(friend.id);
		}
	}
}
</script>

<style lang="scss">
.friend-page {

	.divider {
		border-bottom: 1px #ddd solid;
		margin: 10px;
	}

	.aside {
		display: flex;
		flex-direction: column;
		background: var(--im-background);

		.header {
			height: 50px;
			display: flex;
			align-items: center;
			padding: 0 8px;

			.add-btn {
				padding: 5px !important;
				margin: 5px;
				font-size: 16px;
				border-radius: 50%;
			}
		}

		.friend-items {
			flex: 1;

			.top-item {
				height: 50px;
				display: flex;
				position: relative;
				padding: 5px 10px;
				align-items: center;
				white-space: nowrap;
				cursor: pointer;

				&:hover {
					background-color: var(--im-background-active);
				}

				&.active {
					background-color: var(--im-background-active-dark);
				}

				.top-item-avatar {
					display: flex;
					justify-content: center;
					align-items: center;

					.unread-text {
						position: absolute;
						background-color: #f56c6c;
						right: -4px;
						top: -8px;
						color: white;
						border-radius: 30px;
						padding: 1px 5px;
						font-size: 10px;
						text-align: center;
						white-space: nowrap;
						border: 1px solid #f1e5e5;
					}
				}

				.top-item-info {
					flex: 1;
					display: flex;
					flex-direction: column;
					padding-left: 10px;
					text-align: left;

					.top-item-name {
						font-size: var(--im-font-size);
						white-space: nowrap;
						overflow: hidden;
					}
				}
			}
		}

		.letter {
			text-align: left;
			font-size: var(--im-larger-size-larger);
			padding: 5px 15px;
			color: var(--im-text-color-light);
		}
	}

	.container {
		display: flex;
		flex-direction: column;

		.header {
			height: 50px;
			display: flex;
			justify-content: space-between;
			align-items: center;
			padding: 0 12px;
			font-size: var(--im-font-size-larger);
			border-bottom: var(--im-border);
			box-sizing: border-box;
		}

		.request-box {
			flex: 1;
		}

		.friend-info {
			display: flex;
			padding: 50px 80px 20px 80px;
			text-align: center;

			.info-items {
				margin-left: 60px;
				background-color: #ffffff;
				max-width: 400px;

				.el-descriptions-item__content {
					line-height: 21px;
				}

				.more {
					margin-left: 5px;
					cursor: pointer;
				}

				.icon-modify {
					margin-left: 5px;
					cursor: pointer;
					color: var(--im-color-primary)
				}
			}
		}

		.btn-group {
			text-align: left !important;
			margin-top: 20px;
			margin-left: 60px;
		}
	}
}
</style>