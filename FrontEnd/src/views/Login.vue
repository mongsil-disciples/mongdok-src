<template lang="">
   <div class="login">
      <div class="lg-container">
         <div class="content">
            <p class="sub">당신의 꿈과 함께하는 공간</p>
            <p class="main kyoboHand">몽실이네 독서실</p>
            <v-lottie-player name="scooterAnim" :width="'250px'" :height="'250px'" loop path="https://assets7.lottiefiles.com/private_files/lf30_z8mfdyic.json" />
            <login-kakao-button></login-kakao-button>
            <p class="btnFAQ" @click="toggleModalFAQ"><i class="fas fa-question-circle"></i> 몽실이네 독서실이 궁금하세요?</p>
            <modal-faq :isOpenFAQ="isOpenFAQ" @onClick="toggleModalFAQ"></modal-faq>
            <p class="btnFAQ btnGuide" @click="toggleModalGuide"><i class="fas fa-info-circle"></i> 퀵 가이드</p>
            <modal-guide :isOpenGuide="isOpenGuide" @onClick="toggleModalGuide"></modal-guide>
         </div>
         <logo></logo>
      </div>
   </div>
</template>
<script>
import LoginKakaoButton from '@/components/LoginKakaoButton';
import ModalFaq from '@/components/ui/ModalFaq';
import ModalGuide from '@/components/ui/ModalGuide';
import Logo from '@/components/ui/Logo';

export default {
   name: 'Login',
   components: { LoginKakaoButton, Logo, ModalFaq, ModalGuide },
   props: {},
   data() {
      return {
         isOpenFAQ: false,
         isOpenGuide: false, // 가이드 토글용
      };
   },
   computed: {},
   watch: {},
   //lifecycle area
   methods: {
      toggleModalFAQ() {
         this.isOpenFAQ = !this.isOpenFAQ;
      },
      toggleModalGuide() {
         this.isOpenGuide = !this.isOpenGuide;
      },
   },
};
</script>
<style scoped lang="scss">
.login {
   width: 100%;
   height: 100%;

   display: flex;
   align-items: center;
   justify-content: center;

   /* border: 1px solid red; */

   .lg-container {
      display: inherit;
      flex-direction: column;
      justify-content: space-between;
      align-items: center;
      width: 70%;
      height: 90%;

      /* border: 1px solid rgb(34, 0, 255); */

      .content {
         /* border: 1px solid rgb(0, 159, 93); */
         /* margin-top: 20vh; */

         p.sub {
            color: rgb(164, 164, 164);
            font-weight: 600;
            font-size: 9pt;
            letter-spacing: 3px;
            text-align: center;
         }

         p.main {
            color: rgb(53, 53, 53);
            font-weight: 600;
            font-size: 20pt;
            text-align: center;
            margin-top: 12px;
            margin-bottom: 20px;
         }

         .kakao {
            margin: 0px auto;
            margin-top: 20px;
         }

         .btnFAQ {
            cursor: pointer;
            font-size: 10pt;
            font-weight: 600;
            text-align: center;
            letter-spacing: 2px;
            color: rgb(176, 176, 176);

            margin-top: 30px;

            &.btnGuide {
               margin-top: 10px;
            }
         }
      }
   }
}
</style>
