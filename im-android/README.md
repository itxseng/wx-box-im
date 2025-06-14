# IM Android Native Module

这是一个参考 `im-uniapp` 逻辑实现的原生 Android 模块示例，使用 Kotlin 编写。

该示例包含一个简单的 `LoginActivity`，通过 OkHttp 向后端 `/login` 接口发送请求，获取 `accessToken` 与 `refreshToken`。登录成功后会立即通过 OkHttp WebSocket 连接 `wss://www.boxim.online/im`，并定时发送心跳包。接收到的消息会根据会话 ID 存入 `MessageStore`。随后会进入 `MainActivity`，其中使用 `TabLayout` 和 `ViewPager2` 展示 "会话"、"好友"、"群" 三个列表，会话页会显示最近一条消息。

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
      java/com/bx/android/MainActivity.kt
      java/com/bx/android/ConversationListFragment.kt
      java/com/bx/android/FriendListFragment.kt
      java/com/bx/android/GroupListFragment.kt
      res/layout/activity_login.xml
      res/layout/activity_main.xml
      res/layout/fragment_list.xml
```

要编译此模块需安装 Android SDK 并在本地配置好 `ANDROID_HOME` 环境变量，然后执行 `gradle assembleDebug` 即可构建 APK。
