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
            <el-button :icon="Search" @click="search">查询</el-button>
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
            <el-checkbox v-model="input.checked" size="large"/>
            <a class="el-operator">&nbsp;&nbsp;{{ input.name }}&nbsp;&nbsp;
              <el-dropdown style="margin-top:10px">
                <!--              <span class="el-operator">
                                {{ input.operatorName }}
                                <el-icon class="el-icon&#45;&#45;right"><arrow-down /></el-icon>
                              </span>-->
                <el-button type="primary" size="small">
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
                          :label="item.id" v-if="item.type.indexOf(input.type)>=0" @click="()=>{input.setOperator(item.id);}">{{ item.name }}
                      </el-dropdown-item>
                    </template>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              &nbsp;&nbsp;
              <template v-if="input=='text'">
                <template v-if="input instanceof ObjectFilterInputItem">

                </template>
                <el-input v-model="input.value" style="width: 240px" placeholder="Please input"/>
              </template>
              <template v-if="input=='text'">
                <el-input v-model="input.value" style="width: 240px" placeholder="Please input"/>
              </template>

            </a>
          </el-tag>
          <el-button class="button-new-tag" size="small">
            <el-dropdown>
            <span class="el-dropdown-link">
              + New Tag
            </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :disabled="item.checked" v-for="item in allInputs" :key="item.id" @click="addFilter(item)">{{ item.name }}</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </el-button>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script lang="ts" setup>
import {ComponentCustomProperties, ComponentInternalInstance, getCurrentInstance, reactive} from 'vue';
import {FilterOperators, InputType, OperatorType, SearchFilterInputItem} from "@/components/form/config";
import {
  ArrowDown,
  Check,
  CircleCheck,
  CirclePlus,
  CirclePlusFilled,
  Plus,
} from '@element-plus/icons-vue';
const {proxy} = getCurrentInstance() as ComponentInternalInstance;
const {$http} = proxy as ComponentCustomProperties;
const filter = reactive({
  enable: false,
  inputs: new Array<SearchFilterInputItem>()
})
const search = () => {
  console.log(1)
}

const allInputs = reactive<Array<SearchFilterInputItem>>([
  new SearchFilterInputItem(1, "key1", "输入项1", InputType.text, OperatorType.equal),
  new SearchFilterInputItem(2, "key2", "输入项2", InputType.int, OperatorType.equal),
  new SearchFilterInputItem(3, "key3", "输入项3", InputType.decimal, OperatorType.equal),
  new SearchFilterInputItem(4, "key4", "输入项4", InputType.time, OperatorType.range),
  new SearchFilterInputItem(5, "key5", "输入项5", InputType.boolean, OperatorType.true),
]);
const operators = reactive(FilterOperators);


const addFilter = (item: SearchFilterInputItem) => {
  if (item.checked) {
    return;
  }
  item.checked = true;
  filter.inputs.push(item)
}

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
</style>