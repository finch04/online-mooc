<template>
    <div v-if="metrics" class="pad-30">
        <h3 class="font-bold text-lg mb-4">{{ title }}</h3>
        <div class="chart-container" ref="chartContainer" style="height: 400px;"></div>
    </div>
</template>

<script setup>
import { ref, onMounted, watch, nextTick } from 'vue';
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
            series: [
                {
                    name: '总访问量',
                    type: 'bar',
                    data: [0]
                }
            ]
        })
    },
    title: {
        type: String,
        default: '基础流量数据'
    }
});

const chartContainer = ref(null);

// 初始化图表
const initChart = async () => {
    // 等待 DOM 更新后再初始化图表
    await nextTick();

    console.log('初始化图表', props.metrics);
    if (!chartContainer.value || !props.metrics) return;

    const myChart = echarts.init(chartContainer.value);

    // 处理系列数据
    const series = props.metrics.series.map((item, index) => {
        // 根据索引从配色方案中选择颜色
        let color = softColors[index % softColors.length];

        const seriesItem = {
            name: item.name,
            type: item.type || 'bar', // 默认使用柱状图
            data: item.data,
        };

        // 饼图特殊配置
        if (item.type === 'pie') {
            seriesItem.radius = '50%';
            seriesItem.itemStyle = {
                color: (params) => softColors[params.dataIndex % softColors.length]
            };
            seriesItem.label = {
                show: true,
                formatter: '{b}: {c} ({d}%)'
            };
            seriesItem.emphasis = {
                itemStyle: {
                    shadowBlur: 10,
                    shadowOffsetX: 0,
                    shadowColor: 'rgba(0, 0, 0, 0.5)'
                }
            };
        } 
        // 柱状图特殊配置
        else if (item.type === 'bar') {
            seriesItem.itemStyle = {
                color: `rgba(${parseInt(color.slice(1, 3), 16)}, ${parseInt(color.slice(3, 5), 16)}, ${parseInt(color.slice(5, 7), 16)}, 0.7)`
            };
            seriesItem.barWidth = '40%'; // 柱状图宽度
        } 
        // 折线图特殊配置
        else if (item.type === 'line') {
            color = deepLineColors[index % deepLineColors.length];
            seriesItem.smooth = true;
            seriesItem.lineStyle = {
                width: 2,
                color: color
            };
            seriesItem.itemStyle = {
                color: color
            };
            seriesItem.areaStyle = {
                opacity: 0.1,
                color: color
            };
        }

        return seriesItem;
    });

    // 处理坐标轴
    const xAxis = props.metrics.xaxis || [{ type: 'category', data: [] }];
    const yAxis = props.metrics.yaxis || [{ type: 'value' }];

    // 根据图表类型调整 tooltip
    const hasPieChart = props.metrics.series.some(item => item.type === 'pie');
    const tooltip = {
        trigger: hasPieChart ? 'item' : 'axis',
        axisPointer: {
            type: 'cross',
            crossStyle: {
                color: '#999'
            }
        }
    };

    // 配置图例
    const legend = {
        data: props.metrics.series.map(item => item.name)
    };

    const option = {
        tooltip,
        legend,
        xAxis,
        yAxis,
        series,
        grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
        }
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
watch(() => props.metrics, async () => {
    await initChart();
}, { immediate: true });

onMounted(async () => {
    await initChart();
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