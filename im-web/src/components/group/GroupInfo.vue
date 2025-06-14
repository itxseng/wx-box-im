<template>
	<div v-if="show" class="group-info" :style="{ left: pos.x + 'px', top: pos.y + 'px' }" @click.stop>
		<div class="container">
			<div class="avatar">
				<head-image :name="group.name" :url="group.headImageThumb" :size="60" @click.native="showFullImage()"
					radius="10%"> </head-image>
			</div>
			<div class="info-card">
				<div class="header">
					<div class="name">{{ group.name }}</div>
					<div class="member-size">({{ memberSize }}人)</div>
					<el-tag v-if="group.isBanned" size="mini" type="danger">已封禁</el-tag>
				</div>
				<div class="info-item">
					群主: {{ ownerName }}
				</div>
				<div class="info-item">
					群公告: {{ group.notice }}
				</div>
			</div>
		</div>
		<el-divider content-position="center"></el-divider>
		<div class="btn-group">
			<el-button v-if="isInGroup" type="primary" @click="onSendMessage()">发消息</el-button>
			<el-button v-else type="primary" @click="onJoin()">申请加入</el-button>
		</div>
	</div>
</template>

<script>
import HeadImage from '../common/HeadImage.vue';

export default {
	name: "GroupInfo",
	components: {
		HeadImage
	},
	data() {
		return {
			show: false,
			group: {},
			groupMembers: [],
			pos: {
				x: 0,
				y: 0
			}
		}
	},
	methods: {
		open(group, pos) {
			this.show = true;
			this.group = group;
			this.groupMembers = [];
			let w = document.documentElement.clientWidth;
			let h = document.documentElement.clientHeight;
			this.pos.x = Math.min(pos.x, w - 350);
			this.pos.y = Math.min(pos.y, h - 200);
			this.loadGroupMembers();
		},
		close() {
			this.show = false;
		},
		onSendMessage() {
			let chat = {
				type: 'GROUP',
				targetId: this.group.id,
				showName: this.group.showGroupName,
				headImage: this.group.headImage,
			};
			this.chatStore.openChat(chat);
			this.chatStore.setActiveChat(0);
			if (this.$route.path != "/home/chat") {
				this.$router.push("/home/chat");
			}
			this.show = false;
		},
		onJoin() {
			this.$http({
				url: "/group/join/" + this.group.id,
				method: 'post'
			}).then(() => {
				this.groupStore.addGroup(this.group);
				this.$message.success(`您加入了群聊'${this.group.name}'`)
			})
		},
		showFullImage() {
			if (this.group.headImage) {
				this.$eventBus.$emit("openFullImage", this.group.headImage);
			}
		},
		loadGroupMembers() {
			this.$http({
				url: `/group/members/${this.group.id}`,
				method: "get"
			}).then((members) => {
				this.groupMembers = members;
			})
		}
	},
	computed: {
		isInGroup() {
			return this.groupStore.isGroup(this.group.id);
		},
		ownerName() {
			let member = this.groupMembers.find((m) => m.userId == this.group.ownerId);
			return member && member.showNickName;
		},
		memberSize() {
			return this.groupMembers.filter(m => !m.quit).length;
		}
	}
}
</script>

<style lang="scss" scoped>
.group-info {
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

				.name {
					font-size: var(--im-font-size-large);
					font-weight: 600;
				}

				.member-size {
					margin-left: 3px;
					font-size: var(--im-font-size-small);
					font-weight: 600;
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
	}
}
</style>
