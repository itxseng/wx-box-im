<template>
	<div class="local-video">
		<div class="avatar" :style="{ 'height': height, 'width': width }" v-show="!userInfo.isCamera">
			<img class="background" :src="userInfo.headImage" v-if="userInfo.headImage">
			<head-image class="head-image" :url="userInfo.headImage" :name="userInfo.nickName" :size="160">
			</head-image>
		</div>
		<video v-show="userInfo.isCamera" class="video" ref="video"></video>
	</div>
</template>

<script>
import ImCamera from '@/api/camera'
import HeadImage from '@/components/common/HeadImage'
export default {
	name: "localVideo",
	components: {
		HeadImage
	},
	data() {
		return {
			stream: null,
			isReady: false,
			isFacing: true
		}
	},
	props: {
		userInfo: {
			type: Object
		},
		width: {
			type: Number
		},
		height: {
			type: Number
		},
	},
	methods: {
		open(stream) {
			this.stream = stream;
			// 显示本地视频
			this.$refs.video.srcObject = stream;
			this.$refs.video.muted = true;
			this.$refs.video.play().catch(() => {
				console.log("本地流播放异常")
			});
			this.isReady = !!stream;
		},
		setMicroPhone(isEnable) {
			this.stream.getTracks().forEach((track => {
				if (track.kind === 'audio') {
					track.enabled = isEnable;
				}
			}))
		}
	},
	computed: {
		vw() {
			if (!this.isReady || !this.userInfo.isCamera) {
				return 0
			} else {
				return '100%'
			}
		}
	}
}
</script>

<style lang="scss" scoped>
.local-video {
	position: relative;
	width: 100%;
	height: 100%;
	background-color: var(--im-background);
	cursor: pointer;
	overflow: hidden;

	.avatar {
		display: flex;
		justify-content: center;
		align-items: center;
		width: 100%;
		height: 100%;
		overflow: hidden;
	}

	.background {
		width: 100%;
		height: 100%;
		position: absolute;
		left: 0;
		right: 0;
		top: 0;
		bottom: 0;
		filter: blur(50px);
		object-fit: cover;
	}

	.video {
		position: relative;
		width: 100%;
		height: 100%;
		object-fit: cover;
	}
}
</style>
