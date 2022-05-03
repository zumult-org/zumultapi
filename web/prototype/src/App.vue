<template>
<!-- 
App.vue imports the different components used 
in the application and organises them on the page.
Once the DOM is ready, It sends a call to the function
getAjaxData, which loads the JSON data from the ZuMult server
-->
  <div id="app">
    <div id="overlay-loading" class="position-absolute justify-content-center w-100 h-100"  v-show="isLoading">
      <b-spinner class="align-middle" style="width: 3rem; height: 3rem;" variant="secondary" label="Loading..."></b-spinner>
    </div>
    <b-container id="main-container" class="bv-example-row">
      <b-row>
        <b-col id="filters-area" cols="4" class="bg-light">
          <div class="my-3">
            <span>Korpus: <b-form-select v-model="selected" :options="corporaOptions" size="sm" class="w-auto"></b-form-select></span>
            <a href="#" class="float-right" @click="resetAllFilters">Reset all</a>
          </div>

          <div id="filters-group" role="tablist">
            <!-- hide if GWSS is selected as Korpus -->
            <GesprachstypTreeselect v-if="selectedCorpus != 'GWSS'"/>
            <!-- hide if FOLK is selected as Korpus --> 
            <Sprachen v-if="selectedCorpus != 'FOLK'"/>
            <Art />
            <Themen />
            <br>
            <!-- <Sprachregion /> -->
            <SprachregionTreeselect />
            <br>
            <!-- <Dauer /> -->
            <Dauer2 />
            <br>
            <Frequenzlisten />
            <Normalisierung />
            <Artikulationsrate />
            <Ueberlappungen />
            <!-- <Beitragslaenge /> -->
            <br>
            <Wortarten />
            <Muendlichkeitsphaenomene />
          </div>

        </b-col>
        <b-col id="speech-event-area" cols="8">

          <SpeechEventGraph />
          <SpeechEventTable />
          
        </b-col>
      </b-row>
    </b-container>
  </div>
</template>


<script>
// import components
import SpeechEventTable from "@/components/Results_components/SpeechEventTable.vue"
import SpeechEventGraph from "@/components/Results_components/SpeechEventGraph.vue"
import GesprachstypTreeselect from "@/components/Filters_components/GesprachstypTreeselect.vue"
// import Sprachregion from "@/components/Filters_components/Sprachregion.vue"
import SprachregionTreeselect from "@/components/Filters_components/SprachregionTreeselect.vue"
import Themen from "@/components/Filters_components/Themen.vue"
import Art from "@/components/Filters_components/Art.vue"
// import Dauer from "@/components/Filters_components/Dauer.vue"
import Dauer2 from "@/components/Filters_components/Dauer2.vue"
import Frequenzlisten from "@/components/Filters_components/Frequenzlisten.vue"
import Normalisierung from "@/components/Filters_components/Normalisierung.vue"
import Wortarten from "@/components/Filters_components/Wortarten.vue"
import Ueberlappungen from "@/components/Filters_components/Ueberlappungen.vue"
// import Beitragslaenge from "@/components/Filters_components/Beitragslaenge.vue"
import Artikulationsrate from "@/components/Filters_components/Artikulationsrate.vue"
import Muendlichkeitsphaenomene from "@/components/Filters_components/Muendlichkeitsphaenomene.vue"
import Sprachen from "@/components/Filters_components/Sprachen.vue"
// imports state.js and getters.js as objects
import { mapState, mapGetters } from 'vuex'


export default {
  name: 'app',
  components: {
    SpeechEventTable,
    SpeechEventGraph,
    GesprachstypTreeselect,
    // Sprachregion,
    SprachregionTreeselect,
    Art,
    Themen,
    // Dauer,
    Dauer2,
    Frequenzlisten,
    Normalisierung,
    Ueberlappungen,
    // Beitragslaenge,
    Artikulationsrate,
    Wortarten,
    Muendlichkeitsphaenomene,
    Sprachen
  },
  data() {
    return {}
  },
  computed: {
    // use select data from state.js
    ...mapState([
      'selectedCorpus',
      'isLoading',
      'selectedFrequenzliste'
    ]),
    // use select functions from getters.js
    ...mapGetters([
      'rawData',
      'gesprachstypData',
      'corporaOptions',
      'getDauerExtent',
      'getFrequenzlisteExtent',
      'getNormalisierungExtent',
      'getArtikulationsrateExtent',
      'getUeberlappungenExtent',
      'getWortartenExtents',
      'getMündlichkeitsphänomeneExtents',
      'getMündlichkeitsphänomene',
      'getWortarten'
    ]),
    selected: {
      get() {
        return this.selectedCorpus;
      },
      set(value) {
        this.$store.dispatch('setCorpus', value);
        this.resetAllFilters();
      }
    }
  },
  methods: {
    // reset all filters
    resetAllFilters() {
      // use setTimeout to send this function to the end of the queue, so that the sliders
      // don't update too early and produce errors in the console. See Issue #76 https://gitlab.rrz.uni-hamburg.de/Bae2551/ids-sample/-/issues/76
      setTimeout(() => {      
        [
          { measure: "normalisierungsrate", extent: this.getNormalisierungExtent },
          { measure: "anteil_Überlappungen_mit_mehr_als_2_wörtern_tokens", extent: this.getUeberlappungenExtent },
          { measure: "artikulationsrate", extent: this.getArtikulationsrateExtent },
          { measure: "dauer", extent: this.getDauerExtent},
          { measure: this.selectedFrequenzliste, extent: this.getFrequenzlisteExtent},
          { measure: "art", extent: []},
          { measure: "sprachen", extent: []},
          { measure: "themen", extent: []},
          { measure: "sprachregion", extent: []},
          { measure: "gespraechstyp", extent: []}
        ].forEach(measureObject => {
          this.$store.dispatch('updateRange', {measure: measureObject.measure, extent: measureObject.extent});
        });

        this.getWortarten.forEach(wortart => {
          this.$store.dispatch('updateRange', {measure: wortart, extent: this.getWortartenExtents[wortart]});
        });
        
        this.getMündlichkeitsphänomene.forEach(mündlichkeitsphänomen => {
          this.$store.dispatch('updateRange', {measure: mündlichkeitsphänomen, extent: this.getMündlichkeitsphänomeneExtents[mündlichkeitsphänomen]});
        });
      },0);
    }
  },
  mounted: function() {
    // get data via ajax
    // calls getAjaxData defined in actions.js
    this.$store.dispatch('getAjaxData');
  }
}
</script>

<style>
  html {
    font-size: 14px;
  }

  .no-border {
    border: none !important;
  }

  .range-input {
    width: 5em;
  }

  .tooltip-inner {
    text-align: left !important;
  }

  .red-group {
    background-color: rgb(245, 231, 234) !important;
  }
  .green-group {
    background-color: rgb(231, 243, 234) !important;
  }
  .blue-group {
    background-color: rgb(226, 237, 241) !important;
  }
  .yellow-group {
    background-color: rgb(250, 250, 227) !important;
  }
  .purple-group {
    background-color: rgb(233, 234, 255) !important;
  }
</style>

<style scoped>
#main-container {
  margin: 0;
  max-width: 100%;
  height: 100vh;
}

#main-container > .row {
  height: 100%;
}

#filters-area {
  height: 100vh;
  overflow: scroll;
}

#speech-event-area {
  height: 100vh !important;
  overflow: scroll;
}

#overlay-loading {
  z-index: 2;
  background-color: rgb(248 249 250 / 75%)!important;
  display: flex;
  align-items: center;
}
</style>
