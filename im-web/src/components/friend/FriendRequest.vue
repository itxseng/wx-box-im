<template>
	<div class="friend-request" ref="reqTabs">
		<el-tabs type="card">
			<el-tab-pane :label="'收到的申请(' + recvRequests.length + ')'">
				<el-scrollbar v-if="recvRequests.length" class="request-list" :style="tabPaneStyle">
					<div class="request-item" v-for="request in recvRequests" :key="request.id">
						<friend-request-item :request="request"></friend-request-item>
					</div>
				</el-scrollbar>
				<no-data-tip v-else class="no-data-tip" :style="tabPaneStyle"></no-data-tip>
			</el-tab-pane>
			<el-tab-pane :label="'发起的申请(' + sendRequests.length + ')'">
				<el-scrollbar v-if="sendRequests.length" class="request-list" :style="tabPaneStyle">
					<div class="request-item" v-for="request in sendRequests" :key="request.id">
						<friend-request-item :request="request"></friend-request-item>
					</div>
				</el-scrollbar>
				<no-data-tip v-else class="no-data-tip" :style="tabPaneStyle"></no-data-tip>
			</el-tab-pane>
		</el-tabs>
	</div>
</template>

<script>
import NoDataTip from '../common/NoDataTip.vue'
import FriendRequestItem from './FriendRequestItem.vue'

export default {
	name: "friendRequest",
	components: { NoDataTip, FriendRequestItem },
	data() {
		return {
			tabPaneHeight: 600
		}
	},
	computed: {
		mine() {
			return this.userStore.userInfo;
		},
		recvRequests() {
			let requests = this.friendStore.requests;
			return requests.filter((req) => req.recvId == this.mine.id);
		},
		sendRequests() {
			let requests = this.friendStore.requests;
			return requests.filter((req) => req.sendId == this.mine.id);
		},
		tabPaneStyle() {
			return `height:${this.tabPaneHeight}px`;
		}
	},
	mounted() {
		this.tabPaneHeight = this.$refs.reqTabs.offsetHeight - 80;
	}
}
</script>

<style scoped lang="scss">
.friend-request {
	display: flex;
	flex-direction: column;

	.tabs {
		flex: 1;
	}
}
</style>