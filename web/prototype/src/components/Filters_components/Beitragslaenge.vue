<template>
<!-- 
  This component is currently outdated and unused.
  We have decided not to included Beitragslaenge in the end.
 -->
  <b-card no-body class="mb-1">
    <b-card-header header-tag="header" class="p-1 red-group" role="tab">
      <b-button block href="#" v-b-toggle.beitragslaenge class="p-1 text-left bg-transparent text-dark no-border">
        Beitragslänge
        <b-badge class="rounded-circle" variant="info" v-b-tooltip.hover :title="tooltipTitle">?</b-badge>
      </b-button>
    </b-card-header>
    <b-collapse id="beitragslaenge" role="tabpanel">
      <b-card-body>
        <div class="beitragslaengeContainer">

          <div>Reichweite:
            <b-form-input 
              class="d-inline range-input"
              size="sm"
              type="number"
              :min="0"
              :max="beitragslaengeInputRange[1]"
              :step="1"
              v-model="beitragslaengeInputRange[0]" 
              @input="clearErrorMsg()" />
               - 
            <b-form-input 
              class="d-inline range-input"
              size="sm"
              type="number"
              :min="beitragslaengeInputRange[0]"
              :max="100"
              :step="1"
              v-model="beitragslaengeInputRange[1]" 
              @input="clearErrorMsg" />
          </div>
          <span style="color: red; margin-left: 20px;">{{ errorMsg }}</span>
          
          <vue-slider 
            ref="slider"
            v-model="beitragslaengeInputRange"
            :interval="1"
            :marks="[0, 20, 40, 60, 80, 100]"
            @error="error"
            @change="clearErrorMsg" 
            :enable-cross="false"
            :lazy="true"
          ></vue-slider>
        </div>
      </b-card-body>
    </b-collapse>
  </b-card>
</template>


<script>
import { mapState, mapGetters, mapMutations, mapActions } from 'vuex'
import _ from 'lodash'
import VueSlider from 'vue-slider-component'
import 'vue-slider-component/theme/antd.css'

const ERROR_TYPE = {
    VALUE: 1,
    INTERVAL: 2,
    MIN: 3,
    MAX: 4,
    ORDER: 5,
  }

export default {
  props: {},
  components: {
    VueSlider
  },
  data() {
    return {
      errorMsg: '',
      selected: "",
      tooltipTitle: 'Beitragslänge description',
    }
  },
  computed: {
    ...mapState([
      'beitragslaengeRange'
    ]),
    ...mapGetters([
    ]),
    beitragslaengeInputRange: {
      get() {
        return this.beitragslaengeRange;
      },
      set(value) {
        this.$store.dispatch('updateBeitragslaengeRange', value); // tooo intense 
      }
    },
  },
  methods: {
    error(type, msg) {
      switch (type) {
        case ERROR_TYPE.MIN:
          break
        case ERROR_TYPE.MAX:
          break
        case ERROR_TYPE.VALUE:
          break
      }
      this.errorMsg = msg
    },
    clearErrorMsg(e, max) {
      this.errorMsg = ''
    }
  },
  mounted: function() {
  }
}
</script>

<style scoped>
</style>
