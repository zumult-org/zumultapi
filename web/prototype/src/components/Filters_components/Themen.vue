<template>
<!-- 
  Component for the Themen filter
 -->
  <b-card no-body class="mb-1">
    <b-card-header header-tag="header" class="p-1 blue-group" role="tab">
      <b-button block href="#" v-b-toggle.themen class="p-1 text-left bg-transparent text-dark no-border">
        <b>Themen</b>
        <div class="float-right"><b-badge class="rounded-circle" variant="light" v-b-tooltip.hover.html="tooltipTitle">?</b-badge></div>
        </b-button>
    </b-card-header>
    <b-collapse id="themen" role="tabpanel">
      <b-card-body>
        <div class="themenContainer">
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
            <div slot="option-label" slot-scope="{ node, countClassName }">{{ node.label }}
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
      measure: "themen",
      options: null,
      tooltipTitle: "Gesprächsthemen, die im Sprechereignis vorkommen. <a href='../../doc/Handreichung-ZuMal.html#_2.4_Themen' target='_blank'><i>→Handreichung zur Arbeit mit ZuMal: 2.4 Themen</i></a>"
    }
  },
  components: { Treeselect },
  computed: {
    ...mapState([
      'selectedThemen'
    ]),
    ...mapGetters([
      'themenData',
      'dauerFilter',
      'artFilter',
      'gespraechstypFilter',
      'frequenzlistenFilter',
      'normalisierungFilter',
      'ueberlappungenRangeFilter',
      'beitragslaengeRangeFilter',
      'artikulationsrateRangeFilter',
      'graphSelectionFilter',
      'sprachregionFilter',
      'wortartenFilters',
      'mündlichkeitsphänomenFilters',
      'sprachenFilter'
    ]),
    selectedOptions: {
      get() {
        return this.selectedThemen; // ex.: ["Arbeit","Armut","Bücher","Dialektentwicklung"]
      },
      set(value) {
        this.$store.dispatch('updateRange', {measure: this.measure, extent: value});
      }
    },
    getFilteredData() {
      return _.intersection(
                     this.dauerFilter, 
                     this.gespraechstypFilter, 
                     this.artFilter, 
                     this.frequenzlistenFilter, 
                     this.normalisierungFilter, 
                     this.ueberlappungenRangeFilter, 
                     this.artikulationsrateRangeFilter,
                     this.sprachenFilter,
                    //  this.graphSelectionFilter, 
                     this.sprachregionFilter,
                     ...Object.values(this.wortartenFilters),
                     ...Object.values(this.mündlichkeitsphänomenFilters)
          );
    },
    getFilteredThemenData() { // get the list of Themen that are available in the currently filtered speech events
      let filteredThemenList = [];
      this.getFilteredData.forEach( se => {
        filteredThemenList = [... new Set([...filteredThemenList, ...se.themen])];
      });
      
      return this.themenData.filter( item => {
        return filteredThemenList.includes(item.id) ? item : null;
      }).map( item => item).sort((a,b) => {
        const labelA = a.label.toUpperCase().trim();
        const labelB = b.label.toUpperCase().trim();
        return labelA.localeCompare(labelB);
      });
      // ex.:
      // [
      //   {
      //     id:""
      //     label:""
      //   },
      //   {
      //     id:"18. Jahrhundert"
      //     label:"18. Jahrhundert"
      //   },
      //   {
      //     id:"Abbiegeassistenz"
      //     label:"Abbiegeassistenz"
      //   },
      //   {
      //     id:"Abbiegen"
      //     label:"Abbiegen"
      //   },
      // ...
      // ]
    }
  },
  methods: {
    async loadOptions({ action/*, callback*/ }) {
      if (action === LOAD_ROOT_OPTIONS) {
        await sleep(0);
        // if there are more than 250 options for Themen, throw the error, otherwise show list
        if (this.getFilteredThemenData.length > 250) {
          throw new Error('Die Auswahl ist zu groß. Bitte die Auswahl einschränken.');
        } else {
          this.options = this.getFilteredThemenData;
        }
      }
    },
    resetOptions() {
      this.options = null;
    },
    getCount(id) {
      let filteredSEs = this.getFilteredData.filter( se => { // 1st level
        return se.themen.includes(id);
      }).map( se => se);
      let count = filteredSEs.length;

      return count;
    }
  }
}


</script>

<style scoped>
</style>
