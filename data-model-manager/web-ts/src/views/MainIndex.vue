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
              <el-button size="small" @click="addTab((++tempTabIndex) % 2 == 0 ? '/index/about' : '/index/detail')">
                add tab
              </el-button>
              Index
            </el-tab-pane>
            <el-tab-pane class="tab-container"
                         closable
                         v-for="item in editableTabs"
                         :key="item.id"
                         :label="item.title"
                         :name="item.id"
            >
            </el-tab-pane>

            <router-view v-slot="{ Component }" :max="10" class="router-container">
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
import {ref, watch, watchPostEffect, onActivated, onMounted, reactive} from 'vue'
import HomeView from "@/views/HomeView.vue";
import {useRouter, onBeforeRouteUpdate} from "vue-router";
import type{TabsPaneContext} from 'element-plus'

const router = useRouter();
const currentPagePath = "/index";

export declare interface TabInfo {
  id: string
  title: string
  path: string[]
}

const tempTabIndex = ref(2),
    editableTabsValue = ref('0'),
    editableTabs = reactive(new Array<TabInfo>())

const containTab = (targetName: string): boolean => {
  return editableTabs.filter(value => value.id == targetName).length != 0;
}

const addTab = (targetName: string) => {
  if (!containTab(targetName)) {
    editableTabs
        .push({
          id: targetName,
          title: 'page' + Math.random(),
          path: [targetName]
        })
  }
  editableTabsValue.value = targetName
  console.log(targetName)
  console.log(editableTabs[editableTabs.length - 1])
}
const removeTab = (targetName: string) => {
  const tabs = editableTabs
  let activeName = editableTabsValue.value
  if (activeName === targetName) {
    tabs.forEach((tab, index) => {
      if (tab.id === targetName) {
        const nextTab = tabs[index + 1] || tabs[index - 1]
        if (nextTab) {
          activeName = nextTab.id
        }
      }
    })
  }

  editableTabsValue.value = activeName

  for (let i = 0; i < tabs.length; i++) {
    if (tabs[i].id == targetName) {
      tabs.splice(i, 1)
      return;
    }
  }
}
watch([editableTabsValue], ([editableTabsValue], [oldEditableTabsValue]) => {
  console.log(editableTabsValue, oldEditableTabsValue)
  if (containTab(editableTabsValue))
    router.push(editableTabsValue)
  else
    router.push(currentPagePath)
})
onMounted(() => {
  console.log("onMounted", router.currentRoute, router.currentRoute.value.fullPath, currentPagePath, router.currentRoute.value.fullPath.substring(currentPagePath.length));
  if (router.currentRoute.value.fullPath != currentPagePath) {
    addTab(router.currentRoute.value.fullPath.substring(currentPagePath.length))
  }
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
  top: 15px;
  overflow-y: scroll;
  overflow-x: hidden
}
</style>