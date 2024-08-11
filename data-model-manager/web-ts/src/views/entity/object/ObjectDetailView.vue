<template>
  <el-form :model="form" label-width="120px" v-loading="loading">
    <!--    <el-form-item label="Activity name">
          <el-input v-model="form.name"/>
        </el-form-item>


        <el-form-item label="Activity zone">
          <el-select v-model="form.region" placeholder="please select your zone">
            <el-option label="Zone one" value="shanghai"/>
            <el-option label="Zone two" value="beijing"/>
          </el-select>
        </el-form-item>
        <el-form-item label="Activity time">
          <el-col :span="11">
            <el-date-picker
                v-model="form.date1"
                type="date"
                placeholder="Pick a date"
                style="width: 100%"
            />
          </el-col>
          <el-col :span="2" class="text-center">
            <span class="text-gray-500">-</span>
          </el-col>
          <el-col :span="11">
            <el-time-picker
                v-model="form.date2"
                placeholder="Pick a time"
                style="width: 100%"
            />
          </el-col>
        </el-form-item>
        <el-form-item label="Instant delivery">
          <el-switch v-model="form.delivery"/>
        </el-form-item>
        <el-form-item label="Activity type">
          <el-checkbox-group v-model="form.type">
            <el-checkbox label="Online activities" name="type"/>
            <el-checkbox label="Promotion activities" name="type"/>
            <el-checkbox label="Offline activities" name="type"/>
            <el-checkbox label="Simple brand exposure" name="type"/>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="Resources">
          <el-radio-group v-model="form.resource">
            <el-radio label="Sponsor"/>
            <el-radio label="Venue"/>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="Activity form">
          <el-input v-model="form.desc" type="textarea"/>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSubmit">Create</el-button>
          <el-button>Cancel</el-button>
        </el-form-item>-->

    <template v-for="attr in attrs" :key="attr.id">
      <el-form-item :label="attr.name">

      </el-form-item>
    </template>
    <el-form-item>
      <el-button type="primary" @click="onSubmit">Create</el-button>
      <el-button>Cancel</el-button>
    </el-form-item>
  </el-form>

</template>

<script lang="ts" setup>
import {ref, reactive, onActivated, onMounted, getCurrentInstance, ComponentInternalInstance} from 'vue'
import FormItem from "@/components/form/FormItem.vue";
import {FormItemDefine} from "@/components/form/config";
import {useRoute} from "vue-router";

const attrs = reactive(new Array<Record<any, any>>());
const {proxy} = getCurrentInstance() as ComponentInternalInstance, route = useRoute();
const objectTypeId = "1", objectId = "1";
const loading = ref(true)
onActivated(() => {
  console.log("onActivated")
  console.log(route.params)
  load(parseInt(route.params.id as string))
})
const load = (id: number) => {
  proxy?.$http.get(`obj/${id}`)
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
  proxy?.$http.put(`database/object/${objectTypeId}/${objectId}`, form).then(value => {
    loading.value = false
  })
}
console.log(new Date().valueOf())
</script>

<style scoped>

</style>