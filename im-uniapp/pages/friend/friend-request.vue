<template>
	<view class="page friend-request">
		<nav-bar title="新的朋友" back></nav-bar>
		<view class="tab-control">
			<tabs :items="items" @change="onChange" :current="tabIdx"></tabs>
		</view>
		<view v-if="tabIdx === 0" class="content">
			<view v-if="recvRequests.length>0" class="request-item" v-for="request in recvRequests" :key="request.id">
				<friend-request-item :request="request"></friend-request-item>
			</view>
			<no-data-tip v-else class="no-data-tip" tip="您未收到申请"></no-data-tip>
		</view>
		<view v-if="tabIdx === 1" class="content">
			<view v-if="sendRequests.length>0" class="request-item" v-for="request in sendRequests" :key="request.id">
				<friend-request-item :request="request"></friend-request-item>
			</view>
			<no-data-tip v-else class="no-data-tip" tip="您未发起申请"></no-data-tip>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			tabIdx: 0
		}
	},
	methods: {
		onChange(idx){
			console.log(idx)
			this.tabIdx = idx
		}
	},
	computed: {
		mine() {
			return this.userStore.userInfo;
		},
		recvRequests() {
			return this.friendStore.requests.filter((req) => req.recvId == this.mine.id);
		},
		sendRequests() {
			return this.friendStore.requests.filter((req) => req.sendId == this.mine.id);
		},
		items() {
			return [`我收到的(${this.recvRequests.length})`, `我发起的(${this.sendRequests.length})`];
		}
	}
}
</script>

<style lang="scss">
.friend-request {
	display: flex;
	flex-direction: column;
	background-color: white;
	
	.tab-control {
		padding: 20rpx;
	}

	.content {
		position: relative;
		flex: 1;

		.no-data-tip {
			height: 100%;
		}
	}
}
</style>