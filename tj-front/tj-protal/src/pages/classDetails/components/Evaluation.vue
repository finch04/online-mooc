<template>
    <div class="evaluation bg-wt marg-bt-20">
        <div class="tabLab fx-sb">
            <div class="lable">
                <span @click="askCheck('all')" :class="{ act: askType == 'all' }" class="marg-rt-20">全部评价</span>
                <span @click="askCheck('my')" :class="{ act: askType == 'my' }" >我的评价</span>
            </div>
            <div class="lable">
                <!-- <el-button class="bt-round" type="primary" @click="openAddEvaluationDialog">
                    评价课程
                </el-button> -->
                <div class="bt bt-round ft-14" v-if="!isEvaluated" @click="openAddEvaluationDialog">评价课程</div>
            </div>
        </div>

        <el-dialog style="width: 40%;" v-model="dialogVisible" :title="dialogTitle">
            <div class="dialogCont">
                <el-form :model="evaluationForm" ref="formRef" label-width="100px" :rules="rules">
                    <el-form-item label="内容评分" prop="contentRating">
                        <el-rate :texts="rateText" show-text v-model="evaluationForm.contentRating" :max="5"></el-rate>
                    </el-form-item>
                    <el-form-item label="教学评分" prop="teachingRating">
                        <el-rate :texts="rateText" show-text v-model="evaluationForm.teachingRating" :max="5"></el-rate>
                    </el-form-item>
                    <el-form-item label="难度评分" prop="difficultyRating">
                        <el-rate :texts="rateText" show-text v-model="evaluationForm.difficultyRating"
                            :max="5"></el-rate>
                    </el-form-item>
                    <el-form-item label="价值评分" prop="valueRating">
                        <el-rate :texts="rateText" show-text v-model="evaluationForm.valueRating" :max="5"></el-rate>
                    </el-form-item>
                    <el-form-item label="评价内容" prop="comment">
                        <el-input v-model="evaluationForm.comment" type="textarea" placeholder="请输入评价内容（至少10个字）"
                            :minlength="10"></el-input>
                    </el-form-item>
                    <el-form-item label="是否匿名" prop="anonymity">
                        <el-switch v-model="evaluationForm.anonymity"></el-switch>
                    </el-form-item>
                </el-form>
            </div>
            <template #footer>
                <div class="dialog-footer">
                    <div class="bt bt-round ft-14" @click="dialogVisible = false">取消</div>
                    <div class="bt bt-round ft-14" @click="submitForm">提交</div>
                </div>
            </template>
        </el-dialog>
        <div class="askCont">
            <div class="askLists" v-for="item in evaluationListsDataes">
                <div class="userInfo fx">
                    <img v-if="item.userIcon" :src="item.userIcon" alt="">
                    <img v-else src="/src/assets/anonymity.png" alt="" srcset="">
                    {{ item.userName || "匿名用户" }} 
                    <div v-if="item.teacherName" class="ft-12" style="margin-left: 10px;">评价教师：{{ item.teacherName }}</div>
                </div>
                <div class="evaluationInfo">
                    <div class="content-area">
                        <div class="comment-content ft-16">{{ item.comment }}</div>
                    </div>
                    <el-tooltip placement="top" effect="light">
                        <template #content>
                            <div class="rating-details">
                                <div>内容评分:
                                    <el-rate v-model="item.contentRating" disabled show-score text-color="#ff9900" />
                                </div>
                                <div>教学评分:
                                    <el-rate v-model="item.teachingRating" disabled show-score text-color="#ff9900" />
                                </div>
                                <div>难度评分:
                                    <el-rate v-model="item.difficultyRating" disabled show-score text-color="#ff9900" />
                                </div>
                                <div>价值评分:
                                    <el-rate v-model="item.valueRating" disabled show-score text-color="#ff9900" />
                                </div>
                            </div>
                        </template>
                        <div class="overall-rating-container">
                            <div class="overall-rating ft-16">{{ item.overallRating }}</div>
                            <div class="rating-text">综合评分</div>
                        </div>
                    </el-tooltip>
                </div>
                <div class="time fx-sb">
                    <div>{{ item.createTime }}</div>
                    <div class="actBut">
                        <span class="marg-rt-20" @click="editNoteHandle(item)" v-if="userInfo.id == item.userId">
                            <i class="iconfont zhy-a-icon-xiugai22x"></i> 编辑
                        </span>
                        <span class="marg-rt-20" @click="delNoteHandle(item)" v-if="userInfo.id == item.userId">
                            <i class="iconfont zhy-a-icon-delete22x"></i> 删除
                        </span>
                        <span @click="helpfulHandle(item)" :class="{ activeLiked: item.isHelpful }">
                            <i class="iconfont zhy-a-btn_zan_nor2x"></i>
                            {{ item.isHelpful ? '已认为有用' : '有用' }}
                            {{ item.helpCount || 0 }}
                        </span>
                    </div>
                </div>
            </div>
            <div class="pagination fx-ct" v-if="total > 10">
                <el-pagination v-model:currentPage="params.pageNo" v-model:page-size="params.pageSize" background
                    :page-sizes="[10, 20, 50, 100]" layout="total, prev, pager, next, sizes, jumper" :total="total"
                    @size-change="handleSizeChange" @current-change="handleCurrentChange" />
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from "vue"
import { ElMessage, ElMessageBox } from "element-plus";
import { useUserStore, isLogin } from '@/store'
import { useRoute, useRouter } from "vue-router";
import { getEvaluationList, postEvaluation, getEvaluationDetail, updateEvaluation, deleteEvaluation,isEvaluatedByCourseId,putLiked } from "../../../api/classDetails";

const rateText = ref(['太差劲啦', '较差', '一般', '不错', '太好啦'])
const props = defineProps({
    id: {
        type: String
    }
})
const route = useRoute()
const router = useRouter()
const store = useUserStore();
const userInfo = ref();
const dialogTitle = ref('新增课程评价');
const currentEditId = ref(null);

// Validation rules
const rules = {
    contentRating: [
        { required: true, message: '请选择内容评分', trigger: 'change' },
        { type: 'number', min: 1, message: '评分不能为0', trigger: 'change' }
    ],
    teachingRating: [
        { required: true, message: '请选择教学评分', trigger: 'change' },
        { type: 'number', min: 1, message: '评分不能为0', trigger: 'change' }
    ],
    difficultyRating: [
        { required: true, message: '请选择难度评分', trigger: 'change' },
        { type: 'number', min: 1, message: '评分不能为0', trigger: 'change' }
    ],
    valueRating: [
        { required: true, message: '请选择价值评分', trigger: 'change' },
        { type: 'number', min: 1, message: '评分不能为0', trigger: 'change' }
    ],
    comment: [
        { required: true, message: '请输入评价内容', trigger: 'blur' },
        { min: 10, message: '评价内容至少10个字', trigger: 'blur' }
    ]
};

onMounted(() => {
    console.log("是否登录：",isLogin())
    if (isLogin()==true) {    // 获取登录信息中的我的信息
        userInfo.value = store.getUserInfo
        getEvaluationListsDataes()
        isEvaluatedCourse()
    }
})

const params = ref({
    isAsc: true,
    pageNo: 1,
    pageSize: 10,
    courseId: route.query.id,
    teacherId: null,
    onlyMine: false
});

const evaluationListsDataes = ref([])
const total = ref(0)
const askType = ref('all')

const askCheck = type => {
    params.value.pageNo = 1
    params.value.pageSize = 10
    params.value.onlyMine = type === 'my'
    askType.value = type
    getEvaluationListsDataes()
}

//是否已经评价过该课程
const isEvaluated = ref(false)
const isEvaluatedCourse = async () => {
    const res = await isEvaluatedByCourseId( route.query.id)
    isEvaluated.value = res.data
}
const getEvaluationListsDataes = async () => {
    await getEvaluationList(params.value)
        .then((res) => {
            if (res.code == 200) {
                evaluationListsDataes.value = res.data.list
                total.value = Number(res.data.total)
            } else {
                ElMessage.error(res.msg || "获取评价列表失败");
            }
        })
        .catch(() => {
            ElMessage.error("评价列表数据请求出错！");
        });
}

const dialogVisible = ref(false);
const evaluationForm = ref({
    courseId: null,
    teacherId: null,
    contentRating: null,
    teachingRating: null,
    difficultyRating: null,
    valueRating: null,
    comment: '',
    anonymity: false
});
const formRef = ref(null);

const openAddEvaluationDialog = () => {
    dialogTitle.value = '新增课程评价';
    currentEditId.value = null;
    evaluationForm.value = {
        courseId: route.query.id,
        teacherId: null,
        contentRating: null,
        teachingRating: null,
        difficultyRating: null,
        valueRating: null,
        comment: '',
        anonymity: false
    };
    dialogVisible.value = true;
};

const editNoteHandle = async (item) => {
    dialogTitle.value = '编辑课程评价';
    currentEditId.value = item.id;
    try {
        const res = await getEvaluationDetail(item.id);
        if (res.code === 200) {
            evaluationForm.value = {
                ...res.data,
                courseId: route.query.id
            };
            dialogVisible.value = true;
        } else {
            ElMessage.error(res.msg || "获取评价详情失败");
        }
    } catch (error) {
        ElMessage.error("获取评价详情出错");
    }
};

const submitForm = async () => {
    if (!formRef.value) return;

    try {
        await formRef.value.validate();

        if (currentEditId.value) {
            // Update existing evaluation
            const res = await updateEvaluation(currentEditId.value, evaluationForm.value);
            if (res.code === 200) {
                ElMessage.success('评价更新成功');
                dialogVisible.value = false;
                getEvaluationListsDataes();
            } else {
                ElMessage.error(res.msg || "评价更新失败");
            }
        } else {
            // Create new evaluation
            const res = await postEvaluation(evaluationForm.value);
            if (res.code === 200) {
                ElMessage.success('评价提交成功');
                dialogVisible.value = false;
                isEvaluated.value=true;
                getEvaluationListsDataes();
            } else {
                ElMessage.error(res.msg || "评价提交失败");
            }
        }
    } catch (error) {
        console.error("表单验证失败:", error);
    }
};

const delNoteHandle = async (item) => {
    //删除前提醒
    ElMessageBox.confirm(
        `您确认删除该评价吗，点击确认将永久消失？`,
        '确认删除',
        {
            confirmButtonText: '删除',
            cancelButtonText: '取消',
            type: 'delete',
        }
    )
        .then(() => {
            deleteEvaluation(item.id);
            getEvaluationListsDataes();
        })
};


const helpfulHandle = async item => {
    await putLiked({bizId:item.id, liked:!item.isHelpful, bizType: "COMMENT"})
    .then((res) => {
      if (res.code == 200) {
        item.isHelpful = !item.isHelpful
        item.isHelpful ? item.helpCount++ : item.helpCount--
      } else {
        ElMessage({
          message:res.msg,
          type: 'error'
        });
      }
    })
    .catch(() => {
      ElMessage({
        message: "点赞请求出错！",
        type: 'error'
      });
    });
}

const handleSizeChange = (val) => {
    params.value.pageSize = val;
    getEvaluationListsDataes();
}

const handleCurrentChange = (val) => {
    params.value.pageNo = val;
    getEvaluationListsDataes();
}
</script>

<style lang="scss" scoped>
.evaluation {
    padding-top: 30px;

    .tabLab {
        margin-bottom: 10px;

        .lable {
            display: flex;
            align-items: center;

            .bt-round {
                width: 150px;
                padding: 5px;
                border-radius: 20px;

                .iconfont {
                    margin-right: 5px;
                }
            }
        }

        .act {
            background: #19232B;
            border-radius: 20px;
            color: #fff;
            font-size: 14px;
            font-weight: 600;
            padding: 5px 20px;
        }
    }

    .askCont {
        padding-top: 20px;

        .askLists {
            line-height: 40px;
            font-size: 14px;

            .userInfo {
                line-height: 26px;
                color: var(--color-font3);

                img {
                    width: 26px;
                    height: 26px;
                    border-radius: 26px;
                    margin-right: 10px;
                }
            }

            .evaluationInfo {
                display: flex;
                justify-content: space-between;
                align-items: flex-start;
                padding-left: 36px;
                gap: 20px;

                .content-area {
                    flex: 1;
                    display: flex;
                    flex-direction: column;
                    gap: 8px;

                    .teacher-name {
                        font-weight: bold;
                        color: var(--color-font1);
                    }

                    .comment-content {
                        margin-top: 10px;
                        line-height: 1.6;
                    }
                }

                .overall-rating-container {
                    display: flex;
                    flex-direction: column;
                    align-items: center;
                    min-width: 100px;
                    cursor: pointer;

                    .overall-rating {
                        font-weight: bold;
                        font-size: 24px;
                        color: #FF9900;
                        text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.1);
                    }

                    .rating-text {
                        font-size: 12px;
                        color: var(--color-font3);
                        margin-top: 4px;
                    }
                }
            }

            .time {
                color: var(--color-font3);
                width: calc(100% - 36px);
                margin-left: 36px;
                padding-bottom: 20px;
                border-bottom: 1px solid #EEEEEE;
                margin-bottom: 20px;

                .actBut {
                    color: #19232B;
                    cursor: pointer;

                    span {
                        margin-right: 10px;

                        &:last-child {
                            margin-right: 0;
                        }
                    }
                }
            }
        }
    }

    .activeLiked {
        color: var(--color-main)
    }

    .pagination {
        padding-top: 40px;
        text-align: center;
    }
}

.rating-details {
    padding: 10px;
    line-height: 1.8;

    div {
        min-width: 120px;
    }
}

.dialog-footer {
    display: flex;
    justify-content: center;

    .bt-round {
        width: 20%;
        // padding: 8px;
        margin-right: 10px;
        border-radius: 10px;
    }
}
</style>