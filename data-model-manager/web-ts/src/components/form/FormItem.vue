<template>
  <template v-if="item.mode=='input'">
    <el-input
        v-if="['color','email','datetime-local','number','password','search','tel','text','url'].indexOf(item.type)>=0"
        :type="item.type" v-model="form[item.id]"
        :placeholder="item.placeholder"/>
    <el-date-picker v-else-if="['date','datetime','week','month','year'].indexOf(item.type)>=0"
                    v-model="form[item.id]"
                    :type="item.type"
                    :placeholder="item.placeholder"
                    size="default"
    />
    <el-time-picker v-else-if="item.type=='time'" v-model="form[item.id]" :placeholder="item.placeholder"/>
    <template v-else-if="item.type=='checkbox'">
      <el-checkbox-group v-if="item.init.length<=5" v-model="form[item.id]">
        <el-checkbox
            :label="init.value" :name="item.id"
            v-for=" init in item.init||[]" :key="init.value">{{ init.name }}
        </el-checkbox>
      </el-checkbox-group>
      <el-select v-else
                 v-model="form[item.id]"
                 multiple
                 placeholder="Select"
                 style="width: 100%"
      >
        <el-option
            v-for=" init in item.init||[]" :key="init.value"
            :label="init.name"
            :value="init.value"
        />
      </el-select>
    </template>
    <template v-else-if="item.type=='radio'">
      <el-radio-group v-if="item.init.length<=5" v-model="form[item.id]">
        <el-radio :label="init.value" size="large"
                  v-for=" init in item.init||[]" :key="init.value">{{ init.name }}
        </el-radio>
      </el-radio-group>
      <el-select v-else v-model="form[item.id]">
        <el-option
            v-for=" init in item.init||[]" :key="init.value"
            :label="init.name"
            :value="init.value"
            :disabled="init.disabled||false"
        />
      </el-select>
    </template>
    <el-slider v-else-if="item.type=='range'" v-model="form[item.id]" :step="1"/>
  </template>
  <el-input v-else-if="item.mode==='custom'"
            v-model="form[item.id]"
            :placeholder="item.placeholder"
            class="input-with-select" readonly
  >
    <template #append>
      <el-button :icon="Search"/>
    </template>
  </el-input>
  <el-switch v-else-if="item.mode==='switch'" v-model="form[item.id]"/>
  <slot v-else ref="other"></slot>
</template>

<script lang="ts">
import {Options, Vue} from 'vue-class-component';
import {Search} from '@element-plus/icons-vue'
import {FormItemDefine} from '@/components/form/config'

@Options({
  props: {
    item: FormItemDefine,
    form: Object
  }
})
export default class FormItem extends Vue {
  item!: FormItemDefine
  form!: object
}


</script>

<style scoped>

</style>