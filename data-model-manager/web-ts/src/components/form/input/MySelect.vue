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

const model = defineModel();
const props = defineProps<{ option: Array<{ label: string, value: any }>, prop: any }>();
const propsOption = computed(() => props.option);
const propsProp = computed(() => props.prop);


</script>

<style scoped>

</style>