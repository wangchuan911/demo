<template>
  <div>
    <a-radio-group v-model="size" style="margin-bottom: 16px">
      <a-radio-button value="small">Small</a-radio-button>
      <a-radio-button value="default">Default</a-radio-button>
      <a-radio-button value="large">Large</a-radio-button>
    </a-radio-group>
    <a-tabs defaultActiveKey="2" :size="size"
            type="editable-card"  >
      <a-tab-pane tab="Tab 1" key="1">Content of tab 1</a-tab-pane>
      <a-tab-pane tab="Tab 2" key="2">Content of tab 2</a-tab-pane>
      <a-tab-pane tab="Tab 3" key="3">Content of tab 3</a-tab-pane>
    </a-tabs>
  </div>
</template>
<script>
export default {
  data () {
    return {
      size: 'small'
    }
  }
}
</script>
