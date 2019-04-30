<template>
  <a-menu id="menuView"
          mode="inline"
          :defaultSelectedKeys="['1']"
          :defaultOpenKeys="['sub1']"
          :style="{ height: '100%' }"
          @click="handleClick"
          :theme="theme"
  >
    <template v-for="(menu,index) in menus" >
      <template v-if="!(menu.value instanceof Array)">
        <a-menu-item  :key="menu.name+'-'+index">
          <a-icon :type="menu.icon"/>
          <span class="nav-text">{{menu.name}}</span>
        </a-menu-item>
      </template>
      <template v-else>
        <a-sub-menu :key="menu.name+'-'+index" >
          <span slot="title" ><a-icon type="notification"/><span class="nav-text">{{menu.name}}</span></span>
          <a-menu-item v-for="(sub,subIndex) in menu.value" :key="sub.name+'-'+subIndex">{{sub.name}}</a-menu-item>
        </a-sub-menu>
      </template>
    </template>
  </a-menu>
</template>

<script>
export default {
  name: 'siderMenu',
  props: ['menus', 'theme'],
  data () {
    return {
      a: 1
    }
  },
  methods: {
    handleClick (e) {
      /*  console.log('click ', e)
      this.current = e.key
      const val = Math.random()
      console.info(val) */
      this.$emit('click', e)
    },
    changeTheme (checked) {
      this.theme = checked ? 'dark' : 'light'
    }
  }
}
</script>

<style scoped>

</style>
