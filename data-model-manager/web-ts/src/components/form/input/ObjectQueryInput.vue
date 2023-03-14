<template>
  <el-input
      placeholder="Please input"
      class="input-with-select"
      type="input" v-model="object.objectId"
      readonly
  >
    <template #prepend>{{ object.objectTypeId }}</template>
    <template #append>
      <el-button>
        <el-icon>
          <search/>
        </el-icon>
      </el-button>
    </template>
  </el-input>
</template>

<script lang="ts">
import {Search, Loading} from '@element-plus/icons-vue'
import {FormItemDefine} from "../config";
import {Options, Vue} from "vue-class-component";

@Options({
  props: {
    item: Object as unknown as FormItemDefine,
    form: Object as Record<string, any>
  },
  components: {Search}
})
export default class QueryInput extends Vue {
  // eslint-disable-next-line @typescript-eslint/no-empty-function
  item!: FormItemDefine
  form!: Record<string, any>
  object: Record<any, any> = {}

  mounted() {
    console.log(this.item, this.form)
    const fieldId = this.item.id;
    const mapper = this.item.config?.format?.mapper;
    Object.keys(mapper).forEach(key => {
      const value = this.form[`${fieldId}_${mapper[key]}`]
      if (typeof (value) != "undefined") {
        this.object[key] = value
      }
    })
    console.log(this.object)
    this.$http.get("")
  }
}
</script>

<style scoped>

</style>