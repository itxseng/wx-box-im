<template>
    <view class="tabs">
        <view v-for="(item, idx) in items" :key="idx" class="tab" :class="current == idx ? 'active' : ''"
            @click="onClickItem(idx)">
            {{ item }}
        </view>
    </view>
</template>

<script>
export default {
    name: "tabs",
    props: {
        items: {
            type: Array
        },
        current: {
            type: Number,
            default: 0
        }
    },
    data() {
        return {
        };
    },
    methods: {
        onClickItem(idx) {
            if (this.current != idx) {
                this.$emit("change", idx);
            }
        }
    }
}
</script>

<style lang="scss" scoped>
.tabs {
	height: 100rpx;
	display: flex;
	align-items: center;
	border-radius: 20rpx;
	background-color: #f6f7fb;

	.tab {
		color: $im-text-color-lighter;
		padding: 18rpx 30rpx;
		border-radius: 20rpx;
		font-weight: 800 !important;
		flex: 1;
		text-align: center;
		margin: 0 10rpx;
		
		&.active {
			color: $im-text-color;
			background: white;
		}
	}
}
</style>