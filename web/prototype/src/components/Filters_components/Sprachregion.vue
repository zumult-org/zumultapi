<template>
  <b-card no-body class="mb-1">
    <b-card-header header-tag="header" class="p-1 yellow-group" role="tab">
      <b-button block href="#" v-b-toggle.sprachregion class="p-1 text-left bg-transparent text-dark no-border">
        Sprachregion
        <b-badge class="rounded-circle" variant="info" v-b-tooltip.hover :title="tooltipTitle">?</b-badge>
      </b-button>
    </b-card-header>
    <b-collapse id="sprachregion" role="tabpanel">
      <b-card-body>
        <div class="sprachregionMapContainer">
          <!--  Karte -->
          <svg :width="mapWidth" :height="mapHeight" id="sprachregionMapSVG">
            <path class="region"
                  v-for="(region, index) in regions"
                  :key="index"
                  :d="path(region)"
                  :class="getRegionId(region)"
                  v-b-tooltip.hover
                  :title="getRegionName(region)"
            ></path>
            <!-- v-b-tooltip.hover
                  :title="getRegionId(region)" -->
          </svg>
          <!-- Liste -->
        </div>
      </b-card-body>
    </b-collapse>
  </b-card>
</template>

<script>
// import germanyRegionsJson from '@/../../../src/main/java/data/prototypeJson/germany-regions.json' // ==> fetch data from server in actions 
import rede from '@/../../../src/main/java/data/prototypeJson/rede_data_export.json' // real sprachregion data
import * as d3 from 'd3'
import * as topojson from "topojson-client"

export default {
  data() {
    return {
      mapWidth: 200,
      mapHeight: 280,
      tooltipTitle: "Region, in der das Sprechereignis aufgenommen wurde"
    }
  },
  computed: {
    // regions: function() {
    //   return topojson.feature(data_test_json, data_test_json.objects.DEU_adm2).features
    // },
    regions: () => rede.features,
    projection: function() {
      return d3.geoMercator()
      	.center([ 10.6, 51 ]) // germany centre more or less
      	.translate([ this.mapWidth / 2, this.mapHeight / 2])
      	.scale(1200);
    },
    path: function() {
      return d3.geoPath()
    	  .projection(this.projection);
    }
  },
  methods: {
    getRegionId: regionObject => regionObject.properties.ID.toString(),
    getRegionName: regionObject => regionObject.properties.name
  }
}


</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>

#sprachregionMapSVG {
  display: block;
  margin: auto;
}

.region {
  fill: gray;
  fill-opacity: .7;
  stroke: black;
  stroke-width: .3;
}

.yellow-group {
  background-color: rgb(250, 250, 227);
}
</style>
