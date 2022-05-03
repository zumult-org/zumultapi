<template>
<!-- 
  This is the old Filter component for Dauer, the one
  that used a bar chart as a filter interface. It has been
  replaced with the new much simpler one (Dauer2) that
  uses the SliderComponent, like the rest of the filters
  that use a numeric range. This component is disabled and
  and relatively outdated, but could be reactivated if at
  some point we decide to go back to the bar chart filters
 -->
  <b-card no-body class="mb-1">
    <b-card-header header-tag="header" class="p-1 green-group" role="tab">
      <b-button block href="#" v-b-toggle.dauer class="p-1 text-left bg-transparent text-dark no-border">
        Dauer
        <b-badge class="rounded-circle" variant="info" v-b-tooltip.hover :title="tooltipTitle">?</b-badge>
      </b-button>
    </b-card-header>
    <b-collapse id="dauer" role="tabpanel">
      <b-card-body>
        <div class="dauerContainer">
          <!-- <a href="#">Reset</a> -->
          <!-- <a href="#" @click="resetSelection">Reset</a> -->
          <svg :width="dauerWidth" :height="dauerHeight" id="dauerSvg">
            <g id="dauerGroup" :transform="`translate(${margin.left}, ${margin.top})`">
              <g class="brush-dauer">
                <text class="handle-label handle-label-west"
                      :x="selectionWestX"
                      :y="-5"
                      fill="black">{{ getBrushExtremeAsString(brushRange.min) }}</text>
                <text class="handle-label handle-label-east"
                      :y="-5"
                      :x="selectionEastX"
                      fill="black">{{ getBrushExtremeAsString(brushRange.max) }}</text>
              </g>
              <g class="bars-group">
                  <rect class="bar dauer-bar"
                        v-for="(sliderPair, index) in getSliderPairs"
                        :key="index"
                        :x="scale.x(parseInterval(sliderPair.interval))"
                        :y="scale.y(sliderPair.amount) + 0.5"
                        :height="innerHeight - scale.y(sliderPair.amount)"
                        :width="innerWidth / (getMaxDimensions.duration * 6)"
                        v-b-tooltip.hover
                        :title="`Intervall: ${sliderPair.interval}\rAnzahl Sprechereignisse: ${sliderPair.amount}`"
                  />
              </g>
              <g id="dauer-graph-axis-x" class="axis x" :transform="`translate(0,${innerHeight})`"></g>
              <g id="dauer-graph-axis-y" class="axis y" ></g>
            </g>
          </svg>
        </div>
      </b-card-body>
    </b-collapse>
  </b-card>
</template>


<script>
import * as d3 from 'd3'
import { mapState, mapGetters } from 'vuex'
import { parseHHMMSS } from '@/helper'
import _ from 'lodash'

export default {
  props: {},
  data() {
    return {
      tooltipTitle: 'Dauer des Sprechereignisses in Minuten bzw. Stunden',
      seDurations: [],
      dauerWidth: 0,
      dauerHeight: 180,
      margin: { top: 20, right: 15, bottom: 20, left: 20 },
      selectionWestX: null, // for positioning the text element above the handles
      selectionEastX: null,
      brushedDurations: null,
      leftTextWidth: 25, // should be automated to check the initial width of the text label
    }
  },
  computed: {
    ...mapState([
      'brushRange',
      'selectedCorpus'
    ]),
    ...mapGetters([
      'rawData',
      'artFilter',
      'gespraechstypFilter',
      'themenFilter',
      'frequenzlistenFilter',
      'normalisierungFilter',
      'ueberlappungenRangeFilter',
      'beitragslaengeRangeFilter',
      'artikulationsrateRangeFilter',
      'graphSelectionFilter',
      'sprachregionFilter'
    ]),
    innerWidth() { return this.dauerWidth - this.margin.left - this.margin.right },
    innerHeight() { return this.dauerHeight - this.margin.top - this.margin.bottom },
    hoursExtent() {
      return {
        min: parseHHMMSS('0:00:00'),
        max: new Date(parseHHMMSS('0:00:00').setHours(this.getMaxDimensions.duration))
      }
    },
    getMaxDimensions() {
      let sliderPairs = [];
      let amount;
      let duration;
      if (this.rawData.length > 0) {
              this.rawData.map( se => {
        const hhmm = se.maße.dauer.slice(0, -4) + '0';
        if (!sliderPairs.some(o => o.interval === hhmm)) {
          const sliderPair = {
            interval: hhmm,
            amount: 1
          };
          sliderPairs.push(sliderPair);
        } else {
          const sliderPair = sliderPairs.filter(sliderPair => sliderPair.interval === hhmm)[0];
          sliderPair.amount++;
        }
      });
      // console.log(sliderPairs)
      amount = _.max(sliderPairs.map(pair => pair.amount));
      // console.log(amount)
      duration = d3.max(sliderPairs.map(pair => {
        return this.parseInterval(pair.interval)
      })).getHours() + 1;
      }

      return { amount, duration };

    },
    scale() {
      const x = d3.scaleTime()
        .domain([this.hoursExtent.min, this.hoursExtent.max])
        .range([0, this.innerWidth]);
      const y = d3.scaleLinear()
        .domain([0, this.getMaxDimensions.amount]) // max amount
        .range([this.innerHeight, 0]);
      return { x, y };
    },
    brush() {
      return d3.brushX()
        .extent([[0, 0], [this.innerWidth, this.innerHeight]])
        .on("brush", () => {
          this.brushing(d3.event);
        })
        .on("end", () => { // on brush is a very intense operation
          this.brushended(d3.event);
        });
    },
    getSliderPairs() {
      let sliderPairs = [];

      const dauerData = this.gespraechstypFilter != 0
        ? _.intersection(this.gespraechstypFilter, 
                         this.artFilter, 
                         this.themenFilter, 
                         this.frequenzlistenFilter, 
                         this.normalisierungFilter, 
                         this.ueberlappungenRangeFilter, 
                         this.beitragslaengeRangeFilter, 
                         this.artikulationsrateRangeFilter, 
                        //  this.graphSelectionFilter, 
                         this.sprachregionFilter)
        : _.intersection(this.artFilter, 
                         this.themenFilter, 
                         this.frequenzlistenFilter, 
                         this.normalisierungFilter, 
                         this.ueberlappungenRangeFilter, 
                         this.beitragslaengeRangeFilter, 
                         this.artikulationsrateRangeFilter, 
                        //  this.graphSelectionFilter, 
                         this.sprachregionFilter);

      dauerData.map((se) => { // use data filtered by other filters
        const hhmm = se.maße.dauer.slice(0, -4) + '0';
        if (!sliderPairs.some(o => o.interval === hhmm)) {
          const sliderPair = {
            interval: hhmm,
            amount: 1
          };
          sliderPairs.push(sliderPair);
        } else {
          const sliderPair = sliderPairs.filter(sliderPair => sliderPair.interval === hhmm)[0];
          sliderPair.amount++;
        }
      });
      return sliderPairs;
    }
  },
  watch: {
    getMaxDimensions: function () { // redraw axes if final filtered data changes 
      this.getSeDurations();
      this.appendAxisY();
      this.appendAxisX();
    }
  },
  methods: {
    resetSelection() {
      // this.$store.dispatch('setBrushedGraphRange', this.hoursExtent);
      console.log(this.hoursExtent);
      console.log([this.brushRange.min, this.brushRange.max]);
      d3.select('.brush-dauer')
          .call(this.brush)
          .call(this.brush.move, [new Date(parseHHMMSS('0:00:00')), new Date(parseHHMMSS('6:00:00'))].map(this.scale.x));

    },
    getBrushExtremeAsString(extreme) {
      const hours = extreme.getHours();
      const minutes = (extreme.getMinutes() + '').length == 2 ? extreme.getMinutes() + '' : '0' + (extreme.getMinutes() + '');
      return `${hours}:${minutes}`;
    },
    parseInterval(interval) {
      var intervalParser = d3.timeParse('%H:%M');
      return intervalParser(interval);
    },
    // TODO: move this also into a file eventually, then import depending on the corpus
    getSeDurations() {
      this.rawData.map( se => {
        this.seDurations.push(parseHHMMSS(se.maße.dauer));
      })
    },
    appendAxisY() {
      d3.select('#dauer-graph-axis-y')
        .call(d3.axisLeft(this.scale.y)
          .ticks(8))
        .append("text")
          .attr('fill', 'black')
          .attr("class", "y label")
          .attr("y", '-4em')
          .attr("transform", "rotate(-90)")
          .text("Anzahl Sprechereignisse");
    },
    appendAxisX() {
      d3.select('#dauer-graph-axis-x')
        .call(d3.axisBottom(this.scale.x)
          .ticks(d3.timeMinute.every(60))
          .tickFormat(d3.timeFormat("%H:%M")))
        .append("text")
          .attr('fill', 'black')
          .attr("class", "x label")
          .attr("x", this.dauerWidth/2)
          .attr("text-anchor", "end")
          .attr("y", '4em')
          .text("Dauer in HH:MM");
    },
    appendBrush() {
      setTimeout(() => { // timeout here because the dauer bars are not fully loaded when this runs. 
        d3.select('.brush-dauer')
          .call(this.brush)
          .call(this.brush.move, [this.brushRange.min, this.brushRange.max].map(this.scale.x));
        }, 3000);
    },
    brushing(e) {
      // if (!e.sourceEvent) return; // Only transition after input.
      if (!e.selection) return; // Ignore empty selections.

      this.brushedDurations = e.selection.map(this.scale.x.invert);
      if (this.brushedDurations[0] <= this.brushedDurations[1]) {
        var tens0 = Math.floor(this.brushedDurations[0].getMinutes() / 10 % 10) * 10; // probably rework this to make it simpler
        var tens1 = Math.floor(this.brushedDurations[1].getMinutes() / 10 % 10) * 10;
        this.brushedDurations[0] = new Date(this.brushedDurations[0].setMinutes(tens0)).setSeconds(0);
        this.brushedDurations[1] = new Date(this.brushedDurations[1].setMinutes(tens1)).setSeconds(0);
      }
      this.brushRange.min = new Date(Math.min(...this.brushedDurations)); // minimum time range
      this.brushRange.max = new Date(Math.max(...this.brushedDurations)); // max time range
      const selectionWest = Math.ceil(e.selection[0]);
      const selectionEast = Math.ceil(e.selection[1]);

      this.selectionWestX = selectionWest - this.leftTextWidth;
      this.selectionEastX = selectionEast;

      // raise opacity for selected bars
      d3.selectAll('.bar.dauer-bar').classed('selected-bar', function() {
        const x = this.x.baseVal.value + 8; // +8 because of the bar width. necessary
        return selectionWest < x && x < selectionEast;
      });
    },
    brushended(e) {
      if (!e.sourceEvent) return; // Only transition after input.
      if (!e.selection) return; // Ignore empty selections.
      d3.select('.brush-dauer').transition().call(e.target.move, this.brushedDurations.map(this.scale.x));
    },
    getDauerWidth() {
        this.dauerWidth = document.getElementById('filters-group').clientWidth * .8;
    },
  },
  mounted: function() {
      this.getDauerWidth(),
      this.getSeDurations(),
      this.appendAxisY(),
      this.appendAxisX(),
      this.appendBrush()
  }
}
</script>

<style scoped>
#dauerSvg {
  display: block;
  margin: auto;
  margin-bottom: 2em;
  overflow: visible;
}

.bar {
  fill: steelblue;
  stroke-width: .5px;
  opacity: .5;
}

.bar.selected-bar {
  opacity: 1 !important;
  stroke: black;
  stroke-width: .5px;
}

text.handle-label {
  font-size: .7em;
}

</style>

<style>
  .brush-dauer rect.handle {
    stroke: black;
    fill: grey;
    width: 3px;
  }

  .brush-dauer rect.handle.handle--w {
    transform: translateX(0px);
  }
  .brush-dauer rect.handle.handle--e {
    transform: translateX(2px);
  }
</style>
