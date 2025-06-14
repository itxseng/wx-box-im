<template>
	<view class="tab-page">
		<nav-bar title="消息" search @search="onSearch()">
			<uni-icons class="drop-menu-btn" type="plusempty" size="24" @click="showDropDownMenu"></uni-icons>
		</nav-bar>
		<view v-if="loading" class="chat-loading">
			<loading :size="50" :mask="false">
				<view>消息接收中...</view>
			</loading>
		</view>
		<view v-if="initializing" class="chat-loading">
			<loading :size="50" :mask="false">
				<view>正在初始化...</view>
			</loading>
		</view>
		<view v-else-if="reconnecting" class="chat-loading">
			<loading :size="50" :mask="false">
				<view>您的网络已断开,正在重连...</view>
			</loading>
		</view>
		<view class="nav-bar" v-if="showSearch">
			<view class="nav-search">
				<uni-search-bar :focus="true" radius="100" v-model="searchText" cancelButton="none"
					placeholder="搜索"></uni-search-bar>
			</view>
		</view>
		<view class="chat-tip" v-if="!loading && chatStore.chats.length == 0">
			温馨提示：您现在还没有任何聊天消息，快跟您的好友发起聊天吧~
		</view>
		<scroll-view class="scroll-bar" v-else scroll-with-animation="true" scroll-y="true">
			<view v-for="(chat, index) in chatStore.chats" :key="index">
				<long-press-menu v-if="isShowChat(chat)" :items="chatMenuItems"
					@select="onSelectChatMenu($event, index)">
					<chat-item :chat="chat" :index="index"></chat-item>
				</long-press-menu>
			</view>
		</scroll-view>
		<popup-modal ref="modal"></popup-modal>
		<drop-down-menu ref="dropDownMenu" :items="drowMenuItems" @select="onSelectDropDownMenu"> </drop-down-menu>
		<!-- #ifdef H5 -->
		<!-- 插件地址:https://ext.dcloud.net.cn/plugin?id=17458 -->
		<cshaptx4869-scancode ref="scaner" @success="onScanOk" @fail="onScanFail"></cshaptx4869-scancode>
		<!-- #endif -->
	</view>
</template>

<script>
import useChatStore from '@/store/chatStore.js'

export default {
	data() {
		return {
			showSearch: false,
			searchText: "",
			chatMenuItems: [{
					key: 'DELETE',
					name: '删除该聊天',
					icon: 'trash',
					color: '#e64e4e'
				},
				{
					key: 'TOP',
					name: '置顶该聊天',
					icon: 'arrow-up'
				}
			],
			drowMenuItems: [{
				key: 'ADD_FRIEND',
				name: '添加好友',
				icon: 'icon-add-friend',
			}, {
				key: 'CREATE_GROUP',
				name: '创建群聊',
				icon: 'icon-create-group',
			}, {
				key: 'SCAN',
				name: '扫 一 扫',
				icon: 'icon-scan',
			}]
		}
	},
	methods: {
		onSelectChatMenu(item, chatIdx) {
			switch (item.key) {
				case 'DELETE':
					this.removeChat(chatIdx);
					break;
				case 'TOP':
					this.moveToTop(chatIdx);
					break;
				default:
					break;
			}
		},
		onSelectDropDownMenu(item) {
			switch (item.key) {
				case 'ADD_FRIEND':
					this.addNewFriend();
					break;
				case 'CREATE_GROUP':
					this.createNewGroup();
					break;
				case 'SCAN':
					this.scanQrcode();
					break;
				default:
					break;
			}
		},
		addNewFriend() {
			uni.navigateTo({
				url: "/pages/friend/friend-add"
			})
		},
		createNewGroup() {
			uni.navigateTo({
				url: "/pages/group/group-edit"
			})
		},
		removeChat(chatIdx) {
			this.chatStore.removeChat(chatIdx);
		},
		moveToTop(chatIdx) {
			this.chatStore.moveTop(chatIdx);
		},

		showDropDownMenu() {
			this.$refs.dropDownMenu.open();
		},
		scanQrcode() {
			// #ifdef H5
			this.$refs.scaner.open()
			// #endif
			// #ifndef H5
			uni.scanCode({
				success: (res) => {
					this.onScanOk(res);
				},
				fail() {
					console.log("fail");
				}
			})
			// #endif
		},
		onScanOk(res) {
			let val = res.result;
			if (!this.$url.isHttpURL(val)) {
				// 不是http地址，则直接弹出扫描结果
				this.$refs.modal.open({
					title: '扫描结果',
					content: val,
					confirmText: '确定',
					showCancel: false
				});
				return
			}
			let params = this.$url.getQueryParams(val);
			if (params.scan && params.userId) {
				// 跳转到用户页面
				uni.navigateTo({
					url: "/pages/common/user-info?id=" + params.userId
				})
			} else if (params.scan && params.groupId) {
				// 跳转到群聊页面
				uni.navigateTo({
					url: "/pages/group/group-info?id=" + params.groupId
				})
			} else {
				// 跳转到扫描出来的url
				const linkUrl = encodeURIComponent(val);
				uni.navigateTo({
					url: '/pages/common/external-link?url=' + linkUrl
				});
			}
		},
		onScanFail(e) {
			let errMessage = e.errMsg;
			if (e.errName == 'InsecureContextError') {
				errMessage = "请在https环境使用扫码功能"
			}
			uni.showToast({
				title: errMessage,
				icon: 'none'
			})
		},
		isShowChat(chat) {
			if (chat.delete) {
				return false;
			}
			return !this.searchText || chat.showName.includes(this.searchText)
		},
		onSearch() {
			this.showSearch = !this.showSearch;
			this.searchText = "";
		},
		checkNotifyPermisson() {
			// #ifdef APP-PLUS  
			if (plus.os.name == 'Android') { // 判断是Android
				var main = plus.android.runtimeMainActivity();
				var pkName = main.getPackageName();
				var uid = main.getApplicationInfo().plusGetAttribute("uid");
				var NotificationManagerCompat = plus.android.importClass(
					"android.support.v4.app.NotificationManagerCompat");
				//android.support.v4升级为androidx
				if (NotificationManagerCompat == null) {
					NotificationManagerCompat = plus.android.importClass("androidx.core.app.NotificationManagerCompat");
				}
				var areNotificationsEnabled = NotificationManagerCompat.from(main).areNotificationsEnabled();
				// 未开通‘允许通知’权限，则弹窗提醒开通，并点击确认后，跳转到系统设置页面进行设置  
				if (!areNotificationsEnabled) {
					this.$refs.modal.open({
						title: '系统消息',
						content: '您还没有开启通知权限，无法接受到消息通知！',
						confirmText: '去设置',
						success: () => {
							var Intent = plus.android.importClass('android.content.Intent');
							var Build = plus.android.importClass("android.os.Build");
							//android 8.0引导  
							if (Build.VERSION.SDK_INT >= 26) {
								var intent = new Intent('android.settings.APP_NOTIFICATION_SETTINGS');
								intent.putExtra('android.provider.extra.APP_PACKAGE', pkName);
							} else if (Build.VERSION.SDK_INT >= 21) { //android 5.0-7.0  
								var intent = new Intent('android.settings.APP_NOTIFICATION_SETTINGS');
								intent.putExtra("app_package", pkName);
								intent.putExtra("app_uid", uid);
							} else { //(<21)其他--跳转到该应用管理的详情页  
								var Settings = plus.android.importClass("android.provider.Settings");
								''
								intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
								var uri = Uri.fromParts("package", mainActivity.getPackageName(), null);
								intent.setData(uri);
							}
							// 跳转到该应用的系统通知设置页  
							main.startActivity(intent);

						}
					});
				}
			} else if (plus.os.name == 'iOS') { // 判断是ISO
				var isOn = undefined;
				var types = 0;
				var app = plus.ios.invoke('UIApplication', 'sharedApplication');
				var settings = plus.ios.invoke(app, 'currentUserNotificationSettings');
				if (settings) {
					types = settings.plusGetAttribute('types');
					plus.ios.deleteObject(settings);
				} else {
					types = plus.ios.invoke(app, 'enabledRemoteNotificationTypes');
				}
				plus.ios.deleteObject(app);
				isOn = (0 != types);
				if (isOn == false) {
					this.$refs.modal.open({
						title: '系统消息',
						content: '您还没有开启通知权限，无法接受到消息通知！',
						confirmText: '去设置',
						success: () => {
							var app = plus.ios.invoke('UIApplication', 'sharedApplication');
							var setting = plus.ios.invoke('NSURL', 'URLWithString:', 'app-settings:');
							plus.ios.invoke(app, 'openURL:', setting);
							plus.ios.deleteObject(setting);
							plus.ios.deleteObject(app);
						}
					});
				}
			}
			// #endif  
		}
	},
	computed: {
		unreadCount() {
			let count = 0;
			this.chatStore.chats.forEach(chat => {
				if (!chat.delete) {
					count += chat.unreadCount;
				}
			})
			return count;
		},
		loading() {
			return this.chatStore.isLoading();
		},
		initializing() {
			return !getApp().$vm.isInit;
		},
		reconnecting() {
			return getApp().$vm.reconnecting;
		}
	},
	watch: {
		unreadCount(newCount, oldCount) {
			this.$badge.refreshChatBadge();
		}
	},
	onLoad() {
		// 检查通知权限
		setTimeout(() => this.checkNotifyPermisson(), 3000)
	},
	onShow() {
		// 每个tab页都要更新一下角标数量
		this.$badge.refreshFriendBadge();
		this.$badge.refreshChatBadge();
	}
}
</script>

<style lang="scss">
.tab-page {
	position: relative;
	display: flex;
	flex-direction: column;

	.drop-menu-btn {
		margin: 0 8rpx;
	}

	.chat-tip {
		position: absolute;
		top: 400rpx;
		padding: 50rpx;
		text-align: left;
		line-height: 50rpx;
		font-size: 30rpx;
		color: $im-text-color-lighter;
	}

	.chat-loading {
		display: block;
		width: 100%;
		height: 100rpx;
		line-height: 100rpx;
		background: white;
		color: $im-text-color-lighter;

		.loading-box {
			position: relative;
		}
	}

	.scroll-bar {
		flex: 1;
		height: 100%;
	}
}
</style>