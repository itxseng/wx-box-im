<template>
	<div class="friend-request-item">
		<div class="friend-avatar">
			<head-image :size="56" :id="friend.id" :name="friend.nickName" :url="friend.headImage"></head-image>
		</div>
		<div class="request-info">
			<div class="nick-name">
				<div>{{ friend.nickName }}</div>
			</div>
			<div class="info-text">
				<div v-if="request.remark">{{ request.remark }}</div>
				<div v-else-if="isSender">请求添加对方为好友</div>
				<div v-else>请求添加您为好友</div>
			</div>
		</div>
		<div class="btn-group">
			<el-button v-if="!isSender" type="danger" size="mini" @click="onReject">拒绝</el-button>
			<el-button v-if="!isSender" type="primary" size="mini" @click="onApprove">同意</el-button>
			<el-button v-if="isSender" type="danger" size="mini" @click="onRecall">撤回</el-button>
		</div>
	</div>
</template>

<script>
import HeadImage from '../common/HeadImage.vue';

export default {
	name: "frinedRequestItem",
	components: {
		HeadImage
	},
	data() {
		return {}
	},
	methods: {
		onApprove() {
			this.$http({
				url: "/friend/request/approve?id=" + this.request.id,
				method: 'post'
			}).then(() => {
				this.friendStore.removeRequest(this.request.id);
				this.$message.success(`${this.request.sendNickName} 已成为您的好友`);
			})
		},
		onReject() {
			this.$http({
				url: "/friend/request/reject?id=" + this.request.id,
				method: 'post'
			}).then(() => {
				this.friendStore.removeRequest(this.request.id);
				this.$message.success(`您拒绝了 ${this.request.sendNickName} 的好友请求`);
			})
		},
		onRecall() {
			this.$http({
				url: "/friend/request/recall?id=" + this.request.id,
				method: 'post'
			}).then(() => {
				this.friendStore.removeRequest(this.request.id);
				this.$message.success(`您撤回了 ${this.request.recvNickName} 的好友请求`);
			})
		}
	},
	props: {
		request: {
			type: Object
		}
	},
	computed: {
		mine() {
			return this.userStore.userInfo;
		},
		isSender() {
			// 我是否发起方
			return this.request.sendId == this.mine.id;
		},
		friend() {
			if (this.isSender) {
				return {
					id: this.request.recvId,
					nickName: this.request.recvNickName,
					HeadImage: this.request.recvHeadImage,
				}
			} else {
				return {
					id: this.request.sendId,
					nickName: this.request.sendNickName,
					HeadImage: this.request.sendHeadImage,
				}
			}
		}
	}

}
</script>

<style scope lang="scss">
.friend-request-item {
	height: 80px;
	display: flex;
	position: relative;
	align-items: center;
	white-space: nowrap;
	cursor: pointer;
	margin: 2px 30px;
	border-bottom: 1px solid #ccc;

	.request-info {
		margin-left: 15px;
		flex: 3;
		display: flex;
		flex-direction: column;
		flex-shrink: 0;
		overflow: hidden;

		.nick-name {
			display: flex;
			align-items: center;
			font-weight: 600;
			font-size: 16px;
			line-height: 30px;
		}

		.info-text {
			display: flex;
			font-size: 14px;
			line-height: 20px;
			height: 20px;
			text-align: left;
		}

	}

}
</style>
