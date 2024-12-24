<template>
  <el-tree-select v-bind="propsProp" :data="treeOptions" v-model="model" @change="(value)=>emit('change',value)">
    <temlate v-for="item in data" :key="item.value">
      <component :is="item.comp"
                 :label="item.name"
                 :value="item.value"
      />
    </temlate>
  </el-tree-select>
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

import {ElMessageBox, ElMessage} from 'element-plus';
import type {Action} from 'element-plus';
import 'element-plus/es/components/select/style/css';
import 'element-plus/es/components/option/style/css';
import {MyOption, MyTreeOption} from "@/components/form/config";

const model = defineModel();
const props = defineProps<{ tree: Array<MyTreeOption>, prop: any }>();
const treeOptions = computed(() => props.tree);
const propsProp = computed(() => props.prop);
watch(treeOptions, (value, oldValue) => {
  console.log(value, oldValue);
});
const emit = defineEmits(["change"]);
</script>

<style scoped>

</style>