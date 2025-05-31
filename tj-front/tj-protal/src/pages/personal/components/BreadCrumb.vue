<template>
    <div class="marg-bt-20">
        <span class="ft-14">
            <span class="font-bt2"  @click="()=> route.meta.parent?$router.go(-2):$router.go(-1)">{{route.meta.title}}</span>
            <span class="font-bt2" @click="()=> $router.go(-1)" v-if="route.meta.parent"> /  {{route.meta.parent}}</span>
            <span> /  {{route.meta.current}}</span>
        </span>
    </div>
</template>
<script setup>
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const router = useRouter()
const route = useRoute() 
</script>

