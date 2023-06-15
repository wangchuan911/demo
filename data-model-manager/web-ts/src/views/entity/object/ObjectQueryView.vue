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

    <template v-for="define in defines " :key="define.code">
      <el-form-item :label="define.name">
        <form-item :item="define" :form="form"></form-item>
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

const {proxy} = getCurrentInstance() as ComponentInternalInstance
const objectTypeId = "1", objectId = "1";
const loading = ref(true)
onActivated(() => {
  console.log("onActivated")
})
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
  const pInit = getInitValue(objectTypeId);
  proxy?.$http.get(`database/object/${objectTypeId}/${objectId}`)
      .then(({data}) => {
        defines.length = 0;
        data.fields.forEach((value: any) => {
          const config = JSON.parse(value.style || "{}")
          if ((value.columns || []).length > 0) {
            const format = (value1: any): any => {
              switch (config.type) {
                case "checkbox":
                  return [value1.formatValue];
                default:
                  return value1.formatValue
              }
            }

            switch (value.columns.length) {
              case 0:
                break;
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
            init: []
          } as FormItemDefine;

          defines.push(field)
        })
        console.log(defines)
        return pInit;
      })
      .then(({data}) => {
        defines.forEach(value => {
          if (typeof (data[value.config.type]) != 'undefined') {
            value.init.push(...data[value.config.type])
          }
        })
      })
      .then(() => {
        loading.value = false
      }, () => {
        loading.value = false
      })

  async function getInitValue(objectId: string) {
    const arr = [
      {name: "Online activities", value: 1},
      {name: "Online activities2", value: 2},
      {name: "Offline activities", value: 3},
      {name: "Simple brand exposure", value: 4},
      {name: "Simple brand exposure1", value: 5},
      {name: "Simple brand exposure2", value: 6}];
    return {
      data: {
        checkbox: arr,
        radio: arr
      }
    } as Record<any, any>
  }
})


// do not use same name with ref
const defines = reactive(new Array<FormItemDefine>(
    //{name: "Activity button", type: "button", key: "name4", mode: "input"},
    //{name: "Activity checkbox", type: "checkbox", key: "name4", mode: "input"},
    /*{name: "Activity color", config: {type: "color"}, id: "1", mode: "input"},
    {name: "Activity date", config: {type: "date"}, id: "2", mode: "input"},
    {name: "Activity datetime-local", config: {type: "datetime-local"}, id: "3", mode: "input"},
    {name: "Activity datetime", config: {type: "datetime"}, id: "4", mode: "input"},
    {name: "Activity email", config: {type: "email"}, id: "5", mode: "input"},
    //{name: "Activity file",config:{ type: "file"}, id: "name4", mode: "input"},
    // {name: "Activity hidden",config:{ type: "hidden"}, id: "name4", mode: "input"},
    //{name: "Activity image",config:{ type: "image"}, id: "name4", mode: "input"},
    {name: "Activity month", config: {type: "month"}, id: "6", mode: "input"},
    {name: "Activity number", config: {type: "number"}, id: "7", mode: "input"},
    {name: "Activity password", config: {type: "password"}, id: "8", mode: "input"},
    //{name: "Activity radio",config:{ type: "radio"}, id: "name4", mode: "input"},
    {name: "Activity range", config: {type: "range", step: 11}, id: "9", mode: "input"},
    //{name: "Activity reset",config:{ type: "reset"}, id: "name4", mode: "input"},
    {name: "Activity search", config: {type: "search"}, id: "10", mode: "input"},
    //{name: "Activity submit",config:{ type: "submit"}, id: "name4", mode: "input"},
    {name: "Activity tel", config: {type: "tel"}, id: "11", mode: "input"},
    {name: "Activity text", config: {type: "text"}, id: "12", mode: "input"},
    {name: "Activity time", config: {type: "time"}, id: "13", mode: "input"},
    {name: "Activity url", config: {type: "url"}, id: "14", mode: "input"},
    {name: "Activity week", config: {type: "week"}, id: "15", mode: "input"},
    {
      name: "Activity checkbox", config: {type: "checkbox"}, id: "16", mode: "input",
      init: [
        {name: "Online activities", value: 1},
        {name: "Online activities2", value: 2},
        {name: "Offline activities", value: 3},
        {name: "Simple brand exposure", value: 4},
        {name: "Simple brand exposure1", value: 5},
        {name: "Simple brand exposure2", value: 6}]
    },
    {
      name: "Activity radio", config: {type: "radio"}, id: "17", mode: "input",
      init: [
        {name: "Online activities", value: 1},
        {name: "Online activities2", value: 2},
        {name: "Offline activities", value: 3},
        {name: "Simple brand exposure", value: 4},
        {name: "Simple brand exposure1", value: 5},
        {name: "Simple brand exposure2", value: 6}]
    }*/));

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