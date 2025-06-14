<template>
	<view class="tab-page friend">
		<nav-bar title="好友" add search @add="onAddNewFriends" @search="onSearch"></nav-bar>
		<view class="nav-bar" v-if="showSearch">
			<view class="nav-search">
				<uni-search-bar v-model="searchText" :focus="true" radius="100" cancelButton="none"
					placeholder="点击搜索好友"></uni-search-bar>
			</view>
		</view>
		<view class="friend-items">
			<up-index-list :index-list="friendIdx" :sticky="false">
				<!-- 顶部列表: 新的朋友、ai机器人等 -->
				<view class="top-item" @click="onClickNewFriend">
					<head-image size="small" url="/static/image/new_friend.png"></head-image>
					<view class="item-info">
						<view class="item-name">新的朋友</view>
						<uni-badge :max-num="99" :text="recvCount" />
					</view>
				</view>
				<template v-for="(friends, i) in friendGroups">
					<up-index-item>
						<up-index-anchor  :text="friendIdx[i] == '*' ? '在线好友' : friendIdx[i]"></up-index-anchor>
						<view v-for="(friend, idx) in friends" :key="idx">
							<friend-item :friend="friend"></friend-item>
						</view>
					</up-index-item>
				</template>
			</up-index-list>
		</view>
		<view class="friend-tip" v-if="!hasFriends">
			温馨提示：您现在还没有任何好友，快点击右上方'+'按钮添加好友吧~
		</view>
	</view>
</template>

<script>
import { pinyin } from 'pinyin-pro';

export default {
	data() {
		return {
			showSearch: false,
			searchText: ''
		}
	},
	methods: {
		onAddNewFriends() {
			uni.navigateTo({
				url: "/pages/friend/friend-add"
			})
		},
		onSearch() {
			this.showSearch = !this.showSearch;
			this.searchText = "";
		},
		onClickNewFriend() {
			uni.navigateTo({
				url: "/pages/friend/friend-request"
			})
		},
		firstLetter(strText) {
			// 使用pinyin-pro库将中文转换为拼音
			let pinyinOptions = {
				toneType: 'none', // 无声调
				type: 'normal' // 普通拼音
			};
			let pyText = pinyin(strText, pinyinOptions);
			return pyText[0];
		},
		isEnglish(character) {
			return /^[A-Za-z]+$/.test(character);
		}
	},
	computed: {
		mine() {
			return this.userStore.userInfo;
		},
		friendGroupMap() {
			// 按首字母分组
			let groupMap = new Map();
			this.friendStore.friends.forEach((f) => {
				if (f.deleted || (this.searchText && !f.showNickName.includes(this.searchText))) {
					return;
				}
				let letter = this.firstLetter(f.showNickName).toUpperCase();
				// 非英文一律为#组
				if (!this.isEnglish(letter)) {
					letter = "#"
				}
				if (f.online) {
					letter = '*'
				}
				if (groupMap.has(letter)) {
					groupMap.get(letter).push(f);
				} else {
					groupMap.set(letter, [f]);
				}
			})
			// 排序
			let arrayObj = Array.from(groupMap);
			arrayObj.sort((a, b) => {
				// #组在最后面
				if (a[0] == '#' || b[0] == '#') {
					return b[0].localeCompare(a[0])
				}
				return a[0].localeCompare(b[0])
			})
			groupMap = new Map(arrayObj.map(i => [i[0], i[1]]));
			return groupMap;
		},
		friendIdx() {
			return Array.from(this.friendGroupMap.keys());
		},
		friendGroups() {
			return Array.from(this.friendGroupMap.values());
		},
		recvCount() {
			let requests = this.friendStore.requests;
			return requests.filter((req) => req.recvId == this.mine.id).length;
		},
		hasFriends() {
			return this.friendStore.friends.some(f => !f.deleted);
		}
	},
	watch: {
		recvCount: {
			handler() {
				this.$badge.refreshFriendBadge();
			}
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
.friend {
	position: relative;
	display: flex;
	flex-direction: column;

	:deep(.u-index-anchor) {
		height: 60rpx !important;
		background-color: unset !important;
		border-bottom: none !important;
	}

	:deep(.u-index-anchor__text) {
		color: $im-text-color !important;
	}
	
	:deep(.u-index-list__letter__item) {
		width: 40rpx !important;
		height: 32rpx !important;
	}

	:deep(.u-index-list__letter__item__index) {
		font-size: $im-font-size-smaller !important;
	}

	.friend-tip {
		position: absolute;
		top: 400rpx;
		padding: 50rpx;
		text-align: left;
		line-height: 50rpx;
		font-size: 30rpx;
		color: $im-text-color-lighter;
	}

	.top-item {
		height: 90rpx;
		display: flex;
		margin-bottom: 1rpx;
		position: relative;
		padding: 10rpx;
		padding-left: 20rpx;
		align-items: center;
		background-color: white;
		white-space: nowrap;

		.item-info {
			flex: 1;
			display: flex;
			padding-left: 20rpx;
			align-items: center;

			.item-name {
				font-size: $im-font-size;
				white-space: nowrap;
				overflow: hidden;
			}
		}
	}

	.friend-items {
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