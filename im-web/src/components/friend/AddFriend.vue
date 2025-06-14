<template>
	<el-dialog v-dialogDrag title="添加好友" :visible.sync="dialogVisible" width="460px" :before-close="onClose"
		:close-on-click-modal="false" custom-class="add-friend">
		<el-input placeholder="用户名/昵称/手机号/邮箱,最多展示20条" class="input-with-select" v-model="searchText" size="small"
			@keyup.enter.native="onSearch()" @input="onSearchTextChange">
			<i class="el-icon-search el-input__icon" slot="suffix" @click="onSearch()"> </i>
		</el-input>
		<el-scrollbar v-if="!isNoData" class="scroll-box">
			<div v-for="(user) in users" :key="user.id" v-show="user.id != userStore.userInfo.id">
				<div class="item">
					<div class="avatar">
						<head-image :name="user.nickName" :url="user.headImageThumb" :online="user.online"></head-image>
					</div>
					<div class="friend-info">
						<div class="nick-name">
							<div class="nick-name-text">{{ user.nickName }}</div>
							<div :class="user.online ? 'online-status  online' : 'online-status'">{{
								user.online ? "[在线]" : "[离线]" }}</div>
							<el-tag v-if="user.status == 1" size="mini" type="danger">已注销</el-tag>
							<el-tag v-if="user.isBanned" size="mini" type="danger">已封禁</el-tag>
						</div>
						<div v-if="searchText == user.phone" class="text-info">
							<div>手机:{{ user.phone }}</div>
						</div>
						<div v-else-if="searchText == user.email" class="text-info">
							<div>邮箱:{{ user.email }}</div>
						</div>
						<div v-else class="text-info">
							<div>用户名:{{ user.userName }}</div>
						</div>
					</div>
					<span class="status-tip" v-if="isFriend(user.id)">已添加</span>
					<span class="status-tip" v-else-if="isWaitingApprove(user.id)">等待对方验证</span>
					<el-button type="success" size="mini" v-else @click="onAddFriend(user)">加为好友</el-button>
				</div>
			</div>
		</el-scrollbar>
		<no-data-tip v-else class="no-data-tip" :tip="`未搜索到'${searchText}'相关的用户`"></no-data-tip>
		<friend-apply ref="applyRef"></friend-apply>
	</el-dialog>
</template>

<script>
import HeadImage from '../common/HeadImage.vue'
import NoDataTip from '../common/NoDataTip.vue'
import FriendApply from './FriendApply.vue'

export default {
	name: "addFriend",
	components: { HeadImage, NoDataTip, FriendApply },
	data() {
		return {
			users: [],
			isNoData: false,
			searchText: ""
		}
	},
	props: {
		dialogVisible: {
			type: Boolean
		}
	},
	methods: {
		onClose() {
			this.$emit("close");
		},
		onSearch() {
			if (!this.searchText) {
				this.users = [];
				return;
			}
			this.$http({
				url: "/user/search",
				method: "get",
				params: {
					name: this.searchText
				}
			}).then((data) => {
				this.users = data;
				this.users.forEach((u => u.waitApprove = false));
				this.isNoData = this.users.length == 0;
			})
		},
		onSearchTextChange() {
			this.isNoData = false;
		},
		onAddFriend(user) {
			this.$refs.applyRef.open(user);
		},
		isFriend(userId) {
			return this.friendStore.isFriend(userId);
		},
		isWaitingApprove(userId) {
			return this.friendStore.isInRecvRequest(userId);
		}
	}
}
</script>

<style lang="scss" scoped>
.add-friend {

	.no-data-tip {
		height: 400px;
	}

	.scroll-box {
		height: 400px;

		.item {
			height: 65px;
			display: flex;
			position: relative;
			padding-left: 15px;
			align-items: center;
			padding-right: 25px;

			.friend-info {
				margin-left: 15px;
				flex: 3;
				display: flex;
				flex-direction: column;
				flex-shrink: 0;
				overflow: hidden;

				.nick-name {
					display: flex;
					align-items: center;
					flex-direction: row;

					.nick-name-text {
						font-size: 16px;
						line-height: 25px;
						font-weight: 600;
					}

					.online-status {
						font-size: 12px;
						font-weight: 600;

						&.online {
							color: #5fb878;
						}
					}

					.el-tag {
						margin-left: 3px;
					}
				}

				.text-info {
					display: flex;
					flex-direction: row;
					font-size: 12px;
					line-height: 20px;
				}
			}

			.status-tip {
				color: var(--im-text-color-light);
			}
		}
	}

}
</style>