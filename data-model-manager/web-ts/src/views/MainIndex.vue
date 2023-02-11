<template>
  <el-container class="container">
    <el-header class="header">Header</el-header>
    <el-container style="width: 100%;height: 100%">
      <el-aside width="200px">Aside</el-aside>
      <el-main>
        <el-tabs
            v-model="editableTabsValue"
            type="border-card"
            @tab-remove="removeTab"
        >
          <el-tab-pane nane="index" label="index">
            <el-button size="small" @click="addTab((++tempTabIndex) % 2 == 0 ? '/about' : '/detail')">
              add tab
            </el-button>
            Index
          </el-tab-pane>
          <el-tab-pane style="height: calc(100vh - 185px);overflow: scroll;overflow-x: hidden;"
                       closable
                       v-for="item in editableTabs"
                       :key="item.name"
                       :label="item.title"
                       :name="item.name"
          >
            <component :is="item.component"></component>
          </el-tab-pane>
        </el-tabs>
      </el-main>
    </el-container>
  </el-container>
</template>

<script lang="ts">
// eslint-disable-next-line vue/no-export-in-script-setup
import {defineComponent} from 'vue'
import {TabInfo} from '@/config/TabView'
import HelloWorld from "@/components/HelloWorld.vue";
import HomeView from "@/views/HomeView.vue";


export default defineComponent({
  data() {
    return {
      tempTabIndex: 2,
      editableTabsValue: '0',
      editableTabs: Array<TabInfo>(),
    }
  },
  // eslint-disable-next-line @typescript-eslint/no-empty-function
  setup() {

  },
  // eslint-disable-next-line @typescript-eslint/no-empty-function
  mounted() {

  },
  methods: {
    addTab: function (targetName: string) {
      if (this.$data.editableTabs.filter(value => value.name == targetName).length == 0) {
        console.log(HelloWorld)
        this.$data.editableTabs
            .push(this.$tab.getTabs()
                .find(value => value.name == targetName) || {
              title: '错误',
              name: '/error',
              component: HelloWorld
            })
      }
      this.$data.editableTabsValue = targetName
      console.log(targetName)
      console.log(this.$data.editableTabs[this.$data.editableTabs.length - 1].component, HomeView)
    }
    , removeTab: function (targetName: string) {
      const tabs = this.$data.editableTabs
      let activeName = this.$data.editableTabsValue
      if (activeName === targetName) {
        tabs.forEach((tab, index) => {
          if (tab.name === targetName) {
            const nextTab = tabs[index + 1] || tabs[index - 1]
            if (nextTab) {
              activeName = nextTab.name
            }
          }
        })
      }

      this.$data.editableTabsValue = activeName
      this.$data.editableTabs = tabs.filter((tab) => tab.name !== targetName)
    }
  }
})


</script>

<style scoped>
.container {
  width: calc(100vw);
  height: calc(100vh);
  background-color: cornflowerblue;
}
</style>