<template>
    <div class="container">
        <header_bar></header_bar>
        主页
    </div>
</template>

<script setup>
import header_bar from './head_box/header.vue'
import { useStore } from '@/store';
import { useRouter } from 'vue-router';
import { BaseApi } from '@/base_api';
import { ref, reactive, onMounted } from 'vue';

const api= new BaseApi();
const router = useRouter();
const store = useStore();

//获取用户信息
const get_user_info=async()=>{
    try{
    const res=await api.post('user',{

        })
        console.log(res);
        if(res.status==200){
            store.$state.user=res.result;
        }
        else{
            console.log('获取用户信息失败');
        }
    }
    catch(e){
        console.log(e);
    }
}

onMounted(()=>{
    get_user_info();
})

</script>

<style lang="css" scoped>

</style>