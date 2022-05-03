<template>
<!-- 
  Component for Art filter
 -->
  <b-card no-body class="mb-1">
    <b-card-header header-tag="header" class="p-1 blue-group" role="tab">
      <b-button block href="#" v-b-toggle.art class="p-1 text-left bg-transparent text-dark no-border">
        <b>Art</b>
        <div class="float-right"><b-badge class="rounded-circle" variant="light" v-b-tooltip.hover.html="tooltipTitle">?</b-badge></div>
        </b-button>
    </b-card-header>
    <b-collapse id="art" role="tabpanel">
      <b-card-body>
        <div class="artContainer">
          <!-- 
            Treeselect component used for filtering categorical data.
            @options contains the list of filtered options
            for more options see https://vue-treeselect.js.org/#props
          -->
          <treeselect
            :multiple="true"
            :load-options="loadOptions"
            :auto-load-root-options="false"
            :options="options"
            @close="resetOptions"
            value-consists-of="BRANCH_PRIORITY"
            v-model="selectedOptions"
            > 
          <!-- Displays the total count of the corresponding option 
            in the current selection of speech events -->
            <div slot="option-label" slot-scope="{ node, countClassName }">{{ node.raw.label }}
              <span :class="countClassName">({{ getCount(node.raw.id) }})</span>
            </div>
          </treeselect>
        </div>
      </b-card-body>
    </b-collapse>
  </b-card>
</template>

<script>
  import Treeselect from '@riophae/vue-treeselect'
  import '@riophae/vue-treeselect/dist/vue-treeselect.css'
  import { LOAD_ROOT_OPTIONS } from '@riophae/vue-treeselect'
  import { mapState, mapGetters } from 'vuex'
  import _ from 'lodash'

  const sleep = d => new Promise(r => setTimeout(r, d))

export default {
  data() {
    return {
      measure: "art",
      options: null,
      tooltipTitle: "Genauere Charakterisierung des Sprechereignisses (bspw. Gespräch in der Familie, Studentisches Alttagsgespräch, Gitarrenunterricht, Teamsitzung). <a href='../../doc/Handreichung-ZuMal.html#_2.3_Art' target='_blank'><i>→Handreichung zur Arbeit mit ZuMal: 2.3 Art</i></a>"
    }
  },
  components: { Treeselect },
  computed: {
    ...mapState([
      'selectedArt'
    ]),
    ...mapGetters([
      'artData',
      'dauerFilter',
      'gespraechstypFilter',
      'themenFilter',
      'frequenzlistenFilter',
      'normalisierungFilter',
      'ueberlappungenRangeFilter',
      'artikulationsrateRangeFilter',
      'graphSelectionFilter',
      'sprachregionFilter',
      'wortartenFilters',
      'mündlichkeitsphänomenFilters',
      'sprachenFilter'
    ]),
    selectedOptions: {
      get() {
        return this.selectedArt;
      },
      set(value) {
        this.$store.dispatch('updateRange', {measure: this.measure, extent: value});
      }
    },
    // get the list of Art that are available in the currently filtered speech events
    getFilteredArtData() {
      let filteredArtList = [... new Set(_.intersection(this.dauerFilter, 
                                                        this.gespraechstypFilter, 
                                                        this.themenFilter, 
                                                        this.frequenzlistenFilter, 
                                                        this.normalisierungFilter, 
                                                        this.ueberlappungenRangeFilter, 
                                                        this.artikulationsrateRangeFilter,
                                                        this.sprachenFilter,
                                                        // this.graphSelectionFilter, 
                                                        this.sprachregionFilter,
                                                        ...Object.values(this.wortartenFilters),
                                                        ...Object.values(this.mündlichkeitsphänomenFilters)
                                                        )
      .map( se => se.art))];
      return this.artData.filter( item => {
        return filteredArtList.includes(item.id) ? item : null;
      }).map(item => item).sort((a,b) => {
        const labelA = a.label.toUpperCase().trim();
        const labelB = b.label.toUpperCase().trim();
        return labelA.localeCompare(labelB);
      });
    }
  },
  methods: {
    // loads filter options asynchronously
    async loadOptions({ action/*, callback*/ }) {
      if (action === LOAD_ROOT_OPTIONS) {
        await sleep(0);
        this.options = this.getFilteredArtData;
      }
    },
    resetOptions() {
      this.options = null;
    },
    // count to be displayed next to the item in the list
    getCount(id) {
      let filteredSEs = _.intersection(this.dauerFilter,
                                       this.gespraechstypFilter, 
                                       this.themenFilter, 
                                       this.frequenzlistenFilter, 
                                       this.normalisierungFilter, 
                                       this.ueberlappungenRangeFilter, 
                                       this.artikulationsrateRangeFilter,
                                       this.sprachenFilter,
                                      //  this.graphSelectionFilter, 
                                       this.sprachregionFilter,
                                       ...Object.values(this.wortartenFilters),
                                       ...Object.values(this.mündlichkeitsphänomenFilters))
      .filter( se => { // 1st level
        return se.art === id;
      }).map( se => se);
      let count = filteredSEs.length;
      return count;
    }
  }
}


</script>

<style scoped>
</style>
