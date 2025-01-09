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
        <!--        <el-switch
                    v-model="scope.row.toMapper"
                    size="small" inline-prompt
                    active-text="使用映射表"
                    inactive-text="自定义映射"
                />-->
        <el-button type="danger" link @click="()=>del('row',scope.$index)">删除</el-button>
        <el-button type="primary" link>限制条件</el-button>
      </template>
    </el-table-column>
  </el-table>

  <el-dialog
      v-model="dialog.obj.show"
      title="选择对象"
      width="500"
  >
    <!--    <span>This is a message</span>-->
    <el-select
        v-model="options"
        filterable
        remote
        placeholder="Please enter a keyword"
        :remote-method="queryObj"
        :loading="dialog.obj.loading"
        style="width: 240px"
    >
      <el-option
          v-for="item in options"
          :key="item.value"
          :label="item.label"
          :value="item.value"
      />
    </el-select>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialog.obj.show = false">Cancel</el-button>
        <el-button type="primary" @click="dialog.obj.show = false">
          Confirm
        </el-button>
      </div>
    </template>
  </el-dialog>

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
import MySelect from "@/components/form/input/MySelect.vue";
import {MyOption} from "@/components/form/config";

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
      dialog.obj.show = true;
      rows.value.push({test: "test"});
      break;
  }
}
const dialog = reactive({
  obj: {
    show: false,
    loading: false
  }
});

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
const options: any[] = reactive([]);

const queryObj = async (query: string) => {
  if (query) {
    if (options.find(option => (option.label || "").toUpperCase().indexOf((query || "").toUpperCase()) >= 0) != null) {
      return
    }
    dialog.obj.loading = true;
    const {data}: { data: { list: Array<Record<any, any>> } } = await $http.post(`obj`, {
      data: {code: query, typeId: 1001},
      page: {page: 1, size: 100},
      query: 'objectSearch',
    });
    dialog.obj.loading = false;
    options.length = 0;
    options.push(...(data?.list || []).map(v => ({value: v.id, label: `[${v.name}]${v.code}`} as any)));
  }
}
</script>

<style scoped>

</style>