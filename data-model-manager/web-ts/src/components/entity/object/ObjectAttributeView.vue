<template>
  <div style="display: flex;margin: 5px 1px;height:32px ">
    <el-button type="primary" @click="addAttr.show=true">添加属性</el-button>
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
      <el-form :model="addAttr.form" label-width="auto" style="max-width: 600px">
        <el-form-item label="标识">
          <el-input v-model="addAttr.form.code"/>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="addAttr.form.name"/>
        </el-form-item>
      </el-form>
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
import {DrawersContent} from "@/components/form/config";
import {AxiosError} from "axios";


class AttrAddDrawersContent extends DrawersContent {
  form: Record<any, any>;

  constructor() {
    super();
    this.form = {};
  }

  beforeClose(done: () => void) {
    ElMessageBox.confirm('放弃保存?', {confirmButtonText: "确定", cancelButtonText: "取消"})
        .then(() => {
          this.form = {};
          done();
        })
        .catch(() => {
          // catch error
        });
  }

  confirm() {
    loading.value = true;
    $http.put(`obj/attrs/${props.id}`, this.form)
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
const normalizedSize = computed(() => props.id);
watch(normalizedSize, (value, oldValue, onCleanup) => {
  console.log(value);
  if (value == -1) {
    loading.value = true;
  } else if (value != oldValue && value > 0) {
    load(value as number);
  }
});
const close = (event: null) => {
  console.log(event);
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