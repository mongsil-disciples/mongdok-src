<template>
   <div id="app" style="border: 1px solid red">
      <h1>포스트잇 테스트</h1>
      <h3>
         작성 가능한 메모(<span :style="memos.length >= 10 ? 'color:red' : ''">{{ memos.length }}</span
         >/10)
      </h3>
      <div>
         <button v-show="!editable" @click="edit">편집하기</button>
         <button v-show="editable" @click="editComplete">편집완료(저장)</button>
         <button v-show="editable" @click="createMemo">메모 생성</button>
         <button v-show="editable && selectedMemoIdx >= 0" @click="removeMemo">이 메모 삭제하기</button>
         <button v-show="editable && selectedMemoIdx >= 0" @click="writeMemo">내용 작성하기</button>
         <button v-for="({ colorName }, index) in colors" :key="'color' + index" v-show="editable && selectedMemoIdx >= 0" @click="changeColor(index)">{{ colorName }}색으로 변경하기</button>
      </div>
      <Moveable
         ref="moveable"
         v-for="({ memoId, content, zIndex, moveable, transform, color }, index) in memos"
         :key="'memo' + memoId"
         class="moveable-container"
         v-bind="moveable"
         @drag="handleDrag"
         @renderEnd="handleRenderEnd(index, ...arguments)"
         @rotate="handleRotate"
         @scale="handleScale"
         @mousedown.native="onSelectMemo(index)"
         :style="{ zIndex, transform }"
      >
         <span class="postit">
            <span class="postit-content">
               {{ content }}
            </span>
            <svg width="100%" height="100%" viewBox="0 0 357 333" fill="none" xmlns="http://www.w3.org/2000/svg">
               <path
                  d="M72.284 6.65342C83.824 5.63342 95.344 4.38342 106.904 3.63342C123.544 2.56342 140.204 1.84342 156.854 0.983417C176.194 -0.0165833 195.544 -0.326583 214.914 0.393417C236.274 1.18342 257.644 1.71342 279.014 2.18342C298.044 2.60342 317.084 2.84342 336.124 3.05342C338.624 3.08342 341.174 2.64342 343.624 2.07342C347.704 1.13342 351.384 3.18342 351.734 7.37342C352.144 12.2334 352.334 17.1834 351.934 22.0334C351.004 33.4534 351.484 44.8334 352.014 56.2334C352.724 71.6234 353.264 87.0134 354.044 102.393C354.564 112.713 355.564 123.013 355.994 133.333C356.314 141.083 356.104 148.853 356.064 156.603C355.974 172.723 355.824 188.833 355.744 204.953C355.714 211.833 356.164 218.743 355.744 225.603C355.464 230.243 354.684 235.053 352.964 239.333C351.624 242.673 348.554 245.333 346.154 248.213C343.864 250.963 341.414 253.583 339.114 256.313C333.524 262.953 327.974 269.633 322.394 276.283C321.814 276.973 321.034 277.543 320.614 278.323C315.524 287.793 307.134 294.283 299.784 301.733C296.524 305.033 293.884 309.033 291.424 313.003C289.544 316.023 287.504 318.223 283.754 318.053C270.104 317.423 256.764 319.493 243.324 321.743C224.524 324.893 205.474 324.803 186.414 323.553C178.324 323.023 170.084 323.523 161.994 324.393C139.814 326.763 117.644 329.063 95.324 329.473C87.164 329.623 79.124 330.293 71.024 331.393C63.714 332.383 56.164 331.573 48.724 331.663C44.024 331.723 39.274 331.683 34.624 332.253C26.704 333.233 20.504 330.323 17.844 322.703C15.774 316.783 14.164 310.513 13.624 304.283C11.754 282.673 10.604 260.993 8.87399 239.363C7.67399 224.373 5.85399 209.423 4.54399 194.443C3.29399 180.203 2.22399 165.943 1.29399 151.683C0.443994 138.643 1.63399 125.623 2.13399 112.593C2.73399 97.2234 2.71399 81.8334 3.20399 66.4634C3.39399 60.6834 4.40399 54.9334 4.78399 49.1534C5.38399 39.9534 5.87399 30.7334 6.22399 21.5234C6.47399 15.0634 8.59399 12.7634 15.014 11.2534C26.404 8.59342 38.024 8.21342 49.554 7.08342C57.074 6.34342 64.644 6.07342 72.184 5.58342C72.224 5.94342 72.254 6.29342 72.284 6.65342ZM16.384 21.3334C16.384 32.8234 16.554 43.6934 16.304 54.5534C16.194 59.5834 15.324 64.6034 14.684 69.6034C14.364 72.0534 13.364 74.4734 13.374 76.9034C13.414 89.0434 14.004 101.183 13.804 113.303C13.584 126.503 12.894 139.663 14.344 152.863C16.124 169.053 17.394 185.293 18.894 201.503C19.994 213.403 21.124 225.303 22.214 237.203C22.994 245.693 24.064 254.163 24.394 262.673C24.944 276.813 25.234 290.953 27.394 304.963C28.164 309.923 28.584 315.013 31.244 318.963C38.894 318.963 46.124 319.083 53.344 318.933C60.554 318.783 67.754 318.353 74.954 318.033C82.344 317.703 89.764 316.853 97.134 317.123C110.244 317.593 123.184 315.973 136.164 314.813C146.444 313.893 156.664 312.003 166.954 311.423C177.224 310.843 187.574 311.043 197.874 311.463C207.494 311.863 217.034 311.913 226.604 310.743C242.414 308.813 258.254 307.063 274.074 305.203C274.714 305.123 275.304 304.603 276.294 304.093C274.794 297.083 273.354 290.193 271.844 283.313C269.784 273.953 267.604 264.613 265.614 255.233C264.634 250.623 264.054 245.933 263.174 241.303C262.124 235.823 263.594 234.083 269.214 233.823C276.274 233.493 283.364 233.283 290.354 232.363C306.944 230.203 323.614 230.133 340.274 229.653C341.434 229.623 342.574 229.083 343.864 228.753C343.864 222.623 343.934 216.833 343.844 211.053C343.714 202.003 342.734 192.903 343.474 183.933C344.814 167.613 343.614 151.343 343.764 135.053C343.824 128.733 342.964 122.403 342.674 116.063C341.794 96.8834 341.184 77.6934 340.074 58.5334C339.414 47.1134 339.084 35.7434 340.074 24.3234C340.474 19.7134 340.144 15.0334 340.144 9.95342C335.964 10.3734 332.574 10.9434 329.174 11.0234C308.494 11.4634 287.814 11.9334 267.134 12.1134C252.984 12.2334 238.834 11.9534 224.684 11.7234C216.534 11.5934 208.384 10.8134 200.244 10.9934C179.974 11.4634 159.714 12.2034 139.464 13.0434C124.964 13.6434 110.484 14.5634 95.994 15.3534C88.214 15.7734 80.444 16.2434 72.674 16.6434C64.174 17.0734 55.654 17.3634 47.154 17.8834C39.394 18.3534 31.634 18.9534 23.884 19.6634C21.604 19.8734 19.344 20.6534 16.384 21.3334ZM285.934 296.973C290.464 292.603 294.424 288.933 298.204 285.093C301.744 281.493 305.174 277.783 308.484 273.973C312.874 268.933 317.134 263.793 321.384 258.633C326.164 252.823 330.864 246.943 335.764 240.883C314.804 241.843 294.394 242.783 273.414 243.753C275.894 262.253 281.704 279.223 285.934 296.973Z"
                  fill="black"
               />
               <path
                  :style="{ fill: colors[color].front }"
                  d="M16.3839 21.3334C19.3439 20.6534 21.6039 19.8734 23.9039 19.6534C31.6539 18.9434 39.4039 18.3434 47.1739 17.8734C55.6739 17.3634 64.1839 17.0734 72.6939 16.6334C80.4739 16.2334 88.2439 15.7734 96.0139 15.3434C110.504 14.5534 124.984 13.6334 139.484 13.0334C159.744 12.1934 180.004 11.4534 200.264 10.9834C208.404 10.7934 216.554 11.5734 224.704 11.7134C238.854 11.9434 253.004 12.2234 267.154 12.1034C287.834 11.9234 308.514 11.4534 329.194 11.0134C332.594 10.9434 335.984 10.3734 340.164 9.94336C340.164 15.0234 340.504 19.6934 340.094 24.3134C339.094 35.7334 339.424 47.0934 340.094 58.5234C341.204 77.6834 341.814 96.8734 342.694 116.053C342.984 122.383 343.834 128.713 343.784 135.043C343.634 151.333 344.834 167.603 343.494 183.923C342.754 192.893 343.734 201.993 343.864 211.043C343.944 216.823 343.884 222.613 343.884 228.743C342.594 229.083 341.454 229.613 340.294 229.643C323.634 230.123 306.954 230.183 290.374 232.353C283.384 233.263 276.294 233.473 269.234 233.813C263.614 234.083 262.144 235.813 263.194 241.293C264.074 245.923 264.664 250.613 265.634 255.223C267.624 264.603 269.804 273.943 271.864 283.303C273.374 290.183 274.814 297.073 276.314 304.083C275.324 304.593 274.744 305.123 274.094 305.193C258.274 307.063 242.434 308.803 226.624 310.733C217.054 311.903 207.514 311.853 197.894 311.453C187.604 311.033 177.254 310.833 166.974 311.413C156.684 311.993 146.464 313.893 136.184 314.803C123.204 315.963 110.264 317.583 97.1539 317.113C89.7839 316.853 82.3739 317.693 74.9739 318.023C67.7739 318.343 60.5739 318.763 53.3639 318.923C46.1439 319.083 38.9139 318.953 31.2639 318.953C28.6039 315.003 28.1839 309.913 27.4139 304.953C25.2539 290.943 24.9639 276.803 24.4139 262.663C24.0839 254.153 23.0139 245.683 22.2339 237.193C21.1439 225.293 20.0139 213.393 18.9139 201.493C17.4139 185.273 16.1339 169.033 14.3639 152.853C12.9139 139.653 13.6039 126.493 13.8239 113.293C14.0239 101.163 13.4339 89.0234 13.3939 76.8934C13.3839 74.4634 14.3839 72.0434 14.7039 69.5934C15.3439 64.5834 16.2139 59.5734 16.3239 54.5434C16.5539 43.6934 16.3839 32.8234 16.3839 21.3334Z"
                  fill="#FAFFDC"
               />
               <path
                  :style="{ fill: colors[color].back }"
                  d="M285.934 296.973C281.704 279.223 275.894 262.253 273.414 243.753C294.404 242.793 314.804 241.853 335.764 240.883C330.864 246.943 326.164 252.823 321.384 258.633C317.144 263.793 312.874 268.943 308.484 273.973C305.174 277.783 301.734 281.493 298.204 285.093C294.424 288.933 290.464 292.593 285.934 296.973Z"
                  fill="#DCE0C6"
               />
            </svg>
         </span>
      </Moveable>
      <div class="modal" v-show="dialog">
         <div v-if="memos[selectedMemoIdx]" class="modal-content">
            <textarea v-model="memos[selectedMemoIdx].content" type="text" class="input-box" />
            <span class="close" @click="dialog = !dialog">닫기</span>
         </div>
      </div>
   </div>
</template>
<script>
import axios from 'axios';
import Moveable from 'vue-moveable';

const MEMO_MAX_SIZE = 10;

export default {
   name: 'app',
   components: {
      Moveable,
   },
   data() {
      return {
         MovedisableState: {
            draggable: false,
            scalable: false,
            rotatable: false,
            resizable: false,
            pinchable: false,
            throttleDrag: 0,
            keepRatio: true,
            throttleScale: 0,
            throttleRotate: 0,
            origin: false,
            zoom: 1,
            className: 'movedisable',
            // dragArea: true,
            snappable: true,
            bounds: { left: 0, top: 0, right: 1000, bottom: 600 },
         },
         MoveableState: {
            draggable: true,
            scalable: true,
            rotatable: true,
            resizable: false,
            pinchable: true,
            throttleDrag: 0,
            keepRatio: true,
            throttleScale: 0,
            throttleRotate: 0,
            origin: false,
            zoom: 1,
            className: 'moveable',
            // dragArea: true,
            snappable: true,
            bounds: { left: 0, top: 0, right: 1000, bottom: 600 },
         },
         deskId: undefined,
         memos: [],
         hasInit: false,
         editable: false,
         clickedMemo: false,
         selectedMemoIdx: -1,
         dialog: false,
         zIndexCount: 1,
         createMemoKeyIndex: 0,
         removedList: [],
         colors: [
            { colorName: '오렌지', front: 'rgb(255, 228, 225)', back: 'rgb(255, 160, 122)' },
            { colorName: '그린', front: 'rgb(240, 255, 240)', back: 'rgb(152, 251, 152)' },
            { colorName: '스카이블루', front: 'rgb(240, 255, 255)', back: 'rgb(224, 255, 255)' },
            { colorName: '레몬', front: 'rgb(245, 245, 220)', back: 'rgb(238, 232, 170)' },
            { colorName: '토마토', front: 'rgb(255, 69, 0)', back: 'rgb(255, 0, 0)' },
         ],
      };
   },
   methods: {
      async getMemos() {
         // var tmp = JSON.parse(localStorage.getItem('memos'));
         // return tmp;
         return await axios
            .get('http://k4a401.p.ssafy.io:2000/desk/all', { params: { nickname: 'ssafy' } })
            .then((res) => {
               console.log(res.data.data);
               this.deskId = res.data.data.deskId;
               console.log('%cApp.vue line:149 deskId', 'color: #007acc;', this.deskId);
               return res.data.data.memoList;
            })
            .catch((err) => {
               console.error(err);
               return undefined;
            });
      },
      async initMemos() {
         this.memos = (await this.getMemos()) || [];
         this.hasInit = false;
         // this.memos = JSON.parse(localStorage.getItem('memos')) || [];
         for (let i = 0; i < this.memos.length; i++) {
            // moveable 초기 세팅
            // 생성하고 난 후 기존에 있던 메모의 위치로 이동해야 하므로 MoveableState 사용
            this.memos[i].moveable = { ...this.MovedisableState };
         }
      },
      handleDrag({ target, transform }) {
         target.style.transform = transform;
      },
      handleRenderEnd(index, event) {
         this.memos[index].transform = event.target.style.transform;
      },
      handleRotate({ target, transform }) {
         target.style.transform = transform;
      },
      handleScale({ target, transform }) {
         target.style.transform = transform;
      },
      edit() {
         this.editable = true; // 편집을 가능 상태로 변경
         this.removedList = []; // 삭제했던 리스트 초기화
         this.selectedMemoIdx = -1; // 클릭된 메모 없음 상태로 변경
         // 모든 메모지를 이동가능한 상태로 업데이트
         for (let i = 0; i < this.memos.length; i++) this.setMemoState(i, true);
      },
      editComplete() {
         this.saveMemo(); // 메모 저장
         this.editable = false; // 편집 불가능 상태로 변경
         this.removedList = []; // 삭제했던 리스트 초기화
         this.selectedMemoIdx = -1; // 클릭된 메모 없음 상태로 변경
         // 모든 메모지를 움직일 수 없는 상태로 업데이트
         for (let i = 0; i < this.memos.length; i++) this.setMemoState(i, false);
      },
      setMemoState(index, state) {
         // 메모지 상태 업데이트
         this.memos[index].moveable.className = state ? 'moveable' : 'movedisable';
         this.memos[index].moveable.draggable = state;
         this.memos[index].moveable.scalable = state;
         this.memos[index].moveable.rotatable = state;
      },
      onSelectMemo(index) {
         // Todo : zindex 관리 필요
         if (this.selectedMemoIdx >= 0 && this.selectedMemoIdx < this.memos.length) {
            this.memos[this.selectedMemoIdx].moveable.className = this.editable ? 'moveable' : 'movedisable'; // 이전에 클릭한 메모의 선택이 풀린 UI로 변경
            this.memos[this.selectedMemoIdx].zIndex = this.zIndexCount++;
         }
         this.selectedMemoIdx = index; // 클릭된 메모의 index 업데이트
         this.memos[this.selectedMemoIdx].moveable.className = this.editable ? 'clicked' : 'moveable'; // 클릭된 UI 적용
         this.memos[this.selectedMemoIdx].zIndex = 3000 + this.zIndexCount;
      },
      removeMemo() {
         this.removedList.push(this.memos[this.selectedMemoIdx].memoId);
         this.memos.splice(this.selectedMemoIdx, 1);
         this.selectedMemoIdx = -1;
      },
      createMemo() {
         if (this.memos.length >= MEMO_MAX_SIZE) return; // 메모지는 최대갯수를 넘길 수 없음
         this.memos.push({
            deskId: this.deskId,
            memoId: 'tmp' + this.createMemoKeyIndex++,
            content: '',
            zIndex: 1,
            moveable: { ...this.MoveableState },
            color: 0,
         });
      },
      writeMemo() {
         this.dialog = true;
      },
      saveMemo() {
         this.memos = this.memos.map((e) => {
            if (isNaN(e.memoId)) {
               e.memoId = undefined;
               return e;
            } else {
               return e;
            }
         });
         console.log('%cApp.vue line:245 this.removedList', 'color: #007acc;', this.removedList);
         this.removedList = this.removedList.filter((e) => {
            return !isNaN(e);
         });
         // 메모 저장
         axios
            .post('http://k4a401.p.ssafy.io:2000/desk/memo/update', this.memos)
            .then((res) => {
               console.log(res);
            })
            .catch((err) => {
               console.error(err);
            });
         // 삭제된 메모
         axios
            .post('http://k4a401.p.ssafy.io:2000/desk/memo/delete', this.removedList)
            .then((res) => {
               console.log(res);
            })
            .catch((err) => {
               console.error(err);
            });
      },
      changeColor(index) {
         this.memos[this.selectedMemoIdx].color = index;
      },
   },
   created() {
      this.initMemos(); // memo 초기화
   },
   mounted() {},
};
</script>
<style>
html,
body {
   width: 100%;
   height: 100%;
   margin: 0;
   padding: 0;
}
#app {
   position: relative;
   width: 100%;
   height: 100%;
}
.postit {
   display: flex;
   justify-content: center;
   align-items: center;
   /* width: 100%;
  height: 100%; */
   /* background-color: red; */
}
.postit-content {
   position: absolute;
   padding: 20px;
}
.moveable-container {
   position: absolute;
   display: flex;
   justify-content: center;
   align-items: center;
   width: 200px;
   height: 200px;
}
.moveable .moveable-line {
   background: black !important;
}
.moveable .moveable-control {
   display: none;
}
.moveable .moveable-rotation-line {
   display: none;
}
.movedisable .moveable-line,
.moveable-control {
   display: none;
}
.clicked .moveable-control,
.moveable-line,
.moveable-rotation-line {
   display: block;
}
/* The Modal (background) */
.modal {
   position: fixed; /* Stay in place */
   z-index: 10000; /* Sit on top */
   padding-top: 100px; /* Location of the box */
   left: 0;
   top: 0;
   width: 100%; /* Full width */
   height: 100%; /* Full height */
   overflow: auto; /* Enable scroll if needed */
   background-color: rgb(0, 0, 0); /* Fallback color */
   background-color: rgba(0, 0, 0, 0.8); /* Black w/ opacity */
}

/* Modal Content */
.modal-content {
   background-color: #fefefe;
   margin: auto;
   padding: 30px;
   border: 1px solid #888;
   width: 80%;
}

/* The Close Button */
.close {
   color: #aaaaaa;
   float: right;
   font-size: 20px;
   font-weight: bold;
}

.close:hover,
.close:focus {
   color: #000;
   text-decoration: none;
   cursor: pointer;
}

textarea {
   border: 2px solid;
   border-radius: 5px;
   width: 100%;
   min-height: 10%;
}
</style>
