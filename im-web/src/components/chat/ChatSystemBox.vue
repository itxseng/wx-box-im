<template>
	<div class="chat-system-box">
		<el-container>
			<el-header height="50px">
				<span>{{ chat.showName }}</span>
			</el-header>
			<el-main class="main" id="chatScrollBox" @scroll="onScroll">
				<div v-for="(msgInfo, idx) in chat.messages" :key="idx">
					<div v-if="idx >= showMinIdx">
						<div class="message-box" @click="onClickMessage(msgInfo)"
							v-if="msgInfo.type == $enums.MESSAGE_TYPE.SYSTEM_MESSAGE">
							<div class="title">{{ msgInfo.title }}</div>
							<img class="cover" v-show="msgInfo.coverUrl" :src="msgInfo.coverUrl" />
							<div class="intro">{{ msgInfo.intro }}</div>
							<div class="bottom-bar">查看详情</div>
						</div>
						<div class="message-tip" v-if="msgInfo.type == $enums.MESSAGE_TYPE.TIP_TIME">
							{{ $date.toTimeText(msgInfo.sendTime) }}
						</div>
					</div>
				</div>
			</el-main>
			<chat-system-content ref="contentRef"></chat-system-content>
		</el-container>
	</div>
</template>

<script>
import ChatSystemContent from './ChatSystemContent';

export default {
	name: "chatSystem",
	components: { ChatSystemContent },
	props: {
		chat: {
			type: Object
		}
	},
	data() {
		return {
			showMinIdx: 5
		}
	},
	methods: {
		onClickMessage(msgInfo) {
			this.$refs.contentRef.show(msgInfo.id, msgInfo.title);
		},
		readedMessage() {
			if (this.chat.unreadCount == 0) {
				return;
			}
			let maxSeqNo = this.chatStore.systemMsgMaxSeqNo;
			let url = `/message/system/readed?maxSeqNo=${maxSeqNo}`
			this.$http({
				url: url,
				method: 'put'
			})
		},
		scrollToBottom() {
			this.$nextTick(() => {
				let div = document.getElementById("chatScrollBox");
				div.scrollTop = div.scrollHeight;
			});
		},
		onScroll(e) {
			let scrollElement = e.target
			let scrollTop = scrollElement.scrollTop
			if (scrollTop < 30) { // 在顶部,不滚动的情况
				// 多展示5条信息
				this.showMinIdx = this.showMinIdx > 5 ? this.showMinIdx - 5 : 0;
			}

		},
	},
	watch: {
		chat: {
			handler(newChat, oldChat) {
				this.readedMessage();
				this.scrollToBottom();
				// 初始状态只显示5条消息
				let size = this.chat.messages.length;
				this.showMinIdx = size > 5 ? size - 5 : 0;
			},
			immediate: true
		}
	},
	mounted() {
		let div = document.getElementById("chatScrollBox");
		div.addEventListener('scroll', this.onScroll)
	}
}
</script>

<style lang="scss" scoped>
.chat-system-box {
	display: flex;
	flex-direction: column;
	position: relative;
	width: 100%;
	background: #f8f8f8;

	.main {
		background: #F8F8F8;
		margin: 0 3px;
		display: flex;
		flex-direction: column;
		align-items: center;

		.message-box {
			width: 400px;
			background-color: white;
			text-align: left;
			border-radius: 3%;
			margin: 15px;
			padding: 5px 20px;
			cursor: pointer;

			.title {
				text-align: center;
				font-size: 18px;
				white-space: nowrap;
				overflow: hidden;
				margin: 10px 20px;
				font-weight: 600;
			}

			.cover {
				width: 100%;
				height: 200px;
				object-fit: cover;
				border-radius: 3px;
			}

			.intro {
				padding: 8px;
				font-size: 16px;
				border-bottom: 1px #eee solid;
				overflow-wrap: break-word
			}

			.bottom-bar {
				font-size: 14px;
				padding: 8px;
				text-align: left;
				color: blue;
			}
		}

		.message-tip {
			line-height: 20px;
			font-size: var(--im-font-size-small);
			color: var(--im-text-color-light);
		}
	}
}
</style>