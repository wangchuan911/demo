<template>
  <div>
    <el-row>
      <el-col :span="24">
        <el-input
            v-model="input3"
            placeholder="Please input"
            class="input-with-select"
        >
          <template #prepend>
            <el-button :icon="filter.enable?ArrowDown:ArrowRight"
                       @click="()=>{console.log(filter.enable);filter.enable = !filter.enable;}">过滤
            </el-button>
          </template>
          <template #append>
            <el-button :icon="Search" @click="search(1)">查询</el-button>
          </template>
        </el-input>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="24" v-if="filter.enable">
        <div style="width: inherit;text-align: left">
          <el-tag
              v-for="(input,index) in filter.inputs"
              :key="index"
              size="large"
              closable
              :disable-transitions="false"
              @close="()=>{filter.inputs.splice(index,1);input.checked=false}"
          >
            <a class="el-operator" style="display: inline-block;height: 32px">
              <el-checkbox v-model="input.enable" size="large" style="height: 32px"/>
              &nbsp;&nbsp;{{ input.name }}&nbsp;&nbsp;
              <el-dropdown style="height: inherit">
                <!--              <span class="el-operator">
                                {{ input.operatorName }}
                                <el-icon class="el-icon&#45;&#45;right"><arrow-down /></el-icon>
                              </span>-->
                <el-button type="primary" size="small" style="margin-top:5px">
                  {{ input.operator.val }}
                  <el-icon class="el-icon--right">
                    <arrow-down/>
                  </el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <template v-for="(item,index) in operators">
                      <el-dropdown-item
                          :key="index"
                          :label="item.id" v-if="item.type.indexOf(input.type)>=0"
                          @click="()=>{input.setOperator(item.id);}">{{ item.name }}
                      </el-dropdown-item>
                    </template>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              &nbsp;
              <el-popover
                  placement="top" :width="300"
              >
                <template #reference>
                  <el-link v-if="input.value == '' || input.value == null" type="danger">未输入条件</el-link>
                  <el-link v-else type="success" style="min-width: 70px;">{{input.value}}</el-link>
                </template>
                <div>
                  <template v-if="input.operator.key=='range'">
                    <template v-if="input.type=='int'">
                      <div style="margin-bottom: 10px;margin-top: 10px">
                        <div style="display: inline-block">开始:</div>
                        <el-input-number style="width: 240px" v-model="input.value[0]"
                                         class="filter-item-input"/>
                      </div>
                      <div style="margin-bottom: 10px;">
                        <div style="display: inline-block">结束:</div>
                        <el-input-number style="width: 240px" v-model="input.value[1]"
                                         class="filter-item-input"/>
                      </div>
                    </template>
                    <template v-if="input.type=='decimal'">
                      <div style="margin-bottom: 10px;margin-top: 10px">
                        <div style="display: inline-block">开始:</div>
                        <el-input-number style="width: 240px" v-model="input.value[0]"
                                         class="filter-item-input" :precision="2"/>
                      </div>
                      <div style="margin-bottom: 10px;">
                        <div style="display: inline-block">结束:</div>
                        <el-input-number style="width: 240px" v-model="input.value[1]"
                                         class="filter-item-input" :precision="2"/>
                      </div>
                    </template>
                  </template>
                  <template v-else>
                    <el-input-number v-if="input.type=='int'" v-model="input.value" class="filter-item-input"/>
                    <el-input-number v-if="input.type=='decimal'" v-model="input.value" :precision="2"
                                     class="filter-item-input"/>
                  </template>
                  <el-input v-if="input.type=='text'" v-model="input.value"
                            class="filter-item-input"
                            placeholder="Please input"/>
                </div>
              </el-popover>


            </a>
          </el-tag>
          <el-button class="button-new-tag" size="small">
            <el-dropdown>
            <span class="el-dropdown-link">
              + New Tag
            </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :disabled="item.checked" v-for="item in allInputs" :key="item.id"
                                    @click="addFilter(item)">{{ item.name }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </el-button>
        </div>
      </el-col>
    </el-row>
    <el-table :data="tableData" style="width: 100%;height: calc(100vh - 300px)" v-elTableScroll="pageNext"
              ref="table">
      <el-table-column prop="date" label="Date" width="180"/>
      <el-table-column prop="name" label="Name" width="180"/>
      <el-table-column prop="address" label="Address"/>
      <template #append>
        <div v-if="pager.loading">加载中...</div>
      </template>
    </el-table>
  </div>
</template>

<script lang="ts" setup>
import {
  ComponentCustomProperties,
  ComponentInternalInstance,
  computed,
  defineProps,
  getCurrentInstance,
  reactive, watch,
  ref
} from 'vue';
import {FilterOperators, InputType, OperatorType, SearchFilterInputItem} from "@/components/form/config";
import {
  ArrowDown,
  Check,
  CircleCheck,
  CirclePlus,
  CirclePlusFilled,
  Plus,
} from '@element-plus/icons-vue';
import {TableInstance} from "element-plus";
import Directives from "@/config/Directives";

const {proxy} = getCurrentInstance() as ComponentInternalInstance;
const {$http} = proxy as ComponentCustomProperties;
const filter = reactive({
  enable: false,
  inputs: new Array<SearchFilterInputItem>()
});

const allInputs = reactive<Array<SearchFilterInputItem>>([
  new SearchFilterInputItem(1, "key1", "输入项1", InputType.text, OperatorType.equal),
  new SearchFilterInputItem(2, "key2", "输入项2", InputType.int, OperatorType.equal),
  new SearchFilterInputItem(3, "key3", "输入项3", InputType.decimal, OperatorType.equal),
  new SearchFilterInputItem(4, "key4", "输入项4", InputType.time, OperatorType.range),
  new SearchFilterInputItem(5, "key5", "输入项5", InputType.boolean, OperatorType.true),
]);
const operators = reactive(FilterOperators);
const props = defineProps<{ id: number }>();
console.log(props.id);
const objectId = computed(() => props.id);
// watch(objectId, (value, oldValue, onCleanup) => {
//   console.log(value);
//   if (value < 0) {
//     loading.value = true;
//   } else {
//     load(value as number);
//   }
// });
const pager = reactive({page: 1, size: 20, loading: false, nomore: false});
const search = async (page: number) => {
  pager.page = page;
  pager.nomore = false;
  console.log(1);
  const params: any = {};
  allInputs.filter((item) => item.checked).forEach(item => {
    params[item.code] = item.value;
  });
  console.log(params);
  pager.loading = true;
  try {
    const {data} = await $http.post(`obj/template/query/${objectId.value}`, {params, pager});
    pager.nomore = (data.length < pager.size);
    if (page == 1) {
      tableData.length = 0;
    }
    tableData.push(...data);
  } finally {
    pager.loading = false;
  }
  if (Directives.TableScrollConfig.canLoadMore(table.value.$el)) {
    await search(page + 1);
  }
};
const table = ref<TableInstance>();
const tableData = reactive<Array<any>>([]);
const pageNext = () => {
  if (pager.nomore) {
    return;
  }
  search(pager.page + 1);
};

const addFilter = (item: SearchFilterInputItem) => {
  if (item.checked) {
    return;
  }
  item.checked = true;
  filter.inputs.push(item);
};

</script>

<style scoped>
.el-operator {
  --el-tag-font-size: 12px;
  --el-tag-border-radius: 4px;
  --el-tag-border-radius-rounded: 9999px;
  /*align-items: center;*/
  /*background-color: var(--el-tag-bg-color);*/
  /*border-color: var(--el-tag-border-color);*/
  /*border-radius: var(--el-tag-border-radius);*/
  /*border-style: solid;*/
  /*border-width: 1px;*/
  /*box-sizing: border-box;*/
  color: var(--el-tag-text-color);
  /*display: inline-flex;*/
  font-size: var(--el-tag-font-size);
  height: 24px;
  /*justify-content: center;*/
  /*line-height: 1;*/
  /*padding: 0 9px;*/
  /*vertical-align: middle;*/
  /*white-space: nowrap;*/
  /*--el-icon-size: 14px;*/
}

.filter-item-input {
  width: 270px;
  height: 26px;
  margin-top: -6px;
}
</style>