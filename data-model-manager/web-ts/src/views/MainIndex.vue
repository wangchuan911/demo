<template>
  <el-container class="container">
    <el-header class="header">Header</el-header>
    <el-container style="width: 100%;height: 100%">
      <el-aside width="200px">Aside</el-aside>
      <el-main>
        <el-tabs type="border-card" style="height: calc(100vh - 110px)">
          <el-tab-pane label="User">User</el-tab-pane>
          <el-tab-pane label="Config">Config</el-tab-pane>
          <el-tab-pane label="Role">Role</el-tab-pane>
          <el-tab-pane label="Task">Task</el-tab-pane>
        </el-tabs>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
// eslint-disable-next-line vue/no-export-in-script-setup

</script>

<style scoped>
.container {
  width: calc(100vw);
  height: calc(100vh);
  background-color: cornflowerblue;
}
</style>