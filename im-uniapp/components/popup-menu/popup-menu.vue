<template>
	<uni-popup ref="popup" type="bottom">
		<view class="popup-menu">
			<view class="menu">
				<view class="menu-item" v-for="(item) in items" :key="item.key" @click.prevent="onSelectMenu(item)">
					{{item.name}}
				</view>
				<view class="menu-item cancel">取消</view>
			</view>
		</view>
	</uni-popup>
</template>

<script>
export default {
	name: "popup-menu",
	props: {
		items: {
			type: Array
		}
	},
	data() {
		return {

		}
	},
	methods: {
		open() {
			this.$refs.popup.open();
		},
		close(){
			this.$refs.popup.close();
		},
		onSelectMenu(item) {
			this.$emit("select", item);
			this.$refs.popup.close();
		}
	}


}
</script>

<style lang="scss" scoped>
.popup-menu {
	position: relative;
	background-color: #eee;
	border-radius: 15rpx 15rpx 0 0;
	overflow: hidden;
	
	.menu {
		.menu-item {
			width: 100%;
			height: 100rpx;
			margin-top: 2rpx;
			background-color: white;
			line-height: 100rpx;
			text-align: center;
			font-size: $im-font-size-larger;
		}
		
		.cancel {
			margin-top: 10rpx;
			color: #f14747;
		}
	}
}
</style>