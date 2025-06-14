<template>
	<view @click="showMenu()">
		<slot></slot>
		<popup-menu ref="refMenu" :items="menuItems" @select="onSelectMenuItem"></popup-menu>
	</view>
</template>

<script>
import UNI_APP from '@/.env.js'

export default {
	name: "image-upload",
	data() {
		return {
			menuItems: [{
				sourceType: 'album',
				name: "从本机选取"
			},
			{
				sourceType: 'camera',
				name: "使用相机拍摄"
			}
			]
		}
	},
	props: {
		maxSize: {
			type: Number,
			default: 50
		},
		onBefore: {
			type: Function,
			default: null
		},
		onSuccess: {
			type: Function,
			default: null
		},
		onError: {
			type: Function,
			default: null
		}
	},
	methods: {
		showMenu() {
			this.$refs.refMenu.open();
		},
		async onSelectMenuItem(item) {
			console.log(item)
			if(item.sourceType=='album' && !await this.$permission.storage()){
				console.log("相册权限未获得")
				return
			}
			if(item.sourceType=='camera' && !await this.$permission.camera()){
				console.log("相机权限未获得")
				return
			}
			uni.chooseVideo({
				sourceType: [item.sourceType],
				extension: [".mp4"],
				compressed: false,
				success: (file) => {
					if (file.size > this.maxSize * 1024 * 1024) {
						uni.showToast({
							title: `视频文件不得大于${this.maxSize}M`,
							icon: "none"
						})
						return;
					}
					if (!this.onBefore || this.onBefore(file)) {
						// 调用上传图片的接口
						this.uploadVideo(file);
					}
				}
			})
		},
		uploadVideo(file) {
			uni.uploadFile({
				url: UNI_APP.BASE_URL + '/video/upload',
				header: {
					accessToken: uni.getStorageSync("loginInfo").accessToken
				},
				filePath: file.tempFilePath, // 要上传文件资源的路径
				name: 'file',
				success: (res) => {
					let data = JSON.parse(res.data);
					if (data.code != 200) {
						uni.showToast({
							icon: "none",
							title: data.message,
						})
						this.onError && this.onError(file, data);
					} else {
						this.onSuccess && this.onSuccess(file, data);
					}
				},
				fail: (err) => {
					console.log(err);
					this.onError && this.onError(file, err);
				}
			})
		}
	}
}
</script>

<style></style>