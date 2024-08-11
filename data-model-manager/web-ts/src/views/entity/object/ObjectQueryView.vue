<template>
  <div v-loading="loading" style="position: absolute">
    <el-form ref="formRef" :inline="true" :model="formModel">
      <el-form-item label="table name" prop="name">
        <el-input v-model="formModel.name"/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submitForm(formRef)">Query</el-button>
        <el-button @click="resetForm(formRef)">Reset</el-button>
      </el-form-item>
    </el-form>
    <el-table :data="datas" style="width: 100%" height="calc(100vh - 300px)">
      <el-table-column prop="id" label="ID" width="180"/>
      <el-table-column prop="code" label="表名"/>
      <el-table-column prop="name" label="描述"/>
      <el-table-column prop="typeDesc" label="类型"/>
      <el-table-column fixed="right" label="操作">
        <template #default="scope">
          <el-button link type="primary" size="small" @click="tableEdit(scope.row)">Edit</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div style="width: 100%;position: relative">
      <div style="position: absolute;right: 0">
        <el-pagination
            v-model:current-page="page.page"
            v-model:page-size="page.size"
            :small="small"
            :disabled="disabled"
            :background="background"
            layout="prev, pager, next, jumper"
            :total="page.total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>

import {useRouter, onBeforeRouteUpdate, useRoute, RouteLocationNormalizedLoaded} from "vue-router";
import {ref, reactive, onActivated, onMounted, getCurrentInstance, ComponentInternalInstance} from 'vue'

import type {FormInstance} from 'element-plus';
import {ElMessage} from 'element-plus';

const router = useRouter(), datas = reactive(new Array<{ id: string, name: string, desc?: string }>()),
    page = reactive({page: 1, total: 0, size: 100})
const {proxy} = getCurrentInstance() as ComponentInternalInstance
const loading = ref(false), formRef = ref<FormInstance>(), formModel = reactive({name: ""})
onActivated(() => {
  console.log("onActivated")
})
onMounted(() => {
  console.log("onMounted")
})

const resetForm = (formEl: FormInstance | undefined) => {
  if (!formEl) return
  formEl.resetFields()
}, submitForm = (formEl: FormInstance | undefined) => {
  console.log(formEl)
  handleCurrentChange(1);
}, handleCurrentChange = (pageNumber: number) => {
  loading.value = true;
  proxy?.$http.post(`obj`, {
    data: formModel,
    page: {page: pageNumber, size: page.size}
  }).then(value => {
    loading.value = false;
    console.log(value.data)
    datas.length = 0;
    page.page = pageNumber;
    page.total = value.data.total;
    datas.push(...value.data.list);
  }, reason => {
    ElMessage({
      showClose: true,
      message: reason.toString(),
      type: 'error',
    })
    loading.value = false;
  })
}, tableEdit = (row: any) => {
  router.push({path: `/index/object-detail/${row.id}`})
}
</script>

<style scoped>

</style>