<template>
	<view class="video-player" :class="size">
		<!--视频图片列表-->
		<!-- #ifndef MP-WEIXIN -->
		<image class="cover" mode="heightFix" :src="poster"></image>
		<view v-if="!isPlaying" class="play-mask">
			<image class="play-btn" src="/static/image/play.png" @click.prevent.stop="play()"></image>
		</view>
		<!-- 宽高不能为0,会导致部分设备无法全屏 -->
		<video :id=id :src="url" style="width:1px;height: 1px;" show-loading="false"
			@fullscreenchange="onScreenChange"></video>
		<!-- #endif -->
		<!-- #ifdef MP-WEIXIN -->
		<!-- 小程序的createVideoContext似乎没效果，只能放个video标签将就用用了 -->
		<video class="mp-video" :poster="poster" :src="url" show-loading="false"></video>
		<!-- #endif -->
	</view>
</template>

<script>
export default {
	name: "video-player",
	props: {
		url: {
			type: String
		},
		poster: {
			type: String
		},
		size: {
			type: String,
			default: 'normal'
		}
		
	},
	data() {
		return {
			id: null,
			isPlaying: false,
			videoContext: null
		};
	},
	methods: {
		play() {
			// 这里有个延迟，否则在show的同时就执行全屏和播放，在H5下无效
			setTimeout(() => {
				try {
					if (!this.videoContext) {
						this.videoContext = uni.createVideoContext(this.id)
					}
					this.videoContext.requestFullScreen({ direction: 0 })
					this.videoContext.play();
				} catch (e) {
					console.log("播放失败", e)
					this.isPlaying = false;
					uni.showToast({
						icon: "none",
						title: "播放失败",
					})
				}
			}, 100)
			this.isPlaying = true
		},
		// 根据全屏切换事件，控制视频的显示隐藏
		onScreenChange(e) {
			if (!e.detail.fullScreen) {
				this.isPlaying = false;
				this.videoContext.stop();
			}
		},
		generateId() {
			// 生成临时id 
			return String(new Date().getTime()) + String(Math.floor(Math.random() * 1000));
		}
	},
	mounted() {
		this.id = this.generateId();
	}
}
</script>

<style lang="scss">
.video-player {
	width: 100%;
	position: relative;
	border-radius: 10rpx;

	.cover {
		min-width: 200rpx;
		max-width: 400rpx;
		vertical-align: bottom;
		border-radius: 10rpx;
	}

	.play-mask {
		position: absolute;
		width: 100%;
		height: 100%;
		display: flex;
		align-items: center;
		justify-content: center;
		top: 0;
		left: 0;
		z-index: 99;

		.play-btn {
			width: 100rpx;
			height: 100rpx;
			vertical-align: bottom;
		}
	}

	.mp-video {
		max-width: 400rpx;
		height: 350rpx;
		object-fit: contain;
		border-radius: 10rpx;
	}
	
	&.small {
		.cover {
			min-width: 100rpx;
			max-width: 200rpx;
			height: 150rpx;
		}

		.play-btn {
			width: 50rpx;
			height: 50rpx;
			vertical-align: bottom;
		}

		.mp-video {
			max-width: 200rpx;
			height: 150rpx;
			object-fit: contain;
			border-radius: 5rpx;
		}
	}
}
</style>