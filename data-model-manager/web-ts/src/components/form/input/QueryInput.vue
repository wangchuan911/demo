<template>
  <el-input
      placeholder="Please input"
      class="input-with-select"
      :type="item.config.type" v-model="form[item.id]" v-bind="item.config"
  >
    <template #append>
      <el-button :icon="Search"/>
    </template>
  </el-input>
</template>

<script lang="ts">
import {Search} from '@element-plus/icons-vue'
import {FormItemDefine} from "../config";
import {Options, Vue} from "vue-class-component";

@Options({
  props: {
    item: Object as unknown as FormItemDefine,
    form: Object as Record<string, any>
  }
})
export default class QueryInput extends Vue {
  // eslint-disable-next-line @typescript-eslint/no-empty-function
  item!: FormItemDefine
  form!: Record<string, any>
}
</script>

<style scoped>

</style>