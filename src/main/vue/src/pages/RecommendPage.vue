<template>
  <div>
    <div class="uk-text-center">
      <span class="uk-label uk-label-success">个性推荐</span>
    </div>
    <div v-if="recommendList!=null&&recommendList.length>0">
      <div v-for="item in recommendList">
        <recommend-item v-bind:blog="item"></recommend-item>
      </div>
    </div>
    <blank-icon v-else></blank-icon>
    <pageable v-bind:fetch-data-func="fetchMyRecommendListAction" size="5"></pageable>

  </div>
</template>

<script>
  import {mapActions, mapState} from 'vuex'
  import RecommendItem from "../components/RecommendItem.vue";
  import Pageable from '../components/Pageable.vue'
  import BlankIcon from "../components/BlankIcon.vue";

  export default {
    components: {
      BlankIcon,
      RecommendItem,
      Pageable
    },
    name: 'RecommendPage',
    data() {
      return {}
    },
    computed: {
      ...mapState({
        recommendList: state => state.common.myRecommendList,
      })
    },

    methods: {
      ...mapActions([
        'fetchMyRecommendListAction'
      ]),
    }
  }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>

</style>
