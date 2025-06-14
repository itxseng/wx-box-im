import { defineStore } from 'pinia';
import http from '../common/request'

export default defineStore('userStore', {
	state: () => {
		return {
			userInfo: {}
		}
	},
	actions: {
		setUserInfo(userInfo) {
			this.userInfo = userInfo;
		},
		clear() {
			this.userInfo = {};
		},
		loadUser() {
			return new Promise((resolve, reject) => {
				http({
					url: '/user/self',
					method: 'GET'
				}).then((userInfo) => {
					console.log(userInfo)
					this.setUserInfo(userInfo);
					if (userInfo.deleted || userInfo.isBanned) {
						reject("账户已注销或被封禁");
					} else {
						resolve();
					}
				}).catch((res) => {
					reject(res);
				});
			})
		}
	}
})