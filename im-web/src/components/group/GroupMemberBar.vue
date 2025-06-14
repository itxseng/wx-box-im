<template>
	<div ref="item" class="group-member-bar" :class="active ? 'active' : ''" :style="{ 'height': height + 'px' }">
		<head-image :size="headImageSize" :name="member.showNickName" :url="member.headImage" :online="member.online">
		</head-image>
		<div class="name" :style="{ 'line-height': height + 'px' }">
			<span class="name-text">{{ member.showNickName }}</span>
			<el-tag class="tag" v-if="mine.id == member.userId" size="mini" type="primary">我</el-tag>
			<el-tag class="tag" v-if="member.userId == group.ownerId" size="mini" type="danger">群主</el-tag>
			<el-tag class="tag" v-if="member.isManager" size="mini" type="primary">管理员</el-tag>
			<el-tag class="tag" v-if="member.isMuted" size="mini" type="warning">禁言中</el-tag>
		</div>
		<slot></slot>
	</div>
</template>
<script>
import HeadImage from "../common/HeadImage.vue";

export default {
	name: "groupMemberBar",
	components: { HeadImage },
	data() {
		return {

		};
	},
	props: {
		group: {
			type: Object,
			required: true
		},
		member: {
			type: Object,
			required: true
		},
		height: {
			type: Number,
			default: 50
		},
		active: {
			type: Boolean,
			default: false
		}
	},
	computed: {
		mine() {
			return this.userStore.userInfo;
		},
		headImageSize() {
			return Math.ceil(this.height * 0.75)
		}
	}
}
</script>


<style lang="scss" scoped>
.group-member-bar {
	display: flex;
	position: relative;
	padding: 0 15px;
	align-items: center;
	white-space: nowrap;
	box-sizing: border-box;

	&:hover {
		background-color: var(--im-background-active);
	}

	&.active {
		background: #E1EAF7;
	}

	.name {
		display: flex;
		align-items: center;
		flex: 1;
		height: 100%;
		margin-left: 10px;

		.name-text {
			text-align: left;
			font-size: var(--im-font-size);
			white-space: nowrap;
			overflow: hidden;
		}

		.tag {
			margin-left: 3px;
			font-size: 10px;
			border-radius: 3px;
		}
	}
}
</style>