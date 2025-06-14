<template>
	<view class="chat-message-item" :class="active?'active':''">
		<view class="message-tip" v-if="msgInfo.type == $enums.MESSAGE_TYPE.TIP_TEXT">
			{{ msgInfo.content }}
		</view>
		<view class="message-tip" v-else-if="msgInfo.type == $enums.MESSAGE_TYPE.TIP_TIME">
			{{ $date.toTimeText(msgInfo.sendTime) }}
		</view>
		<view class="message-normal" v-else-if="isNormal" :class="{ 'message-mine': msgInfo.selfSend }">
			<head-image class="avatar" @longpress.prevent="$emit('longPressHead')" :id="msgInfo.sendId" :url="headImage"
				:name="showName" size="small"></head-image>
			<view class="content">
				<view v-if="msgInfo.groupId && !msgInfo.selfSend" class="top">
					<view class="name">{{ showName }}</view>
					<uni-tag v-if="isGroupOwner(msgInfo.sendId)" text="群主" size="small" type="error"
						:inverted="true"></uni-tag>
					<uni-tag v-if="isGroupManager(msgInfo.sendId)" text="管理员" size="small" type="primary"
						:inverted="true"></uni-tag>
				</view>
				<view class="bottom">
					<view v-if="msgInfo.type == $enums.MESSAGE_TYPE.TEXT">
						<long-press-menu :items="menuItems" @select="onSelectMenu">
							<!-- rich-text支持显示表情，但是不支持点击a标签 -->
							<rich-text v-if="$emo.containEmoji(msgInfo.content)" class="message-text"
								:nodes="nodesText"></rich-text>
							<!-- up-parse支持点击a标签,但安卓打包后表情无法显示,原因未知 -->
							<up-parse v-else class="message-text" :showImgMenu="false" :content="nodesText"></up-parse>
						</long-press-menu>
					</view>
					<view class="message-image" v-if="msgInfo.type == $enums.MESSAGE_TYPE.IMAGE">
						<long-press-menu :items="menuItems" @select="onSelectMenu">
							<view class="image-box">
								<image class="send-image" mode="heightFix" :src="JSON.parse(msgInfo.content).thumbUrl"
									   lazy-load="true" @click.stop="onShowFullImage()">
								</image> 
								<loading v-if="loading"></loading>
							</view>
						</long-press-menu>
						<text title="发送失败" v-if="loadFail" @click="onSendFail"
							class="send-fail iconfont icon-warning-circle-fill"></text>
					</view>
					<view class="message-video" v-if="msgInfo.type == $enums.MESSAGE_TYPE.VIDEO">
						<long-press-menu :items="menuItems" @select="onSelectMenu">
							<view class="video-load-box">
								<video-player :poster="JSON.parse(msgInfo.content).coverUrl"
									:url="JSON.parse(msgInfo.content).videoUrl">
								</video-player>
								<loading v-if="loading"></loading>
							</view>
						</long-press-menu>
						<text title="发送失败" v-if="loadFail" @click="onSendFail"
							class="send-fail iconfont icon-warning-circle-fill"></text>
					</view>
					<view class="message-file" v-if="msgInfo.type == $enums.MESSAGE_TYPE.FILE">
						<long-press-menu :items="menuItems" @select="onSelectMenu">
							<view class="file-box">
								<view class="file-info">
									<uni-link class="file-name" :text="contentData.name" showUnderLine="true"
										color="#007BFF" :href="contentData.url"></uni-link>
									<view class="file-size">{{ fileSize }}</view>
								</view>
								<view class="file-icon iconfont icon-file"></view>
								<loading v-if="loading"></loading>
							</view>
						</long-press-menu>
						<text title="发送失败" v-if="loadFail" @click="onSendFail"
							class="send-fail iconfont icon-warning-circle-fill"></text>
					</view>
					<long-press-menu v-if="msgInfo.type == $enums.MESSAGE_TYPE.AUDIO" :items="menuItems"
						@select="onSelectMenu">
						<view class="message-audio message-text" @click="onPlayAudio()">
							<text class="iconfont icon-voice-play"></text>
							<text class="audio-text">{{ JSON.parse(msgInfo.content).duration + '"' }}</text>
							<text v-if="audioPlayState == 'PAUSE'" class="iconfont icon-play"></text>
							<text v-if="audioPlayState == 'PLAYING'" class="iconfont icon-pause"></text>
						</view>
					</long-press-menu>
					<long-press-menu v-if="msgInfo.type == $enums.MESSAGE_TYPE.USER_CARD" :items="menuItems"
						@select="onSelectMenu">
						<view class="message-card" @click="onShowUserInfo">
							<view class="card-body">
								<head-image :url="contentData.headImage" :name="contentData.nickName"
									radius="10%"></head-image>
								<view class="name">{{ contentData.nickName }}</view>
							</view>
							<view class="card-tip">个人名片</view>
						</view>
					</long-press-menu>
					<long-press-menu v-if="msgInfo.type == $enums.MESSAGE_TYPE.GROUP_CARD" :items="menuItems"
						@select="onSelectMenu">
						<view class="message-card" @click="onShowGroupInfo">
							<view class="card-body">
								<head-image :url="contentData.headImage" :name="contentData.groupName"
									radius="10%"></head-image>
								<view class="name">{{ contentData.groupName }}</view>
							</view>
							<view class="card-tip">群名片</view>
						</view>
					</long-press-menu>
					<long-press-menu v-if="isAction" :items="menuItems" @select="onSelectMenu">
						<view class="chat-realtime message-text" @click="$emit('call')">
							<text v-if="msgInfo.type == $enums.MESSAGE_TYPE.ACT_RT_VOICE"
								class="iconfont icon-chat-voice"></text>
							<text v-if="msgInfo.type == $enums.MESSAGE_TYPE.ACT_RT_VIDEO"
								class="iconfont icon-chat-video"></text>
							<text>{{ msgInfo.content }}</text>
						</view>
					</long-press-menu>
					<view class="quote-message" v-if="msgInfo.quoteMessage">
						<long-press-menu ref="quoteMenu" :items="quoteItems" @select="onSelectMenu">
							<chat-quote-message :msgInfo="msgInfo.quoteMessage" :showName="quoteShowName"
								@click.native="onClickQuoteMessage"></chat-quote-message>
						</long-press-menu>
					</view>
					<view class="message-status" v-if="!isAction">
						<text class="chat-readed" v-if="msgInfo.selfSend && !msgInfo.groupId
							&& msgInfo.status == $enums.MESSAGE_STATUS.READED">已读</text>
						<text class="chat-unread" v-if="msgInfo.selfSend && !msgInfo.groupId
							&& msgInfo.status != $enums.MESSAGE_STATUS.READED">未读</text>
					</view>
					<view class="chat-receipt" v-if="msgInfo.receipt" @click="onShowReadedBox">
						<text v-if="msgInfo.receiptOk" class="tool-icon iconfont icon-ok"></text>
						<text v-else>{{ msgInfo.readedCount }}人已读</text>
					</view>
				</view>
			</view>
		</view>
		<chat-group-readed ref="chatGroupReaded" :groupMembers="groupMembers" :msgInfo="msgInfo"></chat-group-readed>

	</view>
</template>

<script>
export default {
	name: "chat-message-item",
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
		},
		group: {
			type: Object
		},
		groupMembers: {
			type: Array
		},
		active: {
			type: Boolean,
			default: false
		}
	},
	data() {
		return {
			audioPlayState: 'STOP',
			innerAudioContext: null
		}
	},
	methods: {
		onSendFail() {
			uni.showToast({
				title: "该文件已发送失败，目前不支持自动重新发送，建议手动重新发送",
				icon: "none"
			})
		},
		onPlayAudio() {
			// 初始化音频播放器
			if (!this.innerAudioContext) {
				this.innerAudioContext = uni.createInnerAudioContext();
				let url = JSON.parse(this.msgInfo.content).url;
				this.innerAudioContext.src = url;
				this.innerAudioContext.onEnded((e) => {
					console.log('停止')
					this.audioPlayState = "STOP"
					this.emit();
				})
				this.innerAudioContext.onError((e) => {
					this.audioPlayState = "STOP"
					console.log("播放音频出错");
					console.log(e)
					this.emit();
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
			this.emit();
		},
		onShowUserInfo() {
			uni.navigateTo({
				url: "/pages/common/user-info?id=" + this.contentData.userId
			})
		},
		onShowGroupInfo() {
			uni.navigateTo({
				url: "/pages/group/group-info?id=" + this.contentData.groupId
			})
		},
		onSelectMenu(item) {
			// 菜单id转驼峰作为事件key
			let eventKey = item.key.toLowerCase().replace(/_([a-z])/g, (g) => g[1].toUpperCase());
			this.$emit(eventKey, this.msgInfo);
		},
		onShowFullImage() {
			let imageUrl = JSON.parse(this.msgInfo.content).originUrl;
			uni.previewImage({
				urls: [imageUrl]
			})
		},
		onShowReadedBox() {
			this.$refs.chatGroupReaded.open();
		},
		onClickQuoteMessage() {
			if (!this.$refs.quoteMenu.isShowMenu) {
				this.$emit('locateQuote', this.msgInfo)
			}
		},
		emit() {
			this.$emit("audioStateChange", this.audioPlayState, this.msgInfo);
		},
		stopPlayAudio() {
			if (this.innerAudioContext) {
				this.innerAudioContext.stop();
				this.innerAudioContext = null;
				this.audioPlayState = "STOP"
			}
		},
		isGroupOwner(userId) {
			return this.group.ownerId == userId;
		},
		isGroupManager(userId) {
			let m = this.groupMembers.find(m => m.userId == userId);
			return m && m.isManager
		}
	},
	computed: {
		loading() {
			return this.msgInfo.loadStatus && this.msgInfo.loadStatus === "loading";
		},
		loadFail() {
			return this.msgInfo.loadStatus && this.msgInfo.loadStatus === "fail";
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
		menuItems() {
			let items = [];
			if (this.msgInfo.type == this.$enums.MESSAGE_TYPE.TEXT) {
				items.push({
					key: 'COPY',
					name: '复制'
				});
			}
			if (this.msgInfo.id) {
				if (this.msgInfo.selfSend) {
					items.push({
						key: 'RECALL',
						name: '撤回'
					});
				}
				if (this.$msgType.isNormal(this.msgInfo.type)) {
					items.push({
						key: 'QUOTE',
						name: '引用'
					});
				}
				if (this.$msgType.isNormal(this.msgInfo.type)) {
					items.push({
						key: 'FORWARD',
						name: '转发'
					});
				}
				if ((this.isOwner || this.isManager) && this.$msgType.isNormal(this.msgInfo.type)) {
					items.push({
						key: 'TOP',
						name: '置顶'
					});
				}
			}
			items.push({
				key: 'DELETE',
				name: '删除',
				color: '#e64e4e'
			});
			if (this.msgInfo.type == this.$enums.MESSAGE_TYPE.FILE) {
				items.push({
					key: 'DOWNLOAD',
					name: '下载并打开',
				});
			}
			return items;
		},
		quoteItems() {
			let items = [];
			if (this.msgInfo.quoteMessage &&
				this.msgInfo.quoteMessage.status != this.$enums.MESSAGE_STATUS.RECALL) {
				items.push({
					key: 'LOCATE_QUOTE',
					name: '定位到原消息'
				});
			}
			return items;
		},
		isAction() {
			return this.$msgType.isAction(this.msgInfo.type);
		},
		isNormal() {
			const type = this.msgInfo.type;
			return this.$msgType.isNormal(type) || this.$msgType.isAction(type)
		},
		isOwner() {
			let userId = this.userStore.userInfo.id;
			return this.group && userId == this.group.ownerId
		},
		isManager() {
			let userId = this.userStore.userInfo.id;
			let m = this.groupMembers.find((m) => m.userId == userId);
			return m && m.isManager;
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
.chat-message-item {
	padding: 15rpx 20rpx;
	border-radius: 20rpx;

	&.active {
		background: $im-color-primary-light-9;
	}

	.message-tip {
		line-height: 60rpx;
		text-align: center;
		color: $im-text-color-lighter;
		font-size: $im-font-size-smaller-extra;
	}

	.message-normal {
		position: relative;
		padding-left: 110rpx;
		min-height: 80rpx;

		.avatar {
			position: absolute;
			top: 0;
			left: 0;
		}

		.content {
			text-align: left;

			.top {
				display: flex;
				flex-wrap: nowrap;
				align-items: center;

				.name {
					color: $im-text-color-light;
					font-size: $im-font-size-smaller;
					line-height: $im-font-size-smaller;
					height: $im-font-size-smaller;
				}

				.uni-tag {
					border: 0;
					opacity: 0.75;
				}
			}

			.bottom {
				display: inline-block;
				padding-right: 80rpx;
				margin-top: 10rpx;

				.message-text {
					position: relative;
					line-height: 1.6;
					padding: 16rpx 24rpx;
					background-color: $im-bg;
					border-radius: 20rpx;
					color: $im-text-color;
					font-size: $im-font-size;
					text-align: left;
					display: inline-flex;
					word-break: break-all;
					white-space: pre-line;
					overflow: visible;

					&:after {
						content: "";
						position: absolute;
						left: -18rpx;
						top: 26rpx;
						width: 6rpx;
						height: 6rpx;
						border-style: solid dashed dashed;
						border-color: $im-bg transparent transparent;
						border-width: 18rpx;
					}
				}

				.send-fail {
					color: #e60c0c;
					font-size: 50rpx;
					cursor: pointer;
					margin: 0 20rpx;
				}

				.message-image {
					display: flex;
					flex-wrap: nowrap;
					flex-direction: row;
					align-items: center;

					.image-box {
						position: relative;

						.send-image {
							min-width: 200rpx;
							max-width: 420rpx;
							cursor: pointer;
							border-radius: 8rpx;
						}
					}
				}

				.message-video {
					display: flex;
					flex-wrap: nowrap;
					flex-direction: row;
					align-items: center;

					.video-load-box {
						position: relative;
					}
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
						min-height: 120rpx;
						border-radius: 8rpx;
						padding: 10rpx 30rpx;
						box-shadow: $im-box-shadow-dark;

						.file-info {
							flex: 1;
							height: 100%;
							text-align: left;
							font-size: 28rpx;
							width: 300rpx;

							.file-name {
								font-weight: 600;
								margin-bottom: 30rpx;
								word-break: break-all;
							}
						}

						.file-icon {
							font-size: 80rpx;
							color: #d42e07;
						}
					}
				}

				.message-audio {
					display: flex;
					align-items: center;

					.audio-text {
						padding-right: 15rpx;
					}

					.icon-voice-play {
						font-size: 34rpx;
						padding-right: 16rpx;
					}
				}

				.message-card {
					display: flex;
					flex-direction: column;
					flex-wrap: nowrap;
					border-radius: 10rpx;
					padding: 10rpx 20rpx;
					height: 160rpx;
					width: 320rpx;
					box-shadow: $im-box-shadow-dark;

					.card-body {
						flex: 1;
						display: flex;
						align-items: center;
						border-bottom: 3rpx solid #eee;

						.name {
							margin-left: 20rpx;
							font-weight: 600;
							font-size: $im-font-size;
							white-space: nowrap;
							overflow: hidden;
							text-overflow: ellipsis;
						}
					}

					.card-tip {
						margin-top: 6rpx;
						color: $im-text-color-light;
						font-size: $im-font-size-smaller-extra;
						text-align: left;
					}
				}

				.chat-realtime {
					display: flex;
					align-items: center;

					.iconfont {
						font-size: 40rpx;
						padding-right: 16rpx;
					}
				}

				.message-status {
					line-height: $im-font-size-smaller-extra;
					font-size: $im-font-size-smaller-extra;
					margin-top: 5rpx;

					.chat-readed {
						display: block;
						padding-top: 2rpx;
						color: $im-text-color-lighter;
					}

					.chat-unread {
						color: $im-color-danger;
					}
				}

				.chat-receipt {
					font-size: $im-font-size-smaller;
					color: $im-text-color-lighter;
					font-weight: 600;

					.icon-ok {
						font-size: 20px;
						color: $im-color-success;
					}
				}
			}
		}


		&.message-mine {
			text-align: right;
			padding-left: 0;
			padding-right: 110rpx;

			.avatar {
				left: auto;
				right: 0;
			}

			.content {
				text-align: right;

				.bottom {
					padding-left: 80rpx;
					padding-right: 0;

					.message-text {
						margin-left: 10px;
						background-color: $im-color-primary-light-2;
						color: #fff;

						&:after {
							left: auto;
							right: -15rpx;
							border-top-color: $im-color-primary-light-2;
						}
					}

					.message-image {
						flex-direction: row-reverse;
					}

					.message-video {
						flex-direction: row-reverse;
					}

					.message-file {
						flex-direction: row-reverse;
					}

					.message-audio {
						flex-direction: row-reverse;

						.audio-text {
							padding-right: 0;
							padding-left: 8px;
						}

						.icon-voice-play {
							transform: rotateY(180deg);
						}
					}

					.chat-realtime {
						display: flex;
						flex-direction: row-reverse;

						.iconfont {
							transform: rotateY(180deg);
						}
					}
				}
			}
		}
	}
}
</style>