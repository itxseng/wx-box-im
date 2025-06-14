<template>
	<div v-loading="!isConnected" class="remote-video" ref="box">
		<div class="avatar" :style="{ 'height': height, 'width': width }"
			v-show="!isConnected || !userInfo.isCamera">
			<img class="background" :src="userInfo.headImage" v-if="userInfo.headImage">
			<head-image class="head-image" :url="userInfo.headImage" :name="userInfo.nickName" :size="160">
			</head-image>
		</div>
		<div class="info-bar">
			<div v-show="userInfo.isMicroPhone" class="icon iconfont icon-microphone-on"></div>
			<div v-show="!userInfo.isMicroPhone" class="icon iconfont icon-microphone-off"></div>
			<div class="name">
				{{ userInfo.nickName }}
			</div>
		</div>
		<video v-show="isConnected && userInfo.isCamera" class="video" ref="video"></video>
	</div>
</template>

<script>
import ImWebRtc from '@/api/webrtc'
import RtcGroupApi from '@/api/rtcGroupApi'
import HeadImage from '@/components/common/HeadImage'
export default {
	name: "localVideo",
	components: {
		HeadImage
	},
	data() {
		return {
			API: new RtcGroupApi(),
			webrtc: new ImWebRtc(), // webrtc相关
			localStream: null,
			remoteStream: null,
			candidates: [],
			isInit: false,
			isConnected: false,
			isReady: false
		}
	},
	props: {
		width: {
			type: Number
		},
		height: {
			type: Number
		},
		groupId: {
			type: Number
		},
		userInfo: {
			type: Object
		}
	},
	methods: {
		init(localStream) {
			this.isInit = true;
			this.webrtc.init(this.configuration);
			this.localStream = localStream;
			// 建立webrtc连接
			this.webrtc.setupPeerConnection((remoteStream) => {
				// 对方视频流
				console.log("获取到远端流")
				this.remoteStream = remoteStream;
				this.$refs.video.srcObject = remoteStream;
				this.$refs.video.play().catch(() => {
					console.log("远端流播放失败")
				});
			})
			// 设置本地流
			this.webrtc.setStream(localStream);
			// 监听候选信息
			this.webrtc.onIcecandidate((candidate) => {
				if (this.isReady) {
					// 连接已就绪,直接发送
					this.API.candidate(this.groupId, this.userInfo.id, JSON.stringify(candidate));
				} else {
					// 连接未就绪,缓存起来，连接后再发送
					this.candidates.push(candidate)
				}
			})
			// 监听连接成功状态
			this.webrtc.onStateChange((state) => {
				if (state == "connected") {
					// 就绪
					this.isConnected = true;
				} else if (state == "disconnected") {
					// this.$message.error("当前通话质量不佳")
				}
			})
		},
		reconnect(localStream) {
			this.localStream = localStream;
			this.webrtc.setStream(localStream);
			this.connect();
		},
		setMute(isMute) {
			this.$refs.video.muted = isMute;
		},
		connect() {
			this.webrtc.createOffer().then((offer) => {
				this.isReady = true;
				// 推送offer给对方
				this.API.offer(this.groupId, this.userInfo.id, JSON.stringify(offer));
			});

		},
		onOffer(offer) {
			this.webrtc.createAnswer(offer).then((answer) => {
				this.isReady = true;
				// 推送answer给对方
				this.API.answer(this.groupId, this.userInfo.id, JSON.stringify(answer));
			});
		},
		onAnswer(answer) {
			// 设置对方answer信息
			this.webrtc.setRemoteDescription(answer);
			// 推送candidate
			this.sendCandidate();
		},
		setCandidate(candidate) {
			this.webrtc.addIceCandidate(candidate);
		},
		sendCandidate() {
			this.candidates.forEach((candidate) => {
				this.API.candidate(this.groupId, this.userInfo.id, JSON.stringify(candidate));
			});
			this.candidates = [];
		},
		close() {
			this.webrtc.close();
			this.$refs.video.srcObject = null;
			this.isInit = false;
			this.isConnected = false;
			this.isReady = false;
			this.candidates = [];
		}
	},
	computed: {
		configuration() {
			const iceServers = this.configStore.webrtc.iceServers;
			return {
				iceServers: iceServers
			}
		}
	}
}
</script>

<style lang="scss" scoped>
.remote-video {
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

	.info-bar {
		position: absolute;
		bottom: 10px;
		left: 10px;
		overflow: hidden;
		color: white;
		font-size: var(--im-font-size);
		display: flex;
		align-items: center;
		z-index: 2;
		background-color: rgb(0, 0, 0, 0.4);
		padding: 2px 6px;
		border-radius: 4px;

		.icon {
			font-size: var(--im-font-size);
			margin-right: 2px;
		}
	}

	.video {
		position: relative;
		width: 100%;
		height: 100%;
		object-fit: cover;
	}
}
</style>
