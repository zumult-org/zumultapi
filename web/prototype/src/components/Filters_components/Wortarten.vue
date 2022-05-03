<template>
  <b-card no-body class="mb-1">
    <b-card-header header-tag="header" class="p-1 purple-group" role="tab">
      <b-button block href="#" v-b-toggle.wortarten class="p-1 text-left bg-transparent text-dark no-border">
        <b>Wortarten</b>
        <div class="float-right"><b-badge class="rounded-circle" variant="light" v-b-tooltip.hover.html="tooltipTitle">?</b-badge></div>
      </b-button>
    </b-card-header>
    <b-collapse id="wortarten" role="tabpanel">
      <b-card-body>
        <a href="#" class="float-right" @click="resetAll">Reset all</a>
        <!-- make a slider for each wortart -->
        <template v-for="(wortart) in getWortarten">
          <!-- Slider used for selecting the range -->
          <SliderComponent :name="wortart" 
                           :label="labels[wortart]"
                           :key="wortart"
                           :range="wortartenRanges[wortart]"
                           :extent="getWortartenExtents[wortart]"
                           :isAnteil="true"
                           :inputText="inputText"
          />
        </template>
      </b-card-body>
    </b-collapse>
  </b-card>
</template>


<script>
import { mapState, mapGetters } from 'vuex'
import 'vue-slider-component/theme/antd.css'
import SliderComponent from "@/components/Filters_components/SliderComponent.vue"

8
export default {
  components: {
    SliderComponent
  },
  data() {
    return {
      measure: "wortarten",
      inputText: "Anteil Token in %: ",
      tooltipTitle: "Anteil ausgewählter Wortarten im Sprechereignis. <a href='../../doc/Handreichung-ZuMal.html#_4.1__Wortarten' target='_blank'><i>→Handreichung zur Arbeit mit ZuMal: 4.1 Wortarten</i></a>",
      labels: {
        NN: "Nomen (NN)",
        NE: "Eigennamen (NE)",
        V: "Verben (V)",
        ADJ: "Adjektive (ADJ)",
        ADV: "Adverbien (ADV)",
        PTKVZ: "Trennbare Verben in Distanzstellung (PTKVZ)"
      }
    }
  },
  computed: {
    ...mapState([
      'wortartenRanges'
    ]),
    ...mapGetters([
      'getWortarten',
      'getWortartenExtents'
    ]),
  },
  methods: {
    resetAll() {
      this.getWortarten.forEach(wortart => {
        this.$store.dispatch('updateRange', {measure: wortart, extent: this.getWortartenExtents[wortart]});
      });
    }
  }
}
</script>

<style scoped>
</style>

<style>
</style>
