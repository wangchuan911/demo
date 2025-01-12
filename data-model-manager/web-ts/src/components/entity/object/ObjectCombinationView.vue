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
      <el-table-column label="对象描述">
        <template #default="scope">
          {{scope.row.object?.name}}<template v-if="scope.row.object!=null && scope.row.attribute!=null">-</template>{{scope.row.attribute?.name}}
        </template>
      </el-table-column>
      <el-table-column prop="typeDesc" label="关联方式"/>
      <el-table-column label="对象标识">
        <template #default="scope">{{scope.row.object?.code}}<template v-if="scope.row.object!=null && scope.row.attribute!=null">.</template>{{scope.row.attribute?.code}}</template>
      </el-table-column>
      <el-table-column  label="对象类型">
        <template #default="scope">{{scope.row.object?.typeDesc}}<template v-if="scope.row.object!=null && scope.row.attribute!=null">-</template>{{scope.row.attribute?.typeDesc}}</template>
      </el-table-column>
      <el-table-column prop="instanceId" label="对象实例ID"/>
      <el-table-column>
        <template #header>
          <el-button type="primary" size="small" @click="()=>operation(3,null)">继承</el-button>
        </template>
        <template #default="scope">
          <!--<el-button
              link
              type="primary"
              size="small"
              @click.prevent="deleteRow(scope.$index)"
          >
            Remove
          </el-button>-->
          <el-dropdown v-if="scope.row._flag && scope.row._level==1 && scope.row.typeId==3006">
            <span class="el-dropdown-link">
              操作<el-icon class="el-icon--right"><arrow-down/></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item :icon="Plus" @click.prevent="operation(0,scope.row)">
                  向[{{scope.row.object.name}}]后添加关联对象
                </el-dropdown-item>
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
  <el-drawer v-model="addLink.show" :title="addLink.name" size="70%" show-close
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
        const setFlag = (dat: Record<any, any>, flag: boolean, level: number) => {
          dat._flag = (dat.id < 0 || flag);
          dat._level = level;
          (dat.children || []).forEach((dat2: Record<any, any>) => setFlag(dat2, dat._flag, level + 1));
        };
        data.forEach(value => setFlag(value, false, 1));
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
    case 3:
      addLink.value = new ChoiceParentDrawersContent();
      addLink.value.open({});
      break;
    case 0:
      addLink.value = new ObjectLinkDrawersContent(row);
      addLink.value.open(row);
      break;

  }
};
import {DrawersContent, FormContent, stringLike} from "@/components/config";
import {ElMessage, ElMessageBox} from "element-plus";
import {AxiosError} from "axios";
import {
  EasySearchItem,
  FormDrawersContent,
  InputItem,
  ItemConfig,
  MyOption,
  SelectItem,
  TextItem
} from "@/components/form/config";
import MyFormContainer from "@/components/form/MyFormContainer.vue";
import {ObjectRelItem} from "@/components/entity/object/config";

class LinkAddDrawersContent extends FormDrawersContent {
  type: number | undefined;

  constructor() {
    super();
  }

}


class ObjectLinkDrawersContent extends LinkAddDrawersContent {
  constructor(row: Record<any, any>) {
    super();
    this.name = "添加对象";

    const _attrs: Array<any> = attrs.slice(0, attrs.findIndex(value => value.instanceId == row.instanceId) + 1);
    this.content.addInput(
        new SelectItem("type", "类型", {
          async inputLoadHandler(input, content) {
            const {data}: { data: Array<Record<any, any>> } = await $http.get(`link/types/obj${objectId.value}`);
            input.setOptions(...data.map(v => new MyOption(v.id, v.desc)));
          }
        } as ItemConfig<SelectItem>),
        new TextItem("parent", "上级", {
          async inputLoadHandler(input, content) {
            console.log(row);
            input.prop.readonly = true;
            input.prop.value = `[${row.instanceId}][${row.object.code}]${row.object.name}`;
          },
          async valueToData(input, form, content) {
            form[input.code] = row.instanceId;
          }
        } as ItemConfig<TextItem>),
        new SelectItem("object", "对象", {
          async inputLoadHandler(input, content) {
            input.prop.filterable = true;
            input.prop.remote = true;
            input.prop.reserveKeyword = true;
            input.prop.placeholder = "Please enter a keyword";
            input.prop.loading = false;
            input.prop.remoteMethod = async function (query: string) {
              if (query) {
                input.prop.loading = true;
                const {data}: { data: Array<Record<any, any>> } = await $http.get(`query/object/${objectId.value}?text=${query}`);
                input.prop.loading = false;
                input.setOptions(...data.map(v => new MyOption(v.id, v.desc)));
              } else {
                input.setOptions();
              }
            };
          }
        } as ItemConfig<SelectItem>),
        new ObjectRelItem("rel", "关系", {
          inputLoadHandler: (input, content) => {
            console.log("");
            input.prop.objects = _attrs;
          },
          async inputChangeHandler(input, changeInput, value, content) {
            console.log(changeInput.code, value);
            switch (changeInput.code) {
              case 'type': {
                input.prop.loading = true;
                const {data}: { data: Array<Record<any, any>> } = await $http.get(`query/link/type/${value}`);
                input.prop.loading = false;
                console.log(data);
                input.prop.linkTypes = data.map(v => new MyOption(v.id, v.desc));
                break;
              }
              case 'object': {
                input.prop.loading = true;
                const {data}: { data: Array<Record<any, any>> } = await $http.get(`obj/${value}`);
                input.prop.loading = false;
                input.prop.objects = [{object: data, instanceId: "当前"}, ..._attrs];
                break;
              }
            }
          },
          async valueToData(input, form, content) {
            async function getVal(node: any): Promise<any> {
              let children: Array<any> = null;
              if (node.children != null) {
                children = [];
                for (const link of node.children) {
                  children.push(await getVal(link));
                }
              }

              return {
                objectId: node.item?.object?.id,
                instanceId: node.item?.instanceId,
                linkTypeId: node.linkTypeId,
                attributeId: node.item?.attrs?.[node.attrIndex || 0]?.id,
                children
              };
            }

            const convertValues = [];
            for (const link of (content.form[input.code] || [])) {
              convertValues.push(await getVal(link));
            }
            form[input.code] = convertValues;
          }
        } as ItemConfig<ObjectRelItem>)
    );
  }

  async confirm() {
    try {
      await $http.post(`add/obj/link/rel/${objectId.value}`, await this.content.getForm(true));
      this._close();
      load(objectId.value);
    } catch (e: any) {
      ElMessage({
        showClose: true,
        message: e.toString(),
        type: 'error',
      });
    }
  }
}

const addLink = ref({} as LinkAddDrawersContent);

class ChoiceParentDrawersContent extends LinkAddDrawersContent {
  constructor() {
    super();
    this.name = "设置继承对象";
    this.content.addInput(new SelectItem("parent", "对象", {
      async inputLoadHandler(input, content) {
        input.prop.filterable = true;
        input.prop.remote = true;
        input.prop.reserveKeyword = true;
        input.prop.placeholder = "Please enter a keyword";
        input.prop.loading = false;
        input.prop.remoteMethod = async function (query: string) {
          if (query) {
            if (input.prop['options'].find((option: any) => (option.name || "").toUpperCase().indexOf((query || "").toUpperCase()) >= 0) != null) {
              return
            }
            input.prop.loading = true;
            const {data}: { data: { list: Array<Record<any, any>> } } = await $http.post(`obj`, {
              data: {code: query},
              page: {page: 1, size: 100},
              query: 'objectSearch',
            });
            input.prop.loading = false;
            input.setOptions(...(data?.list || []).map(v => new MyOption(v.id, `[${v.name}]${v.code}`)));
          } else {
            input.setOptions();
          }
        };
      }
    } as ItemConfig<SelectItem>));
  }

  async confirm() {
    try {
      const data = await this.content.getForm(true);
      await $http.post(`obj${objectId.value}/parent${data.parent}`, {});
      this._close();
      load(objectId.value);
    } catch (e: any) {
      ElMessage({
        showClose: true,
        message: e.toString(),
        type: 'error',
      });
    }
  }
}
</script>

<style scoped>
.el-dropdown-link {
  cursor: pointer;
  color: var(--el-color-primary);
  display: flex;
  align-items: center;
}
</style>