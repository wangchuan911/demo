<template>
  <el-table :data="list" style="width: 100%" border v-loading="loading" max-height="calc(100vh - 197px)" row-key="_id"
            :lazy="false"
            :default-expand-all="true">
    <el-table-column prop="level" label="层级" width="160"/>
    <el-table-column label="对象">
      <template #default="scope">
        <div style="display: flex">
          <div style="flex: 1">
            <el-select v-model="scope.row.itemIndex" @change="(value)=>objectChange(value,scope.row)">
              <el-option v-for="(item,index) in objects" :key="index"
                         :label="`[${item.instanceId}] ${item.object.code} [${item.object.name}]`"
                         :value="index"
              />
            </el-select>
          </div>
          <div style="flex: 1">
            <el-select v-model="scope.row.attrIndex">
              <el-option v-for="(item,index) in scope.row?.item?.attrs" :key="index"
                         :label="`${item.code} [${item.name}]`"
                         :value="index"
              />
            </el-select>
          </div>
        </div>
      </template>
    </el-table-column>
    <el-table-column label="关联方式" width="200">
      <template #default="scope">
        <el-select v-model="scope.row.linkTypeId">
          <el-option v-for="item1 in linkTypes" :key="item1.value"
                     :label="item1.name"
                     :value="item1.value"
          />
        </el-select>
      </template>
    </el-table-column>
    <el-table-column width="200">
      <template #header>
        <el-button type="primary" size="small" @click="()=>add(null,null)">新增下级</el-button>
      </template>
      <template #default="scope">
        <el-button link type="primary" size="small" @click="()=>add(scope.row)">新增下级</el-button>
        <el-button link type="primary" size="small" @click="()=>del(scope.row._id)">删除</el-button>
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
const {proxy} = getCurrentInstance() as ComponentInternalInstance;
const {$http} = proxy as ComponentCustomProperties;
const props = defineProps<{ objects: Array<any>, linkTypes: Array<any> }>();
const objects = computed(() => props.objects);
const linkTypes = computed(() => props.linkTypes);
watch(objects, (value, oldValue, onCleanup) => {
  console.log(value);
});
watch(linkTypes, (value, oldValue, onCleanup) => {
  console.log(value);
});
console.log(list);
console.log(objects);
const getIndex = () => {
  return new Date().valueOf();
};
const add = (row: Record<any, any>) => {
  console.log(row, list);
  if (row) {
    row.children = row.children || [];
    row.children.push(createItem({level: row.level + 1}));
  } else if (typeof (list.value) == 'undefined') {
    list.value = [createItem({level: 1})];
  } else {
    list.value.push(createItem({level: 1}));
  }
  console.log(list.value);
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

async function objectChange(index: number, row: any) {
  row.item = objects.value[index];
  console.log(row.item);
  const {data}: { data: Array<Record<any, any>> } = await $http.get(`obj/attrs/${row.item.objectId}`);
  row.item.attrs = data;
}
</script>

<style scoped>

</style>