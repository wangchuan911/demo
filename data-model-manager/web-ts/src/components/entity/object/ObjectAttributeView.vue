<template>
  <div style="display: flex;margin: 5px 1px;height:32px ">
    <el-button type="primary" @click="addAttr.open({})">添加属性</el-button>
  </div>
  <el-table :data="attrs" style="width: 100%" border v-loading="loading" max-height="calc(100vh - 197px)">
    <el-table-column prop="name" label="属性描述"/>
    <el-table-column prop="code" label="属性标识"/>
    <el-table-column fixed="right" label="">
      <template #default="scope">
        <!--<el-button link type="primary" size="small" @click="delAttr(scope.row.id)">删除</el-button>-->
        <el-dropdown>
            <span class="el-dropdown-link">
              操作<el-icon class="el-icon--right"><arrow-down/></el-icon>
            </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item :icon="Plus" @click.prevent="modAttr(scope.row)">修改</el-dropdown-item>
              <el-dropdown-item :icon="Plus" @click.prevent="delAttr(scope.row.id)">删除</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </template>
    </el-table-column>
  </el-table>
  <el-drawer v-model="addAttr.show" title="添加属性" size="50%" show-close
             :before-close="(done)=>addAttr.beforeClose(done)">
    <template #default>
      <my-form-container v-model="addAttr.content"></my-form-container>
    </template>
    <template #footer>
      <div style="flex: auto">
        <el-button @click="addAttr.show=false">cancel</el-button>
        <el-button type="primary" @click="addAttr.confirm()">confirm</el-button>
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
  defineProps,
  defineEmits,
  watch,
  computed,
  defineModel, ComponentCustomProperties
} from 'vue';
import {
  ArrowDown,
  Check,
  CircleCheck,
  CirclePlus,
  CirclePlusFilled,
  Plus,
} from '@element-plus/icons-vue';
import {ElMessageBox, ElMessage} from 'element-plus';
import type {Action} from 'element-plus';
import 'element-plus/es/components/message-box/style/css';
import 'element-plus/es/components/message/style/css';
import {DrawersContent, FormContent} from "@/components/config";
import {AxiosError} from "axios";
import {
  FormDrawersContent, InputItem,
  ItemConfig,
  MyOption,
  MyTreeOption,
  SelectItem,
  SelectTreeItem,
  TextItem
} from "@/components/form/config";
import {AttrObjMapperItem} from "@/components/entity/object/config";


class AttrAddDrawersContent extends FormDrawersContent {

  constructor() {
    super();
    this.name = "属性";
    this.content.addInput(new TextItem("code", "标识"), new TextItem("name", "描述", {} as ItemConfig<TextItem>),
        new SelectTreeItem("attr", "关联表字段", {
          async inputLoadHandler(input: SelectTreeItem, content: FormContent): Promise<void> {
            const {data}: { data: Array<any> } = await $http.get(`attr/bind/tree/${props.id}`);
            // input.prop.defaultExpandAll = false;
            input.prop.prop.expandOnClickNode = false;
            input.prop.prop.renderAfterExpand = false;
            // input.prop.prop.showCheckbox = true;

            // input.prop.showCheckbox= true;
            const format = (values: any[]): MyTreeOption[] => {
              if (!values) {
                return null as unknown as MyTreeOption[];
              }
              return values.map(val => new MyTreeOption(val.seq, `[${val.instanceId}][${val.type}]${val.code}`, format(val.children)));
            };
            input.setOptions(...format(data));
          },
          async dataToValue(input: SelectTreeItem, value: any, content: FormContent): Promise<void> {
            if (value.id) {
              const {data}: { data: Array<any> } = await $http.get(`attr/path/${value.id}`);
              if (data.length > 0) {
                const seq = JSON.stringify(data);
                console.log(data, seq)
                content.form[input.code] = seq;
                return
              }
            }
            content.form[input.code] = value[input.code];
          },
          /*async inputChangeHandler(input: SelectTreeItem, changeInput: InputItem, value: any, content: FormContent): Promise<void> {
            console.log(value)
            const {data}: { data: Array<any> } = await $http.post(`attr/bind/obj/${props.id}`, {
              path: value
            });
            content.form["attrId"] = data;
          }*/
        } as ItemConfig<SelectTreeItem>).andThen(item => {
          item.prop.defaultExpandAll = true;
        }),
        new AttrObjMapperItem('colMapper', "属性映射", {
          async inputLoadHandler(input: AttrObjMapperItem, content: FormContent): Promise<void> {
            input.prop.objectId = props.id;
          },
          async dataToValue(input: AttrObjMapperItem, value: any, content: FormContent): Promise<void> {
            input.prop.cols.length = 0;
            input.prop.rows.length = 0;
            if (value.id) {
              const {data}: { data: { cols: Array<Record<any, any>>, rows: Array<Array<Record<any, any>>> } } = await $http.get(`attr/mapper/${value.id}`);
              console.log(data)
              console.log(input.prop)
              console.log(input.prop);
              const rows: Array<Record<any, any>> = [];
              data.rows.forEach((row, index) => {
                rows[index] = rows[index] || {
                  mapper: {
                    current: row[0].attributeId
                  },
                  objectId: row[0].objectId,
                  attrs: [],
                  objs: []
                };
                for (let i = 1; i < row.length; i++) {
                  rows[index].mapper[`${i - 1}`] = row[i].attributeId;
                }
              });
              for (let row of rows) {
                const {data} = await $http.get(`obj/attrs/${row.objectId}`);
                row.attrs.push(...data);
              }
              for (let row of rows) {
                const {data} = await $http.get(`obj/${row.objectId}`);
                row.objs.push({value: data.id, label: `[${data.name}]${data.code}`});
              }
              input.prop.cols.push(...data.cols.filter((value1, index) => index != 0).map((value1) => ({attrId: value1.attributeId})));
              input.prop.rows.push(...rows);
              console.log(rows);
              return;
            }
            content.form[input.code] = value[input.code];
          },
          async valueToData(input: AttrObjMapperItem, form: Record<any, any>, content: FormContent): Promise<void> {
            form[input.code] = {
              rows: content.form[input.code].rows,
              cols: content.form[input.code].cols
            };
          }
        } as ItemConfig<AttrObjMapperItem>));
  }

  confirm() {
    loading.value = true;
    this.content.getForm(true).then(form => {
      $http.put(`obj/attrs/${props.id}`, {...this.data, ...form})
          .then(({data}: { data: Array<Record<any, any>> }) => {
            loading.value = false;
            this._close();
            if (data instanceof String) {
              throw data;
            }
            if (this.data?.id != null)
              attrs[attrs.findIndex(value => value.id == this.data?.id)] = data;
            else
              attrs.push(data);
          }, (error: any) => {
            loading.value = false;
            this._close();
            /*if (error instanceof AxiosError)
              ElMessage.error(error.response?.data || error);
            else
              ElMessage.error(error);*/
          });
    });
  }
}

const addAttr = reactive(new AttrAddDrawersContent());

const delAttr = (attrId: number) => {
  ElMessageBox.confirm('是否删除?', {confirmButtonText: "确定", cancelButtonText: "取消"})
      .then(() => {
        loading.value = true;
        $http.delete(`obj/attrs/${attrId}`)
            .then(() => {
              for (let i = 0; i < attrs.length; i++) {
                if (attrs[i].id == attrId) {
                  attrs.splice(i, 1);
                  break;
                }
              }
            })
            .then(() => {
              loading.value = false;
            }, () => {
              loading.value = false;
            });
      })
      .catch(() => {
        // catch error
      });

};
const attrs = reactive(new Array<Record<any, any>>());
const {proxy} = getCurrentInstance() as ComponentInternalInstance;
const {$http} = proxy as ComponentCustomProperties;
const loading = ref(true);
const props = defineProps<{ id: number }>();
const load = (id: number) => {
  $http.get(`obj/attrs/${id}`)
      .then(({data}: { data: Array<Record<any, any>> }) => {
        attrs.length = 0;
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
const close = (event: null) => {
  console.log(event);
};
const modAttr = (row: any) => {
  addAttr.open(row);
};
</script>

<style scoped>
.el-dropdown-link {
  cursor: pointer;
  color: var(--el-color-primary);
  display: flex;
  align-items: center;
}
</style>