import { getAuthToken, login, loginByAuthToken } from '../../api/user';

const state = () => ({
  userInfo: {},
  roomInfo: undefined,
  videoSourceList: [],
  videoSource: undefined,
});

const getters = {};

const actions = {
  async GET_AUTH_TOKEN({ commit }, code) {
    return await getAuthToken(
      { authorizeCode: code },
      (res) => {
        commit('SET_KAKAO_ID', res.data);
      },
      (error) => {
        localStorage.removeItem('vuex');
        localStorage.removeItem('authToken');
        alert('😕 카카오아이디를 받아오는데 실패했어요. 다시 로그인해주세요.' + error);
      }
    );
  },
  LOGIN({ state, commit }) {
    return new Promise((resolve, reject) => {
      login(
        { kakaoId: state.userInfo.kakaoId },
        (res) => {
          if (res.data.login === 'OK') {
            commit('SET_USER_INFO', res.data.user);
            localStorage.setItem('authToken', state.userInfo.authToken);
            resolve('ok');
          } else if (res.data.login === 'Join') {
            console.log(res.data);
            resolve('join');
          } else {
            reject('😭 로그인에 실패했어요. 다시 시도해주세요.');
          }
        },
        (error) => {
          reject(error);
        }
      );
    });
  },
  async LOGIN_BY_AUTH_TOKEN({ commit }, token) {
    commit('SET_AUTH_TOKEN', token);
    await loginByAuthToken(
      (res) => {
        commit('SET_USER_INFO', res.data);
      },
      (error) => {
        alert(error);
      }
    );
  },
  LOGOUT({ commit }) {
    commit('SET_AUTH_TOKEN', undefined);
    commit('SET_USER_INFO', undefined);
    localStorage.removeItem('authToken');
    localStorage.removeItem('GuideShown');
  },
};

const mutations = {
  SET_KAKAO_ID(state, payload) {
    state.userInfo.kakaoId = payload;
  },
  SET_USERINFO_PROPERTY(state, { key, value }) {
    state.userInfo[key] = value;
  },
  SET_USER_INFO(state, payload) {
    state.userInfo = payload;
  },
  SET_VIDEO_SOURCE_LIST(state, payload) {
    state.videoSourceList = payload;
  },
  SET_VIDEO_SOURCE(state, payload) {
    state.videoSource = payload;
  },
  SET_AUTH_TOKEN(state, payload) {
    state.userInfo.authToken = payload;
  },
  SET_ROOM_INFO(state, payload) {
    state.roomInfo = payload;
  },
};

export default {
  state,
  getters,
  actions,
  mutations,
};
