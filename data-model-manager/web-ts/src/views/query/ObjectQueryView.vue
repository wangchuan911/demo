<template>
  <div style="width: 100%;height: 100%">
    <el-table :data="datas" :border="true" style="width: 100%;height: calc(100% - 50px)" height="100%">
      <template v-for="header0 in headers" :key="header0.id">
        <el-table-column v-if="header0.children==null" :prop="`H${header0.id}`" :label="header0.name"/>
        <el-table-column v-else-if="header0.children!=null" :label="header0.name">
          <template v-for="header1 in header0.children" :key="header1.id">
            <el-table-column v-if="header1.children==null" :prop="`H${header0.id}_H${header1.id}`"
                             :label="header1.name"/>
            <el-table-column v-else-if="header1.children!=null" :label="header1.name">
              <template v-for="header2 in header1.children" :key="header2.id">
                <el-table-column v-if="header2.children==null" :prop="`H${header0.id}_H${header1.id}_H${header2.id}`"
                                 :label="header2.name"/>
                <el-table-column v-else-if="header2.children!=null" :label="header2.name">
                  <template v-for="header3 in header2.children!=null" :key="header3.id">
                    <el-table-column v-if="header3.children==null"
                                     :prop="`H${header0.id}_H${header1.id}_H${header2.id}_H${header3.id}`"
                                     :label="header3.name"/>
                  </template>
                </el-table-column>
              </template>
            </el-table-column>
          </template>
        </el-table-column>
      </template>
    </el-table>
    <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :small="false"
        :disabled="false"
        :background="false"
        layout="prev, pager, next, jumper"
        :total="1000"
        @size-change="handleSizeChange"
        @current-change="nextPage"
    />
  </div>
</template>

<script lang="ts" setup>
import {ref, reactive, getCurrentInstance, ComponentInternalInstance} from 'vue'

const datas = reactive(new Array<Record<any, any>>()), headers = reactive([] as Array<Record<any, any>>)
const {proxy} = getCurrentInstance() as ComponentInternalInstance
const queryId = 1
proxy?.$http.get(`query/header/${queryId}`).then(({data}) => {
  console.log(data)
  headers.push(...data)
})
const pageSize = ref(50)
const currentPage = ref(1)


const nextPage = (number: number) => {
  console.log(number)
  let i = 50;
  datas.length = 0
  while (i--) {
    datas.push({H1: `XXXX-${(currentPage.value - 1) * 50 + (50 - i)}`, H2_: ""})
  }
}
nextPage(pageSize.value)
</script>

<style scoped>

</style>