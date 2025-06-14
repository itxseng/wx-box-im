const requestPermissions = (scope, message) => {
	return new Promise((resolve, reject) => {
		plus.android.requestPermissions(
			["android.permission." + scope],
			function(resultObj) {
				console.log(resultObj, "resultObj");
				resolve(resultObj);
				if (resultObj.deniedPresent.length > 0 ||
					resultObj.deniedAlways.length > 0) {
					goSetting(message);
				}
			},
			function(error) {
				goSetting("获取权限失败，请重试");
			}
		);
	});
}


// 跳转权限设置
const goSetting = (message) => {
	uni.showModal({
		title: "提示",
		content: message,
		// showCancel: false, // 不显示取消按钮
		success(res) {
			if (res.confirm) {
				var Intent = plus.android.importClass("android.content.Intent");
				var Settings = plus.android.importClass("android.provider.Settings");
				var Uri = plus.android.importClass("android.net.Uri");
				var mainActivity = plus.android.runtimeMainActivity();
				var intent = new Intent();
				intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
				var uri = Uri.fromParts("package", mainActivity.getPackageName(), null);
				intent.setData(uri);
				mainActivity.startActivity(intent);
			}
		}
	});
}

let topMessageView;
const createTopMessage = (title, message) => {
	topMessageView = new plus.nativeObj.View("topMessageView", {
		width: "100%",
		height: "100%",
		backgroundColor: "rgba(0,0,0,0.85)",
	});
	topMessageView.drawText(
		title, {
			top: "-100px",
			left: "10%",
			width: "80%",
			height: "80%",
		}, {
			color: "#ffffff",
			size: "22px",
		}
	);
	topMessageView.drawText(
		message, {
			top: "-50px",
			left: "10%",
			width: "80%",
			height: "80%",
		}, {
			color: "#ffffff",
			whiteSpace: "normal",
		}
	);
	// 显示消息提示框
	topMessageView.show();
}


const checkAndRequest = async (scope, title, messageTip, settingTip) => {
	//#ifdef APP-PLUS
	if (plus.os.name !== "iOS") {
		let t = setTimeout(() => createTopMessage(title, messageTip),  300);
		let res = await requestPermissions(scope, settingTip);
		clearTimeout(t);
		topMessageView && topMessageView.close();
		console.log("res:", res);
		if (!res.granted[0]) {
			// 无权限
			return false;
		}
	}
	// #endif
	return true;
}

const storage = async() => {
	const scope = "WRITE_EXTERNAL_STORAGE"
	const title = "访问媒体和文件权限说明";
	const messageTip = "用于用户发送图片、视频、文件或上传头像等场景";
	const settingTip = "访问媒体和文件权限未获得,此权限用于用户发送图片、视频、文件或上传头像等场景,请前往设置中打开";
	return checkAndRequest(scope, title, messageTip, settingTip);
}

const camera = async () => {
	const scope = "CAMERA"
	const title = "相机使用权限说明";
	const messageTip = "用于拍照、录像、视频通话等场景";
	const settingTip = "相机使用权限未获得,此权限用于拍照、录像、视频通话等场景,请前往设置中打开";
	return checkAndRequest(scope, title, messageTip, settingTip);
}


const micro = async() => {
	const scope = "RECORD_AUDIO"
	const title = "麦克风使用权限说明";
	const messageTip = "用于拍摄、录制语音消息、视频或语音通话等场景";
	const settingTip = "麦克风使用权限未获得,此权限用于用于拍摄、录制语音消息、视频或语音通话等场景,请前往设置中打开";
	return checkAndRequest(scope, title, messageTip, settingTip);
}







export {
	createTopMessage,
	storage,
	camera,
	micro
}