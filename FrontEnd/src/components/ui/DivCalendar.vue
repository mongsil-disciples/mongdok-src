<template lang="">
  <div class="study-calendar">
    <!-- 달력 관련 부분 -->
    <div class="calendar-warpper">
      <!-- 최상단 : 년월 이동 버튼 존재 -->
      <div class="top-wrapper">
        <div v-wave class="btn" @click="calendarData(-1)"><i class="fas fa-caret-left"></i></div>
        {{ year }} / {{ month }}
        <div v-wave class="btn" @click="calendarData(1)"><i class="fas fa-caret-right"></i></div>
      </div>
      <!-- 달력 부분이 들어감 -->
      <table class="calendar">
        <!-- 월화수목금토일 -->
        <thead class="head-section">
          <th v-for="day in days" :key="day">{{ day }}</th>
        </thead>

        <!-- 달력부분 -->
        <tbody class="day-section">
          <tr v-for="(week, idx) in dates" :key="idx">
            <td
              v-wave
              v-for="(day, jdx) in week"
              :key="jdx"
              class="day"
              :class="[
                {
                  'day--today': day === today && month === currentMonth && year === currentYear,
                  'day--pre': idx === 0 && day >= lastMonthStart,
                  'day--next': dates.length - 1 === idx && nextMonthStart > day,
                },
                `level--${convertLevel(day)}`,
              ]"
              @click="openDayDetail(day)"
            >
              {{ day }}
            </td>
          </tr>
        </tbody>
      </table>
      <div class="desc">
        <div v-for="(item, idx) in levelDesc" :key="'desc-' + idx" class="col">
          <div class="color-box" :style="{ 'background-color': item.color }"></div>
          <div class="color-desc">{{ item.desc }}</div>
        </div>
      </div>
    </div>

    <!-- 달력 디테일 부분 -->
    <transition name="detail-down">
      <div v-if="isOpenDetail" class="detail-wrapper">
        <p class="title">DAY STUDY TIME</p>
        <table class="running-table">
          <tr class="day" v-for="(items, indexs) in dayDetail" :key="'dayLine' + indexs">
            <td class="head">
              <img :src="require(`@/assets/img/emoji/${emoji[indexs]}.png`)" alt="" />
            </td>
            <td
              class="time"
              v-for="(item, index) in items"
              :key="'day-' + indexs + '-' + index"
              :class="{ 'study-hour': item }"
            >
              {{ getDayDisplay(8 * indexs + index) }}
            </td>
          </tr>
        </table>

        <div class="statistics">
          <p class="title">오늘 공부한 시간</p>
          <p class="num">{{ convertMs2Time(this.selectedDayTotal) }}</p>
        </div>
      </div>
    </transition>
  </div>
</template>
<script>
import { mapState } from 'vuex';

export default {
  props: {
    userName: String,
  },
  data() {
    return {
      days: ['일', '월', '화', '수', '목', '금', '토'],
      dates: [],
      emoji: ['night', 'day', 'noon', 'fire'],

      // 달력 제어용
      currentYear: 0, // 실제 현재 연도
      currentMonth: 0, // 실제 현재 월
      year: 0, // 조회용 연도
      month: 0, // 조회용 월
      today: 0,

      // 달력에서 회색으로 표기하기 위한 변수
      lastMonthStart: 0,
      nextMonthStart: 0,

      // 선택한 날의 상세 러닝타임
      isOpenDetail: false, // 상세 공부 시간 모달 제어
      selectedDayDetail: [], // 선택한 날의 공부시간 배열(24H)
      selectedDayTotal: 0, // 선택한 날의 전체 공부 시간
      dayRunningTable: [7, 8, 9, 10, 11, 12, 13, 14],
      noonRunningTable: [15, 16, 17, 18, 19, 20, 21, 22],
      nightRunningTable: [23, 24, 1, 2, 3, 4, 5, 6],
      levelDesc: [
        {
          desc: '0~3H',
          color: '#a9ffa9',
        },
        {
          desc: '3~6H',
          color: '#5cdf5c',
        },
        {
          desc: '6~9H',
          color: '#10a410',
        },
        {
          desc: '9H▴',
          color: '#073a07',
        },
      ],
    };
  },
  computed: {
    ...mapState({
      studyCalendarMonth: (state) => state.calendar.studyCalendarMonth, // 보고있는 책상의 ID
      studyCalendarDay: (state) => state.calendar.studyCalendarDay, //  책상의 메모들
      runningTimeCalendar: (state) => state.calendar.runningTimeCalendar, //  책상의 디데이들
    }),
    dayDetail() {
      const dDetail = this.selectedDayDetail;
      const tmps = [];
      let tmp = [];
      for (let i = 1; i <= 24; i++) {
        tmp.push(dDetail[i - 1]);
        if (i % 8 === 0) {
          tmps.push(tmp);
          tmp = [];
        }
      }
      // 24시간이 넘을 경우
      if (dDetail.length > 24) {
        for (let i = 25; i <= 32; i++) {
          tmp.push(dDetail[i - 1] ? dDetail[i - 1] : false);
        }
        tmps.push(tmp);
      }
      return tmps;
    },
  },
  watch: {
    month: {
      // immediate: true,
      handler(value) {
        // console.log('📅 watch');

        const param = {
          userName: this.userName,
          month: value,
          year: this.year,
        };

        this.$store.dispatch('GET_CALENDAR', param);
      },
    },
  },
  // beforeCreate() {
  //    console.log('📅 beforeCreate');
  //    console.log(this.days);
  // },
  created() {
    this.initCal();
  },
  methods: {
    // 달력 초기 셋팅
    initCal: function() {
      // console.log('📅 initCal');
      const date = new Date();

      this.currentYear = date.getFullYear();
      this.currentMonth = date.getMonth() + 1;
      this.today = date.getDate(); // 오늘 날짜

      // 현재 날짜를 기준으로, 조회용 날짜를 셋팅함
      this.year = this.currentYear;
      this.month = this.currentMonth;

      this.calendarData();
    },

    // 달력 생성
    calendarData: function(arg) {
      // 인자가 있을 경우
      if (arg < 0) {
        // -1이 들어오면 지난 달 달력으로 이동
        this.month -= 1;
      } else if (arg === 1) {
        // 1이 들어오면 다음 달 달력으로 이동
        this.month += 1;
      }

      if (this.month === 0) {
        // 작년 12월
        this.year -= 1;
        this.month = 12;
      } else if (this.month > 12) {
        // 내년 1월
        this.year += 1;
        this.month = 1;
      }

      const [monthFirstDay, monthLastDate, lastMonthLastDate] = this.getFirstDayLastDate(
        this.year,
        this.month
      );
      this.isOpenDetail = false;

      // getMonthOfDays를 통해 받은 날짜 배열을 dates 배열에 할당
      this.dates = this.getMonthOfDays(monthFirstDay, monthLastDate, lastMonthLastDate);
    },

    // 지난 달의 마지막 날짜 가져오기
    getFirstDayLastDate: function(year, month) {
      const firstDay = new Date(year, month - 1, 1).getDay(); // 이번 달 시작 요일
      const lastDate = new Date(year, month, 0).getDate(); // 이번 달 마지막 날짜
      let lastYear = year;
      let lastMonth = month - 1;
      if (month === 1) {
        lastMonth = 12;
        lastYear -= 1;
      }
      const prevLastDate = new Date(lastYear, lastMonth, 0).getDate(); // 지난 달 마지막 날짜
      return [firstDay, lastDate, prevLastDate];
    },

    // 현재 달의 날짜들을 배열로 반환
    getMonthOfDays: function(monthFirstDay, monthLastDate, prevMonthLastDate) {
      let day = 1;
      let prevDay = prevMonthLastDate - monthFirstDay + 1;
      const dates = [];
      let weekOfDays = [];

      while (day <= monthLastDate) {
        if (day === 1) {
          // 1일이 어느 요일인지에 따라 테이블에 그리기 위한 지난 셀의 날짜들을 구할 필요가 있다.
          for (let j = 0; j < monthFirstDay; j += 1) {
            if (j === 0) this.lastMonthStart = prevDay; // 지난 달에서 제일 작은 날짜
            weekOfDays.push(prevDay);
            prevDay += 1;
          }
        }
        weekOfDays.push(day);
        if (weekOfDays.length === 7) {
          // 일주일 채우면
          dates.push(weekOfDays);
          weekOfDays = []; // 초기화
        }
        day += 1;
      }
      const len = weekOfDays.length;
      if (len > 0 && len < 7) {
        for (let k = 1; k <= 7 - len; k += 1) {
          weekOfDays.push(k);
        }
      }
      if (weekOfDays.length > 0) dates.push(weekOfDays); // 남은 날짜 추가
      this.nextMonthStart = weekOfDays[0]; // 이번 달 마지막 주에서 제일 작은 날짜
      return dates;
    },

    // ===================================================
    // 현재 날짜의 러닝타임을 5단계 스탭으로 변환하여 표시
    convertLevel: function(day) {
      if (!this.runningTimeCalendar) {
        return 0;
      } else {
        var runningTime = this.runningTimeCalendar[day].runningTime;
        if (runningTime == 0) return 0;

        var convertHour = parseInt(runningTime / 3600000);

        // 4단계 기준
        // 1: 0~3시간
        // 2: 3~6시간
        // 3: 6~9시간
        // 4: 9시간 이상

        var step;
        switch (convertHour) {
          case 0:
          case 1:
          case 2:
            step = 1;
            break;
          case 3:
          case 4:
          case 5:
            step = 2;
            break;
          case 6:
          case 7:
          case 8:
            step = 3;
            break;
          default:
            step = 4;
            break;
        }

        return step;
      }
    },

    // 현재 클릭한 날짜의 상세 데이터 출력
    openDayDetail: function(day) {
      const selectedDay = this.runningTimeCalendar[day].runningDetail;

      if (selectedDay.length == 0) {
        // 상세 데이터 없는 경우
        this.isOpenDetail = false;
        return;
      }

      var sumTime = 0;
      this.selectedDayDetail = new Array(24);

      for (let i = 0; i < selectedDay.length; i++) {
        const { startTime, runningTime } = selectedDay[i];

        sumTime += runningTime;

        const _startTime = new Date(startTime);
        const startHour = _startTime.getHours();
        const runningHour = runningTime / (1000 * 60 * 60);
        // array splice 기능 이용해서 해당 시간에 true 삽입
        for (let j = 0; j < runningHour; j++) {
          this.selectedDayDetail.splice(startHour + j, 1, true); // 시작시간부터 공부시간만큼 제거
        }
      }

      this.selectedDayTotal = sumTime;

      // 상세정보 열기
      this.isOpenDetail = true;
    },

    // 밀리세턴드를 시간 형태로 변환
    convertMs2Time(ms) {
      var min = parseInt((ms / (1000 * 60)) % 60);
      var hours = parseInt((ms / (1000 * 60 * 60)) % 24);

      return `${hours}시간 ${min}분`;
    },

    getDayDisplay(day) {
      if (day < 31) return day;
      else return '⋯';
    },
  },
};
</script>
<style scoped lang="scss">
* {
  /* border: 1px dashed red; */
}

/* 테이블 day의 높이(이걸 기준으로 전체 캘린더 높이가 결정됨) */
$cell_h: 27px;

.study-calendar {
  width: 240px;
  height: 460px;
  /* border: 1px dashed red; */

  .calendar-warpper {
    width: 100%;
    height: auto;
    padding: 12px;
    padding-bottom: 30px;

    background-color: white;
    border-radius: 20px;
    box-shadow: 0px 8px 15px rgba(0, 0, 0, 0.2);

    /* border: 1px solid red; */

    /* 버튼, 날짜 등 */
    .top-wrapper {
      display: flex;
      align-items: center;
      justify-content: space-around;
      margin-bottom: 10px;

      color: rgb(31, 31, 31);
      font-weight: 600;
      font-size: 9pt;
      letter-spacing: 5px;

      .btn {
        width: 30px;
        height: 30px;
        border-radius: 50%;

        line-height: 30px;
        text-align: center;
        cursor: pointer;
      }
    }

    table.calendar {
      width: 92%;
      margin: 0 auto;
      /* border: 1px solid blue; */

      border-spacing: 0px; // cell 사이 여백을 없앰

      /* 월화수목금토일 */
      thead.head-section {
        color: gray;
        font-size: 8pt;

        th {
          height: 20px;
          line-height: 20px;
        }
        /* 일요일 */
        th:nth-child(1) {
          color: rgb(247, 53, 53);
        }
        /* 토요일 */
        th:nth-child(7) {
          color: rgb(49, 17, 255);
        }
      }
    }

    /* 하단 설명 부분 */
    .desc {
      /* width: 100%; */
      display: flex;
      justify-content: center;

      margin-top: 20px;

      .col {
        margin: 0 2px;
        width: 30px;
      }

      .color-box {
        width: 100%;
        height: 4px;
      }

      .color-desc {
        color: rgb(92, 92, 92);
        margin-top: 5px;
        font-size: 4pt;
        text-align: center;
      }
    }
  }

  .detail-wrapper {
    width: 90%;
    height: 150px;

    margin: 0 auto;
    margin-top: 10px;

    background-color: white;
    border-radius: 15px;
    box-shadow: 0px 8px 15px rgba(0, 0, 0, 0.2);

    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;

    .title {
      font-weight: 600;
      letter-spacing: 4px;
      font-size: 5pt;
      margin-bottom: 10px;
    }

    .running-table {
      border-spacing: 2px 2px;
      border-collapse: separate;

      tr.day {
        margin: 10px;
      }

      td {
        font-size: 7pt;
        width: 18px;
        vertical-align: middle;
        text-align: center;

        border: 1px solid rgb(189, 189, 189);
        color: rgb(109, 109, 109);

        &.time {
          border-radius: 3px;
        }

        &.study-hour {
          color: white;
          background-color: rgb(34, 150, 16);
        }

        &.head {
          border: none;
          background-color: transparent;
          width: 26px;
          img {
            width: 60%;
          }
        }
      }
    }

    .statistics {
      margin-top: 10px;

      p.title {
        letter-spacing: 2px;
        font-weight: 200;
        color: rgb(101, 101, 101);
        margin-bottom: 5px;
      }

      .num {
        font-size: 8pt;
        margin: 0 auto;
        text-align: center;
        font-weight: 600;
        color: rgb(4, 101, 4);
      }
    }
  }
}

/* 달력 부분의 디자인 */
/* 가독성을 위해 분리 */
tbody.day-section {
  /* background-color: rgb(203, 203, 203); */

  .day {
    text-align: center;
    font-size: 8pt;

    width: 18px;
    height: $cell_h;
    line-height: $cell_h;

    box-shadow: 0 0 0 0.5px rgb(255, 255, 255) inset;
    cursor: pointer;

    color: rgb(157, 157, 157);

    &--today {
      color: rgb(255, 77, 0) !important;
      font-weight: 600;
    }

    /* 이전달, 다음달 */
    &--pre,
    &--next {
      color: rgb(206, 206, 206) !important;
      background-color: rgb(238, 238, 238) !important;
      pointer-events: none;
    }

    /* 레벨 단계 */
    &.level--0 {
      background-color: transparent;
    }
    &.level--1 {
      color: white;
      background-color: #a9ffa9;
    }
    &.level--2 {
      color: white;
      background-color: #5cdf5c;
    }
    &.level--3 {
      color: white;
      background-color: #10a410;
    }
    &.level--4 {
      color: white;
      background-color: #073a07;
    }
  }
}

/* 트랜지션 */
.detail-down-enter-active {
  transition: all 0.5s ease;
}
.detail-down-leave-active {
  transition: all 0.2s ease;
}
.detail-down-enter,
.detail-down-leave-to {
  transform: translateY(-50px);
  opacity: 0;
}
</style>
