<template>
	<div class="chat-group-side">
		<chat-group-member v-if="showAllMembers" @back="showAllMembers = false" :group="group"
			:groupMembers="showMembers"></chat-group-member>
		<div v-else>
			<div class="member-area">
				<div class="member-items">
					<div v-for="(member, idx) in showMembers" :key="member.id">
						<group-member-item v-if="idx < showMaxIdx" class="member-item" :group="group"
							:groupMembers="groupMembers" :member="member" type="card"></group-member-item>
					</div>
					<div class="member-tools">
						<div class="tool-btn" title="邀请好友进群聊" @click="onInvite()">
							<i class="el-icon-plus"></i>
						</div>
						<div class="tool-text">邀请</div>
						<add-group-member ref="addGroupMember" :groupId="group.id" :members="groupMembers"
							@reload="$emit('reload')"></add-group-member>
					</div>
					<div class="member-tools" v-if="isOwner || isManager">
						<div class="tool-btn" title="选择成员移出群聊" @click="onRemove()">
							<i class="el-icon-minus"></i>
						</div>
						<div class="tool-text">移除</div>
						<group-member-selector ref="removeSelector" title="选择成员进行移除" :group="group"
							@complete="onRemoveComplete"></group-member-selector>
					</div>
					<div class="member-tools" v-if="isOwner || isManager">
						<div class="tool-btn" title="禁言" @click="onMuted()">
							<span class="icon iconfont icon-chat-muted" style="font-size: 16px"></span>
						</div>
						<div class="tool-text">禁言</div>
						<group-member-selector ref="mutedSelector" title="选择成员进行禁言" :group="group"
							@complete="onMutedComplete"></group-member-selector>
					</div>
					<div class="member-tools" v-if="isOwner || isManager">
						<div class="tool-btn" title="解除禁言" @click="onUnmuted()">
							<span class="icon iconfont icon-chat-unmuted" style="font-size: 16px"></span>
						</div>
						<div class="tool-text">解除禁言</div>
						<group-member-selector ref="unmutedSelector" title="选择成员进行解除禁言" :group="group"
							@complete="onUnmutedComplete"></group-member-selector>
					</div>
				</div>
				<div class="more-member" @click="onShowMoreMember">查看全部群成员{{ showMembers.length }}人 ></div>
			</div>
			<el-divider v-if="!group.quit" content-position="center"></el-divider>
			<el-form labelPosition="top" class="form" :model="group" size="small">
				<el-form-item label="全员禁言" title="开启全员禁言后，仅群主可以发言">
					<el-switch :disabled="(!isOwner && !isManager) || !editing" v-model="group.isMuted"> </el-switch>
				</el-form-item>
				<el-form-item label="群聊名称">
					<el-input v-model="group.name" :disabled="(!isOwner && !isManager) || !editing"
						maxlength="20"></el-input>
				</el-form-item>
				<el-form-item label="群主">
					<el-input :value="ownerName" disabled></el-input>
				</el-form-item>
				<el-form-item label="群公告">
					<el-input v-model="group.notice" :disabled="(!isOwner && !isManager) || !editing" type="textarea"
						maxlength="1024"></el-input>
				</el-form-item>
				<el-form-item label="备注">
					<el-input v-model="group.remarkGroupName" :disabled="!editing" maxlength="20"></el-input>
				</el-form-item>
				<el-form-item label="我在本群的昵称">
					<el-input v-model="group.remarkNickName" :disabled="!editing" maxlength="20"></el-input>
				</el-form-item>
				<div v-show="!group.quit" class="btn-group">
					<el-button v-if="editing" type="success" @click="onSaveGroup()">保存</el-button>
					<el-button v-if="!editing" type="primary" @click="editing = !editing">编辑</el-button>
					<el-button type="danger" v-show="!isOwner" @click="onQuit()">退出群聊</el-button>
				</div>
			</el-form>
		</div>
	</div>
</template>

<script>
import AddGroupMember from '../group/AddGroupMember.vue';
import GroupMemberItem from '../group/GroupMemberItem.vue';
import GroupMemberSelector from '../group/GroupMemberSelector.vue';
import ChatGroupMember from './ChatGroupMember.vue';

export default {
	name: "chatGroupSide",
	components: {
		AddGroupMember,
		GroupMemberItem,
		GroupMemberSelector,
		ChatGroupMember
	},
	data() {
		return {
			editing: false,
			showAllMembers: false,
			group: {}
		}
	},
	props: {
		groupId: {
			type: Number
		},
		groupMembers: {
			type: Array
		}
	},
	methods: {
		onClose() {
			this.$emit('close');
		},
		onInvite() {
			this.$refs.addGroupMember.open();
		},
		onRemove() {
			// 群主和自己不显示
			let hideIds = [this.group.ownerId, this.mine.id];
			// 只有群主可以移除管理员
			if (!this.isOwner) {
				hideIds = hideIds.concat(this.managerIds);
			}
			this.$refs.removeSelector.open(50, [], [], hideIds);
		},
		onRemoveComplete(members) {
			let userIds = members.map(m => m.userId);
			let data = {
				groupId: this.group.id,
				userIds: userIds
			}
			this.$http({
				url: "/group/members/remove",
				method: 'delete',
				data: data
			}).then(() => {
				this.$emit('reload');
				this.$message.success(`您移除了${userIds.length}位成员`);
			})
		},
		onMuted() {
			// 群主和自己不显示
			let hideIds = [this.group.ownerId, this.mine.id];
			// 只有群主可以禁言管理员
			if (!this.isOwner) {
				hideIds = hideIds.concat(this.managerIds);
			}
			// 已禁言的用户不可选中
			let lockedIds = this.groupMembers.filter(m => m.isMuted).map(m => m.userId);
			this.$refs.mutedSelector.open(50, [], lockedIds, hideIds);
		},
		onMutedComplete(members) {
			let userIds = members.map(m => m.userId);
			let data = {
				groupId: this.group.id,
				userIds: userIds,
				isMuted: true
			}
			let tip = `您对${userIds.length}位成员进行了禁言`;
			this.sendMemberMuted(data, tip);
		},
		onUnmuted() {
			// 过滤掉未禁言的用户
			let hideIds = this.groupMembers.filter(m => !m.isMuted).map(m => m.userId)
			// 只有群主可以解除管理员的禁言
			if (!this.isOwner) {
				hideIds = hideIds.concat(this.managerIds);
			}
			this.$refs.unmutedSelector.open(50, [], [], hideIds);
		},
		onUnmutedComplete(members) {
			let userIds = members.map(m => m.userId);
			let data = {
				groupId: this.group.id,
				userIds: userIds,
				isMuted: false
			}
			let tip = `您解除了${userIds.length}位成员的禁言`;
			this.sendMemberMuted(data, tip);
		},
		onSaveGroup() {
			let vo = this.group;
			this.$http({
				url: "/group/modify",
				method: "put",
				data: vo
			}).then(group => {
				this.editing = !this.editing
				this.groupStore.updateGroup(group);
				this.$emit('reload');
				this.$message.success("修改成功");
			})
		},
		onShowMoreMember() {
			this.showAllMembers = true;
		},
		onQuit() {
			this.$confirm('退出群聊后将不再接受群里的消息，确认退出吗？', '确认退出?', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			}).then(() => {
				this.$http({
					url: `/group/quit/${this.group.id}`,
					method: 'delete'
				}).then(() => {
					this.groupStore.removeGroup(this.group.id);
					this.chatStore.removeGroupChat(this.group.id);
				});
			})
		},
		sendMemberMuted(data, tip) {
			this.$http({
				url: "/group/members/muted",
				method: "put",
				data: data
			}).then(() => {
				this.$emit('reload');
				this.$message.success(tip)
			})
		}
	},
	computed: {
		mine() {
			return this.userStore.userInfo;
		},
		ownerName() {
			let member = this.groupMembers.find((m) => m.userId == this.group.ownerId);
			return member && member.showNickName;
		},
		isOwner() {
			return this.group.ownerId == this.mine.id;
		},
		isManager() {
			let userId = this.mine.id;
			let m = this.groupMembers.find((m) => m.userId == userId);
			return m && m.isManager;
		},
		managerIds() {
			return this.groupMembers.filter(m => m.isManager).map(m => m.userId)
		},
		showMembers() {
			return this.groupMembers.filter((m) => !m.quit)
		},
		showMaxIdx() {
			return this.isOwner || this.isManager ? 11 : 14;
		}
	},
	mounted() {
		// vuex的数据不允许编辑，这里拷贝一份数据
		let group = this.groupStore.findGroup(this.groupId);
		this.group = JSON.parse(JSON.stringify(group));
	}

}
</script>

<style lang="scss">
.chat-group-side {
	position: relative;
	height: 100%;

	.el-divider--horizontal {
		margin: 0;
	}

	.el-form-item {
		margin-bottom: 0px !important;
	}

	.member-area {
		padding: 10px;

		.member-items {
			display: flex;
			align-items: center;
			flex-wrap: wrap;
			font-size: 14px;
			text-align: center;

			.member-item {
				margin-left: 5px;
			}

			.member-tools {
				display: flex;
				flex-direction: column;
				align-items: center;
				width: 54px;
				margin-left: 5px;

				.tool-btn {
					width: 38px;
					height: 38px;
					line-height: 38px;
					border: var(--im-border);
					font-size: 14px;
					cursor: pointer;
					box-sizing: border-box;

					&:hover {
						border: #aaaaaa solid 1px;
					}
				}

				.tool-text {
					font-size: 12px;
					text-align: center;
					width: 100%;
					height: 30px;
					line-height: 30px;
					white-space: nowrap;
					text-overflow: ellipsis;
					overflow: hidden
				}
			}
		}

		.more-member {
			flex: 1;
			font-size: var(--im-font-size);
			color: var(--im-text-color-light);
			cursor: pointer;
			padding: 5px;
			text-align: center;
		}
	}

	.form {
		text-align: left;
		padding: 10px;
		height: 30%;

		.el-form-item {
			margin-bottom: 12px;

			.el-form-item__label {
				padding: 0;
				line-height: 30px;
			}

			.el-textarea__inner {
				min-height: 100px !important;
			}
		}

		.el-input__inner,
		.el-textarea__inner {
			color: var(--im-text-color) !important;
		}

		.btn-group {
			text-align: center;
			margin-top: 12px;
		}
	}

}
</style>