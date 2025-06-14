<template>
	<div v-if="show" class="user-info" :style="{ left: pos.x + 'px', top: pos.y + 'px' }" @click.stop>
		<div class="container">
			<div class="avatar">
				<head-image :name="user.nickName" :url="user.headImageThumb" :size="60" :online="user.online"
					@click.native="showFullImage()" radius="10%"> </head-image>
			</div>
			<div class="info-card">
				<div class="header">
					<div class="nick-name">{{ user.nickName }}</div>
					<div v-if="user.sex == 0" class="icon iconfont icon-man" style="color: darkblue;"></div>
					<div v-if="user.sex == 1" class="icon iconfont icon-girl" style="color: darkred;"></div>
					<el-tag v-if="user.status == 1" size="mini" type="danger">已注销</el-tag>
					<el-tag v-if="user.isBanned" size="mini" type="danger">已封禁</el-tag>
				</div>
				<div class="info-item">
					用户名: {{ user.userName }}
				</div>
				<div class="info-item">
					个性签名: {{ user.signature }}
				</div>
			</div>
		</div>
		<el-divider content-position="center"></el-divider>
		<div class="btn-group">
			<el-button v-if="isFriend" type="primary" @click="onSendMessage()">发消息</el-button>
			<span class="wait-text" v-else-if="isWaitingApprove">好友验证中</span>
			<el-button v-else-if="user.id != mine.id" type="primary" @click="onAddFriend()">加为好友</el-button>
		</div>
		<friend-apply ref="applyRef"></friend-apply>
	</div>
</template>

<script>
import FriendApply from '../friend/FriendApply.vue';
import HeadImage from './HeadImage.vue'

export default {
	name: "userInfo",
	components: {
		HeadImage,
		FriendApply
	},
	data() {
		return {
			show: false,
			user: {},
			pos: {
				x: 0,
				y: 0
			}
		}
	},
	methods: {
		open(user, pos) {
			this.show = true;
			this.user = user;
			let w = document.documentElement.clientWidth;
			let h = document.documentElement.clientHeight;
			this.pos.x = Math.min(pos.x, w - 350);
			this.pos.y = Math.min(pos.y, h - 200);
		},
		close() {
			this.show = false;
		},
		onSendMessage() {
			let user = this.user;
			let chat = {
				type: 'PRIVATE',
				targetId: user.id,
				showName: user.nickName,
				headImage: user.headImage,
			};
			this.chatStore.openChat(chat);
			this.chatStore.setActiveChat(0);
			if (this.$route.path != "/home/chat") {
				this.$router.push("/home/chat");
			}
			this.show = false;
		},
		onAddFriend() {
			this.$refs.applyRef.open(this.user);
		},
		showFullImage() {
			if (this.user.headImage) {
				this.$eventBus.$emit("openFullImage", this.user.headImage);
			}
		}
	},
	computed: {
		mine() {
			return this.userStore.userInfo;
		},
		isFriend() {
			return this.friendStore.isFriend(this.user.id);
		},
		isWaitingApprove() {
			return this.friendStore.isInRecvRequest(this.user.id);
		}
	}
}
</script>

<style lang="scss">
.user-info {
	position: absolute;
	width: 300px;
	background-color: white;
	box-shadow: var(--im-box-shadow);
	border-radius: 4px;
	padding: 15px;
	z-index: 9999;

	.container {
		display: flex;
		align-items: center;

		.info-card {
			flex: 1;
			padding-left: 10px;

			.header {
				display: flex;
				align-items: center;

				.nick-name {
					font-size: var(--im-font-size-large);
					font-weight: 600;
				}

				.icon {
					margin-left: 3px;
					font-size: var(--im-font-size);
				}

				.el-tag {
					margin-left: 3px;
				}
			}

			.info-item {
				font-size: var(--im-font-size);
				margin-top: 5px;
				word-break: break-all;

			}
		}

	}

	.el-divider--horizontal {
		margin: 18px 0;
	}

	.btn-group {
		text-align: center;

		.wait-text {
			font-size: 14px;
			color: var(--im-text-color-light);
		}
	}
}
</style>
