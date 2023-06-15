<template>
  <el-form ref="formRef" :inline="true">

  </el-form>

</template>

<script lang="ts" setup>
import {ref, reactive, onActivated, onMounted, getCurrentInstance, ComponentInternalInstance} from 'vue'


import type {FormInstance} from 'element-plus'

const {proxy} = getCurrentInstance() as ComponentInternalInstance
const loading = ref(true), formRef = ref<FormInstance>()
onActivated(() => {
  console.log("onActivated")
})
onMounted(() => {
  console.log("onMounted")
})

</script>

<style scoped>

</style>