<template>
	<view class="chat-history-item">
		<view class="chat-message">
			<head-image class="avatar" :id="msgInfo.sendId" :url="headImage" :name="showName" size="small"></head-image>
			<view class="chat-content">
				<view class="chat-top">
					<view>{{ showName }}</view>
					<view class="chat-time">{{ $date.toTimeText(msgInfo.sendTime,true) }}</view>
				</view>
				<view class="chat-bottom">
					<view v-if="msgInfo.type == $enums.MESSAGE_TYPE.TEXT">
						<rich-text class="chat-text" :nodes="nodesText"></rich-text>
					</view>
					<view v-if="msgInfo.type == $enums.MESSAGE_TYPE.FILE">
						<view class="chat-text">[文件] {{data.name}}</view>
					</view>
					<view class="quote-message" v-if="msgInfo.quoteMessage">
						<chat-quote-message :msgInfo="msgInfo.quoteMessage"
							:showName="quoteShowName"></chat-quote-message>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	name: "chat-history-item",
	props: {
		headImage: {
			type: String,
			required: true
		},
		showName: {
			type: String,
			required: true
		},
		quoteShowName: {
			type: String,
			default: ''
		},
		msgInfo: {
			type: Object,
			required: true
		}
	},
	data() {
		return {}
	},
	methods: {},
	computed: {
		data() {
			return JSON.parse(this.msgInfo.content)
		},
		fileSize() {
			let size = this.data.size;
			if (size > 1024 * 1024) {
				return Math.round(size / 1024 / 1024) + "M";
			}
			if (size > 1024) {
				return Math.round(size / 1024) + "KB";
			}
			return size + "B";
		},
		nodesText() {
			let color = this.msgInfo.selfSend ? 'white' : '';
			let text = this.$url.replaceURLWithHTMLLinks(this.msgInfo.content, color)
			return this.$emo.transform(text, 'emoji-normal')
		}
	}
}
</script>

<style scoped lang="scss">
.chat-history-item {
	padding: 15rpx 20rpx;
	margin-bottom: 3rpx;
	background: white;

	&:hover {
		background: #f4f4f4;
	}

	.chat-message {
		position: relative;
		padding-left: 110rpx;
		min-height: 80rpx;

		.avatar {
			position: absolute;
			top: 0;
			left: 0;
		}

		.chat-content {
			text-align: left;

			.chat-top {
				display: flex;
				flex-wrap: nowrap;
				color: $im-text-color-lighter;
				font-size: $im-font-size-smaller;
				line-height: $im-font-size-smaller;
				height: $im-font-size-smaller;

				.chat-time {
					flex: 1;
					text-align: right;
				}
			}

			.chat-bottom {
				display: inline-block;
				margin-top: 5rpx;

				.chat-text {
					position: relative;
					line-height: 1.6;
					margin-top: 10rpx;
					border-radius: 20rpx;
					color: $im-text-color;
					font-size: $im-font-size;
					text-align: left;
					display: inline-flex;
					word-break: break-all;
					white-space: pre-line;
					overflow: visible;
				}
			}
		}
	}
}
</style>