<template>
	<view class="page chat-system">
		<nav-bar title="系统通知" back></nav-bar>
		<view class="chat-msg">
			<scroll-view class="scroll-box" scroll-y="true" upper-threshold="200"
				:scroll-into-view="'chat-item-' + scrollMsgIdx">
				<view v-if="chat" v-for="(msgInfo, idx) in chat.messages" :key="idx" :id="'chat-item-' + idx">
					<view class="message-box" @click.native="onClickMessage(msgInfo)"
						v-if="msgInfo.type == $enums.MESSAGE_TYPE.SYSTEM_MESSAGE">
						<view class="title">{{ msgInfo.title }}</view>
						<image class="cover" v-show="msgInfo.coverUrl" :src="msgInfo.coverUrl"  />
						<view class="intro">{{ msgInfo.intro }}</view>
						<view class="bottom-bar">查看详情</view>
					</view>
					<view class="chat-msg-tip" v-if="msgInfo.type == $enums.MESSAGE_TYPE.TIP_TIME">
						{{ $date.toTimeText(msgInfo.sendTime) }}
					</view>
				</view>
			</scroll-view>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			chat: {},
			scrollMsgIdx: 0, // 滚动条定位为到哪条消息
		}
	},
	methods: {
		onClickMessage(msgInfo) {
			uni.navigateTo({
				url: `/pages/chat/chat-system-content?id=${msgInfo.id}&title=${msgInfo.title}`
			})
		},
		readedMessage() {
			console.log("readedMessage")
			if (this.chat.unreadCount == 0) {
				return;
			}
			let maxSeqNo = this.chatStore.systemMsgMaxSeqNo;
			let url = `/message/system/readed?maxSeqNo=${maxSeqNo}`
			this.$http({
				url: url,
				method: 'PUT'
			})
		},
		scrollToBottom() {
			this.$nextTick(() => {
				let size = this.chat.messages.length;
				if (size > 0) {
					console.log("scrollToBottom")
					this.scrollMsgIdx = size - 1;
				}
			});
		}
	},
	onLoad(options) {
		// 聊天数据
		this.chat = this.chatStore.chats[options.chatIdx];
		this.scrollToBottom();
		this.readedMessage();
	}
}
</script>

<style lang="scss" scoped>
.chat-system {
	position: relative;
	display: flex;
	flex-direction: column;

	.chat-msg {
		flex: 1;
		padding: 0;
		overflow: hidden;
		position: relative;
		background-color: #f6f6f6;
		padding: 20rpx;

		.scroll-box {
			height: 100%;
		}

		.message-box {
			background-color: white;
			text-align: left;
			border-radius: 3%;
			margin: 0 20rpx 50rpx 20rpx;
			padding: 5rpx 20rpx;
			cursor: pointer;

			.title {
				text-align: center;
				font-size: $im-font-size	;
				padding: 15rpx;
				font-weight: 600;
				height: 50rpx;
				line-height: 50rpx;
				overflow: hidden;
			}

			.cover {
				width: 100%;
				height: 350rpx;
				object-fit: fill;
				border-bottom: 1px #eee solid;
			}

			.intro {
				padding: 16rpx;
				font-size: 32rpx;
				border-bottom: 1px #eee solid;
				overflow-wrap: break-word; 
			}

			.bottom-bar {
				font-size: 26rpx;
				padding: 16rpx;
				text-align: left;
				color: blue;
				height: 40rpx;
			}
		}

		.chat-msg-tip {
			line-height: 60rpx;
			text-align: center;
			color: #555;
			font-size: 24rpx;
			padding: 10rpx;
		}
	}

}
</style>