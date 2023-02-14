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
import {useRouter, onBeforeRouteUpdate, useRoute, RouteLocationNormalizedLoaded} from "vue-router";
import type{TabsPaneContext} from 'element-plus'

const router = useRouter(),
    route = useRoute();
const currentPagePath = "/index";

export declare interface TabInfo {
  id: string
  title: string
  path: TabPathInfo[]
}

export declare interface TabPathInfo {
  name: string
  path: string
}

const tempTabIndex = ref(2),
    editableTabsValue = ref('0'),
    editableTabs = reactive(new Array<TabInfo>())

const containTab = (targetName: string): TabInfo | undefined => {
  return editableTabs.find(value => value.id == targetName);
}
const isContainTab = (targetName: string): boolean => {
  return containTab(targetName) != null;
}

const addTab = (targetName: string) => {
  console.log(targetName)
  router.push(targetName)
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
watch(editableTabsValue,
    (editableTabsValue, oldEditableTabsValue) => {
      console.log("editableTabsValue", editableTabsValue, oldEditableTabsValue)
      if (editableTabsValue != oldEditableTabsValue) {
        const tab = containTab(editableTabsValue);
        if (tab != null)
          router.push(tab.path[tab.path.length - 1].path)
        else
          router.push(currentPagePath)
      }
    })
const tabInstance = (value: RouteLocationNormalizedLoaded) => {
  const obj: any = value.matched[value.matched.length - 1].components?.default,
      name = obj['__name'];
  editableTabs
      .push({
        id: value.path,
        title: 'page' + Math.random(),
        path: [{name, path: value.path}]
      })
}

watch(route, (value, oldValue) => {
  console.log("route", value, oldValue)
  if (value.path == currentPagePath) return
  editableTabsValue.value = value.path;
  if (isContainTab(value.path)) return;
  tabInstance(value)


})
onMounted(() => {
  console.log(router.currentRoute.value.fullPath, currentPagePath)
  if (router.currentRoute.value.fullPath != currentPagePath) {
    addTab(router.currentRoute.value.fullPath)
    tabInstance(route)
    editableTabsValue.value = route.path;
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