<template>
  <el-select v-bind="propsProp" v-model="model">
    <el-option
        v-for="item in propsOption"
        :key="item.value"
        :label="item.label"
        :value="item.value"
    />
  </el-select>
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
import {MyOption} from "@/components/form/config";

const model = defineModel();
const props = defineProps<{ options: Array<MyOption>, prop: any }>();
const propsOption = computed(() => props.options);
const propsProp = computed(() => props.prop);
watch(propsOption, (value, oldValue) => {
  console.log(value, oldValue);
});

</script>

<style scoped>

</style>