<template>
  <div>
    <div style="display: flex;margin: 5px 1px;height:32px ">
      <el-popover
          placement="bottom"
          title="SQL"
          :width="200"
          trigger="click"
          :content="sql"
          @show="showSql"
      >
        <template #reference>
          <el-button type="primary">查看SQL</el-button>
        </template>
      </el-popover>
    </div>

    <el-table :data="attrs" style="width: 100%" border v-loading="loading" max-height="calc(100vh - 197px)" row-key="id"
              :lazy="lazy"
              :load="expand"
              :default-expand-all="!lazy"
              :tree-props="lazy?{ children: 'children', hasChildren: 'hasChildren' }:{}">
      <el-table-column prop="object.name" label="对象描述"/>
      <el-table-column prop="object.code" label="对象标识"/>
      <el-table-column prop="object.typeDesc" label="对象类型"/>
      <el-table-column prop="typeDesc" label="关联方式"/>
      <el-table-column prop="instanceId" label="对象实例ID"/>
      <el-table-column>
        <template #default="scope">
          <!--<el-button
              link
              type="primary"
              size="small"
              @click.prevent="deleteRow(scope.$index)"
          >
            Remove
          </el-button>-->
          <el-dropdown v-if="scope.row._flag && scope.row.typeId==3006">
            <span class="el-dropdown-link">
              操作<el-icon class="el-icon--right"><arrow-down/></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item :icon="Plus" @click.prevent="operation(0,scope.row)">添加关联对象</el-dropdown-item>
                <!--<el-dropdown-item :icon="CirclePlusFilled" @click.prevent="operation(2,scope.row)">
                  Action 2
                </el-dropdown-item>
                <el-dropdown-item :icon="CirclePlus" @click.prevent="operation(3,scope.row)">Action 3</el-dropdown-item>
                <el-dropdown-item :icon="Check" @click.prevent="operation(4,scope.row)">Action 4</el-dropdown-item>
                <el-dropdown-item :icon="CircleCheck" @click.prevent="operation(5,scope.row)">Action 5</el-dropdown-item>-->
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-dropdown v-else-if="!scope.row._flag">
            <span class="el-dropdown-link">
              操作<el-icon class="el-icon--right"><arrow-down/></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item :icon="CirclePlusFilled" @click.prevent="operation(1,scope.row)"
                                  v-if="stringLike(scope.row.type,'SqlToJoin')">
                  添加关联对象
                </el-dropdown-item>
                <el-dropdown-item :icon="CirclePlus" @click.prevent="operation(2,scope.row)">添加关联关系</el-dropdown-item>
                <el-dropdown-item :icon="Check" @click.prevent="operation(3,scope.row)"
                                  v-if="!stringLike(scope.row.type,'SqlToJoin')">修改关联关系
                </el-dropdown-item>
                <el-dropdown-item :icon="CircleCheck" @click.prevent="operation(4,scope.row)">删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>
  </div>
  <el-drawer v-model="addLink.show" :title="addLink.name" size="50%" show-close
             :before-close="(done)=>addLink.beforeClose(done)">
    <template #default>
      <!--<el-form :model="addLink.form" label-width="auto" style="max-width: 600px">
        &lt;!&ndash;<el-form-item label="标识">
          <el-input v-model="addLink.form.code"/>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="addLink.form.name"/>
        </el-form-item>&ndash;&gt;
        <el-form-item :label="input.label" v-for="input in addLink.content.inputs" :key="input.code" :index="input.code">
          <component :is="input.comp" v-bind="input.prop" v-model="addLink.content.form[input.code]"></component>
        </el-form-item>
      </el-form>-->
      <my-form-container v-model="addLink.content"></my-form-container>
    </template>
    <template #footer>
      <div style="flex: auto">
        <el-button @click="addLink.show=false">cancel</el-button>
        <el-button type="primary" @click="addLink.confirm()">confirm</el-button>
      </div>
    </template>
  </el-drawer>
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
  ArrowDown,
  Check,
  CircleCheck,
  CirclePlus,
  CirclePlusFilled,
  Plus,
} from '@element-plus/icons-vue';

const attrs = reactive(new Array<Record<any, any>>());
const {proxy} = getCurrentInstance() as ComponentInternalInstance;
const {$http} = proxy as ComponentCustomProperties;
const loading = ref(false);
const props = defineProps<{ id: number }>();
const lazy = ref(false);
const load = (id: number) => {
  loading.value = true;
  $http.get(`obj/combination/${id}`)
      .then(({data}: { data: Array<Record<any, any>> }) => {
        attrs.length = 0;
        console.log(data);
        const setFlag = (dat: Record<any, any>, flag: boolean) => {
          dat._flag = (dat.id < 0 || flag);
          (dat.children || []).forEach((dat2: Record<any, any>) => setFlag(dat2, dat._flag));
        };
        data.forEach(value => setFlag(value, false));
        attrs.push(...data);

      })
      .then(() => {
        loading.value = false;
      }, () => {
        loading.value = false;
      });
};
console.log(props.id);
const objectId = computed(() => props.id);
watch(objectId, (value, oldValue, onCleanup) => {
  console.log(value);
  if (value < 0) {
    loading.value = true;
  } else {
    load(value as number);
  }
});
const expand = (row: Record<any, any>,
                treeNode: unknown,
                resolve: (date: Record<any, any>[]) => void) => {
  loading.value = true;
  console.log(row);
  $http.get(`link/expand/${row.id <= -1 ? `obj${row.objectId}` : row.id}`)
      .then(({data}: { data: Record<any, any>[] }) => {
        console.log(data);
        resolve(data);
      })
      .then(() => {
        loading.value = false;
      }, () => {
        loading.value = false;
      });
};
const sql = ref(new String());
const showSql = () => {
  loading.value = false;
  $http.get(`link/show/${props.id}`)
      .then(({data}: { data: Record<any, any>[] }) => {
        console.log(data);
        sql.value = data as unknown as string;
      })
      .then(() => {
        loading.value = false;
      }, () => {
        loading.value = false;
      });
};
const operation = (type: number, row: Record<any, any>) => {
  console.log(type);
  console.log(row);
  switch (type) {
    case 1:
      addLink.value = new ObjectLinkDrawersContent(row);
      addLink.value.open(row);
      break;
    case 2:
      addLink.value = new RelLinkDrawersContent();
      addLink.value.open(row);
      break;
  }
};
import {DrawersContent, FormContent, stringLike} from "@/components/config";
import {ElMessage, ElMessageBox} from "element-plus";
import {AxiosError} from "axios";
import {EasySearchItem, InputItem, MyOption, Prop, SelectItem, TextItem} from "@/components/form/config";
import MyFormContainer from "@/components/form/MyFormContainer.vue";
import {ObjectRelItem} from "@/components/entity/object/config";

class LinkAddDrawersContent extends DrawersContent {
  name: string;
  type: number | undefined;
  data: Record<any, any> | undefined;
  content: FormContent;

  constructor() {
    super();
    this.content = new FormContent({});
    this.name = "未知操作";
  }

  open(data: Record<any, any>) {
    this.data = data;
    this._open();
    this.content.onLoaded();
  }

  beforeClose(done: () => void) {
    ElMessageBox.confirm('放弃保存?', {confirmButtonText: "确定", cancelButtonText: "取消"})
        .then(() => {
          this.content.form = {};
          done();
        })
        .catch(() => {
          // catch error
        });
  }

  confirm() {
    console.log(this.content.form);
  }

  addLink(value: any) {
    loading.value = true;
    $http.put(`link`, value)
        .then(({data}: { data: Array<Record<any, any>> }) => {
          loading.value = false;
          this._close();
          if (data instanceof String) {
            throw data;
          }
          attrs.push(data);
        }, (error: any) => {
          loading.value = false;
          this._close();
          if (error instanceof AxiosError)
            ElMessage.error(error.response?.data || error);
          else
            ElMessage.error(error);
        });
  }
}

class RelLinkDrawersContent extends LinkAddDrawersContent {
  constructor() {
    super();
    this.name = "添加关系";
    this.content.addInput(new SelectItem("code", "编码").addOptions(new MyOption("1", "1"), new MyOption("2", "2")), new TextItem("name", "名称"));
  }

}

class ObjectLinkDrawersContent extends LinkAddDrawersContent {
  constructor(row: Record<any, any>) {
    super();
    this.name = "添加对象";
    this.content.addInput(
        new SelectItem("code", "类型", (input, content) => {
          $http.get(`link/types/obj${objectId.value}`).then(({data}: { data: Array<Record<any, any>> }) => {
            input.setOptions(...data.map(v => new MyOption(v.id, v.desc)));
          });
        }),
        new TextItem("parent", "上级", (input, content) => {
          console.log(row);
          input.prop.readonly = true;
          input.prop.value = `[${row.object.code}]${row.object.name}`;
        }),
        new SelectItem("object", "对象", (input, content) => {
          input.prop.filterable = true;
          input.prop.remote = true;
          input.prop.reserveKeyword = true;
          input.prop.placeholder = "Please enter a keyword";
          input.prop.loading = false;
          input.prop.remoteMethod = (query: string) => {
            if (query) {
              input.prop.loading = true;
              /*setTimeout(() => {
                input.setOptions(new MyOption(query, query));
                input.prop.loading = false;
              }, 200);*/
              $http.get(`query/object/${objectId.value}?text=${query}`).then(({data}: { data: Array<Record<any, any>> }) => {
                input.prop.loading = false;
                input.setOptions(...data.map(v => new MyOption(v.id, v.desc)));
              });
            } else {
              input.setOptions();
            }
          };
        }),
        new ObjectRelItem("rel", "关系", (input, content) => {
          console.log("");
        })
    );
  }

}

const addLink = ref({} as LinkAddDrawersContent);
</script>

<style scoped>
.el-dropdown-link {
  cursor: pointer;
  color: var(--el-color-primary);
  display: flex;
  align-items: center;
}
</style>