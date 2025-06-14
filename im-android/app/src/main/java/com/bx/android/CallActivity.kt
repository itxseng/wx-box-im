package com.bx.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bx.android.rtc.RtcManager
import org.webrtc.SurfaceViewRenderer
import org.webrtc.PeerConnection

class CallActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)

        val localView = findViewById<SurfaceViewRenderer>(R.id.localView)
        val remoteView = findViewById<SurfaceViewRenderer>(R.id.remoteView)

        // 初始化 WebRTC
        RtcManager.init(this)
        val config = PeerConnection.RTCConfiguration(emptyList())
        RtcManager.setupPeerConnection(config, null) { stream ->
            // 这里简单演示, 实际应将远端流渲染到 remoteView
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        RtcManager.close()
    }
}
