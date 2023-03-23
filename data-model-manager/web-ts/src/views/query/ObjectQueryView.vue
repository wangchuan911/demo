<template>
  <el-table :data="datas" style="width: 100%">
    <template v-for="header0 in headers" :key="header0.id">
      <el-table-column v-if="header0.children==null" :prop="'H'+header0.id" :label="header0.name"
                       width="150"></el-table-column>
      <el-table-column v-else-if="header0.children!=null" :label="header0.name">
        <template v-for="header1 in header0.children" :key="header1.id">
          <el-table-column v-if="header1.children==null" :prop="'H'+header1.id" :label="header1.name"
                           width="150"></el-table-column>
          <el-table-column v-else-if="header1.children!=null" :label="header1.name">
            <template v-for="header2 in header1.children" :key="header2.id">
              <el-table-column v-if="header2.children==null" :prop="'H'+header2.id" :label="header2.name"
                               width="150"></el-table-column>
              <el-table-column v-else-if="header2.children!=null" :label="header2.name">

              </el-table-column>
            </template>
          </el-table-column>
        </template>
      </el-table-column>
    </template>
  </el-table>
</template>

<script lang="ts" setup>
import {ref, reactive, getCurrentInstance, ComponentInternalInstance} from 'vue'

const datas = reactive([]), headers = reactive([] as Array<Record<any, any>>)
const {proxy} = getCurrentInstance() as ComponentInternalInstance
const queryId = 1
proxy?.$http.get(`query/header/${queryId}`).then(({data}) => {
  console.log(data)
  headers.push(...data)
})
</script>

<style scoped>

</style>