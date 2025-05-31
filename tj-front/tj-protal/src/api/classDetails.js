import request from "@/utils/request.js"
// 课程详情页面接口
const COURSE_API_PREFIX = "/cs"
const LEARNING_API_PREFIX = "/ls"
// 课程分类
export const getClassDetails = (id) =>
	request({
		url: `${COURSE_API_PREFIX}/courses/baseInfo/${id}`,
		method: 'get',
	})
// 查询课程老师
export const getClassTeachers = (id) =>
	request({
		url: `${COURSE_API_PREFIX}/courses/teachers/${id}`,
		method: 'get',
	})
// 查询课程目录
export const getClassList = (id) =>
	request({
		url: `${COURSE_API_PREFIX}/courses/catas/${id}`,
		method: 'get'
	})	
// 获取课程章节 下拉数据展示
export const getClassCourses = (id) =>
request({
	url: `${COURSE_API_PREFIX}/courses/catas/${id}`,
	method: 'get'
})	
// 获取课程小节 - 问答详情使用
export const getClassChapter = (id) =>
	request({
		url: `${COURSE_API_PREFIX}/courses/catas/index/list/${id}`,
		method: 'get'
	})	

// 问答相关
// 获取问答列表-全部
export const getAskList = (params) =>
	request({
		url: `${LEARNING_API_PREFIX}/questions/page`,
		method: 'get',
		params,
	})	

// 新增提问
export const postQuestions = (params) =>
	request({
		url: `${LEARNING_API_PREFIX}/questions`,
		method: 'post',
		data:params,
	})	
// 获取问题详情
export const getQuestionsDetails = (id) =>
request({
	url: `${LEARNING_API_PREFIX}/questions/${id}`,
	method: 'get',
})	
// 编辑提问
export const putQuestions = (id,params) =>
	request({
		url: `${LEARNING_API_PREFIX}/questions/${id}`,
		method: 'put',
		data:params
	})			
// 删除提问
export const delQuestions = (id) =>
	request({
		url: `${LEARNING_API_PREFIX}/questions/${id}`,
		method: 'delete',
	})		
// 回复
export const postAnswers = params =>
	request({
		url: `${LEARNING_API_PREFIX}/replies`,
		method: 'post',
		data:params
	})			

// 分页查询回复、回答列表
export const getReply = params =>
request({
	url: `${LEARNING_API_PREFIX}/replies/page`,
	method: 'get',
	params
})

// 点赞接口
export const putLiked = params =>
request({
	url: `/rs/likes`,
	method: 'post',
	data: params
})

// 评价系统
// 分页查询评价
export const getEvaluationList = params =>
request({
	url: `${LEARNING_API_PREFIX}/evaluation/page`,
	method: 'get',
	params
})

// 新增评价
export const postEvaluation= params =>
request({
	url: `${LEARNING_API_PREFIX}/evaluation`,
	method: 'post',
	data: params
})
// 根据id获取评价详情
export const getEvaluationDetail= (id) =>
	request({
		url: `${LEARNING_API_PREFIX}/evaluation/${id}`,
		method: 'get',
	})
// 编辑评价
export const updateEvaluation= (id,params) =>
	request({
		url: `${LEARNING_API_PREFIX}/evaluation/${id}`,
		method: 'put',
		data: params
	})
// 删除评价
export const deleteEvaluation= (id) =>
	request({
		url: `${LEARNING_API_PREFIX}/evaluation/${id}`,
		method: 'delete',
	})
// 是否已经评价过该课程
export const isEvaluatedByCourseId= (id) =>
	request({
		url: `${LEARNING_API_PREFIX}/evaluation/evaluated/${id}`,
		method: 'get',
	})