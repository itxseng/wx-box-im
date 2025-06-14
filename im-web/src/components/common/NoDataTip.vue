<template>
	<div class="no-data-tip">
		<img class="image" :src="require('@/assets/image/no_data.png')" />
		<div class="text">{{ tip }}</div>
	</div>
</template>

<script>
export default {
	name: "noDataTip",
	props: {
		tip: {
			type: String,
			default: '没有数据'
		}
	}
}
</script>

<style lang="scss">
.no-data-tip {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	
	.image {
		width: 150px;
		height: 150px;
	}

	.text {
		text-align: center;
		font-size: var(--im-font-size);
		color: #888;
		margin-top: 3px;
	}
}
</style>