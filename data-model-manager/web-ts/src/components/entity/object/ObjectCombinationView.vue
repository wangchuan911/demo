<template>
  <el-table :data="attrs" style="width: 100%" border v-loading="loading" max-height="calc(100vh - 239px)" row-key="id"
            lazy
            :load="expand"
            :tree-props="{ children: 'children', hasChildren: 'hasChildren' }">
    <el-table-column prop="object.name" label="对象描述"/>
    <el-table-column prop="object.code" label="对象标识"/>
    <el-table-column prop="object.typeDesc" label="对象类型"/>
    <el-table-column prop="typeDesc" label="关联方式"/>
  </el-table>
</template>

<script lang="ts" setup>
import {
  ref,
  reactive,
  getCurrentInstance,
  ComponentInternalInstance,
  ComponentCustomProperties,
  defineProps,
  defineEmits,
  watch,
  computed,
  defineModel
} from 'vue'

const attrs = reactive(new Array<Record<any, any>>());
const {proxy} = getCurrentInstance() as ComponentCustomProperties;
const loading = ref(false)
const props = defineProps<{ id: number }>()
const load = (id: number) => {
  loading.value = true
  proxy?.$http.get(`obj/combination/${id}`)
      .then(({data}: { data: Array<Record<any, any>> }) => {
        attrs.length = 0;
        data.forEach(value => value.hasChildren = true)
        console.log(data)
        attrs.push(...data)

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
const expand = (row: Record<any, any>,
                treeNode: unknown,
                resolve: (date: Record<any, any>[]) => void) => {
  loading.value = true;
  console.log(row)
  proxy?.$http.get(`link/expand/${row.id == -1 ? `obj${row.objectId}` : row.id}`)
      .then(({data}: { data: Record<any, any>[] }) => {
        console.log(data)
        data.forEach(value => {
          /*delete value['parentId']
          delete value['object']
          delete value['attribute']
          delete value['objectId']
          delete value['sequence']
          delete value['type']
          delete value['typeDesc']
          delete value['typeId']
          delete value['attributeId']
          delete value['instanceId']*/
          delete value['id']
        })
        console.log(data)
        resolve(data)
      })
      .then(() => {
        loading.value = false
      }, () => {
        loading.value = false
      })
}
</script>

<style scoped>

</style>