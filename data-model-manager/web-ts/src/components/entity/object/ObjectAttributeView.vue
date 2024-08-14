<template>
  <el-table :data="attrs" style="width: 100%" border v-loading="loading" max-height="calc(100vh - 239px)">
    <el-table-column prop="name" label="属性描述"/>
    <el-table-column prop="code" label="属性标识"/>
  </el-table>
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
  defineModel
} from 'vue'

const attrs = reactive(new Array<Record<any, any>>());
const {proxy} = getCurrentInstance() as ComponentInternalInstance;
const loading = ref(true)
const props = defineProps<{ id: number }>()
const load = (id: number) => {
  proxy?.$http.get(`obj/${id}`)
      .then(({data}: { data: Record<any, any> }) => {
        attrs.length = 0;
        attrs.push(...data.attributes as Array<Record<any, any>>)

      })
      .then(() => {
        loading.value = false
      }, () => {
        loading.value = false
      })
}
console.log(props.id)
const normalizedSize = computed(() => props.id)
watch(normalizedSize, (value, oldValue, onCleanup) => {
  console.log(value)
  if (value == -1) {
    loading.value = true
  } else if (value != oldValue && value > 0) {
    load(value as number)
  }
})
</script>

<style scoped>

</style>