<template>
	<view class="page group-qrcode">
		<nav-bar title="群二维码" back></nav-bar>
		<view class="container">
			<view class="group-info">
				<head-image :name="group.name" :url="group.headImageThumb" :size="100"></head-image>
				<view class="group-name">
					<text class="name-text">{{ group.name }} </text>
				</view>
			</view>
			<!-- 插件地址: https://ext.dcloud.net.cn/plugin?id=1287 -->
			<uqrcode class="qrcode" ref="uqrcode" canvas-id="qrcode" :options="options" size="500" :value="scanUrl"
				sizeUnit="rpx" :h5SaveIsDownload="true" :h5DownloadName="group.name">
				<template v-slot:loading>
					<text>二维码生成中...</text>
				</template>
			</uqrcode>
			<view class="tip">扫一扫二维码,加入群聊</view>
		</view>
		<button type="primary" class="bottom-btn" @click="onSave()">保存到相册</button>
	</view>
</template>

<script>
import UNI_APP from '@/.env.js'
export default {
	data() {
		return {
			group: {},
			options: {
				margin: 10,
				backgroundImageBorderRadius: 50,
				foregroundImagePadding: 5,
				foregroundImageBorderRadius: 5,
				foregroundImageBackgroundColor: '#eee'
			}
		}
	},
	methods: {
		onSave() {
			this.$refs.uqrcode.save({
				success: (e) => {
					// h5和小程序插件都自带提示
					// #ifdef APP-PLUS
					uni.showToast({
						title: '保存成功'
					})
					// #endif
				}
			});
		}
	},
	computed: {
		scanUrl() {
			return UNI_APP.SCAN_URL + "?scan=1&groupId=" + this.group.id;
		}
	},
	onLoad(options) {
		this.group = this.groupStore.findGroup(options.id);
		this.options.foregroundImageSrc = this.group.headImageThumb
	}
}
</script>

<style lang="scss" scoped>
.group-qrcode {
	display: flex;
	flex-direction: column;

	.container {
		padding: 30rpx;
		margin: 50rpx;
		background: white;
		display: flex;
		flex-direction: column;
		align-items: center;
		border-radius: 20rpx;

		.group-info {
			display: flex;
			align-items: center;
			width: 460rpx;

			.group-name {
				margin-left: 30rpx;
				font-size: $im-font-size-large;

				.name-text {
					font-weight: 600;
				}
			}
		}

		.qrcode {
			margin-top: 20rpx;
		}

		.tip {
			margin-top: 10rpx;
			font-size: $im-font-size-smaller;
			color: $im-text-color-light;
		}
	}
}
</style>