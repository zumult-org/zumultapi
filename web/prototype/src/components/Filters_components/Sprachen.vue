<template>
<!-- 
  Component for Sprachen. Only available to GWSS
 -->
  <b-card no-body class="mb-1">
    <b-card-header header-tag="header" class="p-1 blue-group" role="tab">
      <b-button block href="#" v-b-toggle.sprachen class="p-1 text-left bg-transparent text-dark no-border">
        <b>Sprachen</b>
        <div class="float-right"><b-badge class="rounded-circle" variant="light" v-b-tooltip.hover.html="tooltipTitle">?</b-badge></div>
      </b-button>
    </b-card-header>
    <b-collapse id="sprachen" role="tabpanel">
      <b-card-body>
        <div class="sprachenContainer">
          <!-- 
            Treeselect component used for filtering categorical data.
            @options contains the list of filtered options
            for more options see https://vue-treeselect.js.org/#props
          -->
            <treeselect
            :multiple="true"
            :load-options="loadOptions"
            :auto-load-root-options="false"
            @close="resetOptions"
            :options="getSprachenData"
            value-consists-of="BRANCH_PRIORITY"
            v-model="selectedOptions"
            >
            <!-- Displays the total count of the corresponding option 
            in the current selection of speech events -->
            <div slot="option-label" slot-scope="{ node, countClassName }">{{ getLabelTitlecase(node.raw.label) }}
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
        measure: "sprachen",
        options: null,
        tooltipTitle: "Gruppierung nach den Spachen, die im Sprechereignis überwiegend verwendet wurden, sowie danach, ob diese für die Sprecher/-innen die Erstsprache (L1) oder eine Fremdsprache (L2) darstellen. <a href='../../doc/Handreichung-ZuMal.html#_2.2_Sprachen' target='_blank'><i>→Handreichung zur Arbeit mit ZuMal: 2.2 Sprachen</i></a>",
       }
    },
    components: { Treeselect },
    computed: {
      ...mapState([
        'selectedSprachen'
      ]),
      ...mapGetters([
        'getAllFilters',
        'sprachenData',
        'sprachregionData',
        'dauerFilter',
        'gespraechstypFilter',
        'artFilter',
        'themenFilter',
        'sprachregionFilter',
        'frequenzlistenFilter',
        'normalisierungFilter',
        'ueberlappungenRangeFilter',
        'artikulationsrateRangeFilter',
        'graphSelectionFilter',
        'wortartenFilters',
        'mündlichkeitsphänomenFilters'
      ]),
      selectedOptions: {
        get() {
          return this.selectedSprachen;
        },
        set(value) {
          this.$store.dispatch('updateRange', {measure: this.measure, extent: value});
        }
      },
      // get the list of Sprachen that are available in the currently filtered speech events
      getSprachenData() {
        let filteredSprachenList = [... new Set(_.intersection(
          this.dauerFilter,
          this.sprachregionFilter,
          this.gespraechstypFilter, 
          this.artFilter, 
          this.themenFilter, 
          this.frequenzlistenFilter, 
          this.normalisierungFilter, 
          this.ueberlappungenRangeFilter, 
          // this.graphSelectionFilter,
          this.artikulationsrateRangeFilter,
          ...Object.values(this.wortartenFilters),
          ...Object.values(this.mündlichkeitsphänomenFilters)
        ).map( se => se.sprachen))];

        return this.sprachenData.filter( item => {
          return filteredSprachenList.includes(item.id) ? item : null;
        }).map( item => item).sort((a,b) => {
          const labelA = a.label.toUpperCase().trim();
          const labelB = b.label.toUpperCase().trim();
          return labelA.localeCompare(labelB);
        });
      }
    },
    methods: {
    async loadOptions({ action/*, callback*/ }) {
      if (action === LOAD_ROOT_OPTIONS) {
        await sleep(0);
        this.options = this.getSprachenData;
      }
    },
    resetOptions() {
      this.options = null;
    },
    getCount(id) {
      let filteredSEs = _.intersection(this.dauerFilter,
                                       this.gespraechstypFilter, 
                                       this.artFilter,
                                       this.themenFilter, 
                                       this.frequenzlistenFilter, 
                                       this.normalisierungFilter, 
                                       this.ueberlappungenRangeFilter, 
                                       this.artikulationsrateRangeFilter, 
                                      //  this.graphSelectionFilter, 
                                       this.sprachregionFilter,
                                       ...Object.values(this.wortartenFilters),
                                       ...Object.values(this.mündlichkeitsphänomenFilters))
      .filter( se => { // 1st level
        return se.sprachen === id;
      }).map( se => se);
      let count = filteredSEs.length;
      return count;
    },
    getLabelTitlecase(label) {
      return label[0].toUpperCase() + label.slice(1);
    }
  }
}
</script>

<style scoped>
</style> 