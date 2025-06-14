# H5扫码

## 简介

基于 [vue-qrcode-reader](https://github.com/gruhn/vue-qrcode-reader) 库封装的 H5 扫码组件。

功能特性：

- 支持手电筒
- 支持摄像头切换
- 支持一次扫多个码，自主选择识别哪一个

安全上下文要求：

- 生产环境必须使用 **HTTPS** 协议（可以使用 [mkcert](https://github.com/FiloSottile/mkcert) 工具制作本地信任的开发证书）
- 本地开发可通过 `http://localhost` 或 `http://127.0.0.1` 调用

## **示例**

安装依赖：

```bash
# 5.x版本
npm install vue-qrcode-reader
```

使用：

> 组件可选参数：
>
> - scanType：[扫码类型](https://github.com/Sec-ant/barcode-detector?tab=readme-ov-file#barcode-detector)。数组类型，默认值 `["qr_code"]`
> - strokeStyle：描边样式。字符串类型，默认值 `#007bff`

```vue
<template>
  <button @click="scanCode">扫一扫</button>
  <!-- #ifdef H5 -->
  <cshaptx4869-scancode
    ref="h5ScanCodeRef"
    @success="handleSuccess"
    @fail="handleFail"
  ></cshaptx4869-scancode>
  <!-- #endif -->
</template>

<script setup>
import { ref } from "vue";

const h5ScanCodeRef = ref();
function scanCode() {
  // #ifdef H5
  h5ScanCodeRef.value.open();
  // #endif
  // #ifndef H5
  uni.scanCode({
    success: (res) => {
      uni.showToast({
        icon: "none",
        title: res.result,
      });
    },
    fail: (err) => {
      console.log("err", err);
    },
  });
  // #endif
}
function handleSuccess(res) {
  uni.showToast({
    icon: "none",
    title: res.result,
  });
}
function handleFail(err) {
  uni.showModal({
    title: err.errName,
    content: err.errMsg,
  });
}
</script>
```
