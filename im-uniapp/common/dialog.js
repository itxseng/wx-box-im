const defaultConfig = {
	confirmText: '确定',
	cancelText: '取消',
	confirmBgColor: '#3e45d7',
	showCancel: true,
	titleAlign: 'left',
	descriAlign: 'left'
};


class AppDialog {
	constructor() {
		this.maskEl = {}
		this.popupEl = {}
		this.screenHeight = 600;
		this.popupHeight = 230;
		this.popupWidth = 300;
		this.viewWidth = 260;
		this.descrTop = 130;
		this.viewPadding = 20;
		this.iconSize = 80;
		this.titleHeight = 30;
		this.textHeight = 18;
		this.textSpace = 10;
		this.popupContent = []
		this.apkUrl = '';
	}

	// 显示
	open(config) {
		this.config = Object.assign({}, defaultConfig, config)
		this.drawView()
		this.maskEl.show()
		this.popupEl.show()
	}

	// 隐藏
	hide() {
		this.maskEl.hide()
		this.popupEl.hide()
	}

	// 绘制
	drawView() {
		this.screenHeight = plus.screen.resolutionHeight;
		this.popupWidth = plus.screen.resolutionWidth * 0.8;
		this.popupHeight = this.viewPadding * 3 + this.iconSize + 100;
		this.viewWidth = this.popupWidth - this.viewPadding * 2;
		this.descrTop = this.viewPadding + this.iconSize + this.titleHeight;
		this.popupContent = [];
		if (this.config.icon) {
			this.popupContent.push({
				id: 'logo',
				tag: 'img',
				src: this.config.icon,
				position: {
					top: '0px',
					left: (this.popupWidth - this.iconSize) / 2 + 'px',
					width: this.iconSize + 'px',
					height: this.iconSize + 'px'
				}
			});
		} 
		
		// 标题
		if (this.config.title) {
			this.popupContent.push({
				id: 'title',
				tag: 'font',
				text: this.config.title,
				textStyles: {
					size: '18px',
					color: '#333',
					weight: 'bold',
					align: this.config.titleAlign
				},
				position: {
					top: this.descrTop - this.titleHeight - this.textSpace + 'px',
					left: this.viewPadding + 'px',
					width: this.viewWidth + 'px',
					height: this.titleHeight + 'px'
				}
			})
		} else {
			this.descrTop -= this.titleHeight;
		}
		this.drawContent()
		// 取消
		if (this.config.showCancel) {
			const width = (this.viewWidth - this.viewPadding) / 2;
			const confirmLeft = width + this.viewPadding * 2;
			this.drawBtn('cancel', width, this.config.cancelText)
			this.drawBtn('confirm', width, this.config.confirmText, confirmLeft)
		} else {
			this.drawBtn('confirmBox', this.viewWidth, this.config.confirmText)
		}
		this.drawBox() 
	}

	// 描述内容
	drawContent() {
		const content = this.config.content;
		if (Array.isArray(content)) {
			this.drawDesc(content)
		} else { 
			this.drawDesc([content])
		}
	}

	// 描述
	drawDesc(rowText) {
		rowText.forEach((item, index) => {
			if (index > 0) {
				this.descrTop += this.textHeight;
				this.popupHeight += this.textHeight;
			}

			this.popupContent.push({
				id: 'content' + index + 1,
				tag: 'font',
				text: item,
				textStyles: {
					size: '14px',
					color: '#666',
					align: this.config.descriAlign
				},
				position: {
					top: this.descrTop + 'px',
					left: this.viewPadding + 'px',
					width: this.viewWidth + 'px',
					height: this.textHeight + 'px'
				}
			})

			if (item.type == 'break') {
				this.descrTop += this.textSpace;
				this.popupHeight += this.textSpace;
			}
		})
	}

	// 按钮
	drawBtn(id, width, text, left = this.viewPadding) {
		let boxColor = this.config.confirmBgColor,
			textColor = '#ffffff';
		if (id == 'cancel') {
			boxColor = '#f0f0f0';
			textColor = '#666666';
		}

		this.popupContent.push({
			id: id + 'Box',
			tag: 'rect',
			rectStyles: {
				radius: '6px',
				color: boxColor
			},
			position: {
				bottom: this.viewPadding + 'px',
				left: left + 'px',
				width: width + 'px',
				height: '40px'
			}
		})

		this.popupContent.push({
			id: id + 'Text',
			tag: 'font',
			text: text,
			textStyles: {
				size: '14px',
				color: textColor
			},
			position: {
				bottom: this.viewPadding + 'px',
				left: left + 'px',
				width: width + 'px',
				height: '40px'
			}
		})
	}

	// 内容框
	drawBox() {
		this.maskEl = new plus.nativeObj.View('maskEl', {
			top: '0px',
			left: '0px',
			width: '100%',
			height: '100%',
			backgroundColor: 'rgba(0,0,0,0.5)'
		});

		this.popupEl = new plus.nativeObj.View('popupEl', {
			tag: 'rect',
			top: (this.screenHeight - this.popupHeight) / 2 + 'px',
			left: '10%',
			height: this.popupHeight + 'px',
			width: '80%'
		});

		// 白色背景
		this.popupEl.drawRect({
			color: '#ffffff',
			radius: '8px'
		}, {
			top: this.iconSize / 2 + 'px',
			height: this.popupHeight - this.iconSize / 2 + 'px'
		});

		console.log("this.popupContent:", this.popupContent)
		this.popupEl.draw(this.popupContent);

		this.popupEl.addEventListener('click', e => {
			const maxTop = this.popupHeight - this.viewPadding;
			const maxLeft = this.popupWidth - this.viewPadding;
			const buttonWidth = (this.viewWidth - this.viewPadding) / 2;
			if (e.clientY > maxTop - 40 && e.clientY < maxTop) {
				if (this.config.showCancel) {
					// 取消
					 if(e.clientX>this.viewPadding && e.clientX<maxLeft-buttonWidth-this.viewPadding){
						 this.config.cancel && this.config.cancel()
					 }
					// 确定
					if (e.clientX > maxLeft - buttonWidth && e.clientX < maxLeft) {
						this.config.success && this.config.success()
					}
				} else {
					if (e.clientX > this.viewPadding && e.clientX < maxLeft) {
						this.config.success && this.config.success()
					}
				}
				this.hide()
			}
		});
	}
}

export default new AppDialog()