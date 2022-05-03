// This is the place where asynchronous operations are 
// made, such as AJAX calls. The payload is committed to 
// be updated using the invoked functions found in mutations.js. 

export default {
  setCorpus: ({commit}, payload) => {
    commit("SET_CORPUS", payload);
  },
  setFrequenzliste: ({commit}, payload) => {
    commit("SET_FREQUENZLISTE", payload);
  },
  setYAxis: ({commit}, payload) => {
    commit("SET_Y_AXIS", payload);
  },
  setXAxis: ({commit}, payload) => {
    commit("SET_X_AXIS", payload);
  },
  setIsEmptyGraphSelection: ({commit}, payload) => {
    commit("SET_IS_EMPTY_GRAPH_SELECTION", payload);
  },
  setBrushedGraphRange: ({commit}, payload) => {
    commit("SET_BRUSHED_GRAPH_RANGE", payload);
  },
  setSelectedSeId: ({commit}, payload) => {
    commit("SET_SELECTED_SE_ID", payload);
  },
  updateRange: ({commit}, payload) => {
    commit("UPDATE_RANGE", payload);
  },
  setIsLoading: ({commit}, payload) => {
    commit("SET_IS_LOADING", payload);
  },
  getAjaxData: ({commit, state, getters}) => {
      const dataNamePairs = {
        data: "", 
        gesprachstypData: "gesprachstypTreeselect",
        themenData: "themen",
        artData: "art",
        sprachregionData: "sprachregionTreeselect",
        sprachen: "lang"
      };
    const corpora = Object.keys(state.corpusData);
    return new Promise((resolve) => {
      // read the settings in configuration.json
      fetch("configuration.json")
        .then(res => res.json())
        .then(config => {
          // get the configuration values (DATA_SERVLET_URL and BASE_URL)
          // and call the mutation to add this data to state.js
          commit("GET_CONFIG_AJAX", config);
        }).then(() => {

        corpora.forEach(corpus => {
          for (let [key, value] of Object.entries(dataNamePairs)) {
            if (value == "lang" && corpus == "FOLK") return;
            // fetch the JSON data from the ZuMult server
            fetch(state.config.DATA_SERVLET_URL + "?path=prototypeJson/" + value + corpus +".json")
              .then(res => res.json())
              .then(data => {
                // start the spinning loading icon
                commit("SET_IS_LOADING", true);
                commit("SET_AJAX_DATA", {corpus, key, data});
              }).then(() => {
                if (getters.rawData.length === 0) return
                
                // set all calculated extent ranges here
                // each of them are calculated in getters.json
                for (let [key, value] of Object.entries(getters.getWortartenExtents)) {
                  commit("UPDATE_RANGE", {measure: key, extent: value});
                }
                for (let [key, value] of Object.entries(getters.getMündlichkeitsphänomeneExtents)) {
                  commit("UPDATE_RANGE", {measure: key, extent: value});
                }
                commit("UPDATE_RANGE", {measure: "normalisierungsrate", extent: getters.getNormalisierungExtent});
                commit("UPDATE_RANGE", {measure: "anteil_Überlappungen_mit_mehr_als_2_wörtern_tokens", extent: getters.getUeberlappungenExtent});
                commit("UPDATE_RANGE", {measure: "artikulationsrate", extent: getters.getArtikulationsrateExtent});
                commit("UPDATE_RANGE", {measure: "WORTSCHATZ", extent: getters.getFrequenzlisteExtent});
                commit("UPDATE_RANGE", {measure: "dauer", extent: getters.getDauerExtent});

                // stop the spinning loading icon once the data is all loaded and in place
                commit("SET_IS_LOADING", false);
              });
          }
        });
        resolve();
      });
    });
  },
}