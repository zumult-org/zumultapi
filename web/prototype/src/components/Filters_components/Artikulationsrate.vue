<template>
<!-- 
  Component for Sprechgeschwindigkeit (Artikulationsrate)
 -->
  <b-card no-body class="mb-1">
    <b-card-header header-tag="header" class="p-1 red-group" role="tab">
      <b-button block href="#" v-b-toggle.artikulationsrate class="p-1 text-left bg-transparent text-dark no-border">
        <b>Sprechgeschwindigkeit (Artikulationsrate)</b>
        <div class="float-right"><b-badge class="rounded-circle" variant="light" v-b-tooltip.hover.html="tooltipTitle">?</b-badge></div>
      </b-button>
    </b-card-header>
    <b-collapse id="artikulationsrate" role="tabpanel">
      <b-card-body>
        <!-- Slider used for selecting the range -->
        <SliderComponent :name="measure" 
                         :range="artikulationsrateRange"
                         :extent="getArtikulationsrateExtent"
                         :isAnteil="false"
                         :inputText="inputText"
          />
      </b-card-body>
    </b-collapse>
  </b-card>
</template>


<script>
import { mapState, mapGetters } from 'vuex'
import SliderComponent from "@/components/Filters_components/SliderComponent.vue"
import 'vue-slider-component/theme/antd.css'

export default {
  props: {},
  components: {
    SliderComponent
  },
  data() {
    return {
      measure: "artikulationsrate",
      inputText: "Anzahl artikulierter Silben pro Sekunde: ",
      tooltipTitle: "Anteil der artikulierten Silben pro Sekunde. <a href='../../doc/Handreichung-ZuMal.html#_3.3_Sprechgeschwindigkeit_Artikula' target='_blank'><i>→Handreichung zur Arbeit mit ZuMal: 3.3 Sprechgeschwindigkeit (Artikulationsrate)</i></a>",
    }
  },
  computed: {
    ...mapState([
      'artikulationsrateRange'
    ]),
    ...mapGetters([
      'getArtikulationsrateExtent'
    ]),
  },
  methods: {
  },
  mounted: function() {
  }
}
</script>

<style scoped>
</style>
