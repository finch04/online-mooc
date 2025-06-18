<template>
    <div class="page-wrapper">
      <div class="container">
        <Breadcrumb :data="pageTitle"></Breadcrumb>
      </div>
      <div class="content-container container bg-wt">
        <slot></slot>
      </div>
    </div>
  </template>
  <script setup>
  import Breadcrumb from '@/components/Breadcrumb.vue'
  defineProps({
    pageTitle: {
      type: String,
      default: '页面标题'
    }
  })
  </script>
  <style lang="scss" scoped>
  .page-wrapper {
    .content-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 50px;
      text-align: center;
      background: #FFFFFF;
      border: 1px solid #EEEEEE;
      border-radius: 8px;
      margin-top: 10px;
      margin-bottom: 30px;
      
      h1 {
        font-weight: 500;
        font-size: 28px;
        margin-bottom: 20px;
      }
      
      p {
        font-size: 16px;
        line-height: 1.6;
      }
    }
  }
  </style>  