<template>
  <el-tabs v-model="activeTab" @tab-click="onChangeTab">
    <el-tab-pane label="属性管理" name="attr" >
      <object-attribute-view :id="objId"></object-attribute-view>
    </el-tab-pane>
    <el-tab-pane label="对象管理" name="obj">
      <object-combination-view :id="objId"></object-combination-view>
    </el-tab-pane>
  </el-tabs>


</template>

<script lang="ts" setup>
import {ref, reactive, onActivated, onMounted, getCurrentInstance, ComponentInternalInstance} from 'vue'
import {useRoute} from "vue-router";
import {TabsPaneContext} from "element-plus";

const activeTab = ref("attr"), onChangeTab = (tab: TabsPaneContext, event: Event) => {
  console.log(tab, event);
}
const attrs = reactive(new Array<Record<any, any>>());
const {proxy} = getCurrentInstance() as ComponentInternalInstance, route = useRoute();
const loading = ref(true)
const objId = ref(-1)
onActivated(() => {
  console.log("onActivated")
  console.log(route.params)
  objId.value = route.params.id as unknown as number;
})
const load = () => {
  proxy?.$http.get(`obj/${objId.value}`)
      .then(({data}) => {
        attrs.length = 0;
        attrs.push(...data.attributes as Array<Record<any, any>>)

      })
      .then(() => {
        loading.value = false
      }, () => {
        loading.value = false
      })
}
onMounted(() => {
  console.log("onMounted")

  /*Promise.all(
      [proxy?.$http.get(`database/object/${objectTypeId}/${objectId}`), getInitValue(objectTypeId)])
      .then(value => {
        const columns = value[0]?.data, initValue = value[1].data;
        defines.length = 0;
        columns.fields.forEach((value: any) => {
          const config = JSON.parse(value.style || "{}")
          if ((value.columns || []).length > 0) {
            // eslint-disable-next-line no-inner-declarations
            function format(value1: any): any {
              switch (config.type) {
                case "checkbox":
                  return [value1.formatValue];
                default:
                  return value1.formatValue
              }
            }

            switch (value.columns.length) {
              case 0:
                return;
              case 1:
                form[`F${value.id}`] = format(value.columns[0]);
                break;
              default:
                value.columns.forEach((value1: any) => {
                  form[`F${value.id}_C${value1.id}`] = format(value1);
                })
                break
            }
          }
          const field = {
            name: value.name,
            config,
            id: `F${value.id}`,
            mode: value.inputType.value || 'input',
            init: initValue[config.type]
          } as FormItemDefine;

          defines.push(field)
        })
        console.log(defines)

      })*/
})


// do not use same name with ref

const form = reactive({
  /*name: 'qwe',
  region: '',
  date1: '',
  date2: '',
  delivery: false,
  type: [],
  resource: '',
  desc: '',*/
  F13: '2012-11-12 12:12:12'
} as Record<any, any>)
/*const form1 = reactive({
  name: '',
  region: '',
  date1: '',
  date2: '',
  delivery: false,
  type: [],
  resource: '',
  desc: '',
})*/

const onSubmit = () => {
  loading.value = true;
  console.log('submit!')
  console.log(form)
  proxy?.$http.put(`database/object/${objId.value}`, form).then(value => {
    loading.value = false
  })
}
console.log(new Date().valueOf())
</script>

<style scoped>

</style>