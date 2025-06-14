<template>
	<view class='drop-down-menu'>
		<view v-if="showMenu" class="menu-mask" @touchstart="close()"></view>
		<view v-if="showMenu" class="menu">
			<view class="menu-item" v-for="(item) in items" :key="item.key" @click.prevent="onSelectMenu(item)">
				<text v-if="item.icon" class="icon iconfont" :class="item.icon" @click.stop="onShowQrcode()"></text>
				<text class="menu-text"> {{ item.name }}</text>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	name: 'drop-down-menu',
	data() {
		return {
			showMenu: false
		}
	},
	props: {
		items: {
			type: Array
		}
	},
	methods: {
		open(e) {
			this.showMenu = true;
		},
		close() {
			this.showMenu = false;
		},
		onSelectMenu(item) {
			this.$emit("select", item);
			this.showMenu = false;
		}
	}
}
</script>

<style lang="scss" scoped>
.drop-down-menu {
	.menu-mask {
		position: fixed;
		left: 0;
		top: 0;
		right: 0;
		bottom: 0;
		width: 100%;
		height: 100%;
		z-index: 999;
		opacity: 0.5;
	}

	.menu {
		position: absolute;
		top: 0;
		right: 20rpx;
		border-radius: 10rpx;
		overflow: hidden;
		background-color: white;
		z-index: 1000;
		box-shadow: $im-box-shadow-dark;
		padding: 5rpx;

		.menu-item {
			height: 56rpx;
			min-width: 150rpx;
			line-height: 56rpx;
			display: flex;
			padding: 10px 30rpx;
			align-items: center;
			font-weight: 600;
			border-radius: 10rpx;
			&:hover {
				background: $im-bg-active;
			}

			.icon {
				margin-right: 15rpx;
				font-size: 36rpx;
			}

			.menu-text {
				font-size: $im-font-size-small;
			}
		}
	}

}
</style>