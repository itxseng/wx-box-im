package com.bx.android.rtc

import android.content.Context
import org.webrtc.*

/**
 * 简易 WebRTC 管理器, 对应 uniapp 的 ImWebRtc 逻辑
 */
object RtcManager {
    private var factory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var localStream: MediaStream? = null

    fun init(context: Context) {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
        factory = PeerConnectionFactory.builder().createPeerConnectionFactory()
    }

    fun setupPeerConnection(config: PeerConnection.RTCConfiguration,
                            localStream: MediaStream?,
                            callback: (MediaStream) -> Unit) {
        val f = factory ?: return
        peerConnection = f.createPeerConnection(config, object : PeerConnection.Observer {
            override fun onIceCandidate(candidate: IceCandidate) {
                onIceCandidateCallback?.invoke(candidate)
            }

            override fun onAddStream(stream: MediaStream) {
                callback(stream)
            }

            override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState) {
                onStateChangeCallback?.invoke(newState)
            }

            override fun onSignalingChange(newState: PeerConnection.SignalingState) {}
            override fun onIceGatheringChange(newState: PeerConnection.IceGatheringState) {}
            override fun onRemoveStream(stream: MediaStream) {}
            override fun onDataChannel(dc: DataChannel) {}
            override fun onRenegotiationNeeded() {}
            override fun onTrack(transceiver: RtpTransceiver) {}
        })
        if (localStream != null) {
            this.localStream = localStream
            peerConnection?.addStream(localStream)
        }
    }

    fun switchStream(stream: MediaStream) {
        peerConnection?.let { pc ->
            val senders = pc.senders
            val videoTrack = stream.videoTracks.firstOrNull()
            val audioTrack = stream.audioTracks.firstOrNull()
            senders.forEach { sender ->
                when (sender.track()?.kind()) {
                    "video" -> videoTrack?.let { sender.replaceTrack(it, false) }
                    "audio" -> audioTrack?.let { sender.replaceTrack(it, false) }
                }
            }
            localStream = stream
        }
    }

    private var onIceCandidateCallback: ((IceCandidate) -> Unit)? = null
    private var onStateChangeCallback: ((PeerConnection.IceConnectionState) -> Unit)? = null

    fun onIceCandidate(callback: (IceCandidate) -> Unit) { onIceCandidateCallback = callback }
    fun onStateChange(callback: (PeerConnection.IceConnectionState) -> Unit) { onStateChangeCallback = callback }

    fun createOffer(): SessionDescription? {
        var result: SessionDescription? = null
        val latch = java.util.concurrent.CountDownLatch(1)
        peerConnection?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(desc: SessionDescription) {
                peerConnection?.setLocalDescription(this, desc)
                result = desc
                latch.countDown()
            }
            override fun onSetSuccess() {}
            override fun onCreateFailure(p0: String?) { latch.countDown() }
            override fun onSetFailure(p0: String?) {}
        }, MediaConstraints())
        latch.await()
        return result
    }

    fun createAnswer(offer: SessionDescription): SessionDescription? {
        var result: SessionDescription? = null
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onSetSuccess() {
                val latch = java.util.concurrent.CountDownLatch(1)
                peerConnection?.createAnswer(object : SdpObserver {
                    override fun onCreateSuccess(desc: SessionDescription) {
                        peerConnection?.setLocalDescription(this, desc)
                        result = desc
                        latch.countDown()
                    }
                    override fun onSetSuccess() {}
                    override fun onCreateFailure(p0: String?) { latch.countDown() }
                    override fun onSetFailure(p0: String?) {}
                }, MediaConstraints())
                latch.await()
            }
            override fun onSetFailure(p0: String?) {}
            override fun onCreateSuccess(p0: SessionDescription?) {}
            override fun onCreateFailure(p0: String?) {}
        }, offer)
        return result
    }

    fun addIceCandidate(candidate: IceCandidate) {
        peerConnection?.addIceCandidate(candidate)
    }

    fun setRemoteDescription(offer: SessionDescription) {
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onSetSuccess() {}
            override fun onSetFailure(p0: String?) {}
            override fun onCreateSuccess(p0: SessionDescription?) {}
            override fun onCreateFailure(p0: String?) {}
        }, offer)
    }

    fun close() {
        peerConnection?.close()
        peerConnection = null
        localStream = null
    }
}
