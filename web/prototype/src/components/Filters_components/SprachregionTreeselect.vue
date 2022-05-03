<template>
<!-- 
  Component for Sprachregion
 -->
  <b-card no-body class="mb-1">
    <b-card-header header-tag="header" class="p-1 yellow-group" role="tab">
      <b-button block href="#" v-b-toggle.sprachregion class="p-1 text-left bg-transparent text-dark no-border">
        <b>Sprachregion</b>
        <div class="float-right"><b-badge class="rounded-circle" variant="light" v-b-tooltip.hover.html="tooltipTitle">?</b-badge></div>
      </b-button>
    </b-card-header>
    <b-collapse id="sprachregion" role="tabpanel">
      <b-card-body>
        <div class="sprachregionContainer">
          <a href="http://agd.ids-mannheim.de/download/Sprachregionen_FOLK.pdf">Sprachregionen</a>
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
            :options="getSprachregionData"
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
        measure: "sprachregion",
        options: null,
        tooltipTitle: "Region, in der das Sprechereignis aufgenommen wurde. <a href='../../doc/Handreichung-ZuMal.html#_2.5__Sprachregion' target='_blank'><i>→Handreichung zur Arbeit mit ZuMal: 2.5 Sprachregion</i></a>",
       }
    },
    components: { Treeselect },
    computed: {
      ...mapState([
        'selectedSprachregion'
      ]),
      ...mapGetters([
        'sprachregionData',
        'dauerFilter',
        'gespraechstypFilter',
        'artFilter',
        'themenFilter',
        'frequenzlistenFilter',
        'normalisierungFilter',
        'ueberlappungenRangeFilter',
        'artikulationsrateRangeFilter',
        'graphSelectionFilter',
        'wortartenFilters',
        'mündlichkeitsphänomenFilters',
        'sprachenFilter'
      ]),
      selectedOptions: {
        get() {
          return this.selectedSprachregion;
        },
        set(value) {
          this.$store.dispatch('updateRange', {measure: this.measure, extent: value});
        }
      },
       // get the list of Sprachregionen that are available in the currently filtered speech events
      getSprachregionData() {
        let filteredSprachregionList = [... new Set(_.intersection(
          this.dauerFilter, 
          this.gespraechstypFilter, 
          this.artFilter, 
          this.themenFilter, 
          this.frequenzlistenFilter, 
          this.normalisierungFilter, 
          this.ueberlappungenRangeFilter,
          this.sprachenFilter,
          // this.graphSelectionFilter,
          this.artikulationsrateRangeFilter,
          ...Object.values(this.wortartenFilters),
          ...Object.values(this.mündlichkeitsphänomenFilters)
        ).map( se => se.geo.land))];
        
        return this.sprachregionData.filter( item => {
          return filteredSprachregionList.includes(item.id) ? item : null;
        }).map(item => item).sort((a,b) => {
          const labelA = a.label.toUpperCase().trim();
          const labelB = b.label.toUpperCase().trim();
          return labelA.localeCompare(labelB);
        });
        // returns a list of nested objects defining labels, ids, total counts, and children. 
        // [
        //   {
        //     "children": [
        //       {
        //         "children": [
        //           {
        //             "count": "3",
        //             "id": "Ungarn__außerhalb deutschsprachigen Kerngebiets (Ungarn)__außerhalb deutschsprachigen Kerngebiets",
        //             "label": "außerhalb deutschsprachigen Kerngebiets"
        //           }
        //         ],
        //         "count": "3",
        //         "id": "Ungarn__außerhalb deutschsprachigen Kerngebiets (Ungarn)",
        //         "label": "außerhalb deutschsprachigen Kerngebiets (Ungarn)"
        //       }
        //     ],
        //     "count": "3",
        //     "id": "Ungarn",
        //     "label": "Ungarn"
        //   }
        // ...
        // ]
      }
    },
    methods: {
      async loadOptions({ action/*, callback*/ }) {
        if (action === LOAD_ROOT_OPTIONS) {
          await sleep(0);
          this.options = this.getFilteredThemenData;
        }
      },
      resetOptions() {
        this.options = null;
      },
      // get count for each level of Sprachregion options
      getCount(id) {
        const split = id.split('__');
        let filteredSEs = _.intersection(
          this.dauerFilter, 
          this.gespraechstypFilter, 
          this.artFilter, 
          this.themenFilter, 
          this.frequenzlistenFilter, 
          this.normalisierungFilter, 
          this.ueberlappungenRangeFilter,
          this.sprachenFilter,
          // this.graphSelectionFilter,
          this.artikulationsrateRangeFilter,
          ...Object.values(this.wortartenFilters),
          ...Object.values(this.mündlichkeitsphänomenFilters)
        ).filter( se => { // 1st level
          return se.geo.land == split[0];
        }).map( se => se);
        let count = filteredSEs.length;

        if (split.length > 1) { // 2nd level
          filteredSEs = filteredSEs.filter( se => {
            return se.geo.dialektalregion_lameli.includes(split[1]);
          }).map( se => se);
          count = filteredSEs.length;
        }

        if (split.length > 2) { // 3rd level
          filteredSEs = filteredSEs.filter( se => {
            return se.geo.dialektalregion_wiesinger.includes(split[2]);
          }).map( se => se);
          count = filteredSEs.length;
        }

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