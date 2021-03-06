<template>
  <div id="main-container" class="container">
    <div id="join" v-if="!session">
      <div id="img-div"><img src="resources/images/openvidu_grey_bg_transp_cropped.png" /></div>
      <div id="join-dialog" class="jumbotron vertical-center">
        <h1>Join a video session</h1>
        <div class="form-group">
          <p>
            <label>Participant</label>
            <input v-model="myUserName" class="form-control" type="text" required />
          </p>
          <p>
            <label>Session</label>
            <input v-model="mySessionId" class="form-control" type="text" required />
          </p>
          <div id="main-video" class="col-md-6">
            <h3>입장 전에 내 화면을 확인하세요</h3>
            <div id="beforeJoinCam" ref="beforeJoinCam"></div>
            <user-video :stream-manager="beforeJoin" />
          </div>
          <p class="text-center">
            <button class="btn btn-lg btn-success" @click="joinSession()">Join!</button>
          </p>
        </div>
      </div>
    </div>
    <div id="session" v-if="session">
      <input
        class="btn btn-large btn-danger"
        type="button"
        id="buttonLeaveSession"
        @click="leaveSession"
        value="세션에서 나가기"
        style="backgroundColor:red;color:white"
      />
      <h4>
        세션에 접속하려면 Camera ON 버튼을 클릭해서 내 모습을 확인한 뒤 "세션에 접속하기"버튼을
        클릭해주세요
      </h4>
      <div>
        <div v-if="publisher">
          <ov-video :stream-manager="publisher" />
        </div>
        <button class="btn btn-lg btn-success" @click="cameraOff()">
          Camera Off
        </button>
        <div v-if="!isPublished">
          켜고싶은 카메라를 선택해서 카메라를 켜고 세션에 접속하세요
          <br />
          <button
            v-for="(item, index) in videoSourceList"
            :key="'videosourceitem' + index"
            @click="changeVideoSource(item)"
          >
            {{ item.label }}
          </button>
        </div>
        <br />
      </div>
      <div id="session-header">
        <h1 id="session-title">{{ mySessionId }}</h1>
      </div>
      <div id="seat-container" class="col-md-6">
        <user-video
          v-for="(sub, index) in subscribers"
          :key="sub || 'seat' + index"
          :stream-manager="sub"
          @click.native="
            publishSession(index);
            updateUserNameToSendMessage(sub);
          "
          style="display:inline-block"
        />
      </div>
      <br />
      <br />
      <hr />
      <div>
        세션에 존재하는 사람을 클릭하면 해당 사람에게 메시지를 보낼 수 있습니다. 나를 클릭하면
        '모두에게'로 바뀝니다.
      </div>
      <div>
        {{ userNameToSendMessage ? userNameToSendMessage : '모두에게 ' }}
        메시지 전송하기 :
        <input v-model="inputMessage" type="text" @keyup.enter="sendMessage()" />
      </div>
      <hr />
      <h3>전송받은 메시지</h3>
      <div v-for="(item, index) in receivedMessages" :key="'rcvmsg' + index">
        <span style="font-weight:bold">{{ item.from.data }}</span
        >({{ item.from.connectionId }}) :
        {{ item.data }}
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import { OpenVidu } from 'openvidu-browser';
import UserVideo from '../components/UserVideo';
import OvVideo from '../components/OvVideo';
axios.defaults.headers.post['Content-Type'] = 'application/json';
// 민감정보. 실제 프로젝트로 옮길 때 따로 빼서 관리
const OPENVIDU_SERVER_URL = 'https://k4a401.p.ssafy.io';
const OPENVIDU_SERVER_SECRET = 'ssafy';
const USER_MAX_NUMBER = 16;
export default {
  name: 'App',
  components: {
    UserVideo,
    OvVideo,
  },
  data() {
    return {
      userNameToSendMessage: undefined,
      clickedUserConnection: undefined,
      selectedVideoSource: undefined,
      videoSourceList: [],
      inputMessage: '',
      receivedMessages: [],
      cameraState: false,
      isPublished: false,
      OV: undefined,
      session: undefined,
      beforeJoin: undefined,
      mainStreamManager: undefined,
      publisher: undefined,
      subscribers: new Array(USER_MAX_NUMBER),
      mySessionId: 'SessionA',
      myUserName: 'Participant' + Math.floor(Math.random() * 100),
    };
  },
  methods: {
    joinSession() {
      // --- Get an OpenVidu object ---
      if (!this.OV) this.OV = new OpenVidu();
      this.videoSourceList.push({ kind: 'videoinput', label: 'default', deviceId: undefined });
      this.OV.getDevices().then((res) => {
        res.forEach((e) => {
          if (e.kind === 'videoinput' && e.label) {
            this.videoSourceList.push(e);
          }
        });
      });
      // --- Init a session ---
      this.session = this.OV.initSession();
      // --- Specify the actions when events take place in the session ---
      // On every new Stream received...
      this.session.on('streamCreated', ({ stream }) => {
        const subscriber = this.session.subscribe(stream);
        this.subscribers.push(subscriber);
      });
      // On every Stream destroyed...
      this.session.on('streamDestroyed', ({ stream }) => {
        const index = this.subscribers.indexOf(stream.streamManager, 0);
        if (index >= 0) {
          this.subscribers.splice(index, 1);
        }
      });
      // 메시지 시그널
      this.session.on('signal:message', (signalEvent) => {
        this.receivedMessages.push(signalEvent);
      });
      // --- Connect to the session with a valid user token ---
      this.getToken(this.mySessionId).then((token) => {
        this.session
          .connect(token, `aaaaaa##${this.myUserName}`)
          .then(() => {
            // 세션에 성공적으로 입장
            console('세션에 참가했습니다');
          })
          .catch((error) => {
            console.log('There was an error connecting to the session:', error.code, error.message);
          });
      });
      window.addEventListener('beforeunload', this.leaveSession);
    },
    leaveSession() {
      // 현재 접속 중인 세션을 나간다. (disconnect)
      // --- Leave the session by calling 'disconnect' method over the Session object ---
      if (this.session) this.session.disconnect();
      this.cameraOff();
      this.session = undefined;
      this.mainStreamManager = undefined;
      this.subscribers = new Array(USER_MAX_NUMBER);
      this.OV = undefined;
      window.removeEventListener('beforeunload', this.leaveSession);
    },
    updateUserNameToSendMessage(stream) {
      // 메시지 보낼 대상을 변경한다
      if (!stream) return;
      const clikedUserConnection = stream.stream.connection;
      const clikedUserConnectionId = clikedUserConnection.connectionId;
      const clikedUserName = clikedUserConnection.data;
      const mConnectionId = this.session.connection.connectionId;
      this.userNameToSendMessage =
        mConnectionId === clikedUserConnectionId ? undefined : clikedUserName;
      this.clickedUserConnection =
        mConnectionId === clikedUserConnectionId ? undefined : clikedUserConnection;
    },
    getToken(mySessionId) {
      return this.createSession(mySessionId).then((sessionId) => this.createToken(sessionId));
    },
    // See https://docs.openvidu.io/en/stable/reference-docs/REST-API/#post-openviduapisessions
    createSession(sessionId) {
      return new Promise((resolve, reject) => {
        axios
          .post(
            `${OPENVIDU_SERVER_URL}/openvidu/api/sessions`,
            JSON.stringify({
              customSessionId: sessionId,
            }),
            {
              auth: {
                username: 'OPENVIDUAPP',
                password: OPENVIDU_SERVER_SECRET,
              },
            }
          )
          .then((response) => response.data)
          .then((data) => resolve(data.id))
          .catch((error) => {
            if (error.response.status === 409) {
              resolve(sessionId);
            } else {
              console.warn(
                `No connection to OpenVidu Server. This may be a certificate error at ${OPENVIDU_SERVER_URL}`
              );
              if (
                window.confirm(
                  `No connection to OpenVidu Server. This may be a certificate error at ${OPENVIDU_SERVER_URL}\n\nClick OK to navigate and accept it. If no certificate warning is shown, then check that your OpenVidu Server is up and running at "${OPENVIDU_SERVER_URL}"`
                )
              ) {
                location.assign(`${OPENVIDU_SERVER_URL}/accept-certificate`);
              }
              reject(error.response);
            }
          });
      });
    },
    // See https://docs.openvidu.io/en/stable/reference-docs/REST-API/#post-openviduapisessionsltsession_idgtconnection
    createToken(sessionId) {
      return new Promise((resolve, reject) => {
        axios
          .post(
            `${OPENVIDU_SERVER_URL}/openvidu/api/sessions/${sessionId}/connection`,
            {},
            {
              auth: {
                username: 'OPENVIDUAPP',
                password: OPENVIDU_SERVER_SECRET,
              },
            }
          )
          .then((response) => response.data)
          .then((data) => resolve(data.token))
          .catch((error) => reject(error.response));
      });
    },
    async cameraOn() {
      // [비동기] 카메라 켜기
      if (!this.OV) this.OV = new OpenVidu();
      // --- 카메라를 통해 스트림할 데이터의 속성을 초기화한다 ---
      let publisher = await this.OV.initPublisherAsync(undefined, {
        audioSource: false, // 오디오소스. If undefined default microphone
        // videoSource: this.selectedVideoSource, // 비디오소스. If undefined default webcam
        videoSource: undefined, // 비디오소스. If undefined default webcam
        publishAudio: false, // Whether you want to start publishing with your audio unmuted or not
        publishVideo: true, // Whether you want to start publishing with your video enabled or not
        resolution: '320x240', // The resolution of your video
        frameRate: 30, // 프레임. The frame rate of your video
        insertMode: 'APPEND', // How the video is inserted in the target element 'video-container'
        mirror: false, // Whether to mirror your local video or not
      });
      this.publisher = publisher;
      this.cameraState = true;
    },
    cameraOff() {
      // 카메라 끄기
      if (this.session && this.isPublished) {
        // 세션에 들어가있고 "publish 중인 상태"에서는 unpublish 해야 함
        // unpublish는 카메라 자원 해제까지 해주는 메서드 존재
        this.session.unpublish(this.publisher);
      } else {
        // 세션을 통해 unpublish하지 않은 경우 카메라를 OFF만 하면,
        // 카메라 자원이 여전히 실행 중이게 되므로 카메라 자원을 해제하는 작업을 해주어야 함
        if (this.publisher) this.publisher.stream.disposeMediaStream();
      }
      this.publisher = undefined;
      this.cameraState = false;
      this.isPublished = false;
    },
    publishSession(seatNo) {
      // 현재 접속중인 세션에 영상을 publish 함
      // publish에 성공하면, 내 캠화면을 내가 선택한 element에서 보이도록 함
      this.session
        .publish(this.publisher)
        .then(() => {
          this.subscribers.splice(seatNo, 1, this.publisher);
          this.isPublished = true;
        })
        .catch(() => {
          this.isPublished = false;
        });
    },
    sendMessage() {
      // 클릭한 유저에게 메시지를 전송함
      // signalOptions의 to가 undefined일 경우, 모든 참가자에게 메시지를 전송함
      if (!this.inputMessage.trim()) return;
      if (this.session) {
        this.session
          .signal({
            data: this.inputMessage,
            to: this.clickedUserConnection
              ? [this.session.connection, this.clickedUserConnection]
              : this.clickedUserConnection,
            type: 'message',
          })
          .then(() => {
            console.log('메시지가 성공적으로 전송되었습니다.');
            this.inputMessage = '';
          })
          .catch(() => {
            console.warn('메시지가 전송을 실패했습니다.');
          });
      }
    },
    changeVideoSource(videoSource) {
      // 비디오소스(캠)가 변경되면 카메라를 끄고 비디오 소스를 변경한 뒤 다시 카메라를 켬
      this.cameraOff();
      this.selectedVideoSource = videoSource.deviceId;
      this.cameraOn();
    },
  },
};
</script>
<style scoped>
#video-container {
  display: flex;
}
.pointer {
  cursor: pointer;
}
.seat-container {
  display: flex;
  flex-wrap: wrap;
}
</style>
