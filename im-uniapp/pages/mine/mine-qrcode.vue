<template>
	<view class="page mine-qrcode">
		<nav-bar title="我的二维码" back></nav-bar>
		<view class="container">
			<view class="user-info">
				<head-image :name="mine.nickName" :url="mine.headImageThumb" :size="100"></head-image>
				<view class="nick-name">
					<text class="name-text">{{ mine.nickName }} </text>
					<text v-show="mine.sex == 0" class="iconfont icon-man" color="darkblue"></text>
					<text v-show="mine.sex == 1" class="iconfont icon-girl" color="darkred"></text>
				</view>
			</view>
			<!-- 插件地址: https://ext.dcloud.net.cn/plugin?id=1287 -->
			<uqrcode class="qrcode" ref="uqrcode" canvas-id="qrcode" :options="options" size="500" :value="scanUrl"
				sizeUnit="rpx" :h5SaveIsDownload="true" :h5DownloadName="mine.nickName">
				<template v-slot:loading>
					<text>二维码生成中...</text>
				</template>
			</uqrcode>
			<view class="tip">扫一扫加我为好友</view>
		</view>
		<button type="primary" class="bottom-btn" @click="onSave()">保存到相册</button>
	</view>
</template>

<script>
import UNI_APP from '@/.env.js'
export default {
	data() {
		return {
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
					console.log("生成结果:", e)
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
		mine() {
			return this.userStore.userInfo;
		},
		scanUrl() {
			return UNI_APP.SCAN_URL + "?scan=1&userId=" + this.mine.id;
		}
	},
	onLoad() {
		this.options.foregroundImageSrc = this.userStore.userInfo.headImageThumb
	}
}
</script>

<style lang="scss" scoped>
.mine-qrcode {
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

		.user-info {
			display: flex;
			align-items: center;
			width: 460rpx;

			.nick-name {
				margin-left: 30rpx;
				font-size: $im-font-size-large;

				.name-text {
					font-weight: 600;
				}

				.iconfont {
					font-size: $im-font-size;
					padding-left: 5rpx;
				}

				.icon-man {
					color: darkblue;
				}

				.icon-girl {
					color: darkred;
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