<template>
  <el-container class="container">
    <el-header class="header">Header</el-header>
    <el-container style="width: 100%;height: 100%">
      <el-aside width="200px">Aside</el-aside>
      <el-main>
        <div style="position: relative">
          <el-tabs
              v-model="editableTabsValue"
              type="border-card"
              @tab-remove="removeTab"
              @tab-change="changeTab"
          >
            <el-tab-pane nane="index" label="index">
              <el-button size="small" @click="addTab((++tempTabIndex) % 2 == 0 ? '/about' : '/detail')">
                add tab
              </el-button>
              Index
            </el-tab-pane>
            <el-tab-pane style="height: calc(100vh - 185px);"
                         closable
                         v-for="item in editableTabs"
                         :key="item.path"
                         :label="item.title"
                         :name="item.path"
            >
            </el-tab-pane>

            <router-view v-slot="{ Component }" :max="10"
                         style="position: absolute;width: calc(100% - 30px);height:  calc(100vh - 185px);top: 35px;overflow-y: scroll;overflow-x: hidden">
              <keep-alive>
                <component :is="Component"/>
              </keep-alive>
            </router-view>
          </el-tabs>
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script lang="ts" setup>
// eslint-disable-next-line vue/no-export-in-script-setup
import {ref} from 'vue'
import HomeView from "@/views/HomeView.vue";
import {useRouter} from "vue-router";
import type{TabsPaneContext} from 'element-plus'

const router = useRouter();

export declare interface TabInfo {
  title: string
  path: string
}

const tempTabIndex = ref(2),
    editableTabsValue = ref('0'),
    editableTabs = ref(new Array<TabInfo>())

const addTab = (targetName: string) => {
  if (editableTabs.value.filter(value => value.path == targetName).length == 0) {
    editableTabs.value
        .push({
          title: 'page' + Math.random(),
          path: targetName
        })
  }
  editableTabsValue.value = targetName
  console.log(targetName)
  console.log(editableTabs.value[editableTabs.value.length - 1])
}
const removeTab = (targetName: string) => {
  const tabs = editableTabs.value
  let activeName = editableTabsValue.value
  if (activeName === targetName) {
    tabs.forEach((tab, index) => {
      if (tab.path === targetName) {
        const nextTab = tabs[index + 1] || tabs[index - 1]
        if (nextTab) {
          activeName = nextTab.path
        }
      }
    })
  }

  editableTabsValue.value = activeName
  editableTabs.value = tabs.filter((tab) => tab.path !== targetName)
}
const changeTab = (tabName: string) => {
  //if (tabName == editableTabsValue.value) return;
  console.log(tabName, editableTabs)
  if (editableTabs.value.find(value => value.path == tabName) != null)
    router.push(`/index${tabName}`)

}
</script>

<style scoped>
.container {
  width: calc(100vw);
  height: calc(100vh);
}
</style>