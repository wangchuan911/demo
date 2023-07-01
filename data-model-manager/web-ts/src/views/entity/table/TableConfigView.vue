<template>
  <div>
    <el-row justify="start">
      <el-col :offset=22 :span="2">
        <div class="grid-content ep-bg-purple"/>
        <el-button type="primary" @click="editInstance.open = true">
          new
        </el-button>
      </el-col>
    </el-row>
    <el-table :data="datas" style="width: 100%" height="calc(100vh - 300px)">
      <el-table-column prop="id" label="ID" width="180"/>
      <el-table-column prop="code" label="字段"/>
      <el-table-column prop="name" label="描述"/>
      <el-table-column prop="type" label="类型"/>
      <el-table-column fixed="right" label="操作">
        <template #default="scope">
          <el-button link type="primary" size="small" @click="tableEdit(scope.row)">Edit</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-drawer
        :z-index="1001"
        v-model="editInstance.open"
        title="编辑"
        size="50%" :append-to-body="true"
        :before-close="handleClose"
    >
      <el-form ref="formRef">
        <el-form-item label="字段">
          <el-input v-model="editTarget.code"/>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="editTarget.name"/>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="editTarget.type" class="m-2" placeholder="Select">
            <el-option
                v-for="item in typeOptions"
                :key="item.id"
                :label="item.code"
                :value="item.id"
            />
          </el-select>
        </el-form-item>

      </el-form>
      <!--      <p>_(:зゝ∠)_</p>-->
      <div style="position: relative">
        <div style="position: absolute;right: 0;top: 0">
          <el-button>确定</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script lang="ts" setup>
import {ref, reactive, watch, onActivated, onMounted, getCurrentInstance, ComponentInternalInstance} from 'vue'

import 'element-plus/es/components/message/style/css'
import 'element-plus/es/components/message-box/style/css'
import 'element-plus/es/components/drawer/style/css'
import {ElMessageBox, ElMessage} from 'element-plus'
import type {FormInstance} from 'element-plus'
import {useRouter, onBeforeRouteUpdate, useRoute, RouteLocationNormalizedLoaded} from "vue-router";
import {TabPathInfo} from "@/views/MainIndex.vue";

const {proxy} = getCurrentInstance() as ComponentInternalInstance
const loading = ref(true), formRef = ref<FormInstance>(), route = useRoute()
onActivated(() => {
  console.log(route.params)
  console.log("onActivated")
})

export declare interface Colum {
  id: string
  code: string
  name: string
}

const typeOptions = ref(new Array<{ id: string, code: string }>(
    {id: "VARCHAR", code: "字符型"},
    {id: "INTEGER", code: "整形"},
    {id: "DATE", code: "日期"},
    {id: "TIMESTAMP", code: "时间"}))
const editTarget = ref({});
const datas = reactive(new Array<Colum>())
onMounted(() => {
  console.log(route.params)
  console.log("onMounted")
  proxy?.$http.get(`database/table/${route.params.id}`)
      .then(value => {
        loading.value = false;
        console.log(value.data)

        datas.push(...value.data.columns.sort((a: any, b: any) => {
          return a.id > b.id ? 1 : -1;
        }));
      }, reason => {
        ElMessage({
          showClose: true,
          message: reason.toString(),
          type: 'error',
        })
        loading.value = false;
      })
})
watch(route.params, (value, oldValue) => {
  console.log("route", value, oldValue)
})/*
onBeforeRouteUpdate(async (to, from) => {
  //仅当 id 更改时才获取用户，例如仅 query 或 hash 值已更改
  if (to.params.id !== from.params.id) {
    console.log(to.params, from.params)
  }
})*/
const editInstance = ref({open: false, data: {}})
const tableEdit = (row: any) => {
  editInstance.value.open = true
  editTarget.value = row;
}, handleClose = (done: () => void) => {
  ElMessageBox.confirm('放弃修改?').then(value => {
    done()
    editTarget.value = {}
  })
}

</script>

<style scoped>

</style>