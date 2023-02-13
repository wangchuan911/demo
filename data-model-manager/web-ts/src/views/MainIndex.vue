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
          >
            <el-tab-pane nane="index" label="index" class="tab-container">
              <el-button size="small" @click="addTab((++tempTabIndex) % 2 == 0 ? '/about' : '/detail')">
                add tab
              </el-button>
              Index
            </el-tab-pane>
            <el-tab-pane class="tab-container"
                         closable
                         v-for="item in editableTabs"
                         :key="item.path"
                         :label="item.title"
                         :name="item.path"
            >
            </el-tab-pane>

            <router-view  v-slot="{ Component }" :max="10" class="router-container">
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
import {ref, watch, watchPostEffect} from 'vue'
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
watch([editableTabsValue], ([editableTabsValue], [oldEditableTabsValue]) => {
  console.log(editableTabsValue, oldEditableTabsValue)
  if (editableTabs.value.find(value => value.path == editableTabsValue) == null) {
    router.push(`/index`)
  } else
    router.push(`/index${editableTabsValue}`)
})
</script>

<style scoped>
.container {
  width: calc(100vw);
  height: calc(100vh);
}

.tab-container {
  height: calc(100vh - 185px);
}

.router-container {
  position: absolute;
  width: calc(100% - 30px);
  height: calc(100vh - 185px);
  top: 35px;
  overflow-y: scroll;
  overflow-x: hidden
}
</style>