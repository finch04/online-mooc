package com.tianji.data.influxdb;

import com.tianji.data.influxdb.anno.Insert;

import java.util.List;

public interface InfluxDBBaseMapper<T> {

    @Insert
    void insertOne(T entity);

    @Insert
    void insertBatch(List<T> entityList);
}
