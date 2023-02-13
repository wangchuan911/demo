<template>
  <div class="about">
    <h1>This is an about page({{ msg }})</h1>
  </div>
</template>
<script lang="ts" setup>
import {ref, onActivated} from 'vue'

const msg = ref(1)
onActivated(() => {
  msg.value++;
})
</script>