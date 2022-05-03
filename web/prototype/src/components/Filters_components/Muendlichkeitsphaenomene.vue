<template>
  <b-card no-body class="mb-1">
    <b-card-header header-tag="header" class="p-1 purple-group" role="tab">
      <b-button block href="#" v-b-toggle.mündlichkeitsphänomene class="p-1 text-left bg-transparent text-dark no-border">
        <b>Mündlichkeitsphänomene</b>
        <div class="float-right"><b-badge class="rounded-circle" variant="light" v-b-tooltip.hover.html="tooltipTitle">?</b-badge></div>
      </b-button>
    </b-card-header>
    <b-collapse id="mündlichkeitsphänomene" role="tabpanel">
      <b-card-body>
        <a href="#" class="float-right" @click="resetAll">Reset all</a>
        <!-- make a slider for each mündlichkeitsphänomen -->
        <template v-for="(mündlichkeitsphänomen) in getMündlichkeitsphänomene">
          <!-- Slider used for selecting the range -->
          <SliderComponent :name="mündlichkeitsphänomen"
                           :label="labels[mündlichkeitsphänomen]"
                           :key="mündlichkeitsphänomen"
                           :range="mündlichkeitsphänomeneRanges[mündlichkeitsphänomen]"
                           :extent="getMündlichkeitsphänomeneExtents[mündlichkeitsphänomen]"
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
import _ from 'lodash'
import 'vue-slider-component/theme/antd.css'
import SliderComponent from "@/components/Filters_components/SliderComponent.vue"

8
export default {
  components: {
    SliderComponent
  },
  data() {
    return {
      measure: "mündlichkeitsphänomene",
      inputText: "Anteil Token in %: ",
      tooltipTitle: "Anteil ausgewählter Mündlichkeitsphänomene im Sprechereignis. <a href='../../doc/Handreichung-ZuMal.html#_4.2_Ausgewählte_Mündlichkeitsphänom' target='_blank'><i>→Handreichung zur Arbeit mit ZuMal: 4.2 Ausgewählte Mündlichkeitsphänomene</i></a>",
      labels: {
        NGHES: "Häsitationen (NGHES)",
        NGIRR: "Interjektionen, Responsive und Rezeptionssignale (NGIRR)",
        PTKMA: "Modalpartikeln (PTKMA)",
        SEDM: "Diskursmarker (SEDM)",
        SEQU: "Tag Questions (SEQU)",
        CLITIC: "Klitisierungen (CLITIC)"
      }
    }
  },
  computed: {
    ...mapState([
      'mündlichkeitsphänomeneRanges'
    ]),
    ...mapGetters([
      'getMündlichkeitsphänomene',
      'getMündlichkeitsphänomeneExtents'
    ]),
  },
  methods: {
    resetAll() {
      this.getMündlichkeitsphänomene.forEach(mündlichkeitsphänomen => {
        this.$store.dispatch('updateRange', {measure: mündlichkeitsphänomen, extent: this.getMündlichkeitsphänomeneExtents[mündlichkeitsphänomen]});
      });
    }
  }
}
</script>

<style scoped>
</style>

<style>
</style>
