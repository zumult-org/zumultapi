<template>
<!-- 
  Component for Wortschatz (Niveaustufe)
 -->
  <b-card no-body class="mb-1">
    <b-card-header header-tag="header" class="p-1 red-group" role="tab">
      <b-button block href="#" v-b-toggle.frequenzlisten class="p-1 text-left bg-transparent text-dark no-border">
        <b>Wortschatz (Niveaustufe)</b>
        <div class="float-right"><b-badge class="rounded-circle" variant="light" v-b-tooltip.hover.html="tooltipTitle">?</b-badge></div>
      </b-button>
    </b-card-header>
    <b-collapse id="frequenzlisten" role="tabpanel">
      <b-card-body>

          <div>
            <p>Wortschatzliste: 
              <b-form-select 
                v-model="selected" 
                :options="frequenzlistenOptions" 
                size="sm" 
                class="w-auto"
                >
              </b-form-select>
            </p>
          </div>
          <!-- Slider used for selecting the range -->
          <SliderComponent :name="selectedFrequenzliste" 
                           :range="frequenzlistenRange"
                           :extent="getFrequenzlisteExtent"
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
      inputText: "Deckung in %: ",
      tooltipTitle: "Abdeckung des vorkommenden Wortschatzes durch verschiedene Wortschatzlisten (Goethe: GERS-Niveau; Herder: Häufigkeit). <a href='../../doc/Handreichung-ZuMal.html#_3.1_Wortschatz_Niveaustufe_' target='_blank'><i>→Handreichung zur Arbeit mit ZuMal: 3.1 Wortschatz (Niveaustufe)</i></a>",
    }
  },
  computed: {
    ...mapState([
      'frequenzlistenRange',
      'frequenzlistenOptions',
      'selectedFrequenzliste'
    ]),
    ...mapGetters([
      'getFrequenzlisteExtent'
    ]),
    selected: {
      get() { return this.selectedFrequenzliste }, // ex.: "GOETHE_B1"
      set(value) {
        this.$store.dispatch('setFrequenzliste', value);
        // set the selected range to the absolute extent when changing Frequenzliste
        this.$store.dispatch('updateRange', {measure: this.selectedFrequenzliste, extent: this.getFrequenzlisteExtent});
      }
    }
  }
}
</script>

<style scoped>
</style>
