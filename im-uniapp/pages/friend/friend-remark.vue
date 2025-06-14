<template>
	<view class="page friend-remark">
		<nav-bar title="设置备注" back></nav-bar>
		<uni-forms :modelValue="friendInfo" label-position="top">
			<uni-forms-item label="备注" name="remarkNickName">
				<uni-easyinput type="text" v-model="friendInfo.remarkNickName" :placeholder="friendInfo.nickName" />
			</uni-forms-item>
		</uni-forms>
		<button class="bottom-btn" type="primary" @click="onSubmit()">提交</button>
	</view>
</template>

<script>
export default {
	data() {
		return {
			friendInfo: {}
		}
	},
	methods: {
		onSubmit() {
			let data = {
				friendId: this.friendInfo.id,
				remarkNickName: this.friendInfo.remarkNickName
			}
			this.$http({
				url: '/friend/update/remark',
				method: 'put',
				data: data
			}).then((friend) => {
				// 更新好友列表中的昵称和头像
				this.friendStore.updateFriend(friend);
				// 更新会话中的头像和昵称
				this.chatStore.updateChatFromFriend(friend);
				uni.showToast({
					title: "修改成功",
					icon: "none"
				})
			})
		}
	},
	onLoad(options) {
		let id = options.id;
		let friend = this.friendStore.friends.find((f) => f.id == id);
		this.friendInfo = JSON.parse(JSON.stringify(friend));
	}
}
</script>

<style lang="scss" scoped>
.friend-remark {
	padding: 30rpx;

}
</style>