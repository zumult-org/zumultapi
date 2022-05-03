<template>
<!-- 
  Component for Gesprächstyp
 -->
  <b-card no-body class="mb-1">
    <b-card-header header-tag="header" class="p-1 blue-group" role="tab">
      <b-button block href="#" v-b-toggle.gesprachstyp class="p-1 text-left bg-transparent text-dark no-border">
        <b>Gesprächstyp</b>
        <div class="float-right"><b-badge class="rounded-circle" variant="light" v-b-tooltip.hover.html="tooltipTitle">?</b-badge></div>
      </b-button>
    </b-card-header>
    <b-collapse id="gesprachstyp" role="tabpanel">
      <b-card-body>
        <div class="gesprachstypContainer">
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
        measure: "gespraechstyp",
        options: null,
        tooltipTitle: "Gruppierung nach dem Kontext des Sprechereignisses: (1) Interaktionsdomäne (privat, institutionell, öffentlich, sonstiges), (2) gesellschaftlicher Lebensbereich (z.B. Bildung, Behörden, Kunst/Unterhaltung/Sport) oder (3) Aktivität (z.B. Kochen, Vorlesen, Meeting). <a href='../../doc/Handreichung-ZuMal.html#_2.1__Gesprächstyp' target='_blank'><i>→Handreichung zur Arbeit mit ZuMal: 2.1 Gesprächstyp</i></a>"
       }
    },
    components: { Treeselect },
    computed: {
      ...mapState([
        'selectedGespraechstyp'
      ]),
      ...mapGetters([
        'gesprachstypData',
        'dauerFilter',
        'artFilter',
        'themenFilter',
        'frequenzlistenFilter',
        'normalisierungFilter',
        'ueberlappungenRangeFilter',
        'artikulationsrateRangeFilter',
        'graphSelectionFilter',
        'sprachregionFilter',
        'wortartenFilters',
        'mündlichkeitsphänomenFilters',
        'sprachenFilter',
      ]),
      selectedOptions: {
        get() {
          return this.selectedGespraechstyp;
        },
        set(value) {
          this.$store.dispatch('updateRange', {measure: this.measure, extent: value});
        }
      }
    },
    methods: {
      // loads filter options asynchronously
      async loadOptions({ action/*, callback*/ }) {
        if (action === LOAD_ROOT_OPTIONS) {
            await sleep(0);
            this.options = this.gesprachstypData.sort((a,b) => {
              const labelA = a.label.toUpperCase().trim();
              const labelB = b.label.toUpperCase().trim();
              return labelA.localeCompare(labelB);
            });
        }
      },
      resetOptions() {
        this.options = null;
      },
      // get count for each level of Gesprächstyp options
      getCount(id) {
        const split = id.split('__');

          let filteredSEs = _.intersection(this.dauerFilter, 
                                           this.artFilter, 
                                           this.themenFilter, 
                                           this.frequenzlistenFilter, 
                                           this.normalisierungFilter, 
                                           this.ueberlappungenRangeFilter, 
                                           this.artikulationsrateRangeFilter,
                                           this.sprachenFilter,
                                           ...Object.values(this.wortartenFilters),
                                           ...Object.values(this.mündlichkeitsphänomenFilters),
                                          //  this.graphSelectionFilter, 
                                           this.sprachregionFilter).filter( se => { // 1st level
            return se.interaktionsdomäne == split[0];
          }).map( se => se);
          let count = filteredSEs.length;

          if (split.length > 1) { // 2nd level
            filteredSEs = filteredSEs.filter( se => {
              return se.lebensbereich.includes(split[1]);
            }).map( se => se);
            count = filteredSEs.length;

            if (split.length > 2) { // 3rd level
              filteredSEs = filteredSEs.filter( se => {
                return se.aktivität == split[2];
              }).map( se => se);
              count = filteredSEs.length;
            }
          }

        return count;
      }
    }
  }
</script>

<style scoped>
</style>
<style>
</style>
