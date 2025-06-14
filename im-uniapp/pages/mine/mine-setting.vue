<template>
	<view class="page mine-setting">
		<nav-bar title="设置" back></nav-bar>
		<bar-group>
			<switch-bar title="加我为好友时需要验证" :checked="userInfo.isManualApprove"
				@change="onManualApproveChange"></switch-bar>
		</bar-group>
	</view>
</template>

<script>
export default {
	name: "mine-setting",
	data() {

	},
	methods: {
		onManualApproveChange(e) {
			this.$http({
				url: "/user/manualApprove?enabled=" +  e.detail.value,
				method: 'put'
			}).then(() => {
				this.userStore.loadUser()
			})
		}
	},
	computed: {
		userInfo() {
			return this.userStore.userInfo;
		}
	},
}
</script>

<style>
</style>