<template>
	<div>
		<div class="group-item" :class="itemClass" @contextmenu.prevent="showRightMenu($event)">
			<div class="group-avatar">
				<head-image :size="headImageSize" :name="group.showGroupName" :url="group.headImage"> </head-image>
			</div>
			<div class="group-name"> {{ group.showGroupName }} </div>
			<slot></slot>
		</div>
		<right-menu ref="rightMenu" @select="onSelectMenu"></right-menu>
	</div>
</template>

<script>
import HeadImage from '../common/HeadImage.vue';
import RightMenu from "../common/RightMenu.vue";
export default {
	name: "groupItem",
	components: {
		HeadImage,
		RightMenu
	},
	data() {
		return {}
	},
	props: {
		group: {
			type: Object
		},
		active: {
			type: Boolean
		},
		size: {
			type: String,
			default: 'normal'
		},
		menu: {
			type: Boolean,
			default: true
		},
	},
	methods: {
		onSelectMenu(item) {
			this.$emit(item.key.toLowerCase(), this.msgInfo);
		},
		showRightMenu(e) {
			if (this.menu) {
				this.$refs.rightMenu.open(e, this.menuItems);
			}
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
		},
		isOwner() {
			return this.group.ownerId == this.userStore.userInfo.id;
		},
		menuItems() {
			let items = [];
			items.push({
				key: 'CHAT',
				name: '发送消息',
			});
			items.push({
				key: 'CARD',
				name: '分享名片',
			});
			if (this.isOwner) {
				items.push({
					key: 'DISSOLVE',
					name: '解散群聊',
					color: '#F56C6C'
				});
			} else {
				items.push({
					key: 'QUIT',
					name: '退出群聊',
					color: '#F56C6C'
				});
			}
			return items;

		}
	}
}
</script>

<style lang="scss" scoped>
.group-item {
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

	.group-name {
		flex: 1;
		display: flex;
		align-items: center;
		padding-left: 10px;
		height: 100%;
		text-align: left;
		white-space: nowrap;
		overflow: hidden;
		font-size: var(--im-font-size);
	}
}
</style>
