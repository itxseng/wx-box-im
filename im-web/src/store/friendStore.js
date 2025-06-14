import { defineStore } from 'pinia';
import http from '../api/httpRequest.js'
import { TERMINAL_TYPE } from "../api/enums.js"

export default defineStore('friendStore', {
	state: () => {
		return {
			friends: [], // 好友列表
			requests: [], // 好友申请列表
			timer: null
		}
	},
	actions: {
		setFriends(friends) {
			this.friends = friends;
		},
		setRequests(requests) {
			this.requests = requests;
		},
		addRequest(request) {
			this.requests.unshift(request);
		},
		removeRequest(id) {
			for (let idx in this.requests) {
				if (this.requests[idx].id == id) {
					this.requests.splice(idx, 1);
					break;
				}
			}
		},
		updateFriend(friend) {
			this.friends.forEach((f, index) => {
				if (f.id == friend.id) {
					// 拷贝属性
					let online = this.friends[index].online;
					Object.assign(this.friends[index], friend);
					this.friends[index].online = online;
				}
			})
		},
		removeFriend(id) {
			this.friends.filter(f => f.id == id).forEach(f => f.deleted = true);
		},
		addFriend(friend) {
			if (this.friends.some(f => f.id == friend.id)) {
				this.updateFriend(friend)
			} else {
				this.friends.unshift(friend);
			}
		},
		updateOnlineStatus(onlineData) {
			let friend = this.findFriend(onlineData.userId);
			if (onlineData.terminal == TERMINAL_TYPE.WEB) {
				friend.onlineWeb = onlineData.online;
			} else if (onlineData.terminal == TERMINAL_TYPE.APP) {
				friend.onlineApp = onlineData.online;
			}
			friend.online = friend.onlineWeb || friend.onlineApp;
		},
		clear() {
			this.timer && clearTimeout(this.timer);
			this.friends = [];
			this.timer = null;
		},
		loadFriend() {
			return new Promise((resolve, reject) => {
				http({
					url: '/friend/list',
					method: 'GET'
				}).then(async (friends) => {
					this.setFriends(friends);
					// 加载好友申请列表
					const requests = await http({
						url: '/friend/request/list',
						method: 'GET'
					});
					this.setRequests(requests);
					resolve();
				}).catch(() => {
					reject();
				})
			});
		}
	},
	getters: {
		isFriend: (state) => (userId) => {
			return state.friends.filter((f) => !f.deleted).some((f) => f.id == userId);
		},
		findFriend: (state) => (userId) => {
			return state.friends.find((f) => f.id == userId);
		},
		isInRecvRequest: (state) => (userId) => {
			return state.requests.some((req) => req.recvId == userId);
		},
		isInSendRequest: (state) => (userId) => {
			return state.requests.some((req) => req.sendId == userId);
		}
	}
});