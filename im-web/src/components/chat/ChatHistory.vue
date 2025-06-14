<template>
	<el-drawer title="聊天记录" size="700px" :visible.sync="isShow" direction="rtl" :before-close="close">
		<div class="chat-history">
			<div class="search-bar">
				<el-input class="search-text" size="small" placeholder="搜索聊天记录" v-model="searchText"
					@input="onSearchTextChange">
					<i class="el-icon-search el-input__icon" slot="prefix"> </i>
				</el-input>
			</div>
			<div class="chat-tabs" ref="chatTabs">
				<el-tabs v-model="tabName" @tab-click="onTabClick">
					<el-tab-pane label="全部" name="all">
						<el-scrollbar v-if="messageSize > 0" ref="allScrollBox" :style="tabPaneStyle">
							<div v-for="(msgInfo, idx) in showMessages" :key="showMinIdx + idx">
								<chat-history-item @click.native="onClickItem(showMinIdx + idx)"
									@contextmenu.prevent.native="onRclickItem($event, showMinIdx + idx)"
									@dblclick.native="onDblclickItem(showMinIdx + idx)"
									:active="activeIdx == showMinIdx + idx" :headImage="headImage(msgInfo)"
									:showName="showName(msgInfo)" :quoteShowName="showName(msgInfo.quoteMessage)"
									:msgInfo="msgInfo">
								</chat-history-item>
							</div>
						</el-scrollbar>
						<no-data-tip v-else :style="tabPaneStyle" :tip="noDataTip"></no-data-tip>
					</el-tab-pane>
					<el-tab-pane label="文字" name="text">
						<el-scrollbar v-if="messageSize > 0" ref="textScrollBox" :style="tabPaneStyle">
							<div v-for="(msgInfo, idx) in showMessages" :key="showMinIdx + idx">
								<chat-history-item @click.native="onClickItem(showMinIdx + idx)"
									@contextmenu.prevent.native="onRclickItem($event, showMinIdx + idx)"
									@dblclick.native="onDblclickItem(showMinIdx + idx)"
									:active="activeIdx == showMinIdx + idx" :headImage="headImage(msgInfo)"
									:showName="showName(msgInfo)" :quoteShowName="showName(msgInfo.quoteMessage)"
									:msgInfo="msgInfo">
								</chat-history-item>
							</div>
						</el-scrollbar>
						<no-data-tip v-else :style="tabPaneStyle" :tip="noDataTip"></no-data-tip>
					</el-tab-pane>
					<el-tab-pane label="图片" name="image">
						<el-scrollbar v-if="messageSize > 0" ref="imageScrollBox" :style="tabPaneStyle">
							<div v-if="tabName == 'image'" class="chat-image-video-list">
								<div v-for="(msgInfo, idx) in showMessages" :key="showMinIdx + idx">
									<div class="chat-image-video" :class="activeIdx == showMinIdx + idx ? 'active' : ''"
										@contextmenu.prevent="onRclickItem($event, showMinIdx + idx)"
										@dblclick="onDblclickItem(showMinIdx + idx)"
										@click="onClickItem(showMinIdx + idx)">
										<img class="image" :src="JSON.parse(msgInfo.content).thumbUrl" loading="lazy"
											@click="showFullImageBox(msgInfo)" />
										<span class="upload-text">{{ showName(msgInfo) }}上传于 {{
											$date.toTimeText(msgInfo.sendTime, true) }} </span>
									</div>
								</div>
							</div>
						</el-scrollbar>
						<no-data-tip v-else :style="tabPaneStyle"></no-data-tip>
					</el-tab-pane>
					<el-tab-pane label="文件" name="file">
						<el-scrollbar v-if="messageSize > 0" ref="fileScrollBox" :style="tabPaneStyle">
							<div v-for="(msgInfo, idx) in showMessages" :key="showMinIdx + idx">
								<chat-history-item @click.native="onClickItem(showMinIdx + idx)"
									@contextmenu.prevent.native="onRclickItem($event, showMinIdx + idx)"
									@dblclick.native="onDblclickItem(showMinIdx + idx)"
									:active="activeIdx == showMinIdx + idx" :headImage="headImage(msgInfo)"
									:showName="showName(msgInfo)" :quoteShowName="showName(msgInfo.quoteMessage)"
									:msgInfo="msgInfo">
								</chat-history-item>
							</div>
						</el-scrollbar>
						<no-data-tip v-else :style="tabPaneStyle" :tip="noDataTip"></no-data-tip>
					</el-tab-pane>
					<el-tab-pane label="语音" name="voice">
						<el-scrollbar v-if="messageSize > 0" ref="voiceScrollBox" :style="tabPaneStyle">
							<div v-for="(msgInfo, idx) in showMessages" :key="showMinIdx + idx">
								<chat-history-item @click.native="onClickItem(showMinIdx + idx)"
									@contextmenu.prevent.native="onRclickItem($event, showMinIdx + idx)"
									@dblclick.native="onDblclickItem(showMinIdx + idx)"
									:active="activeIdx == showMinIdx + idx" :headImage="headImage(msgInfo)"
									:showName="showName(msgInfo)" :quoteShowName="showName(msgInfo.quoteMessage)"
									:msgInfo="msgInfo">
								</chat-history-item>
							</div>
						</el-scrollbar>
						<no-data-tip v-else :style="tabPaneStyle"></no-data-tip>
					</el-tab-pane>
					<el-tab-pane label="视频" name="video">
						<el-scrollbar v-if="messageSize > 0" ref="videoScrollBox" :style="tabPaneStyle">
							<div v-if="tabName == 'video'" class="chat-image-video-list">
								<div v-for="(msgInfo, idx) in showMessages" :key="showMinIdx + idx">
									<div class="chat-image-video" :class="activeIdx == showMinIdx + idx ? 'active' : ''"
										@contextmenu.prevent="onRclickItem($event, showMinIdx + idx)"
										@dblclick="onDblclickItem(showMinIdx + idx)"
										@click="onClickItem(showMinIdx + idx)">
										<video class="video" controls preload="none"
											:poster="JSON.parse(msgInfo.content).coverUrl"
											:src="JSON.parse(msgInfo.content).videoUrl" />
										<span class="upload-text">{{ showName(msgInfo) }}上传于 {{
											$date.toTimeText(msgInfo.sendTime, true) }} </span>
									</div>
								</div>
							</div>
						</el-scrollbar>
						<no-data-tip v-else :style="tabPaneStyle"></no-data-tip>
					</el-tab-pane>
				</el-tabs>
			</div>
		</div>
		<right-menu ref="rightMenu" @select="onSelectMenu"></right-menu>
	</el-drawer>
</template>

<script>
import ChatMessageItem from './ChatMessageItem.vue';
import ChatHistoryItem from './ChatHistoryItem.vue';
import RightMenu from '../common/RightMenu.vue';
import NoDataTip from '../common/NoDataTip.vue';
export default {
	name: 'chatHistory',
	components: {
		ChatMessageItem,
		ChatHistoryItem,
		RightMenu,
		NoDataTip
	},
	props: {
		chat: {
			type: Object
		},
		friend: {
			type: Object
		},
		group: {
			type: Object
		},
		groupMembers: {
			type: Array,
		}
	},
	data() {
		return {
			isShow: false,
			tabName: "all",
			showMinIdx: 0,
			activeIdx: -1,
			searchText: '',
			tabPaneHeight: 500,
			menuItems: [{
				key: 'LOCATE_QUOTE',
				name: '在聊天中定位'
			}]
		}
	},
	methods: {
		open() {
			this.isShow = true;
			this.searchText = '';
			this.tabName = 'all';
			this.resetShowMinIdx();
			this.initEvent();
			this.scrollToBottom();
			this.$nextTick(() => {
				// 滚动条高度
				this.tabPaneHeight = this.$refs.chatTabs.offsetHeight - 80;
			})
		},
		close() {
			this.isShow = false
		},
		onTabClick() {
			this.resetShowMinIdx();
			this.initEvent();
			this.scrollToBottom();
		},
		onScroll(e) {
			let scrollElement = e.target
			let scrollTop = scrollElement.scrollTop
			if (scrollTop < 30) {
				// 多展示20条信息
				this.showMinIdx = this.showMinIdx > 20 ? this.showMinIdx - 20 : 0;
			}
		},
		onSearchTextChange() {
			this.resetShowMinIdx();
		},
		onClickItem(idx) {
			this.activeIdx = idx;
		},
		onDblclickItem(idx) {
			this.activeIdx = idx;
			this.onSelectMenu(this.menuItems[0]);
		},
		onRclickItem(e, idx) {
			this.activeIdx = idx;
			this.$refs.rightMenu.open(e, this.menuItems)
		},
		onSelectMenu(item) {
			// 菜单id转驼峰作为事件key
			let eventKey = item.key.toLowerCase().replace(/_([a-z])/g, (g) => g[1].toUpperCase());
			this.$emit(eventKey, this.messages[this.activeIdx]);
		},
		resetShowMinIdx() {
			this.showMinIdx = this.messageSize > 30 ? this.messageSize - 30 : 0;
		},
		initEvent() {
			if (!this.messageSize) {
				return;
			}
			this.$nextTick(() => {
				let scrollBoxName = this.tabName + "ScrollBox";
				let scrollWrap = this.$refs[scrollBoxName].$el.querySelector('.el-scrollbar__wrap');
				scrollWrap.removeEventListener('scroll', this.onScroll)
				scrollWrap.addEventListener('scroll', this.onScroll);
			})
		},
		showName(msgInfo) {
			if (!msgInfo)
				return '';
			if (this.isGroup) {
				let member = this.groupMembers.find((m) => m.userId == msgInfo.sendId);
				return member ? member.showNickName : msgInfo.sendNickName || "";
			} else {
				return msgInfo.selfSend ? this.mine.nickName : this.chat.showName
			}
		},
		headImage(msgInfo) {
			if (this.isGroup) {
				let member = this.groupMembers.find((m) => m.userId == msgInfo.sendId);
				return member ? member.headImage : "";
			} else {
				return msgInfo.selfSend ? this.mine.headImageThumb : this.chat.headImage
			}
		},
		showMenu(e) {
			this.rightMenu.pos = {
				x: e.x,
				y: e.y
			};
			this.rightMenu.show = "true";
		},
		showFullImageBox(msgInfo) {
			let imageUrl = JSON.parse(msgInfo.content).originUrl;
			if (imageUrl) {
				this.$eventBus.$emit("openFullImage", imageUrl);
			}
		},
		scrollToBottom() {
			if (!this.messageSize) {
				return;
			}
			this.$nextTick(() => {
				let scrollBoxName = this.tabName + "ScrollBox";
				let scrollWrap = this.$refs[scrollBoxName].$el.querySelector('.el-scrollbar__wrap');
				scrollWrap.scrollTop = scrollWrap.scrollHeight;
			});
		}
	},
	computed: {
		mine() {
			return this.userStore.userInfo;
		},
		isGroup() {
			return this.chat.type == 'GROUP';
		},
		allMessage() {
			return this.chat.messages.filter(m => this.$msgType.isNormal(m.type));
		},
		imageMessage() {
			let type = this.$enums.MESSAGE_TYPE.IMAGE;
			return this.chat.messages.filter(m => m.type == type);
		},
		fileMessage() {
			let type = this.$enums.MESSAGE_TYPE.FILE;
			return this.chat.messages.filter(m => m.type == type);
		},
		videoMessage() {
			let type = this.$enums.MESSAGE_TYPE.VIDEO;
			return this.chat.messages.filter(m => m.type == type);
		},
		voiceMessage() {
			let type = this.$enums.MESSAGE_TYPE.AUDIO;
			return this.chat.messages.filter(m => m.type == type);
		},
		textMessage() {
			let type = this.$enums.MESSAGE_TYPE.TEXT;
			return this.chat.messages.filter(m => m.type == type);
		},
		messages() {
			return this[this.tabName + 'Message'].filter(m => {
				if (!this.searchText) {
					return true;
				}
				// 只有文字和文件支持检索
				if (this.tabName != 'all' && this.tabName != 'text' && this.tabName != 'file') {
					return true;
				}
				if (this.$enums.MESSAGE_TYPE.TEXT == m.type) {
					return m.content.toLowerCase().includes(this.searchText.toLowerCase())
				} else if (this.$enums.MESSAGE_TYPE.FILE == m.type) {
					return JSON.parse(m.content).name.toLowerCase().includes(this.searchText.toLowerCase());
				}
				return false;
			});
		},
		showMessages() {
			return this.messages.slice(this.showMinIdx);
		},
		messageSize() {
			return this.isShow ? this.messages.length : 0;
		},
		tabPaneStyle() {
			return `height:${this.tabPaneHeight}px`;
		},
		noDataTip() {
			return this.searchText ? `未搜索到与'${this.searchText}'相关的内容` : "没有数据";
		}
	}
}
</script>

<style lang="scss">
.chat-history {
	display: flex;
	height: 100%;
	padding: 0 25px;
	flex-direction: column;

	.search-bar {
		margin-bottom: 10px;
	}

	.chat-tabs {
		flex: 1;

		.chat-image-video-list {
			display: flex;
			flex-wrap: wrap;

			.chat-image-video {
				display: flex;
				flex-direction: column;
				padding: 10px;
				width: 140px;
				border-radius: 5px;
				cursor: pointer;

				.image {
					width: 140px;
					height: 140px;
					border-radius: 5px;
					object-fit: cover;
				}

				.video {
					width: 140px;
					height: 140px;
					border-radius: 5px;
					object-fit: cover;
				}

				.upload-text {
					color: var(--im-text-color-light);
					font-size: var(--im-font-size-small);
					margin-top: 5px;
					word-break: break-all;
				}

				&:hover {
					background: #f4f4f4;
				}

				&.active {
					background: #E1EAF7;
				}
			}

		}
	}
}
</style>