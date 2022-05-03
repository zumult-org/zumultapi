<template>
<!-- 
  Component for the Scatterplot chart in the results section
 -->
  <div id="graph-container" class="SE-graph-container">
    <!-- Y axis selection -->
    <div class="mt-3 d-inline">
      Y Achse: 
      <b-form-select 
        v-model="selectedYAxis" 
        :options="yAxisOptions"
        value-field="item"
        text-field="name"
        size="sm" 
        class="w-auto">
      </b-form-select>
    </div>
    <!-- X axis selection -->
    <div class="mt-3 d-inline">
      X Achse: 
      <b-form-select 
        v-model="selectedXAxis" 
        :options="xAxisOptions"
        value-field="item"
        text-field="name"
        size="sm" 
        class="w-auto">
      </b-form-select>
    </div>

    <!-- Graph SVG element -->
    <svg :width="graphWidth" id="graph-svg">
      <!-- 
        MUST COME BEFORE CIRCLES, OTHERVISE POINTER EVENTS DON'T 
        WORK ON CIRCLES BECAUSE THE BRUSH TAKES ALL THE POINTER EVENTS 
        -->
      <g class="brush-graph" :transform="`translate(${margin.left}, ${margin.top})`"></g>
      
      <!-- 
        The main graph area is constructed here
      -->
      <g id="graph-group" :transform="`translate(${margin.left}, ${margin.top})`">
        <g class="circles-group" ref="circlesGroup">
          <template v-for="(se) in getFilteredSeList">
            <!-- 
              Individual data point on the graph.
              cx = x position of the circle
              cy = y position of the circle
            -->
            <circle class="circle"
                  :id="se.id"
                  :ref="se.id"
                  :key="se.id"
                  :cx="scale.x(getTheRightValueX(se))"
                  :cy="scale.y(getTheRightValueY(se))"
                  r="5"
                  @click="setSelectedSeId(se.id)"
            />
          </template>
        </g>

        <!-- X axis -->
        <g id="graph-axis-x" class="axis x" :transform="`translate(0,${innerHeight})`">
          <!-- X axis label -->
          <text fill="black"
                class="x label"
                y="4em"
                :x="innerWidth / 2"
          >{{ getXAxisLabel(selectedXAxis) }}
          </text>
        </g>
        <!-- Y axis -->
        <g id="graph-axis-y" class="axis y" >
          <!-- Y axis label -->
          <text fill="black"
                class="y label"
                :x="-innerHeight / 2"
                :y="-margin.left *4/5"
                transform="rotate(-90)"
          >{{ getYAxisLabel(selectedYAxis) }}
          </text>
        </g>
      </g>
    </svg>

    <!-- Popover table that displays on click-->
    <template v-for="(se) in getFilteredSeList">
      <b-popover
        :key="`_${se.id}`"
        :ref="`${se.id}_popover`"
        class="popover"
        :target="se.id"
        triggers="click"
      >
      <template #title>
        <b-button @click="closePopover(se.id)" class="close" aria-label="Close">
          <span class="d-inline-block" aria-hidden="true">&times;</span>
        </b-button>
        {{ se.art }}
      </template>
        <table class="table table-sm popover-table">
          <tbody>
            <tr>
              <th scope="row">ID</th>
              <td>{{ se.id }}</td>
            </tr>
            <tr>
              <th scope="row">Beschreibung</th>
              <td v-if="se.beschreibung && se.beschreibung.length > 90">
                <span>{{ se.beschreibung.substring(0,90).trim() }}...</span>
                <a href=""
                  @click.prevent="toggleDescription($event, se)"
                >More
                </a>
              </td>
              <td v-else>{{ se.beschreibung }}</td>
            </tr>
            <tr>
              <th scope="row">Gesamttokenzahl</th>
              <td>{{ se.gesamttokenzahl }}</td>
            </tr>
            <tr>
              <th scope="row">Dauer</th>
              <td>{{ se.maße.dauer }}</td>
            </tr>
            <tr>
              <th scope="row">Anzahl Sprecher</th>
              <td>{{ se.maße.anzahl_sprecher }}</td>
            </tr>
            <tr v-if="se.interaktionsdomäne">
              <th scope="row">Interaktionstyp</th>
              <td>
                <ul>
                  <li>{{ se.interaktionsdomäne }}</li>
                  <ul>
                    <li v-if="se.lebensbereich">
                      <template v-for="(area, i) in se.lebensbereich">
                        <span :key="area" v-if="i === se.lebensbereich.length - 1 ">{{ area }}</span>
                        <span :key="area" v-else>{{ area }}, </span>
                      </template>
                      </li>
                    <ul>
                      <li v-if="se.aktivität">{{ se.aktivität }}</li>
                    </ul>
                  </ul>
                </ul>

              </td>
            </tr>
            <tr>
              <th scope="row">Themen</th>
              <td>
                <template v-for="(theme, index) in se.themen">
                  <span v-if="index !== se.themen.length-1" :key="index">
                    {{ theme }}; 
                  </span>
                  <span v-else :key="index">
                    {{ theme }}
                  </span>
                </template>
              </td>
            </tr>
            <tr>
              <th scope="row">Abdeckung von Tokens in GeR-Niveaustufen und Frequenzlisten</th>
              <td>
                  <table class="table table-sm popover-table"> 
                    <tbody>
                      <tr v-for="(listName) in frequenzlistenOptions" :key="listName">
                        <th scope="row">
                          <a :href="`${config.BASE_URL}/ProtoZumult/jsp/zuViel.jsp?transcriptID=${se.id}_T_01&wordlistID=${listName}`"
                            target="_blank"
                            title="Link zur Übersicht über die Schnittmenge von Wortliste und Sprechereignis"
                            >{{ listName }}</a>
                        </th>
                        <td v-if="isNaN(se.maße.wortschatz[listName].tokens_ratio)">{{ se.maße.wortschatz[listName].tokens_ratio }}</td>
                        <td v-else>{{ Math.round(+se.maße.wortschatz[listName].tokens_ratio.replace(',', '.') * 100) }}%</td>
                      </tr>
                    </tbody>
                  </table>
              </td>
            </tr>
          </tbody>
        </table>
      </b-popover>

    </template>
  </div>
</template>

<script>
import { mapState, mapGetters } from 'vuex'
import * as d3 from 'd3'
import { parseHHMMSS } from '@/helper'
import _ from 'lodash'

export default {
  props: {},
  data() {
    return {
      graphWidth: 0,
      graphHeight: 0,
      margin: { top: 50, right: 15, bottom: 20, left: 50 },
    }
  },
  computed: {
    ...mapState([
      'yAxisOptions',
      'xAxisOptions',
      'frequenzlistenOptions',
      'selectedCorpus',
      'selectedYAxisGraph',
      'selectedXAxisGraph',
      'brushedGraph',
      'isEmptyGraphSelection',
      'selectedSeId',
      'config'
    ]),
    ...mapGetters([
      'gespraechstypFilter',
      'dauerFilter',
      'artFilter',
      'rawData',
      'themenFilter',
      'frequenzlistenFilter',
      'normalisierungFilter',
      'ueberlappungenRangeFilter',
      'artikulationsrateRangeFilter',
      'sprachregionFilter',
      'graphSelectionFilter',
      'wortartenFilters',
      'mündlichkeitsphänomenFilters',
      'sprachenFilter',
      'getWortarten',
      'getMündlichkeitsphänomene'
    ]),
    // getter and setter for X axis
    selectedXAxis: {
        get: function() { return this.selectedXAxisGraph },
        set: function(value) {
          this.$store.dispatch('setXAxis', value);
          if (this.frequenzlistenOptions.includes(value)) this.$store.dispatch('setFrequenzliste', value);
          this.appendAxisX();
        }
    },
    // getter and setter for Y axis
    selectedYAxis: {
        get: function() { return this.selectedYAxisGraph },
        set: function(value)  {
          this.$store.dispatch('setYAxis', value);
          if (this.frequenzlistenOptions.includes(value)) this.$store.dispatch('setFrequenzliste', value);
          this.appendAxisY();
        }
    },
    innerWidth() { return this.graphWidth - this.margin.left - this.margin.right },
    innerHeight() { return this.graphHeight - this.margin.top - this.margin.bottom },
    getFilteredSeList() { // return list of speech events that comply to criteria from other filters
      return _.intersection(
                      this.gespraechstypFilter, 
                       this.dauerFilter, 
                       this.artFilter, 
                       this.themenFilter, 
                       this.frequenzlistenFilter, 
                       this.normalisierungFilter, 
                       this.ueberlappungenRangeFilter, 
                       this.artikulationsrateRangeFilter, 
                       this.sprachregionFilter,
                       this.sprachenFilter,
                       ...Object.values(this.wortartenFilters),
                       ...Object.values(this.mündlichkeitsphänomenFilters)
                      //  this.graphSelectionFilter  // remove graphSelectionFilter if don't want to filter out data points on the graph on graph selection
                       )
    },
    // scaling applied in order to position the data points correctly
    scale() {
      let x, y;
      if (this.selectedXAxis == 'dauer') {
        // const max = d3.max(this.getFilteredSeList.map( se => this.getTheRightValueX(se))); // uncomment this if the X scale of the graph is supposed to change depending on the extent of the data
        const max = d3.max(this.rawData.map( se => this.getTheRightValueX(se))); // uncomment this if the X scale of the graph is supposed to stay constant
        const min = parseHHMMSS('0:00:00');
        x = d3.scaleTime()
          .domain([min, max]).nice()
          .range([0, this.innerWidth]);
      } else {
        x = d3.scaleLinear()
          // .domain([0, d3.max(this.getFilteredSeList.map( se => this.getTheRightValueX(se)))]).nice() // uncomment this if the X scale of the graph is supposed to change depending on the extent of the data
          .domain([0, d3.max(this.rawData.map( se => this.getTheRightValueX(se)))]).nice() // uncomment this if the X scale of the graph is supposed to stay constant
          .range([0, this.innerWidth]);
      }
      
      if (this.selectedYAxis == 'dauer') {
        // const max = d3.max(this.getFilteredSeList.map( se => this.getTheRightValueY(se))); // uncomment this if the Y scale of the graph is supposed to change depending on the extent of the data
        const max = d3.max(this.rawData.map( se => this.getTheRightValueY(se))); // uncomment this if the Y scale of the graph is supposed to stay constant
        const min = parseHHMMSS('0:00:00');
        y = d3.scaleTime()
          .domain([min, max]).nice()
          .range([this.innerHeight, 0]);
      } else {
        y = d3.scaleLinear()
          // .domain([0, d3.max(this.getFilteredSeList.map( se => this.getTheRightValueY(se)))]).nice() // uncomment this if the Y scale of the graph is supposed to change depending on the extent of the data
          .domain([0, d3.max(this.rawData.map( se => this.getTheRightValueY(se)))]).nice() // uncomment this if the Y scale of the graph is supposed to stay constant
          .range([this.innerHeight, 0]);
      }
      
      return { x, y };
    },
    // make graph brushable, so that a selection can be drawn by dragging on it with the mouse
    brush() {
      return d3.brush()
        .extent([[0, 0], [this.innerWidth, this.innerHeight]])
        // .on("start brush", () => this.brushed(d3.event)); // ACTIVATE THIS AGAIN IN ORDER TO MAKE IT RESPONSIVE AS IT DRAGS
        .on("start brush", () => {
          this.brushing(d3.event);
        })
        .on("end", () => {
          this.brushended(d3.event);
        });
    }
  },
  watch: {
    getFilteredSeList: function () { // redraw axes if final filtered data changes
      this.appendAxisY();
      this.appendAxisX();
    },
    selectedSeId: function(newSeId, oldSeId) { // emit popover for selected speech event
      if (oldSeId) { this.closePopover(oldSeId); }
      if (this.$refs[newSeId + '_popover']) { this.$refs[newSeId + '_popover'][0].$emit('open') }
    }
  },
  methods: {
    getXAxisLabel(selectedXAxis) {
      return this.xAxisOptions.filter( obj => {
        return obj.item === selectedXAxis;
      })[0].name;
    },
    getYAxisLabel(selectedYAxis) {
      return this.yAxisOptions.filter( obj => {
        return obj.item === selectedYAxis;
      })[0].name;
    },
    // used for dauer data
    parseInterval(interval) {
      var intervalParser = d3.timeFormat('%H:%M:%S');
      return intervalParser(interval);
    },
    toggleDescription(e, se) {
      if (!e.target.classList.contains('expanded')) {
        e.target.previousElementSibling.innerText = se.beschreibung;
        e.target.classList.add('expanded');
        e.target.innerText = "Less";
      } else {
        e.target.previousElementSibling.innerText = se.beschreibung.substring(0,90).trim() + "...";
        e.target.classList.remove('expanded');
        e.target.innerText = "More";
      }
    },
    appendAxisY() {
      if (this.selectedYAxis == 'dauer') {
        d3.select('#graph-axis-y')
          .call(d3.axisLeft(this.scale.y)
            .ticks(d3.timeMinute.every(60))
            .tickFormat(d3.timeFormat("%H:%M")))
      } else {
        d3.select('#graph-axis-y')
          .call(d3.axisLeft(this.scale.y)); // .tickSize( -this.innerWidth, 0));
      }
    },
    appendAxisX() {
      if (this.selectedXAxis == 'dauer') {
        d3.select('#graph-axis-x')
          .call(d3.axisBottom(this.scale.x)
            .ticks(d3.timeMinute.every(60))
            .tickFormat(d3.timeFormat("%H:%M")))
      } else {
      d3.select('#graph-axis-x')
        .call(d3.axisBottom(this.scale.x));
      }
    },
    appendBrush() {
        d3.select('.brush-graph')
          .call(this.brush)
          // .call(this.brush.move, [this.brushRange.min, this.brushRange.max].map(this.scale.x));
    },
    getGraphWidth() { this.graphWidth = document.getElementById('speech-event-area').clientWidth - this.margin.left },
    getGraphHeight() { this.graphHeight = document.getElementById('graph-container').clientHeight - this.margin.top },
    getTheRightValueY(se) {
      // make sure to access the correct value for Y axis from the speech event (se) object
      let value = "";
      if (this.frequenzlistenOptions.includes(this.selectedYAxis)) {
        value = se.maße.wortschatz[this.selectedYAxis].tokens_ratio == "nicht verfügbar"
        ? 0
        : Math.round(+se.maße.wortschatz[this.selectedYAxis].tokens_ratio.replace(',','.') * 100);
      } else if (this.getWortarten.includes(this.selectedYAxis)) {
        value = se.maße.wortarten[this.selectedYAxis].tokens_ratio == "nicht verfügbar"
        ? 0
        : +se.maße.wortarten[this.selectedYAxis].token_ratio.replace(',','.');
      } else if (this.getMündlichkeitsphänomene.includes(this.selectedYAxis)) {
        value = se.maße.mündlichkeitsphänomene[this.selectedYAxis].tokens_ratio == "nicht verfügbar"
        ? 0
        : +se.maße.mündlichkeitsphänomene[this.selectedYAxis].token_ratio.replace(',','.');
      } else {
        value = this.selectedYAxis != 'dauer' 
          ? se.maße[this.selectedYAxis] == "nicht verfügbar"
            ? 0
            : +se.maße[this.selectedYAxis].replace(',','.')
          : parseHHMMSS(se.maße[this.selectedYAxis]);
      }
      return value;
    },
    // make sure to access the correct value for X axis from the speech event (se) object
    getTheRightValueX(se) {
      let value = "";
      if (this.frequenzlistenOptions.includes(this.selectedXAxis)) {
        value = se.maße.wortschatz[this.selectedXAxis].tokens_ratio == "nicht verfügbar"
        ? 0
        : Math.round(+se.maße.wortschatz[this.selectedXAxis].tokens_ratio.replace(',','.') * 100);
      } else if (this.getWortarten.includes(this.selectedXAxis)) {
        value = se.maße.wortarten[this.selectedXAxis].token_ratio == "nicht verfügbar"
        ? 0
        : +se.maße.wortarten[this.selectedXAxis].token_ratio.replace(',','.');
      } else if (this.getMündlichkeitsphänomene.includes(this.selectedXAxis)) {
        value = se.maße.mündlichkeitsphänomene[this.selectedXAxis].token_ratio == "nicht verfügbar"
        ? 0
        : +se.maße.mündlichkeitsphänomene[this.selectedXAxis].token_ratio.replace(',','.');
      } else {
        value = this.selectedXAxis != 'dauer'
          ? se.maße[this.selectedXAxis] == "nicht verfügbar"
            ? 0
            : +se.maße[this.selectedXAxis].replace(',','.')
          : parseHHMMSS(se.maße[this.selectedXAxis]);
      }
      return value;
    },
    closePopover(id) {
      this.$refs[id + '_popover'][0].$emit('close');
    },
    setSelectedSeId(id) { // activates when a data point on the graph is clicked on
      if (!document.getElementById(id).hasAttribute("aria-describedby")) {
        // if point of the graph does not have a popover activated
        const tableEntry = document.getElementById(id + '_index');
        const entryTop = tableEntry.closest("td").offsetTop;
        const headerHeight = document.querySelector("#main-table > thead").offsetHeight;

        // scroll to the corresponding entry in the table
        document.querySelector("#main-table").closest("div").scrollTo({ left: 0, top: entryTop - headerHeight, behavior: "smooth"});

        this.$store.dispatch('setSelectedSeId', id);
      } else {
        this.$store.dispatch('setSelectedSeId', null);
      }
      
    },
    // while brushing, mark brushed circle elements (se)
    brushing(e) {
      const extent = e.selection;
      d3.selectAll("circle.circle").classed("selected-circle", function() {
        const cy = this.cy.baseVal.value,
              cx = this.cx.baseVal.value,
              x0 = extent[0][0],
              x1 = extent[1][0],
              y0 = extent[0][1],
              y1 = extent[1][1];
        return x0 <= cx && cx <= x1 && y0 <= cy && cy <= y1;
      });
    },
    // once the selection has been drawn (= mouse lifted)
    brushended(e) {
      if (!e.sourceEvent) return; // Only transition after input.
      if (!e.selection) {
        this.$store.dispatch('setIsEmptyGraphSelection', true); // let others know the selection is empty
        return; // Ignore empty selections.
      }
      
      this.$store.dispatch('setIsEmptyGraphSelection', false);

      // get the Y coordinates of the brushed range (vertical starting point and vertical ending point)
      const brushedGraphYRange = [];
      let x, y;
      // inverted because not using _.inRange in getters anymore
      brushedGraphYRange[0] = e.selection[1][1]; 
      brushedGraphYRange[1] = e.selection[0][1];
      if (this.selectedYAxis != 'dauer') {
        y = brushedGraphYRange.map(this.scale.y.invert);
      } else {
        const asDates = brushedGraphYRange.map(this.scale.y.invert);
        y = asDates.map( item => {
          const dateAsString = this.parseInterval(item);
          return dateAsString.length === 8 && _.head(dateAsString) === '0'
            ? dateAsString.slice(1)
            : dateAsString;
        });
      }
      
      // get the X coordinates of the brushed range (horizontal starting point and horizontal ending point)
      const brushedGraphXRange = [];
      brushedGraphXRange[0] = e.selection[0][0];
      brushedGraphXRange[1] = e.selection[1][0];
      if (this.selectedXAxis != 'dauer') {
        x = brushedGraphXRange.map(this.scale.x.invert);
      } else {
        const asDates = brushedGraphXRange.map(this.scale.x.invert);
        x = asDates.map( item => {
          const dateAsString = this.parseInterval(item);
          return dateAsString.length === 8 && _.head(dateAsString) === '0'
            ? dateAsString.slice(1)
            : dateAsString;
        });
      }
      // save them in state.js so other components have access to it
      this.$store.dispatch('setBrushedGraphRange', {x: x, y: y});
    }
  },
  // once the component is mounted, draw the graph parts
  mounted: function() {
    this.getGraphWidth(),
    this.getGraphHeight(),
    this.appendAxisY(),
    this.appendAxisX(),
    this.appendBrush()
  }
}
</script>


<style>
.popover {
  max-width: 36em !important;
  font-size: .75em !important;
}

/* .tick line {
  opacity: 0.5;
  stroke-width: 0.5px;
} */
</style>

<style scoped>
#graph-svg {
  height: 42vh;
}
circle.circle {
  opacity: .5;
  fill: steelblue;
}

circle.circle.selected-circle {
  opacity: 1 !important;
  stroke: black;
  stroke-width: .5px;
}

.popover-table ul {
  list-style: none;
  padding-left: 0;
  margin: 0;
}

.popover-table ul:not(:first-child) {
  padding-left: 1em;
}

.popover-table ul li:before {
  content: "";
  padding-right: .5em;
}

.popover-table ul:not(:first-child) li:before {
  content: "└";
  padding-right: .5em;
}

</style>

