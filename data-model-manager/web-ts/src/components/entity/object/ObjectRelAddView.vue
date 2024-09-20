<template>
  <el-table :data="attrs" style="width: 100%" border v-loading="loading" max-height="calc(100vh - 197px)" row-key="id"
            :lazy="lazy"
            :load="expand"
            :default-expand-all="!lazy"
            :tree-props="lazy?{ children: 'children', hasChildren: 'hasChildren' }:{}">
    <el-table-column prop="object.name" label="描述"/>
    <el-table-column prop="object.code" label="标识"/>
    <el-table-column prop="object.name" label="对象描述"/>
    <el-table-column prop="object.code" label="对象标识"/>
    <el-table-column prop="object.typeDesc" label="对象类型"/>
    <el-table-column prop="typeDesc" label="关联方式"/>
    <el-table-column prop="instanceId" label="对象实例ID"/>
    <el-table-column>
      <template #header>
        <el-button type="primary" size="small" @click="delAttr(scope.row.id)">新增下级</el-button>
      </template>
      <template #default="scope">
        <el-button link type="primary" size="small" @click="delAttr(scope.row.id)">新增下级</el-button>
        <el-button link type="primary" size="small" @click="delAttr(scope.row.id)">删除</el-button>
      </template>
    </el-table-column>
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
} from 'vue';

</script>

<style scoped>

</style>