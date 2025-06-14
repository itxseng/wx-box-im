<template>
	<view class="page chat-history-image none-pointer-events">
		<nav-bar title="图片与视频" back></nav-bar>
		<scroll-view v-if="messageMap.size>0" class="chat-message-box" scroll-y="true" upper-threshold="200"
			@scrolltolower="onScrollToBottom">
			<view v-for="[timeText, messages] of messageMap.entries()" :key="timeText">
				<view class="time-tip">{{timeText}}</view>
				<view class="chat-message-list">
					<view v-for="m in messages">
						<view class="chat-message" @longpress.prevent.stop="onLongPress(m)" @touchmove="onTouchMove"
							@touchend="onTouchEnd">
							<image v-if="m.type==$enums.MESSAGE_TYPE.IMAGE" class="chat-image" mode="aspectFill"
								:src="JSON.parse(m.content).thumbUrl" lazy-load="true" @click.stop="onShowFullImage(m)">
							</image>
							<video-player v-if="m.type==$enums.MESSAGE_TYPE.VIDEO" class="chat-video"
								:poster="JSON.parse(m.content).coverUrl" :url="JSON.parse(m.content).videoUrl">
							</video-player>
						</view>
					</view>
				</view>
			</view>
		</scroll-view>
		<no-data-tip v-else class="tip" :tip="noDataTip"></no-data-tip>
		<popup-menu ref="popMenu" :items="menuItems" @select="onSelectMenuItem"></popup-menu>
	</view>
</template>

<script>
export default {
	data() {
		return {
			chatIdx: -1,
			chat: {},
			showMaxIdx: 30,
			activeMessage: null,
			menuItems: [{
				key: 'LOCATE_MESSAGE',
				name: '在聊天中定位'
			}],
			isTouchMove: false
		}
	},
	methods: {
		onShowFullImage(m) {
			let imageUrl = JSON.parse(m.content).originUrl;
			uni.previewImage({
				urls: [imageUrl]
			})
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
		timeText(timeStamp) {
			let dateTime = new Date(timeStamp)
			if (this.$date.isWeek(dateTime)) {
				return '本周'
			} else if (this.$date.isMonth(dateTime)) {
				return '本月'
			} else {
				return this.$date.formatDateTime(dateTime).substr(0, 7);
			}
		}
	},
	computed: {
		messageMap() {
			let map = new Map();
			if (!this.chat.messages) {
				return map;
			}
			// 过滤出图片和视频消息
			let messages = this.chat.messages.filter(m => m.type == this.$enums.MESSAGE_TYPE.IMAGE || m.type == this
				.$enums.MESSAGE_TYPE.VIDEO).reverse().slice(0, this.showMaxIdx);
			// 按时间分组

			messages.forEach(m => {
				let timeText = this.timeText(m.sendTime);
				if (map.has(timeText)) {
					map.get(timeText).push(m);
				} else {
					map.set(timeText, [m]);
				}
			})
			return map;
		}
	},
	onLoad(options) {
		// 聊天数据
		this.chatIdx = options.chatIdx;
		this.chat = this.chatStore.chats[options.chatIdx];
	}
}
</script>

<style lang="scss" scoped>
.chat-history-image {
	display: flex;
	
	.chat-message-box {
		flex: 1;
		height: 100%;

		.time-tip {
			margin-top: 20rpx;
			text-align: left;
			padding: 10rpx;
			color: $im-text-color-light;
		}

		.chat-message-list {
			display: flex;
			flex-wrap: wrap;
			
			.chat-image {
				margin: 5rpx;
				width: 240rpx;
				height: 240rpx;
				border-radius: 10rpx;
				background: #333;
			}

			.chat-video {
				padding: 5rpx;
				width: 240rpx;
				height: 240rpx;
				overflow: hidden;

				.cover {
					width: 100% !important;
					height: 100% !important;
				}

				.play-btn {
					width: 75rpx !important;
					height: 75rpx !important;
				}

				.mp-video {
					width: 100% !important;
					height: 100% !important;
				}
			}
		}
	}

	.tip {
		width: 100%;
		flex: 1;
	}
}
</style>