<template>
  <b-card no-body class="mb-1">
    <b-card-header header-tag="header" class="p-1 green-group" role="tab">
      <b-button block href="#" v-b-toggle.dauer class="p-1 text-left bg-transparent text-dark no-border">
        <b>Dauer</b>
        <div class="float-right"><b-badge class="rounded-circle" variant="light" v-b-tooltip.hover.html="tooltipTitle">?</b-badge></div>
      </b-button>
    </b-card-header>
    <b-collapse id="dauer" role="tabpanel">
      <b-card-body>
        <!-- Slider used for selecting the range -->
        <SliderComponent :name="measure" 
                         :range="dauerRange"
                         :extent="getDauerExtent"
                         :dauerData="getDauerTextData"
                         :dauerMarks="marks"
          />
      </b-card-body>
    </b-collapse>
  </b-card>
</template>


<script>
import { mapState, mapGetters } from 'vuex'
import SliderComponent from "@/components/Filters_components/SliderComponent.vue"
import { timeStringToMin, minutesToTimeString, getMarks } from '@/helper'


export default {
  props: {},
  components: {
    SliderComponent
  },
  data() {
    return {
      measure: "dauer",
      tooltipTitle: "Dauer des Sprechereignisses in Minuten bzw. Stunden. <a href='../../doc/Handreichung-ZuMal.html#_2.6__Dauer' target='_blank'><i>→Handreichung zur Arbeit mit ZuMal: 2.6 Dauer</i></a>",
    }
  },
  computed: {
    ...mapState([
      'dauerRange'
    ]),
    ...mapGetters([
      'getDauerExtent'
    ]),
    // list time units, with increment of 1 min ["00:00","00:01",..."05:30", etc]
    getDauerTextData() {
      let allTimeAsText = [];
      const start = timeStringToMin(this.getDauerExtent[0]);
      const end = timeStringToMin(this.getDauerExtent[1]);

      for (let i = start; i < end + 1; i++) {
        allTimeAsText.push(minutesToTimeString(i));
      }
      return allTimeAsText;
    },
    // get the formatted marks to display on the slider axis.
    marks() {
      const start = timeStringToMin(this.getDauerExtent[0]);
      const end = timeStringToMin(this.getDauerExtent[1]);
      const minMarks = getMarks([start, end]);

      const marks = minMarks.map(time => minutesToTimeString(time));
      return marks;
    },
  }
}
</script>

<style scoped>
</style>

<style>
  .range-input {
    width: auto !important;
  }
</style>
