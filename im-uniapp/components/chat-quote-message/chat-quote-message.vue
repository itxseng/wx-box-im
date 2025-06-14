<template>
	<view class="chat-quote-message">
		<view v-if="isRecall">{{msgInfo.content}}</view>
		<view v-if="!isRecall" class="send-user">
			{{ showName + ":" }}
		</view>
		<view v-if="!isRecall" class="quote-content">
			<view v-if="msgInfo.type == $enums.MESSAGE_TYPE.TEXT">
				<rich-text class="message-text" :nodes="$emo.transform(msgInfo.content,'emoji-normal')"></rich-text>
			</view>
			<view v-if="msgInfo.type == $enums.MESSAGE_TYPE.IMAGE">
				<image class="message-image" mode="heightFix" :src="contentData.thumbUrl" lazy-load="true"
					@click.prevent.stop="onShowFullImage()">
				</image>
			</view>
			<view v-if="msgInfo.type == $enums.MESSAGE_TYPE.VIDEO">
				<video-player size="small" :poster="contentData.coverUrl" :url="contentData.videoUrl">
				</video-player>
			</view>
			<view class="message-file" v-if="msgInfo.type == $enums.MESSAGE_TYPE.FILE">
				<view class="file-box">
					<view class="file-info">
						<uni-link class="file-name" :text="contentData.name" showUnderLine="true" color="#007BFF"
							:href="contentData.url"></uni-link>
						<view class="file-size">{{ fileSize }}</view>
					</view>
					<view class="file-icon iconfont icon-file"></view>
				</view>
			</view>
			<view class="message-card" v-if="msgInfo.type == $enums.MESSAGE_TYPE.USER_CARD">
				<view class="card-body">
					<head-image :url="contentData.headImage" :name="contentData.nickName" radius="10%"
						size="mini"></head-image>
					<view class="name">{{ contentData.nickName }}</view>
				</view>
				<view class="card-tip">个人名片</view>
			</view>
			<view class="message-card" v-if="msgInfo.type == $enums.MESSAGE_TYPE.GROUP_CARD">
				<view class="card-body">
					<head-image :url="contentData.headImage" :name="contentData.groupName" radius="10%"
						size="mini"></head-image>
					<view class="name">{{ contentData.groupName }}</view>
				</view>
				<view class="card-tip">群名片</view>
			</view>
			<view class="message-audio" v-if="msgInfo.type == $enums.MESSAGE_TYPE.AUDIO"
				@click.prevent.stop="onPlayAudio()">
				<text class="iconfont icon-voice-play"></text>
				<text class="audio-text">{{ contentData.duration + '"' }}</text>
				<text v-if="audioPlayState == 'PAUSE'" class="iconfont icon-play"></text>
				<text v-if="audioPlayState == 'PLAYING'" class="iconfont icon-pause"></text>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	name: "chat-quote-message",
	props: {
		showName: {
			type: String,
			required: true
		},
		msgInfo: {
			type: Object,
			required: true
		}
	},
	data() {
		return {
			audioPlayState: 'STOP',
			innerAudioContext: null
		};
	},
	methods: {
		onShowFullImage() {
			let imageUrl = this.contentData.originUrl;
			uni.previewImage({
				urls: [imageUrl]
			})
		},
		onPlayAudio() {
			// 初始化音频播放器
			if (!this.innerAudioContext) {
				this.innerAudioContext = uni.createInnerAudioContext();
				let url = JSON.parse(this.msgInfo.content).url;
				this.innerAudioContext.src = url;
				this.innerAudioContext.onEnded((e) => {
					this.audioPlayState = "STOP"
					this.emit();
				})
				this.innerAudioContext.onError((e) => {
					this.audioPlayState = "STOP"
				});
			}
			if (this.audioPlayState == 'STOP') {
				this.innerAudioContext.play();
				this.audioPlayState = "PLAYING";
			} else if (this.audioPlayState == 'PLAYING') {
				this.innerAudioContext.pause();
				this.audioPlayState = "PAUSE"
			} else if (this.audioPlayState == 'PAUSE') {
				this.innerAudioContext.play();
				this.audioPlayState = "PLAYING"
			}
		},
	},
	computed: {
		quoteMessageText() {
			return this.msgInfo.content + "hello";
		},
		contentData() {
			return JSON.parse(this.msgInfo.content)
		},
		fileSize() {
			let size = this.contentData.size;
			if (size > 1024 * 1024) {
				return Math.round(size / 1024 / 1024) + "M";
			}
			if (size > 1024) {
				return Math.round(size / 1024) + "KB";
			}
			return size + "B";
		},
		isRecall() {
			return this.msgInfo.status == this.$enums.MESSAGE_STATUS.RECALL
		}
	}
}
</script>

<style lang="scss" scoped>
.chat-quote-message {
	background: #eee;
	padding: 10rpx;
	display: inline-flex;
	align-items: center;
	border-radius: 10rpx;
	font-size: $im-font-size-smaller;
	color: $im-text-color-lighter;
	margin-top: 8rpx;

	.send-user {
		margin-right: 20rpx;
		font-weight: 600;
		white-space: nowrap;
	}

	.quote-content {
		.message-text {
			position: relative;
			line-height: 1.6;
			text-align: left;
			display: block;
			word-break: break-all;
			white-space: pre-line;
		}

		.message-image {
			min-width: 50rpx;
			max-width: 100rpx;
			height: 80rpx;
			cursor: pointer;
			border-radius: 4rpx;
		}

		.message-file {
			display: flex;
			flex-wrap: nowrap;
			flex-direction: row;
			align-items: center;
			cursor: pointer;

			.file-box {
				position: relative;
				display: flex;
				flex-wrap: nowrap;
				align-items: center;
				min-height: 80rpx;

				.file-info {
					flex: 1;
					height: 100%;
					text-align: left;
					font-size: 24rpx;
					width: 300rpx;

					.file-name {
						font-weight: 600;
						margin-bottom: 30rpx;
						word-break: break-all;
					}
				}

				.file-icon {
					font-size: 50rpx;
					color: #d42e07;
				}
			}
		}

		.message-audio {
			display: flex;
			align-items: center;

			.audio-text {
				padding-right: 16rpx;
			}

			.icon-voice-play {
				font-size: 28rpx;
				padding-right: 16rpx;
			}
		}

		.message-card {
			display: flex;
			flex-direction: column;
			flex-wrap: nowrap;
			border-radius: 5rpx;
			padding: 5rpx 10rpx;
			height: 110rpx;
			width: 220rpx;
			background: white;
			box-shadow: $im-box-shadow-dark;

			.card-body {
				flex: 1;
				display: flex;
				align-items: center;
				border-bottom: 3rpx solid #eee;

				.name {
					margin-left: 10rpx;
					font-weight: 600;
					font-size: $im-font-size-smaller;
					color: $im-text-color;
					white-space: nowrap;
					overflow: hidden;
					text-overflow: ellipsis;
				}
			}

			.card-tip {
				margin-top: 3rpx;
				color: $im-text-color-light;
				font-size: 20rpx;
				text-align: left;
			}
		}
	}
}
</style>