import { defineStore } from 'pinia';
import http from '@/common/request';

export default defineStore('groupStore', {
	state: () => {
		return {
			groups: []
		}
	},
	actions: {
		setGroups(groups) {
			this.groups = groups;
		},
		addGroup(group) {
			if (this.groups.some((g) => g.id == group.id)) {
				this.updateGroup(group);
			} else {
				this.groups.unshift(group);
			}
		},
		removeGroup(id) {
			this.groups.filter(g => g.id == id).forEach(g => g.quit = true);
		},
		updateGroup(group) {
			let g = this.findGroup(group.id);
			Object.assign(g, group);
		},
		updateTopMessage(id, topMessage) {
			let group = this.findGroup(id);
			if (group) {
				group.topMessage = topMessage;
			}
		},
		clear() {
			this.groups = [];
		},
		loadGroup() {
			return new Promise((resolve, reject) => {
				http({
					url: '/group/list',
					method: 'GET'
				}).then((groups) => {
					this.setGroups(groups);
					resolve();
				}).catch((res) => {
					reject(res);
				})
			});
		}
	},
	getters: {
		findGroup: (state) => (id) => {
			return state.groups.find(g => g.id == id);
		},
		isGroup: (state) => (id) => {
			return state.groups.filter(g => !g.quit).some(g => g.id == id);
		},
	}
})