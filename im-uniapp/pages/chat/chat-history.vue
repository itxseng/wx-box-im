<template>
	<view class="page chat-history">
		<nav-bar title="聊天记录" back></nav-bar>
		<view class="nav-bar" id="navBar">
			<view class="nav-search">
				<uni-search-bar radius="100" v-model="searchText" placeholder="搜索聊天记录"
					cancelButton="none"></uni-search-bar>
			</view>
		</view>
		<view v-if="!searchText">
			<view class="search-tip">快速搜索聊天内容 </view>
			<view class="search-tabs">
				<view class="search-tab" @click="onClickFileTab">
					<view class="search-icon iconfont icon-doc"></view>
					<view class="search-name">文件</view>
				</view>
				<view class="search-tab" @click="onClickImageTab">
					<view class="search-icon iconfont icon-image"></view>
					<view class="search-name">图片与视频</view>
				</view>
			</view>
		</view>
		<scroll-view v-else-if="messages.length>0" class="chat-message-list" :style="{height: scrollbarHeight+'px'}"
			scroll-y="true" upper-threshold="200" @scrolltolower="onScrollToBottom">
			<view v-for="(msgInfo, idx) in messages" :key="idx">
				<chat-history-item :headImage="headImage(msgInfo)" :showName="showName(msgInfo)"
					:quoteShowName="showName(msgInfo.quoteMessage)" :msgInfo="msgInfo"
					@tap="onClickMessageItem(msgInfo)">
				</chat-history-item>
			</view>
		</scroll-view>
		<no-data-tip v-else class="tip" :tip="`未搜索到与'${searchText}'相关的内容`"></no-data-tip>
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
			navBarHeight: 0
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
			}, 50)

		},
		onClickFileTab() {
			uni.navigateTo({
				url: `/pages/chat/chat-history-file?chatIdx=${this.chatIdx}`
			})
		},
		onClickImageTab() {
			uni.navigateTo({
				url: `/pages/chat/chat-history-image?chatIdx=${this.chatIdx}`
			})
		},
		onScrollToBottom() {
			this.showMaxIdx += 20;
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
			if (!this.chat.messages) return [];
			console.log(this.chat)
			return this.chat.messages.filter(m => {
				// 只有文字和文件支持检索
				if (this.$enums.MESSAGE_TYPE.TEXT == m.type) {
					return m.content.toLowerCase().includes(this.searchText.toLowerCase())
				} else if (this.$enums.MESSAGE_TYPE.FILE == m.type) {
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
.chat-history {
	position: relative;
	display: flex;
	flex-direction: column;

	.search-tip {
		margin-top: 100rpx;
		color: $im-text-color-lighter;
		text-align: center;
		padding: 20rpx;
		font-size: $im-font-size-smaller;
	}

	.search-tabs {
		padding: 20rpx 80rpx;
		display: flex;
		justify-content: center;

		.search-tab {
			padding: 30rpx 80rpx;
			display: flex;
			flex-direction: column;
			align-items: center;
			color: $im-text-color-light;

			.search-icon {
				font-size: 50rpx;
			}

			.search-name {
				margin-top: 5rpx;
				font-size: $im-font-size-smaller;
			}
		}
	}

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