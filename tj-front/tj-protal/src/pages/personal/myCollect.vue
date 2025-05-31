<!-- 我的收藏 -->
<template>
  <div class="myCollectionWrapper">
    <div class="personalCards" v-if="data != null">
      <CardsTitle class="marg-bt-20" title="我的收藏" />
      <div v-if="count == 0" class="nodata">
        <Empty />
      </div>
      <!-- 使用 v-for 遍历 data 数组 -->
      <div class="classCards fx-sb fx-ct" v-for="item in data" :key="item.id">
        <div class="marg-rt-20">
          <img :src="item.courseCoverUrl" alt="" @click="$router.push({path: '/details/index', query: {id: item.courseId}})">
        </div>
        <div class="info fx-1">
          <div class="tit " >{{item.courseName}}</div>
          <div>节数 ：{{item.sections}}</div>
          <div>收藏时间 ：{{item.createTime}}</div>
        </div>
        <div class="fx-ct info">
           <span class="bt bt-round" @click="addCollect(item.courseId)" >取消收藏</span>
        </div>
      </div>
      <div class="pageination" v-if="count > 0">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="count"
          class="mt-4"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
/** 数据导入 **/
import { onMounted, ref, reactive } from "vue";
import { ElMessage ,ElMessageBox} from "element-plus";
import { getMyCollect, addMyCollect } from "@/api/class.js";
import { useRouter } from "vue-router";

// 组件导入
import CardsTitle from './components/CardsTitle.vue'
import Empty from "@/components/Empty.vue";

const router = useRouter();

// mounted生命周期
onMounted(async () => {
  // 查询我的收藏记录
  getCollectionListData()
});

/** 方法定义 **/

// 查询我的收藏记录
const data = ref(null)
const count = ref(0)
const params = reactive({
  pageNo: 1,
  pageSize: 10,
})

const addCollect = async (courseId) => { 
  //确定要取消收藏吗
  ElMessageBox.confirm('确定要取消收藏吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
      addMyCollect({
      courseId: courseId,
      collected: false
    }).then(res => {
      if (res.code == 200) {
        ElMessage({
          message: "取消收藏成功！",
          type: 'success'
        });
        getCollectionListData()
      }
    }).catch(() => {
      ElMessage({
        message: "取消收藏失败！",
        type: 'error'
      })
    })
  }).catch(() => {
    
  });
  
}
// 查询我的收藏记录
const getCollectionListData = async () => {
  await getMyCollect(params)
    .then((res) => {
      if (res.code == 200 && res.data != null) {
        data.value = res.data.list
        console.log(data.value)
        count.value = Number(res.data.total)
      }
    })
    .catch(() => {
      ElMessage({
        message: "收藏数据请求出错！",
        type: 'error'
      });
    });
}

const handleSizeChange = (val) => {
  params.pageSize = val
  getCollectionListData()
}

const handleCurrentChange = (val) => {
  params.pageNo = val
  getCollectionListData()
}

</script>

<style lang="scss">
.myCollectionWrapper {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;

  .personalCards {
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    padding: 20px;

    .marg-bt-20 {
      margin-bottom: 20px;
    }

    .nodata {
      text-align: center;
      padding: 40px 0;
    }

    .pageination {
      text-align: center;
      margin-top: 20px;
    }

    img {
      width: 236px;
      height: 132px;
      border-radius: 8px;
      object-fit: cover; // 确保图片填充容器且不变形
    }

    .info {
      line-height: 30px;
      font-size: 14px;

      span {
        display: inline-block;
        color: #ffffff;
        min-width: 85px;
      }

      .tit {
        font-size: 20px;
        font-weight: 500;
        line-height: 40px;
      }
    }
  }
}
</style>