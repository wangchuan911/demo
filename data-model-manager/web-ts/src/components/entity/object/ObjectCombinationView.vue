<template>
  <div>
    <div style="display: flex;margin: 5px 1px;height:32px ">
      <el-popover
          placement="bottom"
          title="SQL"
          :width="200"
          trigger="click"
          :content="sql"
          @show="showSql"
      >
        <template #reference>
          <el-button type="primary">查看SQL</el-button>
        </template>
      </el-popover>
    </div>

    <el-table :data="attrs" style="width: 100%" border v-loading="loading" max-height="calc(100vh - 197px)" row-key="id"
              :lazy="lazy"
              :load="expand"
              :default-expand-all="!lazy"
              :tree-props="lazy?{ children: 'children', hasChildren: 'hasChildren' }:{}">
      <el-table-column prop="object.name" label="对象描述"/>
      <el-table-column prop="object.code" label="对象标识"/>
      <el-table-column prop="object.typeDesc" label="对象类型"/>
      <el-table-column prop="typeDesc" label="关联方式"/>
      <el-table-column prop="instanceId" label="对象实例ID"/>
    </el-table>
  </div>
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
const lazy = ref(false)
const load = (id: number) => {
  loading.value = true
  proxy?.$http.get(`obj/combination/${id}`)
      .then(({data}: { data: Array<Record<any, any>> }) => {
        attrs.length = 0;
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
const objectId = computed(() => props.id)
watch(objectId, (value, oldValue, onCleanup) => {
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
  proxy?.$http.get(`link/expand/${row.id <= -1 ? `obj${row.objectId}` : row.id}`)
      .then(({data}: { data: Record<any, any>[] }) => {
        console.log(data)
        resolve(data)
      })
      .then(() => {
        loading.value = false
      }, () => {
        loading.value = false
      })
}
const sql = ref(new String());
const showSql = () => {
  loading.value = false
  proxy?.$http.get(`link/show/${props.id}`)
      .then(({data}: { data: Record<any, any>[] }) => {
        console.log(data)
        sql.value = data as unknown as string
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