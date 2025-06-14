# IM Android Native Module

这是一个参考 `im-uniapp` 逻辑实现的原生 Android 模块示例，使用 Kotlin 编写。

该示例包含一个简单的 `LoginActivity`，通过 OkHttp 向后端 `/login` 接口发送请求，获取 `accessToken` 与 `refreshToken`。登录成功后会立即通过 OkHttp WebSocket 连接 `wss://www.boxim.online/im`，并定时发送心跳包。

## 目录结构

```
im-android/
  build.gradle       // 根构建脚本
  settings.gradle    // 项目设置
  app/
    build.gradle     // App 构建脚本
    src/main/
      AndroidManifest.xml
      java/com/bx/android/LoginActivity.kt
      res/layout/activity_login.xml
```

要编译此模块需安装 Android SDK 并在本地配置好 `ANDROID_HOME` 环境变量，然后执行 `gradle assembleDebug` 即可构建 APK。
