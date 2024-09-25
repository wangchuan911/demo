<template>
  <el-input
      v-bind="propsProp" v-model="model"
      class="input-with-select"
  >
    <template #append>
      <el-button :icon="Search" @click="onSearch"/>
    </template>
  </el-input>
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
import {
  Search,
} from '@element-plus/icons-vue';
const model = defineModel();
const prop = defineProps<{ onSearch: () => void, prop: any }>();
const propsProp = computed(() => prop.prop);
const onSearch = computed(() => prop.onSearch);
watch(propsProp, (value, oldValue) => {
  console.log(value, oldValue);
});

</script>

<style scoped>

</style>