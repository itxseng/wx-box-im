<template>
	<view class="page chat-system">
		<nav-bar :title="title" back></nav-bar>
		<view class="container">
			<scroll-view class="scroll-box" scroll-y="true" upper-threshold="200">
				<web-view v-if="contentType == 1" :src="externLink"></web-view>
				<up-parse v-else :content="richText"></up-parse>
			</scroll-view>
		</view>
	</view>
</template>

<script>
import { Base64 } from 'js-base64';

export default {
	data() {
		return {
			title: "",
			contentType: 0,
			richText: "",
			externLink: ""
		}
	},
	methods: {
		onNavBack() {
			uni.navigateBack();
		},
		init(id, title) {
			this.$http({
				url: '/message/system/content?id=' + id,
				method: 'GET',
			}).then(msgInfo => {
				this.title = title;
				this.richText = Base64.decode(msgInfo.richText);
				this.externLink = msgInfo.externLink;
				this.contentType = msgInfo.contentType;
				this.isShow = true;
			})
		},
	},
	onLoad(options) {
		console.log(options)
		this.init(options.id, options.title);
	}
}
</script>

<style lang="scss" scoped>
.chat-system {
	position: relative;
	display: flex;
	flex-direction: column;

	.container {
		flex: 1;
		padding: 0 40rpx;
		overflow: hidden;
		position: relative;
		background-color: #f6f6f6;

		.scroll-box {
			height: 100%;
		}
	}
}
</style>