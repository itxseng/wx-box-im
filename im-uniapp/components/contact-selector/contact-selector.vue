<template>
	<uni-popup ref="popup" type="bottom">
		<view class="contact-selector">
			<view class="top-bar">
				<view class="tip">{{title}}</view>
				<button class="btn" type="warn" size="mini" @click="onClean()">清空 </button>
				<button class="btn" type="primary" size="mini"
					@click="onOk()">确定({{ checkedFriends.length + checkedGroups.length }})</button>
			</view>
			<view class="checked-items-box" v-if="checkedFriends.length||checkedGroups.length">
				<scroll-view scroll-x="true" scroll-left="120">
					<view class="checked-items">
						<view v-for="(f,idx) in checkedFriends" class="checked-item" :key="idx">
							<head-image :name="f.showNickName" :url="f.headImage" :size="60"></head-image>
						</view>
						<view v-for="(g,idx) in checkedGroups" class="checked-item" :key="idx">
							<head-image :name="g.showGroupName" :url="g.headImageThumb" :size="60"></head-image>
						</view>
					</view>
				</scroll-view>
			</view>
			<view class="search-bar">
				<uni-search-bar v-model="searchText" cancelButton="none" placeholder="搜索"></uni-search-bar>
			</view>
			<tabs :items="tabItems" :current="tabIdx" @change="onTabChange"></tabs>
			<view v-if="tabIdx==0">
				<virtual-scroller :items="friends">
					<template v-slot="{ item }">
						<friend-item :friend="item" :detail="false" @tap="onFriendSwitch(item)">
							<radio @click.stop="onFriendSwitch(item)" :checked="isCheckedFriend(item)" />
						</friend-item>
					</template>
				</virtual-scroller>
			</view>
			<view v-if="tabIdx==1">
				<virtual-scroller :items="friends">
					<template v-slot="{ item }">
						<group-item :group="item" :detail="false" @tap="onGroupSwitch(item)">
							<radio @click.stop="onGroupSwitch(item)" :checked="isCheckedGroup(item)" />
						</group-item>
					</template>
				</virtual-scroller>
			</view>
		</view>
	</uni-popup>
</template>

<script>
import search from '../../uni_modules/uview-plus/libs/config/props/search'

export default {
	name: "contact-selector",
	props: {
		title: {
			type: String,
			default: '选择联系人'
		}
	},
	data() {
		return {
			callback: null,
			searchText: "",
			tabIdx: 0,
			tabItems: ['好友', '群聊'],
			checkedFriends: [],
			checkedGroups: [],
		}
	},
	methods: {
		open(callback) {
			this.callback = callback;
			this.checkedFriends = [];
			this.checkedGroups = [];
			this.searchText = '';
			this.$refs.popup.open();
		},
		onTabChange(idx) {
			this.tabIdx = idx;
		},
		onFriendSwitch(friend) {
			let idx = this.checkedFriends.indexOf(friend);
			if (idx >= 0) {
				this.checkedFriends.splice(idx, 1)
			} else {
				this.checkedFriends.push(friend)
			}
		},
		onGroupSwitch(group) {
			let idx = this.checkedGroups.indexOf(group);
			if (idx >= 0) {
				this.checkedGroups.splice(idx, 1)
			} else {
				this.checkedGroups.push(group)
			}
		},
		onClean() {
			this.checkedFriends = [];
			this.checkedGroups = [];
		},
		onOk() {
			this.$refs.popup.close();
			this.callback({
				friends: this.checkedFriends,
				groups: this.checkedGroups
			});
		},
		isCheckedFriend(friend) {
			return this.checkedFriends.indexOf(friend) >= 0;
		},
		isCheckedGroup(group) {
			return this.checkedGroups.indexOf(group) >= 0;
		}
	},
	computed: {
		friends() {
			return this.friendStore.friends.filter(f => !f.deleted && f.showNickName.includes(this.searchText));
		},
		groups() {
			return this.groupStore.groups.filter(g => !g.quit && g.showGroupName.includes(this.searchText));
		}
	}
}
</script>

<style lang="scss" scoped>
.contact-selector {
	position: relative;
	display: flex;
	flex-direction: column;
	background-color: white;
	border-radius: 15rpx 15rpx 0 0;
	overflow: hidden;

	.scroll-bar {
		height: 60vh;
	}

	.top-bar {
		display: flex;
		align-items: center;
		height: 70rpx;
		padding: 20rpx;

		.tip {
			flex: 1;
		}

		.btn {
			margin-left: 10rpx;
		}
	}

	.checked-items-box {
		padding: 0 20rpx;

		.checked-items {
			display: flex;
			align-items: center;
			height: 90rpx;

			.checked-item {
				padding: 3rpx;
			}
		}
	}

}
</style>