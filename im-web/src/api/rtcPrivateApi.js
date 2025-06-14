import http from './httpRequest.js'

class RtcPrivateApi {
}

RtcPrivateApi.prototype.setup = function (uid, mode) {
	return http({
		url: `/webrtc/private/setup?uid=${uid}&mode=${mode}`,
		method: 'post'
	})
}

RtcPrivateApi.prototype.accept = function (uid) {
	return http({
		url: `/webrtc/private/accept?uid=${uid}`,
		method: 'post'
	})
}


RtcPrivateApi.prototype.handup = function (uid) {
	return http({
		url: `/webrtc/private/handup?uid=${uid}`,
		method: 'post'
	})
}

RtcPrivateApi.prototype.cancel = function (uid) {
	return http({
		url: `/webrtc/private/cancel?uid=${uid}`,
		method: 'post'
	})
}

RtcPrivateApi.prototype.reject = function (uid) {
	return http({
		url: `/webrtc/private/reject?uid=${uid}`,
		method: 'post'
	})
}

RtcPrivateApi.prototype.failed = function (uid, reason) {
	return http({
		url: `/webrtc/private/failed?uid=${uid}&reason=${reason}`,
		method: 'post'
	})
}

RtcPrivateApi.prototype.offer = function (uid, offer) {
	return http({
		url: `/webrtc/private/offer?uid=${uid}`,
		method: 'post',
		data: JSON.stringify(offer),
		headers: {
			'Content-Type': 'application/json; charset=utf-8'
		}
	});
}

RtcPrivateApi.prototype.answer = function (uid, answer) {
	return http({
		url: `/webrtc/private/answer?uid=${uid}`,
		method: 'post',
		data: JSON.stringify(answer),
		headers: {
			'Content-Type': 'application/json; charset=utf-8'
		}
	});
}

RtcPrivateApi.prototype.sendCandidate = function (uid, candidate) {
	return http({
		url: `/webrtc/private/candidate?uid=${uid}`,
		method: 'post',
		data: JSON.stringify(candidate),
		headers: {
			'Content-Type': 'application/json; charset=utf-8'
		}
	});
}

RtcPrivateApi.prototype.heartbeat = function (uid) {
	return http({
		url: `/webrtc/private/heartbeat?uid=${uid}`,
		method: 'post'
	})
}

export default RtcPrivateApi;