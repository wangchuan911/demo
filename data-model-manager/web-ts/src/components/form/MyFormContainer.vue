<template>
  <el-form :model="model.form" label-width="auto" ref="formRef">
    <el-form-item :label="input.label" v-for="input in model.inputs" :key="input.code" :index="input.code" :prop="input.code" :rules="input.rules"
                  v-show="!input.disable">
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
import {FormInstance, FormRules} from "element-plus";
import {FormContent} from "@/components/config";

const model = defineModel<FormContent>();
const formRef = ref<FormInstance>()
model.value.formRef = formRef;
console.log(model);
</script>

<style scoped>

</style>