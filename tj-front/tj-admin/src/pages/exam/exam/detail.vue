<template>
    <div class="contentBox">
        <div class="bg-wt radius marg-tp-20">
            <div class="detailBox">
                <!-- 基本信息 -->
                <div class="examBasicInfo">
                    <div class="tit">考试批阅详情</div>
                    <div class="infoTable">
                        <div class="fx-sb">
                            <div class="td fx-1">
                                <div class="marg-bt-10 ft-wt-600 ft-cl-1">所属课程</div>
                                <div>{{ $route.query.courseName }}</div>
                            </div>
                            <div class="td fx-1">
                                <div class="marg-bt-10 ft-wt-600 ft-cl-1">测试名称</div>
                                <div>{{ $route.query.sectionName }}</div>
                            </div>
                            <div class="td fx-1">
                                <div class="marg-bt-10 ft-wt-600 ft-cl-1">学员名称</div>
                                <div>{{ $route.query.name || '--' }}</div>
                            </div>
                        </div>
                        <div class="fx-sb">
                            <div class="td fx-1">
                                <div class="marg-bt-10 ft-wt-600 ft-cl-1">所用时长</div>
                                <div>{{ $route.query.duration ? timeFormat($route.query.duration) : '00 : 00 : 00' }}
                                </div>
                            </div>
                            <div class="td fx-1">
                                <div class="marg-bt-10 ft-wt-600 ft-cl-1">提交时间</div>
                                <div>{{ $route.query.finishTime || '--' }}</div>
                            </div>
                            <div class="td fx-1">
                                <div class="marg-bt-10 ft-wt-600 ft-cl-1">总 分 数</div>
                                <div>{{ $route.query.score || '--' }} / {{ total || '--' }}</div>
                            </div>
                        </div>
                    </div>
                </div>
                <!-- end -->
            </div>
        </div>

        <div class="bg-wt radius marg-tp-20">
            <div class="detailBox">
                <!-- 答题卡 -->
                <div class="answerCardSection" v-if="myExamDetails && myExamDetails.length">
                    <div class="answerCardTitle">答题卡</div>
                    <div class="answerCards">
                        <span v-for="(item, index) in myExamDetails" :class="{
                            'correct-answer': item.correct,
                            'wrong-answer': !item.correct && item.answer != '',
                            'unanswered': item.answer === ''
                        }">
                            {{ index + 1 }}
                        </span>
                    </div>
                    <div class="answerCardLegend">
                        <span class="legend-item"><span class="legend correct-answer"></span>正确</span>
                        <span class="legend-item"><span class="legend wrong-answer"></span>错误</span>
                        <span class="legend-item"><span class="legend unanswered"></span>未答</span>
                    </div>
                </div>
                <!-- end -->
            </div>
        </div>

        <div class="bg-wt radius marg-tp-20">
            <div class="detailBox">
                <!-- 试题详情 -->
                <div class="examQuestions" v-if="myExamDetails && myExamDetails.length">
                    <div class="item" v-for="(item, index) in myExamDetails" :key="index">
                        <div class="examTitle">
                            <div>
                                <img v-if="item.correct" src="/src/assets/icon_right.png" alt="正确">
                                <img v-else src="/src/assets/icon_wrong.png" alt="错误">
                            </div>
                            <div class="quest fx">
                                {{ index + 1 }}. <span v-html="item.question.name"></span>
                            </div>
                        </div>

                        <div class="answer" v-if="item.question.options && item.question.options.length">
                            <li v-for="(it, idx) in formatOptions(item.question.options)" :key="idx">
                                <span v-html="it"></span>
                            </li>
                        </div>

                        <div class="analysis">
                            <div class="answer-info">
                                <div class="your-answer">
                                    <span class="label ft-wt-600">用户答案：</span>
                                    <span class="value" :class="{ 'empty-answer': !item.answer }">
                                        {{ item.answer ? answerChange(item.question.type, item.answer) : '未作答' }}
                                    </span>
                                </div>
                                <div class="correct-answer">
                                    <span class="label ft-wt-600">正确答案：</span>
                                    <span class="value">{{ answerChange(item.question.type,
                                        item.question.answer)}}</span>
                                </div>
                                <div class="difficulty">
                                    <span class="label">难易程度：</span>
                                    <span class="value">{{ defficultyChange(item.question.difficulty) }}</span>
                                </div>
                                <div class="score">
                                    <span class="label">得分：</span>
                                    <span class="value">{{ item.score !== undefined ? item.score : '0' }}</span>
                                </div>
                            </div>

                            <div class="answer-analysis" v-if="item.question.analysis">
                                <div class="label">答案解析： <span class="content" v-html="item.question.analysis"></span>
                                </div>

                            </div>

                            <div class="answer-analysis" v-if="item.comment" style="margin-top: 10px;">
                                <div class="label">教师评语： <span class="content" v-html="item.comment"></span></div>

                            </div>
                        </div>
                    </div>
                </div>
                <div v-else class="no-data">
                    暂无考试详情数据
                </div>
                <!-- end -->
            </div>
        </div>

        <div class="bg-wt radius marg-tp-20">
            <div class="detailBox" style="padding-top:0">
                <div class="btn">
                    <el-button class="button buttonSub" @click="openCommentDialog">教师评语</el-button>
                    <el-button class="button buttonSub" @click="handleGetback">返回</el-button>
                </div>
            </div>
        </div>
        <!-- 教师评语弹窗 -->
        <el-dialog v-model="commentDialogVisible" title="教师评语" width="50%" :close-on-click-modal="false">
            <div class="comment-dialog-content">
                <div class="comment-item" v-for="(item, index) in tempComments" :key="index">
                    <div class="question-status">
                        <span class="question-number">题目{{ index + 1 }}:</span>
                        <span> {{ item.question.name }}</span>
                        <span class="status" :class="item.correct ? 'correct' : 'wrong'">
                            {{ item.correct ? '作答正确' : '作答错误' }}
                        </span>
                    </div>
                    <div class="comment-input">
                        <el-input v-model="item.comment" type="textarea" :rows="2" placeholder="请输入评语" maxlength="200"
                            show-word-limit></el-input>
                    </div>
                </div>
            </div>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="commentDialogVisible = false">取消</el-button>
                    <el-button type="primary" @click="submitComments">提交评语</el-button>
                </span>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import { getExamById, addComment } from "@/api/exam.js";
import { upperAlpha, timeFormat } from "@/utils/tool.js";
import { useRoute, useRouter } from "vue-router";
import { useUserStore } from '@/store'

const store = useUserStore()
const route = useRoute();
const router = useRouter();
// 添加临时评语存储变量
const tempComments = ref([]);
// mounted生命周期
onMounted(async () => {
    // 查询我的考试记录
    getExamDetailsData()
});

/** 方法定义 **/

// 返回
const handleGetback = () => {
    router.push({
        path: "/exam/index",
    });
};

// 格式化选项，添加ABCD前缀
const formatOptions = (options) => {
    if (!options || !options.length) return [];
    return options.map((opt, index) => {
        index = index + 1
        // 这里从 0 开始映射字母前缀
        const prefix = upperAlpha(index);
        return `${prefix}. ${opt}`;
    });
};

// 查询我的考试记录
const total = ref(0)
// 查询我的详情
const myExamDetails = ref()
const getExamDetailsData = async () => {
    await getExamById(route.query.id)
        .then((res) => {
            if (res.code == 200 && res.data != null) {
                myExamDetails.value = res.data
                total.value = res.data.reduce((sum, el) => sum + (el.question?.score || 0), 0);
            } else {
                myExamDetails.value = [];
            }
        })
        .catch(() => {
            ElMessage({
                message: "考试详情数据请求出错！",
                type: 'error'
            });
            myExamDetails.value = [];
        });
}

// 问题类型，1：单选题，2：多选题，3：不定向选择题，4：判断题，5：主观题
const answerChange = (type, val) => {
    if (!val && val !== 0 && val !== false) return '--';

    let data = ''
    switch (parseInt(type)) {
        case 1: {
            data = isNaN(Number(val)) ? val : upperAlpha(Number(val))
            break
        }
        case 2:
        case 3: {
            const arr = typeof val == 'string' ? val.split(',') : val
            data = arr.map(n => isNaN(Number(n)) ? n : upperAlpha(Number(n))).join(', ')
            break
        }
        case 4: {
            data = val ? '正确' : '错误'
            break
        }
        case 5: {
            data = val || '--'
            break
        }
    }
    return data
}

const defficultyChange = (item) => {
    if (item === undefined || item === null) return '--';
    return item == 1 ? '简单' : item == 2 ? '中等' : '困难'
}

// 教师评语相关状态
const commentDialogVisible = ref(false);
const originalComments = ref([]);

// 打开评语弹窗
const openCommentDialog = () => {
    if (!myExamDetails.value || !myExamDetails.value.length) {
        ElMessage.warning('暂无考试详情数据');
        return;
    }

    // 复制当前评语到临时变量，避免直接修改原数据
    tempComments.value = myExamDetails.value.map(item => ({
        ...item,
        comment: item.comment || ''
    }));
    commentDialogVisible.value = true;
};

// 提交评语
const submitComments = async () => {
    try {
        // 准备提交数据
        const comments = tempComments.value.map(item => ({
            id: item.id, // 考试明细id
            comment: item.comment || '' // 评语内容
        }));

        // 调用API提交评语
        const res = await addComment(comments);

        if (res.code === 200) {
            ElMessage.success('评语提交成功');
            // 提交成功后才同步到原数据
            myExamDetails.value = tempComments.value.map((temp, index) => ({
                ...myExamDetails.value[index],
                comment: temp.comment
            }));
            commentDialogVisible.value = false;
        } else {
            ElMessage.error(res.message || '评语提交失败');
        }
    } catch (error) {
        ElMessage.error('评语提交出错');
        console.error('提交评语出错:', error);
    }
};
</script>

<style lang="scss" scoped>
.contentBox {
    .examBasicInfo {
        .tit {
            font-size: 16px;
            font-weight: 600;
            margin-bottom: 20px;
            color: #333;
        }

        .infoTable {
            .td {
                padding: 12px 0;
                border-bottom: 1px solid #f5f5f5;
                line-height: 1.6;
            }
        }
    }

    .answerCardSection {
        .answerCardTitle {
            font-size: 16px;
            font-weight: 600;
            margin-bottom: 15px;
            color: #333;
        }

        .answerCards {
            display: flex;
            flex-wrap: wrap;
            gap: 10px;
            margin-bottom: 15px;

            span {
                display: inline-block;
                width: 32px;
                height: 32px;
                line-height: 32px;
                text-align: center;
                border-radius: 4px;
                background: #f5f5f5;
                color: #666;
                font-size: 14px;

                &.correct-answer {
                    background: #67c23a;
                    color: #fff;
                }

                &.wrong-answer {
                    background: #f56c6c;
                    color: #fff;
                }

                &.unanswered {
                    background: #e6e6e6;
                    color: #999;
                }
            }
        }

        .answerCardLegend {
            display: flex;
            gap: 20px;
            font-size: 14px;
            color: #666;

            .legend-item {
                display: flex;
                align-items: center;

                .legend {
                    display: inline-block;
                    width: 16px;
                    height: 16px;
                    border-radius: 2px;
                    margin-right: 6px;

                    &.correct-answer {
                        background: #67c23a;
                    }

                    &.wrong-answer {
                        background: #f56c6c;
                    }

                    &.unanswered {
                        background: #e6e6e6;
                    }
                }
            }
        }
    }

    .examQuestions {
        .item {
            margin-bottom: 30px;
            padding-bottom: 25px;
            border-bottom: 1px solid #f5f5f5;

            &:last-child {
                border-bottom: none;
            }

            .examTitle {
                display: flex;
                align-items: flex-start;
                margin-bottom: 18px;

                img {
                    width: 20px;
                    margin-right: 10px;
                    margin-top: 3px;
                }

                .quest {
                    font-weight: 600;
                    font-size: 16px;
                    color: #333;
                    line-height: 1.6;
                }
            }

            .answer {
                margin-left: 30px;
                margin-bottom: 20px;

                li {
                    list-style-type: none;
                    margin-bottom: 10px;
                    padding-left: 5px;
                    font-size: 15px;
                    color: #555;
                    line-height: 1.6;
                }
            }

            .analysis {
                background: #f9f9f9;
                padding: 20px;
                border-radius: 6px;

                .answer-info {
                    display: flex;
                    flex-wrap: wrap;
                    gap: 20px 30px;
                    margin-bottom: 20px;

                    >div {
                        min-width: 200px;
                    }

                    .label {
                        color: #666;
                        margin-right: 5px;
                    }

                    .value {
                        color: #333;

                        &.empty-answer {
                            color: #f56c6c;
                        }
                    }

                    .your-answer {
                        .value {
                            font-weight: 500;
                        }
                    }

                    .correct-answer {
                        .value {
                            color: #67c23a;
                            font-weight: 500;
                        }
                    }
                }

                .answer-analysis {
                    .label {
                        font-weight: 600;
                        color: #666;
                        margin-bottom: 8px;
                        display: block;
                    }

                    .content {
                        color: #555;
                        line-height: 1.8;
                    }
                }
            }
        }
    }

    .no-data {
        text-align: center;
        padding: 40px 0;
        color: #999;
        font-size: 16px;
    }

    .detailBox {
        border-top: 1px solid #F5EFEE;
        padding-right: 70px;
        padding-top: 20px;
        padding-bottom: 20px;

        .btn {
            margin-bottom: 10px;
            text-align: right;
        }
    }

    .button {
        width: 130px;
    }
}

.comment-dialog-content {
    max-height: 60vh;
    overflow-y: auto;
    padding-right: 10px;

    .comment-item {
        margin-bottom: 20px;
        padding-bottom: 20px;
        border-bottom: 1px solid #f0f0f0;

        &:last-child {
            border-bottom: none;
        }

        .question-status {
            display: flex;
            align-items: center;
            margin-bottom: 10px;

            .question-number {
                font-weight: 600;
                margin-right: 10px;
            }

            .status {
                padding: 2px 8px;
                border-radius: 4px;
                font-size: 12px;

                &.correct {
                    background-color: #f0f9eb;
                    color: #67c23a;
                }

                &.wrong {
                    background-color: #fef0f0;
                    color: #f56c6c;
                }
            }
        }

        .comment-input {
            margin-left: 20px;
        }
    }
}

.dialog-footer {
    display: flex;
    justify-content: flex-end;
}
</style>