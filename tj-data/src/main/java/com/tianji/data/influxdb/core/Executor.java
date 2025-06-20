package com.tianji.data.influxdb.core;

import com.google.common.collect.Lists;
import com.tianji.common.utils.EmptyUtil;
import lombok.extern.slf4j.Slf4j;
import org.influxdb.InfluxDB;
import org.influxdb.annotation.Measurement;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 执行器
 */
@Slf4j
public class Executor {

    InfluxDB influxDB;

    public Executor() {
    }

    public Executor(InfluxDB influxDB) {
        this.influxDB = influxDB;
    }

    public List<Map<String,Object>> select(String sql,String database) {
        QueryResult queryResult = influxDB.query(new Query(sql, database));
        List<Map<String,Object>> resultList = new ArrayList<>();
        queryResult.getResults().forEach(result -> {
            //查询出错抛出错误信息
            if (!EmptyUtil.isNullOrEmpty(result.getError())){
                throw new RuntimeException(result.getError());
            }
            if (!EmptyUtil.isNullOrEmpty(result)&&!EmptyUtil.isNullOrEmpty(result.getSeries())){
                //获取所有列的集合，一个迭代是代表一组
                List<QueryResult.Series> series= result.getSeries();
                for (QueryResult.Series s : series) {
                    //列中含有多行数据，每行数据含有多列value，所以嵌套List
                    List<List<Object>> values = s.getValues();
                    //每组的列是固定的
                    List<String> columns = s.getColumns();
                    for (List<Object> v:values){
                        //循环遍历结果集，获取每行对应的value，以map形式保存
                        Map<String,Object> queryMap =new HashMap<String, Object>();
                        for(int i=0;i<columns.size();i++){
                            //遍历所有列名，获取列对应的值
                            String column = columns.get(i);
                            if (v.get(i)==null||v.get(i).equals("null")){
                                //如果是null就存入null
                                queryMap.put(column,null);
                            }else {
                                //不是null就转成字符串存储
                                String value = String.valueOf(v.get(i));
                                //如果是时间戳还可以格式转换，我这里懒了
                                queryMap.put(column, value);
                            }
                        }
                        //把结果添加到结果集中
                        resultList.add(queryMap);
                    }
                }
            }
        });
        return resultList;
    }

    public void insert(Object args[]) {
        if (args.length != 1) {
            throw new RuntimeException();
        }
        Object obj = args[0];
        List<Object> list = Lists.newArrayList();
        if (obj instanceof List){
            list = (ArrayList) obj;
        }else {
            list.add(obj);
        }
        if (list.size() > 0) {
            Object firstObj = list.get(0);
            Class<?> domainClass = firstObj.getClass();
            List<Point> pointList = new ArrayList<>();
            for (Object o : list) {
                Point point = Point
                        .measurementByPOJO(domainClass)
                        .addFieldsFromPOJO(o)
                        .build();
                pointList.add(point);
            }
            //获取数据库名和rp
            Measurement measurement = firstObj.getClass().getAnnotation(Measurement.class);
            String database = measurement.database();
            String retentionPolicy = measurement.retentionPolicy();
            BatchPoints batchPoints = BatchPoints
                    .builder()
                    .points(pointList)
                    .retentionPolicy(retentionPolicy).build();
            influxDB.setDatabase(database);
            influxDB.write(batchPoints);
        }
    }

    public void delete(String sql, String database) {
        influxDB.query(new Query(sql, database));
    }

}
