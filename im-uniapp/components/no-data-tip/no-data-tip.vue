<template>
	<view class="no-data-tip">
		<image class="image" src="/static/image/no_data.png" />
		<view class="text">{{ tip }}</view>
	</view>
</template>

<script>
export default {
	name: "no-data-tip",
	props: {
		tip: {
			type: String,
			default: '没有数据'
		}
	},
	data() {
		return {

		};
	}
}
</script>

<style scoped lang="scss">
.no-data-tip {
	display: flex;
	flex: 1;
	height: 100%;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	
	.image {
		width: 240rpx;
		height: 240rpx;
	}

	.text {
		text-align: center;
		font-size: $im-font-size;
		color: $im-text-color-light;
		margin-top: 5rpx;
	}
}
</style>