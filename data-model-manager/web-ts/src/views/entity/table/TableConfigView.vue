<template>
  <div>
    <el-row justify="start">
      <el-col :span="4">
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
        <el-form-item label="字段名">
          <el-input/>
        </el-form-item>
        <el-form-item label="描述">
          <el-input/>
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

const {proxy} = getCurrentInstance() as ComponentInternalInstance
const loading = ref(true), formRef = ref<FormInstance>(), route = useRoute()
onActivated(() => {
  console.log(route.params)
  console.log("onActivated")
})
onMounted(() => {
  console.log(route.params)
  console.log("onMounted")
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
}, handleClose = (done: () => void) => {
  ElMessageBox.confirm('放弃修改?').then(value => done())
}
</script>

<style scoped>

</style>