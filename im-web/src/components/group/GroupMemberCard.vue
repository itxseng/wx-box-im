<template>
	<div class="group-member-card">
		<head-image :id="member.userId" :name="member.showNickName" :url="member.headImage" :size="40"
			:online="member.online"></head-image>
		<div class="name">{{ member.showNickName }}</div>
	</div>
</template>

<script>
import HeadImage from "../common/HeadImage.vue";

export default {
	name: "groupMemberCard",
	components: { HeadImage },
	data() {
		return {};
	},
	props: {
		member: {
			type: Object,
			required: true
		}
	}
}
</script>

<style lang="scss" scoped>
.group-member-card {
	display: flex;
	flex-direction: column;
	align-items: center;
	width: 54px;

	.name {
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
</style>
