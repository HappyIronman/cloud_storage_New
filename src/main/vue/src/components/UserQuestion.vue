<template>
  <div>
    <div class="uk-text-center">
      <span class="uk-label uk-label-success">TA的提问</span>
    </div>
    <div v-if="userQuestionList!=null&&userQuestionList.length>0">
      <div v-for="question in userQuestionList">
        <user-question-item v-bind:question="question"></user-question-item>
      </div>
    </div>
    <blank-icon v-else></blank-icon>
    <pageable v-bind:fetch-data-func="fetchUserQuestionList" size="5"></pageable>
  </div>
</template>

<script>
  import {mapActions, mapState} from 'vuex'
  import SelfPageNav from "./SelfPageNav.vue";
  import Pageable from "./Pageable.vue";
  import UserQuestionItem from "./UserQuestionItem.vue";
  import BlankIcon from "./BlankIcon.vue";

  export default {
    components: {
      BlankIcon,
      UserQuestionItem,
      Pageable,
      SelfPageNav
    },
    name: 'UserQuestion',
    data() {
      return {
        msg: 'Welcome to Ironman\'s world haha !'
      }
    },
    computed: {
      ...mapState({
        userQuestionList: state => state.question.userQuestionList
      })
    },
    methods: {
      ...mapActions([
        'fetchUserQuestionListAction'
      ]),
      fetchUserQuestionList: function (pageParam) {
        return this.fetchUserQuestionListAction({uniqueId: this.$route.params.uniqueId, pageParam: pageParam})
      }
    }
  }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>

</style>
