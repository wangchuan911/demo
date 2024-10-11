<template>
  <el-table :data="list" style="width: 100%" border v-loading="loading" max-height="calc(100vh - 197px)" row-key="_id"
            :lazy="false"
            :default-expand-all="true">
    <el-table-column prop="item.instanceId" label="对象实例ID"/>
    <el-table-column label="对象">
      <template #default="scope">
        <el-select v-model="scope.row.itemIndex" @change="(value)=>scope.row.item=objects[value]">
          <el-option v-for="(item,index) in objects" :key="index"
                     :label="`[${item.object.name}]${item.object.code}`"
                     :value="index"
          />
        </el-select>
      </template>
    </el-table-column>
    <el-table-column prop="item.object.typeDesc" label="对象类型"/>
    <el-table-column prop="item.typeDesc" label="关联方式">
      <template #default="scope">
        <el-select v-model="scope.row.itemIndex" @change="(value)=>scope.row.item=objects[value]">
          <el-option v-for="(item,index) in objects" :key="index"
                     :label="`[${item.object.name}]${item.object.code}`"
                     :value="index"
          />
        </el-select>
      </template>
    </el-table-column>
    <el-table-column>
      <template #header>
        <el-button type="primary" size="small" @click="add(null,null)">新增下级</el-button>
      </template>
      <template #default="scope">
        <el-button link type="primary" size="small" @click="add(scope.row)">新增下级</el-button>
        <el-button link type="primary" size="small" @click="del(scope.row._id)">删除</el-button>
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

const list = defineModel<Array<any>>();
if (typeof (list.value) == 'undefined') {
  list.value = [];
}
const props = defineProps<{ objects: Array<any> }>();
const objects = computed(() => props.objects);
watch(objects, (value, oldValue, onCleanup) => {
  console.log(value);
});
console.log(list);
console.log(objects);
const getIndex = () => {
  return new Date().valueOf();
};
const add = (row: Record<any, any>) => {
  if (row) {
    row.children = row.children || [];
    row.children.push(createItem());
  } else {
    list.value?.push(createItem());
  }
};
const createItem = (def = {}) => {
  return {_id: getIndex(), item: {}, ...def};
};
const del = (_id: number, _list: Array<any>): boolean => {
  _list = _list || list.value;
  let row;
  for (let i = 0; i < _list.length; i++) {
    row = _list[i];
    if (row._id == _id) {
      _list.splice(i, 1);
      return true;
    } else if (row.children instanceof Array && del(_id, row.children)) {
      return true;
    }
  }
  return false;
};
</script>

<style scoped>

</style>