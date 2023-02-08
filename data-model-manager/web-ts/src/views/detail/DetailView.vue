<template>
  <el-form :model="form" label-width="120px">
    <el-form-item label="Activity name">
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
    </el-form-item>

    <template v-for="define in defines " :key="define.code">
      <el-form-item :label="define.name">
        <form-item :item="define" :form="form"></form-item>
      </el-form-item>
    </template>
  </el-form>

</template>

<script lang="ts" setup>
import {ref, reactive} from 'vue'
import FormItem from "@/components/form/FormItem.vue";

// do not use same name with ref
const defines = reactive([
  //{name: "Activity button", type: "button", key: "name4", mode: "input"},
  //{name: "Activity checkbox", type: "checkbox", key: "name4", mode: "input"},
  {name: "Activity color", type: "color", id: "name4", mode: "input"},
  {name: "Activity date", type: "date", id: "name4", mode: "input"},
  {name: "Activity datetime-local", type: "datetime-local", id: "name4", mode: "input"},
  {name: "Activity datetime", type: "datetime", id: "name4", mode: "input"},
  {name: "Activity email", type: "email", id: "name4", mode: "input"},
  //{name: "Activity file", type: "file", id: "name4", mode: "input"},
  // {name: "Activity hidden", type: "hidden", id: "name4", mode: "input"},
  //{name: "Activity image", type: "image", id: "name4", mode: "input"},
  {name: "Activity month", type: "month", id: "name4", mode: "input"},
  {name: "Activity number", type: "number", id: "name4", mode: "input"},
  {name: "Activity password", type: "password", id: "name4", mode: "input"},
  //{name: "Activity radio", type: "radio", id: "name4", mode: "input"},
  {name: "Activity range", type: "range", id: "name4", mode: "input"},
  //{name: "Activity reset", type: "reset", id: "name4", mode: "input"},
  {name: "Activity search", type: "search", id: "name4", mode: "input"},
  //{name: "Activity submit", type: "submit", id: "name4", mode: "input"},
  {name: "Activity tel", type: "tel", id: "name4", mode: "input"},
  {name: "Activity text", type: "text", id: "name4", mode: "input"},
  {name: "Activity time", type: "time", id: "name4", mode: "input"},
  {name: "Activity url", type: "url", id: "name4", mode: "input"},
  {name: "Activity week", type: "week", id: "name4", mode: "input"},
  {
    name: "Activity checkbox", type: "checkbox", id: "name5", mode: "input",
    init: [
      {name: "Online activities", value: 1},
      {name: "Online activities2", value: 2},
      {name: "Offline activities", value: 3},
      {name: "Simple brand exposure", value: 4},
      {name: "Simple brand exposure1", value: 5},
      {name: "Simple brand exposure2", value: 6}]
  },
  {
    name: "Activity radio", type: "radio", id: "name6", mode: "input",
    init: [
      {name: "Online activities", value: 1},
      {name: "Online activities2", value: 2},
      {name: "Offline activities", value: 3},
      {name: "Simple brand exposure", value: 4},
      {name: "Simple brand exposure1", value: 5},
      {name: "Simple brand exposure2", value: 6}]
  },

  // {name: "Activity datetime", type: "datetime", id: "name4", mode: "input"}
])
const form = reactive({
  name: 'qwe',
  region: '',
  date1: '',
  date2: '',
  delivery: false,
  type: [],
  resource: '',
  desc: '',
})
const form1 = reactive({
  name: '',
  region: '',
  date1: '',
  date2: '',
  delivery: false,
  type: [],
  resource: '',
  desc: '',
})

const onSubmit = () => {
  console.log('submit!')
  console.log(form)
}
</script>

<style scoped>

</style>