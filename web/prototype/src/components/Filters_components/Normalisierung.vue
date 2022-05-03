<template>
  <b-card no-body class="mb-1">
    <b-card-header header-tag="header" class="p-1 red-group" role="tab">
      <b-button block href="#" v-b-toggle.normalisierung class="p-1 text-left bg-transparent text-dark no-border">
        <b>Standardnähe (Normalisierungsrate)</b>
        <div class="float-right"><b-badge class="rounded-circle" variant="light" v-b-tooltip.hover.html="tooltipTitle">?</b-badge></div>
      </b-button>
    </b-card-header>
    <b-collapse id="normalisierung" role="tabpanel">
      <b-card-body>
        <!-- Slider used for selecting the range -->
        <SliderComponent :name="measure" 
                         :range="normalisierungRange"
                         :extent="getNormalisierungExtent"
                         :isAnteil="true"
                         :inputText="inputText"
          />
      </b-card-body>
    </b-collapse>
  </b-card>
</template>


<script>
import { mapState, mapGetters } from 'vuex'
import SliderComponent from "@/components/Filters_components/SliderComponent.vue"

export default {
  props: {},
  components: {
    SliderComponent
  },
  data() {
    return {
      measure: "normalisierungsrate",
      inputText: "Anteil normalisierter Token in %: ",
      tooltipTitle: "Angezeigt wird die Normalisierungsrate. Sie gibt an, wie stark das an der Mündlichkeit orientierte Transkript von der üblichen Standardorthographie abweicht. Je höher die Normalisierungsrate, desto stärker ist der Anteil deutlich mündlicher Sprachphänomene (z.B. stark umgangssprachliche oder dialektale Äußerungen und Aussprachevarianten), je niedriger sie ist, desto standardnäher ist die Äußerung. <a href='../../doc/Handreichung-ZuMal.html#_3.2_Standardnähe_Normalisierungsra' target='_blank'><i>→Handreichung zur Arbeit mit ZuMal: 3.2 Standardnähe (Normalisierungsrate)</i></a>",
    }
  },
  computed: {
    ...mapState([
      'normalisierungRange'
    ]),
    ...mapGetters([
      'getNormalisierungExtent'
    ])
  }
}
</script>

<style scoped>
</style>