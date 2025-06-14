import { defineStore } from 'pinia';
import http from '../common/request'
import { TERMINAL_TYPE } from '../common/enums.js'

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
			let f = this.findFriend(friend.id);
			let copy = JSON.parse(JSON.stringify(f));
			Object.assign(f, friend);
			f.online = copy.online;
			f.onlineWeb = copy.onlineWeb;
			f.onlineApp = copy.onlineApp;
		},
		removeFriend(id) {
			this.friends.filter(f => f.id == id).forEach(f => f.deleted = true);
		},
		addFriend(friend) {
			if (this.friends.some((f) => f.id == friend.id)) {
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
			clearTimeout(this.timer);
			this.friends = [];
			this.timer = null;
		},
		loadFriend() {
			return new Promise((resolve, reject) => {
				http({
					url: '/friend/list',
					method: 'GET'
				}).then((friends) => {
					this.setFriends(friends);
					resolve(this.loadRequest());
				}).catch(() => {
					reject();
				})
			});
		},
		loadRequest() {
			return http({
				url: '/friend/request/list',
				method: 'GET'
			}).then((requests) => {
				this.setRequests(requests);
			})
		}
	},
	getters: {
		isFriend: (state) => (userId) => {
			return state.friends.filter((f) => !f.deleted).some((f) => f.id == userId);
		},
		findFriend: (state) => (id) => {
			return state.friends.find((f) => f.id == id);
		},
		findRequest: (state) => (id) => {
			return state.requests.find((req) => req.id == id);
		},
		isInRecvRequest: (state) => (userId) => {
			return state.requests.some((req) => req.recvId == userId);
		},
		isInSendRequest: (state) => (userId) => {
			return state.requests.some((req) => req.sendId == userId);
		}
	}
})