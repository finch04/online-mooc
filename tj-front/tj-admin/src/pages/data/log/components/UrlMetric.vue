<template>
  <div v-if="metrics" class="pad-30">
    <h3 class="font-bold text-lg mb-4">URL 指标数据</h3>
    <div class="chart-container" ref="chartContainer" style="height: 400px;"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import * as echarts from 'echarts';

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
    series: props.metrics.series.map(item => ({
      name: item.name,
      type: 'bar',
      data: item.data,
      itemStyle: {
        color: item.name === '总访问量' ? '#409EFF' : '#F56C6C'
      }
    }))
  };
  
  myChart.setOption(option);
  
  // 监听窗口大小变化，调整图表
  window.addEventListener('resize', () => {
    myChart.resize();
  });
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