<template>
	<uni-popup ref="popup" type="bottom">
		<view class="chat-selector">
			<view class="top-bar">
				<view class="tip">{{title}}</view>
				<button class="btn" type="warn" size="mini" @click="onClean()">清空 </button>
				<button class="btn" type="primary" size="mini" @click="onOk()">确定({{ checkedChats.length }})</button>
			</view>
			<view class="checked-items-box" v-if="checkedChats.length > 0">
				<scroll-view scroll-x="true" scroll-left="120">
					<view class="checked-items">
						<view v-for="(chat,idx) in checkedChats" class="checked-item" :key="idx">
							<head-image :name="chat.showName" :url="chat.headImage" :size="60"></head-image>
						</view>
					</view>
				</scroll-view>
			</view>
			<view class="search-bar">
				<uni-search-bar v-model="searchText" cancelButton="none" placeholder="搜索"></uni-search-bar>
			</view>
			<view class="chat-items">
				<virtual-scroller :items="showChats">
					<template v-slot:top>
						<view class="tool-bar" @click="onNewChat()">
							<head-image size="small" url="/static/image/new_chat.png" :size="90"
								radius="10%"></head-image>
							<text class="title">创建新的聊天</text>
							<uni-icons class="arrow" type="right" size="18"></uni-icons>
						</view>
					</template>
					<template v-slot="{ item }">
						<view class="chat-item" @click="onSwitchChecked(item)">
							<head-image :name="item.showName" :url="item.headImage" :size="90"></head-image>
							<view class="chat-info">
								<view class="chat-name">{{ item.showName }}</view>
								<view class="chat-content">
									<rich-text :nodes="$emo.transform(item.lastContent,'emoji-small')"></rich-text>
								</view>
							</view>
							<view class="chat-checked">
								<radio :checked="isChecked(item)" @click.stop="onSwitchChecked(item)" />
							</view>
						</view>
					</template>
				</virtual-scroller>
			</view>
		</view>
	</uni-popup>
	<contact-selector ref="contactSel"></contact-selector>
</template>


<script>
export default {
	name: "chat-selector",
	props: {
		title: {
			type: String,
			default: '选择最近联系人'
		}
	},
	data() {
		return {
			searchText: "",
			checkedChats: [],
			callback: null,
		}
	},
	methods: {
		open(callback) {
			this.callback = callback;
			this.searchText = '';
			this.checkedChats = [];
			this.$refs.popup.open();
		},
		onSwitchChecked(chat) {
			let idx = this.checkedChats.indexOf(chat);
			if (idx >= 0) {
				this.checkedChats.splice(idx, 1)
			} else {
				this.checkedChats.push(chat)
			}
		},
		onNewChat() {
			this.$refs.contactSel.open(data => {
				this.checkedChats = [];
				// 好友
				data.friends.forEach(friend => {
					let chat = {
						type: 'PRIVATE',
						targetId: friend.id,
						showName: friend.showNickName,
						headImage: friend.headImage,
					};
					this.checkedChats.push(chat)
				})
				// 群聊
				data.groups.forEach(group => {
					let chat = {
						type: 'GROUP',
						targetId: group.id,
						showName: group.showGroupName,
						headImage: group.headImage,
					};
					this.checkedChats.push(chat)
				})
				this.onOk();
			});
		},
		onClean() {
			this.checkedChats = [];
		},
		onOk() {
			this.$refs.popup.close();
			this.callback(this.checkedChats);
		},
		isChecked(chat) {
			return this.checkedChats.indexOf(chat) >= 0;
		},
	},
	computed: {
		showChats() {
			return this.chatStore.chats.filter(chat => {
				if (chat.showName.includes(this.searchText)) {
					// 过滤掉掉已经删除好友关系或者退群的会话
					if (chat.type == 'PRIVATE') {
						return this.friendStore.isFriend(chat.targetId)
					}
					if (chat.type == 'GROUP') {
						return this.groupStore.isGroup(chat.targetId)
					}
				}
				return false;
			})
		}
	}
}
</script>


<style lang="scss" scoped>
.chat-selector {
	position: relative;
	display: flex;
	flex-direction: column;
	background-color: white;
	border-radius: 15rpx 15rpx 0 0;
	overflow: hidden;

	.top-bar {
		display: flex;
		align-items: center;
		height: 70rpx;
		padding: 20rpx;

		.tip {
			flex: 1;
		}

		.btn {
			margin-left: 10rpx;
		}
	}

	.checked-items-box {
		padding: 0 20rpx;

		.checked-items {
			display: flex;
			align-items: center;
			height: 90rpx;

			.checked-item {
				padding: 3rpx;
			}
		}
	}


	.chat-items {
		position: relative;
		flex: 1;
		overflow: hidden;
		background-color: $im-bg;

		.tool-bar {
			width: 100%;
			height: 120rpx;
			font-size: $im-font-size;
			color: $im-text-color;
			padding: 0 20rpx;
			display: flex;
			align-items: center;
			box-sizing: border-box;
			font-weight: 600;
			background: white;

			.title {
				flex: 1;
				padding-left: 20rpx;
			}

			.arrow {
				width: 60rpx;
				text-align: center;
			}
		}

		.tip {
			color: $im-text-color-light;
			font-size: $im-font-size-small;
			padding: 10rpx;
		}

		.chat-item {
			height: 120rpx;
			display: flex;
			position: relative;
			padding: 0 20rpx;
			align-items: center;
			white-space: nowrap;
			background: white;
			margin-top: 2rpx;
			border-radius: 10rpx;

			&:hover {
				background: $im-bg;
			}

			.chat-info {
				display: flex;
				flex: 1;
				flex-direction: column;
				justify-content: center;
				padding-left: 20rpx;
				text-align: left;
				overflow: hidden;

				.chat-name-text {
					flex: 1;
					font-size: $im-font-size-large;
					white-space: nowrap;
					overflow: hidden;
					display: flex;
					align-items: center;
				}

				.chat-content {
					display: flex;
					font-size: $im-font-size-smaller;
					color: $im-text-color-lighter;
					padding-top: 8rpx;
					align-items: center;
				}
			}
		}
	}
}
</style>