<template>
	<div v-show="isShow" class="chat-system-content">
		<el-container>
			<el-header height="50px">
				<span class="back el-icon-back" @click="onBack()"></span>
			</el-header>
			<el-main>
				<span class="title">{{ title }}</span>
				<div v-if="contentType == 0" class="rich-text" v-html="richText"></div>
				<iframe v-else class="ext-link" :src="externLink"></iframe>
			</el-main>
		</el-container>
	</div>
</template>

<script>
import { Base64 } from 'js-base64';

export default {
	name: "chatSystemContent",
	data() {
		return {
			isShow: false,
			title: "",
			contentType: 0,
			richText: "",
			externLink: ""
		}
	},
	methods: {
		show(id, title) {
			this.$http({
				url: '/message/system/content?id=' + id,
				method: 'get',
			}).then(msgInfo => {
				this.title = title;
				this.richText = Base64.decode(msgInfo.richText);
				this.externLink = msgInfo.externLink;
				this.contentType = msgInfo.contentType;
				this.isShow = true;
			})
		},
		onBack() {
			this.isShow = false;
		}
	}
}
</script>

<style lang="scss">
.chat-system-content {
	position: absolute;
	background-color: white;
	width: 100%;
	height: 100%;

	.title {
		font-size: 24px;
		font-weight: bolder;
		text-align: center;
		display: block;
		margin-top: 10px;
	}

	.back {
		position: absolute;
		left: 20px;
		line-height: 50px;
		font-size: 26px;
		cursor: pointer;
	}

	.rich-text {
		padding: 10px 20px 20px 20px;
	}

	.ext-link {
		width: 100%;
		height: 100%;
		border: none;
	}
}
</style>