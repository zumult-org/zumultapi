<template>

  <div :id="name" class="mt-3">
    <h5>{{ label }}</h5>
    <div class="mb-3">
      <span v-if="inputText">{{ inputText }}</span>
      <b-form-input 
        class="d-inline range-input"
        size="sm"
        :type="type"
        :min="min"
        :max="inputRange2-1"
        v-model="inputRange1" 
        />
        - 
      <b-form-input 
        class="d-inline range-input"
        size="sm"
        :type="type"
        :min="inputRange1+1"
        :max="max"
        v-model="inputRange2" 
        />
    </div>
    <vue-slider 
      ref="slider"
      class="px-2 pt-3"
      v-model="sliderInputRange"
      :data="dauerData"
      :min="min"
      :max="max"
      :min-range="1"
      :marks="marks"
      :enable-cross="false"
      :lazy="true"
      @dragging="updateSelection"
    ></vue-slider>
    <div class="mt-4"><a href="#" @click="resetSelection">Reset</a></div>
    <hr v-if="label" :key="`hr_${name}`" />
  </div>
        
</template>


<script>
import VueSlider from 'vue-slider-component'
import 'vue-slider-component/theme/antd.css'
import { getMarks, getMarksAsObject } from '@/helper'


export default {
  props: [
    'name',
    'range',
    'extent',
    'label',
    'isAnteil',
    'inputText',
    'dauerData',
    'dauerMarks'
  ],
  components: {
    VueSlider
  },
  data() {
    return {
    }
  },
  computed: {
    min() {
      return this.name === "dauer" ? null : this.extent[0];
    },
    max() {
      return this.name === "dauer" ? null : this.extent[1];
    },
    inputRange1: {
      get() {
        // make sure the value does not exceed the min and the max
        return this.range[0] < this.min ? this.min : this.range[0];
      },
      set(value) {
        this.updateSelection([value, this.inputRange2]);
      }
    },
    inputRange2: {
      get() {
        // make sure the value does not exceed the min and the max
        return this.range[1] > this.max ? this.max : this.range[1];
      },
      set(value) {
        this.updateSelection([this.inputRange1, value]);
      }
    },
    sliderInputRange: {
      get() {
        // make sure the values do not exceed the min and the max
        const checkedMax = this.range[1] > this.max ? this.max : this.range[1];
        const checkedMin = this.range[0] < this.min ? this.min : this.range[0];
        
        return [checkedMin,checkedMax];
      },
      set(value) {
        this.updateSelection(value);
      }
    },
    type() {
      return this.name === "dauer" ? "time" : "number";
    },
    marks() {
      if (this.name === "dauer") return this.dauerMarks;
      if (this.isAnteil === null) return getMarks(this.extent);
      if (this.isAnteil) return getMarksAsObject(this.isAnteil, this.extent);
      return getMarksAsObject(this.isAnteil, this.extent);
    }
  },
  methods: {
    resetSelection() {
      this.updateSelection(this.extent);
    },
    updateSelection(value) {
      this.$store.dispatch('updateRange', {measure: this.name, extent: value});
    },
  }
}
</script>

<style scoped>
</style>

<style>

</style>
