<template>
  <el-button type="primary" size="small" @click="()=>add('col')">添加列</el-button>
  <el-button type="primary" size="small" @click="()=>add('row')">添加行</el-button>
  <el-table :data="rows" style="width: 100%" border v-loading="loading" max-height="calc(100vh - 197px)">
    <el-table-column width="150">
      <template #header>
        对象
      </template>
      <template #default="scope">
        <el-select
            v-model="scope.row.objectId"
            filterable
            remote
            placeholder="Please enter a keyword"
            :remote-method="(key)=>queryObj(key,scope.row)"
            :loading="dialog.obj.loading"
            @change="(value)=>objectChange(scope.row,value)"
            style="width: 240px"
        >
          <el-option
              v-for="item in scope.row.objs"
              :key="item.value"
              :label="item.label"
              :value="item.value"
          />
        </el-select>
      </template>
    </el-table-column>

    <el-table-column width="150">
      <template #header>
        当前属性
      </template>
      <template #default="scope">
        <el-select v-model="scope.row.mapper.current">
          <el-option v-for="(item,index) in scope.row.attrs" :key="index"
                     :label="`[${item.name}]${item.code}`"
                     :value="item.id"
          />
        </el-select>
      </template>
    </el-table-column>

    <template v-for="(col,index) in cols" v-bind:key="index">
      <el-table-column prop="{{col.code}}" label="{{col.name}}" width="200">
        <template #header>
          <el-row :gutter="10">
            <el-col :span="20">
              <el-select v-model="col.attrId">
                <el-option v-for="(item,index) in attrs" :key="index"
                           :label="`[${item.name}]${item.code}`"
                           :value="item.id"
                />
              </el-select>
              <!--<el-icon>
                <Close @click="()=>del('col',index)" style="color: red"/>
              </el-icon>-->

            </el-col>
            <el-col :span="4">
              <el-button style="width: 20px" type="danger" link @click="()=>del('col',index)">删除</el-button>
            </el-col>
          </el-row>

        </template>
        <template #default="scope">
          <el-select v-model="scope.row.mapper[index]">
            <el-option v-for="(item,index) in scope.row.attrs" :key="index"
                       :label="`[${item.name}]${item.code}`"
                       :value="item.id"
            />
          </el-select>
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

  <!--<el-dialog
      v-model="dialog.obj.show"
      title="选择对象"
      width="500"
  >
    &lt;!&ndash;    <span>This is a message</span>&ndash;&gt;
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
  </el-dialog>-->

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

const values = defineModel<Record<any, any>>();
const {proxy} = getCurrentInstance() as ComponentInternalInstance;
const {$http} = proxy as ComponentCustomProperties;
const props = defineProps<{ cols: Array<Record<any, any>>, rows: Array<Record<any, any>>, objectId: number, dels: Array<string[]> }>();
const cols = computed(() => props.cols);
const rows = computed(() => props.rows);
const dels = computed(() => props.dels);
const objectId = computed(() => props.objectId);
watch(cols, (value, oldValue, onCleanup) => {
  console.log(value);
});
watch(rows, (value, oldValue, onCleanup) => {
  console.log(value);
});
console.log(values.value)
console.log(cols)
console.log(rows)
console.log(objectId)

const attrs: Array<Record<any, any>> = reactive([]);
const setAttr = (attrs: Array<Record<any, any>>, value: number) => {
  $http.get(`obj/attrs/${value}`)
      .then(({data}: { data: Array<Record<any, any>> }) => {
        attrs.length = 0;
        attrs.push(...data);
      })
      .then(() => {
        // loading.value = false;
      }, () => {
        // loading.value = false;
      });
};
watch(objectId, (value, oldValue, onCleanup) => {
  console.log(value);
  if (isNaN(value) || objectId.value < 0) {
    return;
  }
  setAttr(attrs, value);

});
const initValue = () => {
  if (!values.value) {
    values.value = {cols, rows, dels};
  }
}
initValue();
const add = (key: string) => {
  initValue();
  switch (key) {
    case "col":
      cols.value.push({});
      break;
    case "row":
      // dialog.obj.show = true;
      rows.value.push({mapper: {}, attrs: [], objs: []});
      break;
  }
};
const dialog = reactive({
  obj: {
    show: false,
    loading: false
  }
});

const del = (key: string, index: number) => {
  switch (key) {
    case "col": {
      const linkId = cols.value[index].linkId;
      if (linkId != null) {
        dels.value.push(["col", "link", linkId]);
      }
      cols.value.splice(index, 1);
      rows.value.forEach(row => {
        delete row.mapper[index];
        let idx = index + 1;
        while (row.mapper[idx] != null) {
          row.mapper[idx - 1] = row.mapper[idx];
          delete row.mapper[idx];
          idx++;
        }
      });
    }
      break;
    case "row": {
      const seq = rows.value[index].dataIndex
      if (seq != null) {
        dels.value.push(["row", "seq", seq])
      }
      rows.value.splice(index, 1);
    }
      break;
  }

};

const queryObj = async (query: string, row: any) => {
  if (query) {
    const options: Array<any> = row.objs;
    if (options.find(option => (option.label || "").toUpperCase().indexOf((query || "").toUpperCase()) >= 0) != null) {
      return;
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
};

async function objectChange(row: any, value: any) {
  console.log(value);
  setAttr(row.attrs, value);
  row.mapper = {};
}

</script>

<style scoped>

</style>