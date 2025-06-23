<template>
  <div class="funnel-chart-container pad-30" v-if="metrics">
    <div ref="chartRef" class="echarts-funnel" :style="{ height: height }"></div>
    <div class="chart-summary" v-if="showSummary">
      <div class="summary-item" v-for="(item, index) in chartData" :key="index">
        <span class="summary-label">{{ item.name }}</span>
        <span class="summary-value">{{ item.value }}</span>
        <span class="summary-percentage" v-if="index > 0">
          (转化率: {{ ((item.value / chartData[index - 1].value) * 100).toFixed(2) }}%)
        </span>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, watch, nextTick } from 'vue';
import * as echarts from 'echarts';

export default {
  name: 'EchartsFunnel',
  props: {
    metrics: {
      type: Object,
      required: true
    },
    title: {
      type: String,
      default: '转化漏斗分析'
    },
    height: {
      type: String,
      default: '400px'
    },
    showSummary: {
      type: Boolean,
      default: true
    }
  },
  setup(props) {
    const chartRef = ref(null);
    const chartInstance = ref(null);
    
    const formatChartData = () => {
      if (!props.metrics || !props.metrics.value) return [];
      return props.metrics.value.map(item => ({ name: item.name, value: item.value }));
    };
    
    const chartData = ref(formatChartData());
    
    const initChart = async () => {
      await nextTick();
      if (!chartRef.value) return;
      
      chartInstance.value = echarts.init(chartRef.value);
      
      const option = {
        title: {
          text: props.title,
          left: 'center'
        },
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        toolbox: {
          show: true,
          // 新增配置：设置工具箱位置为左上角
          // right: '10%',
          top: '10%',
          feature: {
            saveAsImage: {}
          }
        },
        series: [
          {
            name: '转化漏斗',
            type: 'funnel',
            left: '10%',
            right: '10%',
            top: '15%',
            bottom: '10%',
            min: 0,
            max: 100,
            minSize: '0%',
            maxSize: '100%',
            sort: 'descending',
            gap: 2,
            label: {
              show: true,
              position: 'inside'
            },
            labelLine: {
              length: 10,
              lineStyle: { width: 1, type: 'solid' }
            },
            itemStyle: { borderColor: '#fff', borderWidth: 1 },
            emphasis: { label: { fontSize: 16 } },
            data: chartData.value
          }
        ]
      };
      
      chartInstance.value.setOption(option);
      window.addEventListener('resize', () => chartInstance.value.resize());
    };
    
    watch(() => props.metrics, () => {
      chartData.value = formatChartData();
      initChart();
    }, { deep: true });
    
    onMounted(initChart);
    
    const onUnmounted = () => {
      if (chartInstance.value) chartInstance.value.dispose();
    };
    
    return { chartRef, chartData };
  }
};
</script>

<style scoped>
.pad-30 { padding: 30px; }
.echarts-funnel { width: 100%; }
.chart-summary { display: flex; justify-content: center; margin-top: 20px; flex-wrap: wrap; }
.summary-item { margin: 0 15px; padding: 8px 15px; background-color: #f5f7fa; border-radius: 4px; display: flex; align-items: center; }
.summary-label { font-weight: 500; margin-right: 10px; color: #606266; }
.summary-value { font-weight: 600; color: #303133; margin-right: 5px; }
.summary-percentage { color: #909399; font-size: 12px; }
</style>