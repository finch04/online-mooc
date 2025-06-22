<template>
    <div v-if="metrics" class="pad-30">
      <h3 class="font-bold text-lg mb-4">URL访问量前10名</h3>
      <div class="chart-container" ref="chartContainer" style="height: 400px;"></div>
    </div>
  </template>
  
  <script setup>
  // 代码与 BaseMetric.vue 相同，可直接复制
  import { ref, onMounted, watch } from 'vue';
  import * as echarts from 'echarts';
  
  // 定义柔和的配色方案
  const softColors = [
    '#73A6FF', // 柔和蓝
    '#FF9E7D', // 柔和橙
    '#6FC9C4', // 柔和青
    '#FFB6C1', // 柔和粉
    '#C2C2F0', // 柔和紫
    '#FFD700', // 柔和黄
  ];
  
  // 定义更深的折线图颜色方案
  const deepLineColors = [
    '#3366CC', // 深蓝
    '#FF6600', // 深橙
    '#009966', // 深青
    '#FF0066', // 深粉
    '#663399', // 深紫
    '#FFCC00', // 深黄
  ];
  
  const props = defineProps({
    metrics: {
      type: Object,
      default: () => ({
        xaxis: [
          {
            type: 'category',
            data: []
          }
        ],
        yaxis: [
          {
            type: 'value',
            max: 0,
            min: 0,
            interval: 0
          }
        ],
        series: [
          {
            name: '总访问量',
            type: 'bar',
            data: [0]
          }
        ]
      })
    }
  });
  
  const chartContainer = ref(null);
  
  // 初始化图表
  const initChart = () => {
    if (!chartContainer.value || !props.metrics) return;
    
    const myChart = echarts.init(chartContainer.value);
    
    const option = {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'cross',
          crossStyle: {
            color: '#999'
          }
        }
      },
      legend: {
        data: props.metrics.series.map(item => item.name)
      },
      xAxis: {
        type: props.metrics.xaxis[0].type,
        data: props.metrics.xaxis[0].data
      },
      yAxis: props.metrics.yaxis.map(item => ({
        type: item.type,
        max: item.max,
        min: item.min,
        interval: item.interval
      })),
      series: props.metrics.series.map((item, index) => {
        // 根据索引从配色方案中选择颜色
        let color = softColors[index % softColors.length];
        
        // 根据后端返回的 type 字段设置图表类型
        const seriesItem = {
          name: item.name,
          type: item.type || 'bar', // 默认使用柱状图
          data: item.data,
        };
        
        if (item.type === 'bar') {
          // 对于柱状图，降低颜色透明度使其变浅
          seriesItem.itemStyle = {
            color: `rgba(${parseInt(color.slice(1, 3), 16)}, ${parseInt(color.slice(3, 5), 16)}, ${parseInt(color.slice(5, 7), 16)}, 0.7)`
          };
        } else if (item.type === 'line') {
          // 对于折线图，使用更深的颜色
          color = deepLineColors[index % deepLineColors.length];
          seriesItem.smooth = true;
          seriesItem.lineStyle = {
            width: 2,
            color: color
          };
          seriesItem.itemStyle = {
            color: color
          };
          // 移除区域填充样式
          // seriesItem.areaStyle = {
          //   opacity: 0.2,
          //   color: color
          // };
        }
        
        return seriesItem;
      })
    };
    
    myChart.setOption(option);
    
    // 监听窗口大小变化，调整图表
    window.addEventListener('resize', () => {
      myChart.resize();
    });
    
    // 组件卸载时销毁图表
    return () => {
      if (myChart) {
        myChart.dispose();
      }
    };
  };
  
  // 监听指标数据变化，更新图表
  watch(() => props.metrics, () => {
    initChart();
  }, { immediate: true });
  
  onMounted(() => {
    initChart();
  });
  </script>
  
  <style scoped>
  .pad-30 {
    padding: 30px;
  }
  
  .chart-container {
    width: 100%;
    height: 400px;
  }
  </style>