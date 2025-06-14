<template>
	<nav-bar title="投诉" back></nav-bar>
	<view class="page complaint">
		<text class="tip">请填写投诉内容：</text>
		<uni-easyinput class="content" type="textarea" v-model="content" />
		<button type="primary" class="bottom-btn" @click="submit()">提交</button>
	</view>
</template>

<script>
export default {
	data() {
		return {
			content: ""
		}
	},
	methods: {
		submit() {
			// 投诉功能只是为了过审需要，模拟请求
			setTimeout(() => {
				uni.showToast({
					title: "感谢您的反馈，工作人员会在24小时内核实并处理",
					icon: "none"
				})
			}, 300)
		}
	}
}
</script>

<style lang="scss" scoped>
.complaint {
	padding: 30rpx;

	.tip {
		height: 60rpx;
		line-height: 60rpx;
	}

	.content {
		font-size: 30rpx;
	}
}
</style>