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
        <el-input v-if="define.type=='default'" v-model="form[define.key]" :placeholder="define.placeholder"/>
        <el-input v-else-if="define.type=='textarea'" autosize type="textarea" v-model="form[define.key]"
                  :placeholder="define.placeholder"/>
        <el-input v-else-if="define.type=='search'"
                  v-model="form[define.key]"
                  :placeholder="define.placeholder"
                  class="input-with-select"
        >
          <template #prepend>
            <el-select v-model="select" placeholder="Select" style="width: 115px">
              <el-option label="Restaurant" value="1"/>
              <el-option label="Order No." value="2"/>
              <el-option label="Tel" value="3"/>
            </el-select>
          </template>
          <template #append>
            <el-button :icon="Search"/>
          </template>
        </el-input>
        <slot ref="asd"></slot>
      </el-form-item>
    </template>
  </el-form>

</template>

<script lang="ts" setup>
import {ref, reactive} from 'vue'
import { Search } from '@element-plus/icons-vue'

// do not use same name with ref
const defines = reactive([
  {name: "Activity 1", type: "default", key: "name1"},
  {name: "Activity 2", type: "search", key: "name2"},
  {name: "Activity 3", type: "textarea", key: "name3"},
  {name: "Activity 4", type: "default", key: "name4"}
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