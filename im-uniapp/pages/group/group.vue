<template>
	<view class="tab-page group">
		<nav-bar title="群聊" add search @add="onCreateNewGroup" @search="onSearch()"></nav-bar>
		<view class="nav-bar" v-if="showSearch">
			<view class="nav-search">
				<uni-search-bar v-model="searchText" cancelButton="none" radius="100" focus="true"
					placeholder="点击搜索群聊"></uni-search-bar>
			</view>
		</view>
		<view class="group-tip" v-if="!hasGroups">
			温馨提示：您现在还没有加入任何群聊，点击右上方'+'按钮可以创建群聊哦~
		</view>
		<view class="group-items" v-else>
			<scroll-view class="scroll-bar" scroll-with-animation="true" scroll-y="true">
				<view v-for="group in groupStore.groups" :key="group.id">
					<group-item v-if="!group.quit && group.showGroupName.includes(searchText)"
						:group="group"></group-item>
				</view>
			</scroll-view>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			showSearch: false,
			searchText: ""
		}
	},
	methods: {
		onCreateNewGroup() {
			uni.navigateTo({
				url: "/pages/group/group-edit"
			})
		},
		onSearch() {
			this.showSearch = !this.showSearch;
			this.searchText = "";
		}
	},
	computed: {
		hasGroups() {
			return this.groupStore.groups.some((g) => !g.quit);
		}
	},
	onShow() {
		// 每个tab页都要更新一下角标数量
		this.$badge.refreshFriendBadge();
		this.$badge.refreshChatBadge();
	}
}
</script>

<style lang="scss" scoped>
.group {
	position: relative;
	display: flex;
	flex-direction: column;

	.group-tip {
		position: absolute;
		top: 400rpx;
		padding: 50rpx;
		text-align: left;
		line-height: 50rpx;
		color: $im-text-color-lighter;
		font-size: 30rpx;
	}

	.group-items {
		flex: 1;
		padding: 0;
		overflow: hidden;
		position: relative;

		.scroll-bar {
			height: 100%;
		}

	}
}
</style>