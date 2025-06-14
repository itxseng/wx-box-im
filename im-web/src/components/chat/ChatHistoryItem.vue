<template>
	<div class="chat-history-item " :class="active ? 'active' : ''">
		<div class="message-normal">
			<div class="avatar">
				<head-image :name="showName" :size="38" :url="headImage" :id="msgInfo.sendId"></head-image>
			</div>
			<div class="content">
				<div class="top">
					<span>{{ showName }}</span>
					<span>{{ $date.toTimeText(msgInfo.sendTime) }}</span>
				</div>
				<div class="bottom">
					<div ref="chatMsgBox">
						<span class="message-text" v-if="msgInfo.type == $enums.MESSAGE_TYPE.TEXT"
							v-html="htmlText"></span>
						<div class="message-image" v-if="msgInfo.type == $enums.MESSAGE_TYPE.IMAGE">
							<div v-loading="loading" element-loading-text="上传中.."
								element-loading-background="rgba(0, 0, 0, 0.4)">
								<img class="send-image" :src="JSON.parse(msgInfo.content).thumbUrl"
									@click="showFullImageBox()" loading="lazy" />
							</div>
						</div>
						<div class="message-video" v-if="msgInfo.type == $enums.MESSAGE_TYPE.VIDEO">
							<video ref="videoPlayer" class="send-video" controls preload="none"
								:poster="JSON.parse(msgInfo.content).coverUrl"
								:src="JSON.parse(msgInfo.content).videoUrl" />
						</div>
						<div class="message-file" v-if="msgInfo.type == $enums.MESSAGE_TYPE.FILE">
							<div class="chat-file-box" v-loading="loading">
								<div class="chat-file-info">
									<el-link class="chat-file-name" :underline="true" target="_blank" type="primary"
										:href="contentData.url" :download="contentData.name">{{ contentData.name
										}}</el-link>
									<div class="chat-file-size">{{ fileSize }}</div>
								</div>
								<div class="chat-file-icon">
									<span type="primary" class="el-icon-document"></span>
								</div>
							</div>
						</div>
					</div>
					<chat-user-card v-if="msgInfo.type == $enums.MESSAGE_TYPE.USER_CARD"
						:cardInfo="contentData"></chat-user-card>
					<chat-group-card v-if="msgInfo.type == $enums.MESSAGE_TYPE.GROUP_CARD"
						:cardInfo="contentData"></chat-group-card>
					<div class="message-voice" v-if="msgInfo.type == $enums.MESSAGE_TYPE.AUDIO" @click="onPlayVoice()">
						<audio controls :src="JSON.parse(msgInfo.content).url"></audio>
					</div>
					<div class="quote-message" v-if="msgInfo.quoteMessage">
						<chat-quote-message :msgInfo="msgInfo.quoteMessage"
							:showName="quoteShowName"></chat-quote-message>
					</div>
				</div>
			</div>
		</div>
	</div>
</template>

<script>
import HeadImage from "../common/HeadImage.vue";
import ChatQuoteMessage from "./ChatQuoteMessage.vue";
import ChatUserCard from "./ChatUserCard.vue";
import ChatGroupCard from "./ChatGroupCard.vue";

export default {
	name: "chatHistoryItem",
	components: {
		HeadImage,
		ChatQuoteMessage,
		ChatUserCard,
		ChatGroupCard
	},
	props: {
		active: {
			type: Boolean,
			default: false
		},
		headImage: {
			type: String,
			default: ''
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
	methods: {
		showFullImageBox() {
			let imageUrl = JSON.parse(this.msgInfo.content).originUrl;
			if (imageUrl) {
				this.$eventBus.$emit("openFullImage", imageUrl);
			}
		},
		onPlayVoice() {
			if (!this.audio) {
				this.audio = new Audio();
			}
			this.audio.src = JSON.parse(this.msgInfo.content).url;
			this.audio.play();
			this.onPlayVoice = 'RUNNING';
		}
	},
	computed: {
		loading() {
			return this.msgInfo.loadStatus && this.msgInfo.loadStatus === "loading";
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
		htmlText() {
			let text = this.$url.replaceURLWithHTMLLinks(this.msgInfo.content, '')
			return this.$emo.transform(text, 'emoji-normal')
		}
	}
}
</script>

<style lang="scss">
.chat-history-item {
	padding: 2px 8px;
	width: 620px;
	cursor: pointer;
	border-radius: 10px;
	margin-right: 20px;

	&:hover {
		background: #f4f4f4;
	}

	&.active {
		background: #E1EAF7;
	}

	.message-normal {
		position: relative;
		font-size: 0;
		padding-left: 48px;
		min-height: 50px;
		margin: 5px 0;

		.avatar {
			position: absolute;
			width: 40px;
			height: 40px;
			top: 0;
			left: 0;
		}

		.content {
			text-align: left;

			.top {
				display: flex;
				flex-wrap: nowrap;
				color: var(--im-text-color-light);
				font-size: var(--im-font-size);
				line-height: 20px;

				span {
					margin-right: 12px;
				}
			}

			.bottom {
				padding-right: 100px;
				margin-top: 1px;

				.message-text {
					display: inline-block;
					position: relative;
					line-height: 26px;
					border-radius: 10px;
					font-size: var(--im-font-size);
					text-align: left;
					white-space: pre-wrap;
					word-break: break-all;
				}

				.message-image {
					display: flex;
					flex-wrap: nowrap;
					flex-direction: row;
					align-items: center;

					.send-image {
						min-width: 100px;
						min-height: 75px;
						max-width: 200px;
						max-height: 150px;
						border-radius: 8px;
					}
				}

				.message-video {
					display: flex;
					flex-wrap: nowrap;
					flex-direction: row;
					align-items: center;

					.send-video {
						min-width: 100px;
						min-height: 75px;
						max-width: 200px;
						max-height: 150px;
						border-radius: 8px;
						overflow: hidden;
						object-fit: contain;
					}
				}

				.message-file {
					display: flex;
					flex-wrap: nowrap;
					flex-direction: row;
					align-items: center;
					margin-bottom: 2px;

					.chat-file-box {
						display: flex;
						flex-wrap: nowrap;
						align-items: center;
						box-shadow: var(--im-box-shadow-light);
						border-radius: 4px;
						padding: 8px 15px;

						.chat-file-info {
							flex: 1;
							height: 100%;
							text-align: left;
							font-size: 14px;
							margin-right: 10px;

							.chat-file-name {
								display: inline-block;
								min-width: 160px;
								max-width: 400px;
								font-size: 14px;
								margin-bottom: 4px;
								white-space: pre-wrap;
								word-break: break-all;
							}

							.chat-file-size {
								font-size: var(--im-font-size-smaller);
								color: var(--im-text-color-light);
							}
						}

						.chat-file-icon {
							font-size: 34px;
							color: #d42e07;
						}
					}
				}

				.message-voice audio {
					height: 45px;
				}

				.quote-message {
					display: block;
					margin-top: 3px;
					cursor: pointer;
				}
			}
		}
	}
}
</style>