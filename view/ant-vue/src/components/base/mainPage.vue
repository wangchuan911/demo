<template>
  <a-layout id="components-layout-demo-fixed-sider" ref="container" >
      <a-affix>
      <a-layout-sider id="menu-sider" class ="disableScroll" :style="{ height: '100vh', position: 'relative', left: 0 }" @mouseenter.native="menuMouseEnter"
                      :trigger="null"
                      collapsible
                      v-model="collapsed"
      >
        <a-row type="flex" justify="center" align="middle" style="height: 7%">
          <a-col :xs="16" :sm="16" :md="8" :lg="8" :xl="8" >
            <img class="ant-col-24" src="../../assets/logo.png"  style="height: 100%"/>
          </a-col>
        </a-row>
        <sider-menu class="sider-height"  :menus="menus" theme="dark" @click="handleClick"></sider-menu>
      </a-layout-sider>
      </a-affix>
    <a-layout @mouseenter.native.stop="menuMouseOut">
      <a-layout-header id="header" :style="{ background: '#fff', padding: 0 }">
        <a-row type="flex" justify="center">
          <a-col :span="1" style="text-align: center;font-size: 24px">
            <a-icon
              class="trigger"
              :type="collapsed ? 'menu-unfold' : 'menu-fold'"
              @click="()=> collapsed = !collapsed"
            />
          </a-col>
          <a-col :span="23">
            <header-page></header-page>
          </a-col>
        </a-row>
        <a-divider/>
      </a-layout-header>
      <a-layout-content id="layoutContent" :style="{ margin: '5px 16px 0', overflow: 'initial' }">

        <a-tabs
          v-model="activeKey"
          type="editable-card"
          @edit="onEdit"
          @change="tabsChange"
          :size="size"
          hideAdd
        >
          <a-tab-pane v-for="pane in panes" :tab="pane.title" :key="pane.key" :closable="pane.closable">
            <div class="compContent" :style="{ padding: '24px', background: '#fff', textAlign: 'center' ,marginTop:'-16px',height : contentHeight,overflow:'auto'}">
              <component v-if="pane.type=='comp'" :is="pane.content"></component>
              <template v-else-if="pane.content=='html'" v-html="pane.content"></template>
              <div v-else>{{pane.content}}a</div>
            </div>
          </a-tab-pane>
        </a-tabs>
      </a-layout-content>
      <a-layout-footer :style="{ textAlign: 'center' }">
        Ant Design ©2018 Created by Ant UED
      </a-layout-footer>
    </a-layout>
  </a-layout>
</template>

<script>

import ALayoutSider from 'ant-design-vue/es/layout/Sider'
import ACol from 'ant-design-vue/es/grid/Col'
import Affix from 'ant-design-vue/lib/affix/index.js'
import ATabs from 'ant-design-vue/es/tabs/tabs'
import SiderMenu from './siderMenu'
import ARow from 'ant-design-vue/es/grid/Row'
import HeaderPage from './headerPage'

export default {
  name: 'mainPage',
  data: function () {
    const indexPage = { title: '首页', content: 'Content of Tab 1', key: '1', closable: false }
    const panes = [ ]
    panes.push(indexPage)
    return {
      activeKey: panes[0].key,
      panes,
      indexPage: indexPage,
      newTabIndex: 0,
      size: 'small',
      msg: 'Welcome',
      collapsed: false,
      menus: [
        {name: 'test1', value: 'testView1', icon: 'user', type: 'comp'},
        {name: 'test2', value: 'testView2', icon: 'video-camera'},
        {name: 'test3', value: 'testView2', icon: 'upload'},
        {name: 'test4', value: 'testView2', icon: 'bar-chart'},
        {name: 'test5', value: 'testView2', icon: 'cloud-o'},
        {name: 'test6', value: 'testView2', icon: 'appstore-o'},
        {name: 'test7', value: 'testView2', icon: 'team'},
        {name: 'test8', value: 'testView2', icon: 'shop'},
        {name: 'test9',
          value: [
            {name: 'option1', value: 'option1'},
            {name: 'option2', value: 'option1'},
            {name: 'option3', value: 'option1'},
            {name: 'option4', value: 'option1'},
            {name: 'option5', value: 'option1'},
            {name: 'option6', value: 'option1'},
            {name: 'option6', value: 'option1'},
            {name: 'option6', value: 'option1'}
          ],
          icon: 'shop'}
      ],
      contentHeight: '80vh'
    }
  },
  mounted () {
    console.info('mount')
    /* let layout = document.getElementById('layoutContent')
    this.contentHeight = (layout.clientHeight - document.getElementsByClassName('ant-tabs-bar')[0].clientHeight) + 'px'
    window.onresize = function () {
      layout = document.getElementById('layoutContent')
      this.contentHeight = (layout.clientHeight - document.getElementsByClassName('ant-tabs-bar')[0].clientHeight) + 'px'
    } */
  },
  components: {HeaderPage, ARow, SiderMenu, ACol, ALayoutSider, Affix, ATabs},
  methods: {
    menuMouseEnter (e) {
      console.log('menuMouseEnter ', e.target)
      let sider = document.getElementById('menu-sider')
      sider.classList.add('enableScroll')
      sider.classList.remove('disableScroll')
    },
    menuMouseOut (e) {
      console.log('menuMouseOut ', e.target)
      let sider = document.getElementById('menu-sider')
      sider.classList.remove('enableScroll')
      sider.classList.add('disableScroll')
    },
    handleClick (e) {
      console.log('click ', e)
      let keyPath = e.keyPath
      var menus = this.menus
      var value = null
      for (let i = keyPath.length - 1; i >= 0; i--) {
        var idx = parseInt(keyPath[i].substr(keyPath[i].lastIndexOf('-') + 1))
        var val = menus[idx].value
        if (val instanceof Array) {
          menus = val
          continue
        }
        value = menus[idx]
      }
      this.add({
        name: value.name,
        content: value.value,
        type: value.type
      })
    },
    top () {
      console.info(arguments)
    },
    callback (key) {
      console.log(key)
    },
    onEdit (targetKey, action) {
      this[action](targetKey)
    },
    add (tab) {
      const panes = this.panes
      let activeKey = null
      this.panes.forEach((pane, i) => {
        if (pane.title === tab.name && pane.content === tab.content && pane.type === tab.type) {
          activeKey = pane.key
        }
      })
      if (!activeKey) {
        panes.push({title: tab.name, content: tab.content, type: tab.type, key: activeKey = `tab${this.newTabIndex++}`})
        this.panes = panes
      }
      this.activeKey = activeKey
    },
    remove (targetKey) {
      let activeKey = this.activeKey
      let lastIndex
      this.panes.forEach((pane, i) => {
        if (pane.key === targetKey) {
          lastIndex = i - 1
        }
      })
      const panes = this.panes.filter(pane => pane.key !== targetKey)
      if (lastIndex >= 0 && activeKey === targetKey) {
        activeKey = panes[lastIndex].key
      }
      this.panes = panes
      this.activeKey = activeKey
    },
    tabsChange (activeKey) {
      console.info(activeKey)
    },
    goIndex () {
      let hasIndexPage = false
      if (this.panes.length > 0) {
        this.panes.forEach(pane => {
          hasIndexPage = hasIndexPage || (pane.key === 1)
        })
      }
      if (!hasIndexPage) {
        this.panes.push(this.indexPage)
      }
      this.activeKey = 1
    }
  }
}
</script>

<style scoped>
  #components-layout-demo-basic {
    text-align: center;
  }

  #components-layout-demo-basic .ant-layout-header,
  #components-layout-demo-basic .ant-layout-footer {
    background: #7dbcea;
    color: #fff;
  }

  #components-layout-demo-basic .ant-layout-footer {
    line-height: 1.5;
  }

  #components-layout-demo-basic .ant-layout-sider {
    background: #3ba0e9;
    color: #fff;
    line-height: 120px;
  }

  #components-layout-demo-basic .ant-layout-content {
    background: rgba(16, 142, 233, 1);
    color: #fff;
    min-height: 120px;
    line-height: 120px;
  }

  #components-layout-demo-basic > .ant-layout {
    margin-bottom: 48px;
  }

  #components-layout-demo-basic > .ant-layout:last-child {
    margin: 0;
  }
  .sider-height {
    height:auto!important;
  }
  .enableScroll{
    overflow-y: auto;
  }
  .disableScroll{
    overflow-y: hidden;
  }
  .ant-divider,.ant-divider-horizontal{
    margin:0px
  }
</style>
