import useChatStore from '@/store/chatStore.js'
import useFriendStore from '@/store/friendStore.js'
import useUserStore from '@/store/userStore.js'


const refreshFriendBadge = () => {
	let friendStore = useFriendStore();
	let userStore = useUserStore();
	let userId = userStore.userInfo.id;
	let count = friendStore.requests.filter((req) => req.recvId == userId).length;
	setBadge(1, count);
}

const refreshChatBadge = () => {
	let chatStore = useChatStore();
	let userStore = useUserStore();
	let count = 0;
	chatStore.chats.forEach(chat => {
		if (!chat.delete) {
			count += chat.unreadCount;
		}
	})
	setBadge(0, count);
	// Android平台调用原生API设置角标
	// #ifdef APP-PLUS
	if (uni.getSystemInfoSync().platform === 'android') {
		plus.runtime.setBadgeNumber(count);
	}
	// #endif
}

const setBadge = (idx, count) => {
	if (count > 0) {
		uni.setTabBarBadge({
			index: idx,
			text: count + ""
		})
	} else {
		uni.removeTabBarBadge({
			index: idx,
			complete: () => {}
		})
	}
}

export {
	refreshChatBadge,
	refreshFriendBadge
}