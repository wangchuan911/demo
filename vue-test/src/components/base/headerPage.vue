<template>
  <a-row type="flex" justify="end">
    <template v-if="!isLogin">
      <a-col :xs="4" :sm="6" :md="7" :lg="7" :xl="7">
        <a-menu
          mode="horizontal"
          :defaultSelectedKeys="['2']"
          :style="{ lineHeight: '64px' }"
        >
          <a-menu-item key="1">nav 1</a-menu-item>
          <a-menu-item key="2">nav 2</a-menu-item>
          <a-menu-item key="3">nav 3</a-menu-item>
        </a-menu>
      </a-col>
    </template>
    <a-col :xs="4" :sm="2" :md="1" :lg="1" :xl="1">
      <a-dropdown>
        <a-avatar class="ant-dropdown-link" href="#" icon="user"/>
        <a-menu slot="overlay">
          <a-menu-item key="0">
            <a target="_blank" rel="noopener noreferrer" href="http://www.alipay.com/">注销</a>
          </a-menu-item>
          <a-menu-divider/>
          <a-menu-item key="1">
            <a target="_blank" rel="noopener noreferrer" href="http://www.taobao.com/">用户信息</a>
          </a-menu-item>
        </a-menu>
      </a-dropdown>
    </a-col>
  </a-row>
</template>

<script>
import ARow from 'ant-design-vue/es/grid/Row'
export default {
  name: 'headerPage',
  props: ['isLogin'],
  components: {ARow}
}
</script>

<style scoped>
.ant-divider,.ant-divider-horizontal{
  margin:0px
}
</style>
