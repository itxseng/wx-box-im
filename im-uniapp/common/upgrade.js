import http from './request';
import dialog from './dialog';
import UNI_APP from '@/.env.js'

let checkAndUpgrade = () => {
	plus.runtime.getProperty(plus.runtime.appid, (info) => {
		const curVersion = info.version;
		console.log("当前应用版本：", curVersion);
		http({
			url: '/system/checkVersion?version=' + curVersion
		}).then((res) => {
			if (!res.isLatestVersion) {
				dialog.open({
					title: "发现新版本",
					confirmText: "立即更新",
					cancelText: "稍后再说",
					content: res.changeLog,
					icon: '/static/image/upgrade.png',
					success: () => {
						downloadAndInstall();
					}
				})
			}
		}).catch(() => {
			console.log("获取版本信息异常");
		})
	});
}


let downloadAndInstall = () => {
	const wgtUrl = UNI_APP.WGT_URL;
	const dtask = plus.downloader.createDownload(wgtUrl, {}, (d, status) => {
		if (status == 200) { 
			install(d.filename); // 安装wgt包  
		} else {
			plus.nativeUI.alert('下载失败');
		}
	});
	downloadProgress(dtask); 
}


let install = (path) => {
	let showLoading = plus.nativeUI.showWaiting('正在安装...');
	plus.runtime.install(path, {}, () => {
		showLoading.setTitle('安装成功,应用即将重启...');
		// 重启应用
		setTimeout(() => {
			plus.nativeUI.closeWaiting();
			plus.runtime.restart()
		}, 1500)
	}, (e) => {
		console.log("更新失败:",e)
		plus.nativeUI.closeWaiting();
		plus.nativeUI.alert('更新失败');
	});
}

let downloadProgress = (dtask) => {
	try {
		dtask.start(); //开启下载任务
		let showLoading = plus.nativeUI.showWaiting('正在下载');
		let timeStamp = new Date().getTime();
		dtask.addEventListener('statechanged', (task, status) => {
			// 给下载任务设置监听
			switch (task.state) {
				case 3:
					let prg = parseInt((parseFloat(task.downloadedSize) / parseFloat(task.totalSize)) * 100);
					let curTime = new Date().getTime();
					if (curTime > timeStamp + 100) { 
						showLoading.setTitle('正在下载' + prg + '%');
						timeStamp = curTime
					}
					break;
				case 4:
					// 下载完成
					plus.nativeUI.closeWaiting();
					break;
			}
		})
	} catch (e) {
		plus.nativeUI.closeWaiting();
		uni.showToast({
			title: '更新失败',
			icon: 'none'
		})
	}
}

export {
	checkAndUpgrade
}