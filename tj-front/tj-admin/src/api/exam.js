import request from "@/utils/request.js";


//分页查询考试记录
export const getExamPage = (params) =>
  request({
    url: `/es/admin/exams/page`,
    method: "get",
    params,
  });
//根据id查询考试记录
export const getExamById = (id) =>
  request({
    url: `/es/admin/exams/${id}`,
    method: "get",
  });

//教师评语
export const addComment = (data) =>
  request({
    url: `/es/admin/exams/comment`,
    method: "post",
    data,
  });
