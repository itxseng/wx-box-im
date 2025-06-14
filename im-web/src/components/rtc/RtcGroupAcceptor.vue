<template>
	<div class="rtc-group-acceptor">
		<head-image v-if="inviter" :name="inviter.nickName" :url="inviter.headImage" :size="100"></head-image>
		<div class="tip">
			{{ inviter.nickName + '邀请您加入多人通话' }}
		</div>
		<div class="user-list-text">参与通话的还有:</div>
		<div class="user-list">
			<div class="user-item" v-for="user in userInfos" v-show="user.id != inviterId" :key="user.id">
				<head-image :url="user.headImage" :name="user.nickName" :size="20">
				</head-image>
			</div>
		</div>
		<div class="control-bar">
			<div class="icon iconfont icon-phone-accept accept" title="接受" @click="$emit('accept')"></div>
			<div class="icon iconfont icon-phone-reject reject" title="拒绝" @click="$emit('reject')"></div>
		</div>
	</div>
</template>

<script>
import HeadImage from '../common/HeadImage.vue';

export default {
	name: "rtcGroupAcceptor",
	components: {
		HeadImage
	},
	data() {
		return {
		}
	},
	props: {
		groupId: {
			type: Number
		},
		inviterId: {
			type: Number
		},
		userInfos: {
			type: Array
		}
	},
	computed: {
		inviter() {
			return this.userInfos.find(user => user.id == this.inviterId);
		}
	}
}
</script>

<style scoped lang="scss">
.rtc-group-acceptor {
	position: absolute;
	display: flex;
	flex-direction: column;
	align-items: center;
	right: 2px;
	bottom: 2px;
	width: 250px;
	height: 300px;
	padding: 20px;
	background-color: #fff;
	box-shadow: var(--im-box-shadow-dark);
	border-radius: 4px;

	.tip {
		padding: 10px;
		text-align: center;
		font-size: 16px;
	}

	.user-list-text {
		margin-top: 5px;
		text-align: center;
		font-size: 14px;
		color: #888;
	}

	.user-list {
		display: flex;
		padding: 5px 25px;
		height: 90px;
		flex-wrap: wrap;
		justify-content: center;

		.user-item {
			padding: 2px;
		}
	}

	.control-bar {
		display: flex;
		justify-content: space-around;
		margin-top: 20px;
		width: 100%;

		.icon {
			font-size: 60px;
			cursor: pointer;
			border-radius: 50%;

			&.accept {
				color: green;
				animation: anim 2s ease-in infinite, vibration 2s ease-in infinite;

				@keyframes anim {
					0% {
						box-shadow: 0 1px 0 4px #ffffff;
					}

					10% {
						box-shadow: 0 1px 0 8px rgba(255, 165, 0, 1);
					}

					25% {
						box-shadow: 0 1px 0 12px rgba(255, 210, 128, 1), 0 1px 0 16px rgba(255, 201, 102, 1);
					}

					50% {
						box-shadow: 0 2px 5px 10px rgba(255, 184, 51, 1), 0 2px 5px 23px rgba(248, 248, 255, 1);
					}
				}

				@keyframes vibration {
					0% {
						transform: rotate(0deg);
					}

					25% {
						transform: rotate(20deg);
					}

					50% {
						transform: rotate(0deg);
					}

					75% {
						transform: rotate(-15deg);
					}

					100% {
						transform: rotate(0deg);
					}
				}
			}

			&.reject {
				color: red;
			}
		}
	}
}
</style>
