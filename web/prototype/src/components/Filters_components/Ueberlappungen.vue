<template>
  <b-card no-body class="mb-1">
    <b-card-header header-tag="header" class="p-1 red-group" role="tab">
      <b-button block href="#" v-b-toggle.ueberlappungen class="p-1 text-left bg-transparent text-dark no-border">
        <b>Überlappungen</b>
        <div class="float-right"><b-badge class="rounded-circle" variant="light" v-b-tooltip.hover.html="tooltipTitle">?</b-badge></div>
      </b-button>
    </b-card-header>
    <b-collapse id="ueberlappungen" role="tabpanel">
      <b-card-body>
        <!-- Slider used for selecting the range -->
        <SliderComponent :name="measure" 
                         :range="ueberlappungenRange"
                         :extent="getUeberlappungenExtent"
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
      measure: "anteil_Überlappungen_mit_mehr_als_2_wörtern_tokens",
      inputText: "Anteil Überlappungen (> 2 Wörter) pro 1000 Token: ",
      tooltipTitle: "Gleichzeitig gesprochene Sequenzen (d.h. von mehreren Sprecher*innen; 2 und mehr Token Länge) pro 1000 Token. <a href='../../doc/Handreichung-ZuMal.html#_3.4_Überlappungen' target='_blank'><i>→Handreichung zur Arbeit mit ZuMal: 3.4 Überlappungen</i></a>"
    }
  },
  computed: {
    ...mapState([
      'ueberlappungenRange'
    ]),
    ...mapGetters([
      'getUeberlappungenExtent'
    ])
  }
}
</script>

<style scoped>
</style>
