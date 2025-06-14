<template>
	<div>
		<div class="friend-item" :class="itemClass" @contextmenu.prevent="showRightMenu($event)">
			<div class="friend-avatar">
				<head-image :size="headImageSize" :name="friend.showNickName" :url="friend.headImage"
					:online="friend.online">
				</head-image>
			</div>
			<div class="friend-info">
				<div class="friend-name">{{ friend.showNickName }}</div>
				<div class="friend-online">
					<i class="el-icon-monitor online" v-show="friend.onlineWeb" title="电脑设备在线">
						<span class="online-icon"></span>
					</i>
					<i class="el-icon-mobile-phone online" v-show="friend.onlineApp" title="移动设备在线">
						<span class="online-icon"></span>
					</i>
				</div>
			</div>
			<slot></slot>
		</div>
		<right-menu ref="rightMenu" @select="onSelectMenu"></right-menu>
	</div>
</template>

<script>
import HeadImage from '../common/HeadImage.vue';
import RightMenu from "../common/RightMenu.vue";

export default {
	name: "frinedItem",
	components: {
		HeadImage,
		RightMenu
	},
	props: {
		active: {
			type: Boolean
		},
		friend: {
			type: Object
		},
		menu: {
			type: Boolean,
			default: true
		},
		size: {
			type: String,
			default: 'normal'
		}
	},
	data() {
		return {
			menuItems: [{
				key: 'CHAT',
				name: '发送消息',
			}, {
				key: 'CARD',
				name: '分享名片',
			}, {
				key: 'DELETE',
				name: '删除好友',
				color: '#F56C6C'
			}]
		}
	},
	methods: {
		showRightMenu(e) {
			if (this.menu) {
				this.$refs.rightMenu.open(e, this.menuItems);
			}
		},
		onSelectMenu(item) {
			this.$emit(item.key.toLowerCase(), this.msgInfo);
		}
	},
	computed: {
		headImageSize() {
			if (this.size == 'small') {
				return 36;
			}
			return 42;
		},
		itemClass() {
			let clz = "";
			if (this.active) {
				clz += "active"
			}
			if (this.size == 'small') {
				clz += " small"
			}
			return clz;
		}
	}
}
</script>

<style scope lang="scss">
.friend-item {
	height: 50px;
	display: flex;
	position: relative;
	padding: 5px 10px;
	align-items: center;
	white-space: nowrap;
	cursor: pointer;

	&:hover {
		background-color: var(--im-background-active);
	}

	&.active {
		background-color: var(--im-background-active-dark);
	}

	&.small {
		height: 42px;
		padding: 3px 10px;
	}

	.friend-avatar {
		display: flex;
		justify-content: center;
		align-items: center;
	}

	.friend-info {
		flex: 1;
		display: flex;
		flex-direction: column;
		padding-left: 10px;
		text-align: left;

		.friend-name {
			font-size: var(--im-font-size);
			white-space: nowrap;
			overflow: hidden;
		}

		.friend-online {
			.online {
				font-weight: bold;
				padding-right: 2px;
				font-size: 16px;
				position: relative;
			}

			.online-icon {
				position: absolute;
				right: 0;
				bottom: 0;
				width: 6px;
				height: 6px;
				background: limegreen;
				border-radius: 50%;
				border: 1px solid white;
			}
		}
	}
}
</style>
