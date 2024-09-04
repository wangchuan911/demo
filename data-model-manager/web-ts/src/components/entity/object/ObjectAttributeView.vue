<template>
  <div style="display: flex;margin: 5px 1px;height:32px ">
    <el-button type="primary" @click="drawers.addAttr.show=true">添加属性</el-button>
  </div>
  <el-table :data="attrs" style="width: 100%" border v-loading="loading" max-height="calc(100vh - 197px)">
    <el-table-column prop="name" label="属性描述"/>
    <el-table-column prop="code" label="属性标识"/>
  </el-table>
  <el-drawer v-model="drawers.addAttr.show" title="添加属性" size="50%" show-close
             :before-close="drawers.addAttr.close"></el-drawer>
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
import {ElMessageBox, ElMessage} from 'element-plus'
import type { Action } from 'element-plus'
import 'element-plus/es/components/message-box/style/css'

const drawers = ref(
    {
      addAttr: {
        show: false,
        close: (done: () => void) => {
          ElMessageBox.confirm('You still have unsaved data, proceed?')
              .then(() => {
                done()
              })
              .catch(() => {
                // catch error
              })
        }
      }
    })
const attrs = reactive(new Array<Record<any, any>>());
const {proxy} = getCurrentInstance() as ComponentInternalInstance;
const loading = ref(true)
const props = defineProps<{ id: number }>()
const load = (id: number) => {
  proxy?.$http.get(`obj/attrs/${id}`)
      .then(({data}: { data: Array<Record<any, any>> }) => {
        attrs.length = 0;
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
const close = (event: null) => {
  console.log(event)
}
</script>

<style scoped>

</style>