<template>
  <h5 class="mb-2">Default colors</h5>
  <el-menu
      default-active="2"
      class="el-menu-vertical-demo"
      @open="handleOpen"
      @close="handleClose"

  >
    <tempate v-for="menu in menus" :key="menu.id">
      <el-sub-menu v-if="menu.sub!=null" :index="menu.id+''">
        <template #title>
          <el-icon>
            <component :is="icons[menu.icon]"></component>
          </el-icon>
          <span>{{ menu.name }}</span>
        </template>
        <template v-for="sub1 in menu.sub" :key="sub1.id+''">
          <el-menu-item-group v-if="sub1.group!=null" :title="sub1.name">
            <el-menu-item v-for="group1 in sub1.group" :key="group1.id+''" :index="group1.id+''" @click="group1.click">
              {{ group1.name }}
            </el-menu-item>
          </el-menu-item-group>
          <el-sub-menu v-else-if="sub1.sub!=null" :index="sub1.id+''">
            <template #title>{{ sub1.name }}</template>
            <template v-for="sub2 in sub1.sub" :key="sub2.id+''">
              <el-menu-item :index="sub2.id+''" @click="sub2.click">{{ sub2.name }}</el-menu-item>
            </template>
          </el-sub-menu>
          <el-menu-item v-else :index="sub1.id+''" @click="sub1.click">{{ sub1.name }}</el-menu-item>
        </template>
      </el-sub-menu>
      <el-menu-item v-else :index="menu.id+''" @click="menu.click">
        <el-icon>
          <component :is="icons[menu.icon]"></component>
        </el-icon>
        <span>{{ menu.name }}</span>
      </el-menu-item>
    </tempate>
  </el-menu>
</template>

<script lang="ts" setup>
import {reactive} from "vue"
import * as icons from '@element-plus/icons-vue'
import {useRouter} from "vue-router";

console.log(icons)
const router = useRouter()
const menus = reactive(new Array<Menu>())

export declare interface Menu {
  id: number;
  name: string;
  icon?: any;
  sub?: Array<Menu>;
  group?: Array<Menu>;

  click?: any
}

const _menus = new Array<Menu>({
  id: 1,
  name: "定义",
  icon: 'Location',
  sub: [
    {
      id: 5,
      name: "表", click: () => {
        router.push("/index/object-query")
      }
    },
    {
      id: 8,
      name: "item two",
      group: [
        {id: 9, name: "item three"}
      ]
    },
    {
      id: 10,
      name: "item four",
      sub: [
        {id: 11, name: "item one"}
      ]
    }
  ]
}, {
  id: 2,
  name: "Navigator Two",
  icon: 'Menu',
  click: () => {
    router.push('/index/about')
  }
}, {
  id: 3,
  name: "Navigator Three",
  icon: 'Document',
  click() {
    router.push('/index/detail')
  }
}, {
  id: 4,
  name: "Navigator Four",
  icon: 'Setting',
  click() {
    router.push('/index/object-query')
  }
})
menus.push(..._menus)


const handleOpen = (key: string, keyPath: string[]) => {
  console.log(key, keyPath)
}
const handleClose = (key: string, keyPath: string[]) => {
  console.log(key, keyPath)
}
</script>