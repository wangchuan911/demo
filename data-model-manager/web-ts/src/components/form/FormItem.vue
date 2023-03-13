<template>
  <template v-if="item.mode=='input'">
    <el-input
        v-if="['color','email','datetime-local','number','password','search','tel','text','url'].indexOf(item.config.type)>=0"
        :type="item.config.type" v-model="form[item.id]" v-bind="item.config"
    />
    <el-date-picker v-else-if="['date','datetime','week','month','year'].indexOf(item.config.type)>=0"
                    v-model="form[item.id]"
                    :type="item.config.type"
                    size="default"
                    v-bind="item.config"
    />
    <el-time-picker v-else-if="item.config.type=='time'" v-model="form[item.id]" v-bind="item.config"/>
    <template v-else-if="item.config.type=='checkbox'">
      <el-checkbox-group v-if="item.init.length<=5" v-model="form[item.id]" v-bind="item.config">
        <el-checkbox
            :label="init.value" :name="item.id"
            v-for=" init in item.init||[]" :key="init.value">{{ init.name }}
        </el-checkbox>
      </el-checkbox-group>
      <el-select v-else
                 v-model="form[item.id]"
                 multiple
                 style="width: 100%"
                 v-bind="item.config"
      >
        <el-option
            v-for=" init in item.init||[]" :key="init.value"
            :label="init.name"
            :value="init.value"
            v-bind="item.config"
        />
      </el-select>
    </template>
    <template v-else-if="item.config.type=='radio'">
      <el-radio-group v-if="item.init.length<=5" v-model="form[item.id]" v-bind="item.config">
        <el-radio :label="init.value" size="large"
                  v-for=" init in item.init||[]" :key="init.value">{{ init.name }}
        </el-radio>
      </el-radio-group>
      <el-select v-else v-model="form[item.id]" v-bind="item.config">
        <el-option
            v-for=" init in item.init||[]" :key="init.value"
            :label="init.name"
            :value="init.value"
            :disabled="init.disabled||false"
        />
      </el-select>
    </template>
    <el-slider v-else-if="item.config.type=='range'" v-model="form[item.id]" v-bind="item.config"/>
  </template>
  <!--  <el-input v-else-if="item.mode==='custom'"
              v-model="form[item.id]"
              v-bind="item.config"
              class="input-with-select" readonly
    >
      <template #append>
        <el-button :icon="Search"/>
      </template>
    </el-input>-->
  <component v-else-if="item.mode==='custom'" :is="item.config.type" v-bind:item="item" v-bind:form="form"></component>
  <el-switch v-else-if="item.mode==='switch'" v-model="form[item.id]" v-bind="item.config"/>

  <slot v-else ref="other"></slot>
</template>

<script lang="ts">
import {Options, Vue} from 'vue-class-component';
import {Search} from '@element-plus/icons-vue'
import {FormItemDefine} from '@/components/form/config'
import {InputModule} from '@/components/form/config'

console.log(InputModule)
@Options({
  props: {
    item: Object as unknown as FormItemDefine,
    form: Object as Record<string, any>
  },
  components: InputModule
})
export default class FormItem extends Vue {
  item!: FormItemDefine
  form!: Record<string, any>
}


</script>

<style scoped>

</style>