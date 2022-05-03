import * as d3 from 'd3'

/**
* Returns hhmmss as Date object
*
* @param {String} hhmmss Time as string
* @return {Date} hhmmss as Date object
*/
export const parseHHMMSS = hhmmss => {
  const parse = d3.timeParse('%H:%M:%S');
  return parse(hhmmss);
}

/**
* Returns hhmm time in minutes
*
* @param {String} hhmm Time in hours and mins, ex. "00:00", "01:30"
* @return {Number} hhmm as number of minutes, ex. 0, 90
*/
export const timeStringToMin = hhmm => {
  return (+hhmm.substring(0,2) * 60) + +hhmm.substring(3,5);
}

/**
* Returns minutes as a string of hours and minutes
*
* @param {Number} minutes Time in minutes, ex. 45, 330
* @return {String} minutes formatted as hh:mm, ex. "00:45", "05:30"
*/
export const minutesToTimeString = minutes => {
  const hours = ("0" + (~~(minutes / 60))).slice(-2);
  const mins = ("0" + (minutes % 60)).slice(-2); // add a 0 in front if single digit
  return hours+":"+mins;
}

/**
* Returns list of marks to display on the slider filter
*
* @param {Number} min min of a range
* @param {Number} max max of a range
* @return {Array} list of marks to display on the slider filter, either in a succession of 50, 20, 10, 5, 2, or 1
*/
export const getMarks = ([min, max]) => {
  let factor;
  if ((max - min) > 200) {
    factor = 60;
  } else if ((max - min) > 100) {
    factor = 20;
  } else if ((max - min) > 50) {
    factor = 10;
  } else if ((max - min) > 20) {
    factor = 5;
  } else if ((max - min) > 10) {
    factor = 2;
  } else {
    factor = 1;
  }
  const minMark = Math.ceil(min/factor) * factor;
  const maxMark = factor == 1 ? Math.ceil(max/factor) * factor + 1 : Math.ceil(max/factor) * factor;

  let marks = [];
  for (let n = minMark; n < maxMark; n+=factor) {
    marks = [...marks, n];
  }

  return marks;
}

/**
* Returns list of marks to display on the slider filter
*
* @param {Number} min min of a range
* @param {Number} max max of a range
* @param {boolean} isAnteil whether the label is shown as "... Anteil" as opposed to "niedrig"/"hoch"
* @return {Array} list of marks to display on the slider filter, either in a succession of 50, 20, 10, 5, 2, or 1
*/
export const getMarksAsObject = (isAnteil, [min, max]) => {
  let factor;
  if ((max - min) > 200) {
    factor = 60;
  } else if ((max - min) > 100) {
    factor = 20;
  } else if ((max - min) > 50) {
    factor = 10;
  } else if ((max - min) > 20) {
    factor = 5;
  } else if ((max - min) > 10) {
    factor = 2;
  } else {
    factor = 1;
  }
  const minMark = Math.ceil(min/factor) * factor;
  const maxMark = factor == 1 ? Math.ceil(max/factor) * factor + 1 : Math.ceil(max/factor) * factor;

  let marks = {};
  const labelMin = isAnteil ? "Niedriger Anteil" : "niedrig";
  const labelMax = isAnteil ? "Hoher Anteil" : "hoch";

  for (let n = minMark; n < maxMark; n+=factor) {
    marks[n] = { label: "" }
  }

  marks[min] = {
    label: labelMin,
    labelStyle: {
      right: 0
    }
  };

  marks[max] = {
    label: labelMax,
    labelStyle: {
      transform: "none",
      left: "auto",
      right: 0
    }
  }

  return marks;  
}

/**
* Returns the extremes (min and max) of list of number values
*
* @param {Object} getters getters.js
* @param {Array} valuesList list of number values 
* @return {Array} the min and the max found in the valuesList
*/
export const getExtent = (getters, valuesList) => {
  // if the data has not loaded, set extent to [0,100]
  const min = getters.rawData.length == 0 ? 0 : Math.floor(Math.min(...valuesList));
  const max = getters.rawData.length == 0 ? 100 : Math.ceil(Math.max(...valuesList));
  return [min, max];
}