<template>
	<view class="arrow-bar">
		<text v-if="icon" class="icon iconfont" :class="icon" :style="{color: iconColor}"></text>
		<text class="title">{{ title }}</text>
		<slot></slot>
		<uni-icons class="arrow" type="right" size="16"></uni-icons>
	</view>
</template>

<script>
export default {
	name: "arrow-bar",
	props: {
		title: {
			type: String,
			required: true
		},
		icon: {
			type: String,
			default: ''
		},
		iconColor: {
			type: String,
			default: ''
		}
	},
	data() {}
}
</script>

<style lang="scss" scoped>
.arrow-bar {
	height: 90rpx;
	font-size: $im-font-size;
	color: $im-text-color;
	margin-top: 3rpx;
	background-color: white;
	line-height: 90rpx;
	display: flex;
	padding: 0 20rpx;

	.icon {
		margin-left: 20rpx;
		font-size: 36rpx;
	}

	.title {
		flex: 1;
		margin-left: 20rpx;
	}

	.arrow {
		margin-right: 20rpx;
		margin-left: 10rpx;
	}
}
</style>