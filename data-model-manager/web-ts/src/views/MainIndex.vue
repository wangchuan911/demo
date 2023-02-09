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
          <el-tab-pane label="index" nane="index">
            <el-button size="small" @click="addTab(editableTabsValue)">
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
            <iframe :src="item.content"
                    style="height: calc(100% - 8px);width: 100%;margin: 0;padding: 0;border: white hidden thin"></iframe>
          </el-tab-pane>
        </el-tabs>
      </el-main>
    </el-container>
  </el-container>
</template>

<script lang="ts" setup>
// eslint-disable-next-line vue/no-export-in-script-setup
import {ref} from 'vue'

let tabIndex = 2
const editableTabsValue = ref('2')
const editableTabs = ref([
  {
    title: 'Tab 1',
    name: '1',
    content: 'Tab 1 content',
  },
  {
    title: 'Tab 2',
    name: '2',
    content: 'Tab 2 content',
  },
])

const addTab = (targetName: string) => {
  const newTabName = `${++tabIndex}`
  editableTabs.value.push({
    title: 'New Tab',
    name: newTabName,
    content: editableTabs.value.length % 2 == 0 ? "/#/about" : "/#/home",
  })
  editableTabsValue.value = newTabName
}
const removeTab = (targetName: string) => {
  const tabs = editableTabs.value
  let activeName = editableTabsValue.value
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

  editableTabsValue.value = activeName
  editableTabs.value = tabs.filter((tab) => tab.name !== targetName)
}
</script>

<style scoped>
.container {
  width: calc(100vw);
  height: calc(100vh);
  background-color: cornflowerblue;
}
</style>