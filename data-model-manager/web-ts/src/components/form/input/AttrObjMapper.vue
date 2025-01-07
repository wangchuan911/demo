<template>
  <el-button type="primary" size="small" @click="()=>add('col')">添加列</el-button>
  <el-button type="primary" size="small" @click="()=>add('row')">添加行</el-button>
  <el-table :data="rows" style="width: 100%" border v-loading="loading" max-height="calc(100vh - 197px)" row-key="_id"
            :lazy="false"
            :default-expand-all="true">
    <el-table-column width="200">
      <template #header>
        对象
      </template>
      <template #default="scope">
        {{ scope.$index }}
      </template>
    </el-table-column>
    <template v-for="(col,index) in cols" v-bind:key="index">
      <el-table-column prop="{{col.code}}" label="{{col.name}}" width="160">
        <template #header>
          {{ col.name }}
          <el-icon>
            <Close @click="()=>del('col',index)" style="color: darkred"/>
          </el-icon>
        </template>
        <template #default="scope">
          {{ scope.row[col.key] }}
        </template>
      </el-table-column>
    </template>
    <el-table-column width="400">
      <template #header>
        配置
      </template>
      <template #default="scope">
        <el-switch
            v-model="scope.row.toMapper"
            size="small" inline-prompt
            active-text="使用映射表"
            inactive-text="自定义映射"
        />
        <el-button type="danger" link @click="()=>del('row',scope.$index)">删除</el-button>
        <el-button type="primary" link>限制条件</el-button>
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
import {
  Close,
} from '@element-plus/icons-vue';

const list = defineModel<Array<any>>();
const {proxy} = getCurrentInstance() as ComponentInternalInstance;
const {$http} = proxy as ComponentCustomProperties;
const props = defineProps<{ cols: Array<any>, rows: Array<any> }>();
const cols = computed(() => props.cols);
const rows = computed(() => props.rows);
watch(cols, (value, oldValue, onCleanup) => {
  console.log(value);
});
watch(rows, (value, oldValue, onCleanup) => {
  console.log(value);
});
const add = (key: string) => {
  switch (key) {
    case "col":
      cols.value.push({name: "test", key: "test"});
      break;
    case "row":
      rows.value.push({test: "test"});
      break;
  }
}
const del = (key: string, index: number) => {
  switch (key) {
    case "col":
      cols.value.splice(index, 1)
      break;
    case "row":
      rows.value.splice(index, 1)
      break;
  }

}
</script>

<style scoped>

</style>