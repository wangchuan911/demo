<template>
  <el-form :model="model.form" label-width="auto" >
    <el-form-item :label="input.label" v-for="input in model.inputs" :key="input.code" :index="input.code">
      <component :is="input.comp" v-bind="input.prop" v-model="model.form[input.code]" v-on="input.events"></component>
    </el-form-item>
  </el-form>
</template>

<script lang="ts" setup>
import {
  ref,
  reactive,
  getCurrentInstance,
  ComponentInternalInstance,
  defineProps,
  defineEmits,
  watch,
  computed,
  defineModel, ComponentCustomProperties
} from 'vue';

import 'element-plus/es/components/select/style/css';
import 'element-plus/es/components/option/style/css';

const model = defineModel();

</script>

<style scoped>

</style>