<template>
  <div id="table-container" class="SE-container">

    <!-- TABLE COLUMNS selection  -->
    <b-dropdown id="dropdown-1" variant="transparent" class="pt-1" size="sm">
      <template #button-content>Spalten auswählen
        <b-icon icon="gear-fill" aria-hidden="true" style="opacity:.7">
        </b-icon>
      </template>
      <b-dropdown-form class="" style="height: 30vh; overflow: scroll">
        <b-form-checkbox-group
          id="checkbox-group-1"
          v-model="selectedColumns"
          :options="columns"
        ></b-form-checkbox-group>
      </b-dropdown-form>
    </b-dropdown>

    <!-- Table starts here. For more table options see https://bootstrap-vue.org/docs/components/table#comp-ref-b-table -->
    <b-table
      selectable
      :select-mode="selectMode"
      selected-variant="success"
      @row-selected="onRowSelected"
      id="main-table"
      ref="main_table"
      :items="getItems"
      :fields="getFields"
      sort-icon-left
      responsive="sm"
      small
      sticky-header="50vh"
      striped
      hover
      head-variant="light"
    >

      <!-- Column headers titles -->
      <template v-slot:head(anteil_Überlappungen_mit_mehr_als_2_wörtern_tokens)>
        Überlappungen
      </template>
      <template v-slot:head(_tokens_ratio)>
        Deckung
      </template>
      <template v-slot:head(GOETHE_A1_tokens_ratio)>
        Goethe A1 Deckung
      </template>
      <template v-slot:head(GOETHE_A2_tokens_ratio)>
        Goethe A2 Deckung
      </template>
      <template v-slot:head(GOETHE_B1_tokens_ratio)>
        Goethe B1 Deckung
      </template>
      <template v-slot:head(HERDER_1000_tokens_ratio)>
        HERDER 1000 Deckung
      </template>
      <template v-slot:head(HERDER_2000_tokens_ratio)>
        HERDER 2000 Deckung
      </template>
      <template v-slot:head(HERDER_3000_tokens_ratio)>
        HERDER 3000 Deckung
      </template>
      <template v-slot:head(HERDER_4000_tokens_ratio)>
        HERDER 4000 Deckung
      </template>
      <template v-slot:head(HERDER_5000_tokens_ratio)>
        HERDER 5000 Deckung
      </template>
      <template v-slot:head(ADJ_token_ratio)>
        Anteil ADJ
      </template>
      <template v-slot:head(ADV_token_ratio)>
        Anteil ADV
      </template>
      <template v-slot:head(NE_token_ratio)>
        Anteil NE
      </template>
      <template v-slot:head(NN_token_ratio)>
        Anteil NN
      </template>
      <template v-slot:head(PTKVZ_token_ratio)>
        Anteil PTKVZ
      </template>
      <template v-slot:head(V_token_ratio)>
        Anteil V
      </template>
      <template v-slot:head(CLITIC_token_ratio)>
        Anteil CLITIC
      </template>
      <template v-slot:head(NGHES_token_ratio)>
        Anteil NGHES
      </template>
      <template v-slot:head(NGIRR_token_ratio)>
        Anteil NGIRR
      </template>
      <template v-slot:head(PTKMA_token_ratio)>
        Anteil PTKMA
      </template>
      <template v-slot:head(SEDM_token_ratio)>
        Anteil SEDM
      </template>
      <template v-slot:head(SEQU_token_ratio)>
        Anteil SEQU
      </template>


      <!-- Table cell data formatting (adding % symbol, aligning left/right, etc.) -->
      <template v-slot:cell(index)="row">
        <span class="float-right">{{ row.index + 1 }}</span>
      </template>

      <template v-slot:cell(id)="row">
        <span :id="`${row.item.id}_index`" :data-index="row.index">{{ row.item.id }}</span>
      </template>
      
      <template v-slot:cell(anzahl_sprecher)="data">
        <span class="float-right">{{ data.value }}</span>
      </template>
      
      <template v-slot:cell(gesamttokenzahl)="data">
        <span class="float-right">{{ data.value }}</span>
      </template>

      <template v-slot:cell(dauer)="data">
        <span class="float-right">{{ data.value }}</span>
      </template>

      <template v-slot:cell(normalisierungsrate)="data">
        <span v-if="isNaN(data.value)" class="float-left">{{ data.value }}</span>
        <span v-else class="float-right">{{ Math.round(+data.value) }}%</span>
      </template>

      <template v-slot:cell(anteil_Überlappungen_mit_mehr_als_2_wörtern_tokens)="data">
        <span class="float-right">{{ data.value }}</span>
      </template>
      
      <template v-slot:cell(artikulationsrate)="data">
        <span class="float-right">{{ data.value }}</span>
      </template>
      
      <template v-slot:cell(GOETHE_A1_tokens_ratio)="data">
        <span v-if="isNaN(data.value)" class="float-left">{{ data.value }}</span>
        <span v-else class="float-right">{{ Math.round(+data.value * 100) }}%</span>
      </template>
      <template v-slot:cell(GOETHE_A2_tokens_ratio)="data">
        <span v-if="isNaN(data.value)" class="float-left">{{ data.value }}</span>
        <span v-else class="float-right">{{ Math.round(+data.value * 100) }}%</span>
      </template>
      <template v-slot:cell(GOETHE_B1_tokens_ratio)="data">
        <span v-if="isNaN(data.value)" class="float-left">{{ data.value }}</span>
        <span v-else class="float-right">{{ Math.round(+data.value * 100) }}%</span>
      </template>
      <template v-slot:cell(HERDER_1000_tokens_ratio)="data">
        <span v-if="isNaN(data.value)" class="float-left">{{ data.value }}</span>
        <span v-else class="float-right">{{ Math.round(+data.value * 100) }}%</span>
      </template>
      <template v-slot:cell(HERDER_2000_tokens_ratio)="data">
        <span v-if="isNaN(data.value)" class="float-left">{{ data.value }}</span>
        <span v-else class="float-right">{{ Math.round(+data.value * 100) }}%</span>
      </template>
      <template v-slot:cell(HERDER_3000_tokens_ratio)="data">
        <span v-if="isNaN(data.value)" class="float-left">{{ data.value }}</span>
        <span v-else class="float-right">{{ Math.round(+data.value * 100) }}%</span>
      </template>
      <template v-slot:cell(HERDER_4000_tokens_ratio)="data">
        <span v-if="isNaN(data.value)" class="float-left">{{ data.value }}</span>
        <span v-else class="float-right">{{ Math.round(+data.value * 100) }}%</span>
      </template>
      <template v-slot:cell(HERDER_5000_tokens_ratio)="data">
        <span v-if="isNaN(data.value)" class="float-left">{{ data.value }}</span>
        <span v-else class="float-right">{{ Math.round(+data.value * 100) }}%</span>
      </template>
      <template v-slot:cell(ADJ_token_ratio)="data">
        <span v-if="isNaN(data.value)" class="float-left">{{ data.value }}</span>
        <span v-else class="float-right">{{ +data.value }}%</span>
      </template>
      <template v-slot:cell(ADV_token_ratio)="data">
        <span v-if="isNaN(data.value)" class="float-left">{{ data.value }}</span>
        <span v-else class="float-right">{{ +data.value }}%</span>
      </template>
      <template v-slot:cell(NE_token_ratio)="data">
        <span v-if="isNaN(data.value)" class="float-left">{{ data.value }}</span>
        <span v-else class="float-right">{{ +data.value }}%</span>
      </template>
      <template v-slot:cell(NN_token_ratio)="data">
        <span v-if="isNaN(data.value)" class="float-left">{{ data.value }}</span>
        <span v-else class="float-right">{{ +data.value }}%</span>
      </template>
      <template v-slot:cell(PTKVZ_token_ratio)="data">
        <span v-if="isNaN(data.value)" class="float-left">{{ data.value }}</span>
        <span v-else class="float-right">{{ +data.value }}%</span>
      </template>
      <template v-slot:cell(V_token_ratio)="data">
        <span v-if="isNaN(data.value)" class="float-left">{{ data.value }}</span>
        <span v-else class="float-right">{{ +data.value }}%</span>
      </template>
      <template v-slot:cell(CLITIC_token_ratio)="data">
        <span v-if="isNaN(data.value)" class="float-left">{{ data.value }}</span>
        <span v-else class="float-right">{{ +data.value }}%</span>
      </template>
      <template v-slot:cell(NGHES_token_ratio)="data">
        <span v-if="isNaN(data.value)" class="float-left">{{ data.value }}</span>
        <span v-else class="float-right">{{ +data.value }}%</span>
      </template>
      <template v-slot:cell(NGIRR_token_ratio)="data">
        <span v-if="isNaN(data.value)" class="float-left">{{ data.value }}</span>
        <span v-else class="float-right">{{ +data.value }}%</span>
      </template>
      <template v-slot:cell(PTKMA_token_ratio)="data">
        <span v-if="isNaN(data.value)" class="float-left">{{ data.value }}</span>
        <span v-else class="float-right">{{ +data.value }}%</span>
      </template>
      <template v-slot:cell(SEDM_token_ratio)="data">
        <span v-if="isNaN(data.value)" class="float-left">{{ data.value }}</span>
        <span v-else class="float-right">{{ +data.value }}%</span>
      </template>
      <template v-slot:cell(SEQU_token_ratio)="data">
        <span v-if="isNaN(data.value)" class="float-left">{{ data.value }}</span>
        <span v-else class="float-right">{{ +data.value }}%</span>
      </template>

       <!-- Link to ZuViel -->
      <template v-slot:cell(ZuViel_Link)="row" v-if="selectedColumns.includes('ZuViel_Link')">
        <span><a :href="`${config.BASE_URL}/ProtoZumult/jsp/zuViel.jsp?transcriptID=${row.item.id}_T_01`"
                  target="_blank"
                  title="Zugang zu Visualisierungs-Elementen für Transkripte"
                  >&#8594;ZuViel</a> 
        </span>
      </template>

    </b-table>
  </div>
</template>

<script>
import { mapState, mapGetters } from 'vuex'
import _ from 'lodash'

export default {
  props: {},
  data() {
    return {
      selectedColumnsData: ['id','art','normalisierungsrate','dauer','ZuViel_Link'], // initial column selection
      columnsData: [
        {text: 'Index', value: 'index'}, 
        {text: 'Id', value: 'id', disabled: true}, 
        {text: 'Art', value: 'art'},
        {text: 'Gesamttokenzahl', value: 'gesamttokenzahl'},
        {text: 'Anzahl Sprecher', value: 'anzahl_sprecher'},
        {text: 'Dauer', value: 'dauer'},
        {text: 'Normalisierungsrate', value: 'normalisierungsrate'},
        {text: 'Überlappungen', value: 'anteil_Überlappungen_mit_mehr_als_2_wörtern_tokens'},
        {text: 'Artikulationsrate', value: 'artikulationsrate'},
        {text: 'Goethe A1 Deckung', value: "GOETHE_A1_tokens_ratio"},
        {text: 'Goethe A2 Deckung', value: "GOETHE_A2_tokens_ratio"},
        {text: 'Goethe B1 Deckung', value: "GOETHE_B1_tokens_ratio"},
        {text: 'HERDER 1000 Deckung', value: "HERDER_1000_tokens_ratio"},
        {text: 'HERDER 2000 Deckung', value: "HERDER_2000_tokens_ratio"},
        {text: 'HERDER 3000 Deckung', value: "HERDER_3000_tokens_ratio"},
        {text: 'HERDER 4000 Deckung', value: "HERDER_4000_tokens_ratio"},
        {text: 'HERDER 5000 Deckung', value: "HERDER_5000_tokens_ratio"},
        {text: 'ZuViel Link', value: 'ZuViel_Link'},
        {text: 'Anteil ADJ', value: 'ADJ_token_ratio'},
        {text: 'Anteil ADV', value: 'ADV_token_ratio'},
        {text: 'Anteil NE', value: 'NE_token_ratio'},
        {text: 'Anteil NN', value: 'NN_token_ratio'},
        {text: 'Anteil PTKVZ', value: 'PTKVZ_token_ratio'},
        {text: 'Anteil V', value: 'V_token_ratio'}, 
        {text: 'Anteil CLITIC', value: 'CLITIC_token_ratio'},
        {text: 'Anteil NGHES', value: 'NGHES_token_ratio'},
        {text: 'Anteil NGIRR', value: 'NGIRR_token_ratio'},
        {text: 'Anteil PTKMA', value: 'PTKMA_token_ratio'},
        {text: 'Anteil SEDM', value: 'SEDM_token_ratio'},
        {text: 'Anteil SEQU', value: 'SEQU_token_ratio'}
      ],
      sortDesc: false,
      selectMode: 'single',
      selectedRows: []
    }
  },
  computed: {
    ...mapState([
      'selectedCorpus',
      'selectedFrequenzliste',
      'selectedSeId',
      'config'
    ]),
    ...mapGetters([
      'rawData',
      'gespraechstypFilter',
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
      'sprachenFilter'
    ]),
    selectedColumns: {
      get() {
        return this.selectedColumnsData;
      },
      set(value) {
        this.selectedColumnsData = value;
      }
    },
    columns: {
      get() {
        return this.columnsData;
      },
      set(value) {
        this.columnsData = value;
      }
    },
    // columns for the table
    chosenFields() {
      return  [
              "id", 
              "art",
              "gesamttokenzahl",
              ["maße", "anzahl_sprecher"], 
              ["maße", "dauer"], 
              ["maße", "normalisierungsrate"],
              ["maße", "anteil_Überlappungen_mit_mehr_als_2_wörtern_tokens"],
              ["maße", "artikulationsrate"],
              ["maße", "wortschatz", "GOETHE_A1" , "tokens_ratio"],
              ["maße", "wortschatz", "GOETHE_A2" , "tokens_ratio"],
              ["maße", "wortschatz", "GOETHE_B1" , "tokens_ratio"],
              ["maße", "wortschatz", "HERDER_1000" , "tokens_ratio"],
              ["maße", "wortschatz", "HERDER_2000" , "tokens_ratio"],
              ["maße", "wortschatz", "HERDER_3000" , "tokens_ratio"],
              ["maße", "wortschatz", "HERDER_4000" , "tokens_ratio"],
              ["maße", "wortschatz", "HERDER_5000" , "tokens_ratio"],
              ["maße", "wortarten", "ADJ" , "token_ratio"],
              ["maße", "wortarten", "ADV" , "token_ratio"],
              ["maße", "wortarten", "NE" , "token_ratio"],
              ["maße", "wortarten", "NN" , "token_ratio"],
              ["maße", "wortarten", "PTKVZ" , "token_ratio"],
              ["maße", "wortarten", "V" , "token_ratio"],
              ["maße", "mündlichkeitsphänomene", "CLITIC" , "token_ratio"],
              ["maße", "mündlichkeitsphänomene", "NGHES" , "token_ratio"],
              ["maße", "mündlichkeitsphänomene", "NGIRR" , "token_ratio"],
              ["maße", "mündlichkeitsphänomene", "PTKMA" , "token_ratio"],
              ["maße", "mündlichkeitsphänomene", "SEDM" , "token_ratio"],
              ["maße", "mündlichkeitsphänomene", "SEQU" , "token_ratio"]
      ];
    },
    getFilteredSeList() { // return the list of speech events that satisfy the listed filters
       return _.intersection(this.gespraechstypFilter, 
                         this.dauerFilter, 
                         this.artFilter, 
                         this.themenFilter, 
                         this.frequenzlistenFilter, 
                         this.normalisierungFilter, 
                         this.ueberlappungenRangeFilter, 
                         this.artikulationsrateRangeFilter, 
                         this.graphSelectionFilter, 
                         this.sprachregionFilter,
                         this.sprachenFilter,
                         ...Object.values(this.wortartenFilters),
                         ...Object.values(this.mündlichkeitsphänomenFilters)
    )},
    getItems() { // get table data
      // returns list of row objects. see https://bootstrap-vue.org/docs/components/table#items-record-data
      return this.getFilteredSeList.map( se => {
        let obj = {};
        obj.se = se;
        this.chosenFields.forEach( field => {
          let value = se;
          let key = field;
          if (field && typeof field === 'object' && field.constructor === Array) { // if is array
            field.forEach( item => {
              key = item;
              value = value[item] != null ? value[item] : ""; // if none is selected
            });
          } else {
            value = se[key] != null ? se[key] : "";  // if none is selected
          }
          value = value.trim(); // some metadata have trailing spaces

          if (key === "tokens_ratio" || key === "token_ratio" || key === "lemmas_ratio") {
            key = field[field.length - 2] + "_" + key;
          }
          // check if it is data of one of the selected columns
          if (!this.selectedColumns.includes(key)) return;
          obj[key] = value;
        });
        return obj; // returns a single row object.
      });
    },
    getFields() { // get column headers
      // returns a list of column headers objects. see https://bootstrap-vue.org/docs/components/table#fields-as-an-array-of-objects
      let fields = [];
      if (this.selectedColumns.includes("index")) fields.push({key: "index", sortable: true}); // add index
      this.chosenFields.forEach( field => {
        let key = field;
        let obj = {};
        if (field && typeof field === 'object' && field.constructor === Array) { // if is array
          field.forEach( item => {
            key = item;
          });
        }

        if (key === "tokens_ratio" || key === "token_ratio") {
          key = field[field.length - 2] + "_" + key;
        }
        if (!this.selectedColumns.includes(key)) return; // check if selected
        obj.key = key;
        obj.sortable = true;
        fields.push(obj);
      });
      // check if selected
      if (this.selectedColumns.includes("ZuViel_Link")) fields.push({key: "ZuViel_Link", sortable: true}); // add field for link to ZuViel
      return fields;
    }
  },
  watch: {
    // select the corresponding table row (when a data point (circle) is clicked on in the graph)
    selectedSeId: function(newSeId) {
      if (newSeId) {  // const oldIndex = document.getElementById(oldSeId + '_index') ? +document.getElementById(oldSeId + '_index').getAttribute('data-index') : null;
        const tableEntry = document.getElementById(newSeId + '_index');
        const newIndex = tableEntry ? +tableEntry.getAttribute('data-index') : null;
        this.$refs.main_table.selectRow(newIndex);
      } else {
        this.$refs.main_table.clearSelected();
      }
    }
  },
  methods: {
    // select the corresponding data point (circle) in the graph (when a table row is clicked on)
    onRowSelected(items) {
      if (items[0]) {
        this.$store.dispatch('setSelectedSeId', items[0].id);
      } else {
        this.$store.dispatch('setSelectedSeId', null);
      }
    }
  }
}
</script>


<style>
 #table-container {
   position: relative;
 }

 #table-container .table.b-table > thead > tr > th {
  position: sticky;
  position: -webkit-sticky;
  top: 0;
  z-index: 1;
  transform: translateY(-1px); /* there is 1px area of body visible above header  */
} 

#main-table td, #main-table th {
    border: 1px solid #dee2e6;
}

#main-table > tbody {
  font-size: .9em;;
}

#dropdown-1 .custom-control.custom-checkbox {
    display: flex;
}
</style>

<style scoped>

</style>

