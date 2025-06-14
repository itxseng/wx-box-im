<template>
	<view class="page chat-history-file">
		<nav-bar title="文件" back></nav-bar>
		<view class="nav-bar" id="navBar">
			<view class="nav-search">
				<uni-search-bar radius="100" v-model="searchText" placeholder="搜索文件名"
					cancelButton="none"></uni-search-bar>
			</view>
		</view>
		<scroll-view v-if="messages.length>0" class="chat-message-list" :style="{height: scrollbarHeight+'px'}"
			scroll-y="true" upper-threshold="200" @scrolltolower="onScrollToBottom">
			<view v-for="(m, idx) in messages" :key="idx">
				<view class="chat-message" @longpress.prevent.stop="onLongPress(m)" @touchmove="onTouchMove"
					@touchend="onTouchEnd">
					<chat-history-item :headImage="headImage(m)" :showName="showName(m)" :msgInfo="m"
						@tap="onClickMessageItem(m)">
					</chat-history-item>
				</view>
			</view>
		</scroll-view>
		<no-data-tip v-else class="tip"></no-data-tip>
		<popup-menu ref="popMenu" :items="menuItems" @select="onSelectMenuItem"></popup-menu>
	</view>
</template>

<script>
export default {
	data() {
		return {
			searchText: '',
			chatIdx: -1,
			chat: {},
			groupMembers: [],
			showMaxIdx: 30,
			navBarHeight: 0,
			activeMessage: null,
			menuItems: [{
				key: 'LOCATE_MESSAGE',
				name: '在聊天中定位'
			}],
			isTouchMove: false
		}
	},
	methods: {
		onClickMessageItem(msgInfo) {
			uni.hideKeyboard();
			// 延迟50ms,因为必须等键盘消失再跳转，否则会影响计算聊天页面高度
			setTimeout(() => {
				let messageIdx = this.chatStore.findMessageIdx(this.chat, msgInfo);
				uni.navigateTo({
					url: `/pages/chat/chat-box?chatIdx=${this.chatIdx}&locateMessageIdx=${messageIdx}`
				})
			}, 50);
		},
		onScrollToBottom() {
			this.showMaxIdx += 20;
		},
		onLongPress(m) {
			if (!this.isTouchMove) {
				this.activeMessage = m;
				this.$refs.popMenu.open();
			}
		},
		onTouchMove() {
			this.isTouchMove = true;
		},
		onTouchEnd() {
			this.isTouchMove = false;
		},
		onSelectMenuItem(item) {
			if (item.key == 'LOCATE_MESSAGE') {
				let messageIdx = this.chatStore.findMessageIdx(this.chat, this.activeMessage);
				uni.navigateTo({
					url: `/pages/chat/chat-box?chatIdx=${this.chatIdx}&locateMessageIdx=${messageIdx}`
				})
			}
		},
		headImage(msgInfo) {
			if (this.chat.type == 'GROUP') {
				let member = this.groupMembers.find((m) => m.userId == msgInfo.sendId);
				return member ? member.headImage : "";
			} else {
				return msgInfo.selfSend ? this.mine.headImageThumb : this.chat.headImage
			}
		},
		showName(msgInfo) {
			if (!msgInfo) {
				return "";
			}
			if (this.chat.type == 'GROUP') {
				let member = this.groupMembers.find((m) => m.userId == msgInfo.sendId);
				return member ? member.showNickName : "";
			} else {
				return msgInfo.selfSend ? this.mine.nickName : this.chat.showName
			}
		},
		loadGroupMembers(groupId) {
			this.$http({
				url: `/group/members/${groupId}`,
				method: 'GET'
			}).then((groupMembers) => {
				this.groupMembers = groupMembers;
			});
		}
	},
	computed: {
		mine() {
			return this.userStore.userInfo;
		},
		messages() {
			if (!this.chat.messages) {
				return [];
			}
			return this.chat.messages.filter(m => {
				if (this.$enums.MESSAGE_TYPE.FILE == m.type) {
					return JSON.parse(m.content).name.toLowerCase().includes(this.searchText.toLowerCase());
				}
				return false;
			}).reverse().slice(0, this.showMaxIdx);
		},
		scrollbarHeight() {
			let h = uni.getSystemInfoSync().windowHeight;
			// 减去标题栏高度
			h -= 50;
			// 减去搜索栏高度
			h -= this.navBarHeight;
			// #ifndef H5
			// h5需要减去状态栏高度
			h -= uni.getSystemInfoSync().statusBarHeight;
			// #endif
			return h;
		},
		noDataTip() {
			return this.searchText ? `未搜索到与'${this.searchText}'相关的内容` : "没有数据";
		}
	},
	onLoad(options) {
		// 聊天数据
		this.chatIdx = options.chatIdx;
		this.chat = this.chatStore.chats[options.chatIdx];
		if (this.chat.type == "GROUP") {
			this.loadGroupMembers(this.chat.targetId);
		}
	},
	mounted() {
		// 计算元素高度
		const query = uni.createSelectorQuery().in(this);
		query.select('#navBar').boundingClientRect(rect => {
			this.navBarHeight = Number(rect.height)
		}).exec();
	}
}
</script>

<style lang="scss" scoped>
.chat-history-file {
	position: relative;
	display: flex;
	flex-direction: column;

	.chat-message-list {
		flex: 1;
		height: 100%;
	}

	.tip {
		width: 100%;
		flex: 1;
	}
}
</style>