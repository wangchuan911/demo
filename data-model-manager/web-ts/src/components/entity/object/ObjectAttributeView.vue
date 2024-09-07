<template>
  <div style="display: flex;margin: 5px 1px;height:32px ">
    <el-button type="primary" @click="drawers.addAttr.show=true">添加属性</el-button>
  </div>
  <el-table :data="attrs" style="width: 100%" border v-loading="loading" max-height="calc(100vh - 197px)">
    <el-table-column prop="name" label="属性描述"/>
    <el-table-column prop="code" label="属性标识"/>
    <el-table-column fixed="right" label="操作">
      <template #default="scope">
        <el-button link type="primary" size="small" @click="delAttr(scope.row.id)">删除</el-button>
      </template>
    </el-table-column>
  </el-table>
  <el-drawer v-model="drawers.addAttr.show" title="添加属性" size="50%" show-close
             :before-close="drawers.addAttr.beforeClose">
    <template #default>
      <el-form :model="drawers.addAttr.form" label-width="auto" style="max-width: 600px">
        <el-form-item label="标识">
          <el-input v-model="drawers.addAttr.form.code"/>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="drawers.addAttr.form.name"/>
        </el-form-item>
      </el-form>
    </template>
    <template #footer>
      <div style="flex: auto">
        <el-button @click="drawers.addAttr.show=false">cancel</el-button>
        <el-button type="primary" @click="drawers.addAttr.confirm">confirm</el-button>
      </div>
    </template>
  </el-drawer>
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
import type {Action} from 'element-plus'
import 'element-plus/es/components/message-box/style/css'
import 'element-plus/es/components/message/style/css'

interface IDrawersContent extends Record<any, any> {
  show: boolean;

  beforeClose(done: () => void): void;

  confirm(): void;
}

const drawers = ref({} as Record<string, IDrawersContent>);

function addDrawer(name: string, fun: (arg: string) => IDrawersContent) {
  drawers.value[name] = fun(name);
}

addDrawer("addAttr", (name: string) => ({
  beforeClose(done: () => void) {
    ElMessageBox.confirm('放弃保存?', {confirmButtonText: "确定", cancelButtonText: "取消"})
        .then(() => {
          drawers.value[name].form = {}
          done()
        })
        .catch(() => {
          // catch error
        })
  },
  confirm() {
    loading.value = true
    proxy?.$http.put(`obj/attrs`)
        .then(({data}: { data: Array<Record<any, any>> }) => {
          loading.value = false
          drawers.value[name].show = false
          if (data instanceof String) {
            throw data
          }
          attrs.push(data)
        }, (error: any) => {
          console.log(name)
          loading.value = false
          ElMessage.error(error)
        })
  },
  show: false,
  form: {} as Record<any, any>
}));
const delAttr = (attrId: number) => {
  loading.value = true
  proxy?.$http.delAttr(`obj/attrs/${attrId}`)
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