<template>
    <div class="tabs">
        <div v-for="(item, idx) in items" :key="idx" class="tab" :class="current == idx ? 'active' : ''"
            @click="onClickItem(idx)">
            {{ item }}
        </div>
    </div>
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
    height: 50px;
    display: flex;
    align-items: center;
    border-radius: 10px;
    background-color: #f6f7fb;

    .tab {
        color: var(--im-text-color-light);
        padding: 10px;
        border-radius: 10px;
        font-weight: 600;
        flex: 1;
        text-align: center;
        margin: 0 3px;
        white-space: nowrap;
        cursor: pointer;

        &.active {
            color: var(--im-color-primary);
            background: var(--im-color-primary-light-9);
        }
    }
}
</style>