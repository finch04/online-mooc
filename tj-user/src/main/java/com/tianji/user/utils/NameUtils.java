package com.tianji.user.utils;/**
 * @author fsq
 * @date 2025/5/20 14:34
 */

import java.util.Random;

/* 随机生成用户名
 * 取水浒传108好汉名字
 * 取LOL地名26个,组合而成
 * 一共可以生成2808个不同特色的用户名
 * @Author: fsq
 * @Date: 2025/5/20 14:34
 * @Version: 1.0
 */

public class NameUtils {

    //108个人名
    public final static String SHUIHU_NAME="宋江,卢俊义,吴用,公孙胜,关胜,林冲,秦明,呼延灼,花荣,柴进,李应,朱仝,鲁智深,武松,董平,张清,杨志,徐宁,索超,戴宗,刘唐,李逵,史进,穆弘,雷横,李俊,阮小二," +
            "张横,阮小五,张顺,阮小七,杨雄,石秀,解珍,解宝,朱武,黄信,孙立,宣赞,郝思文,韩滔,燕青,彭记,单廷珪,魏定国,萧让,裴宣,欧鹏,邓飞,燕顺,杨林,凌振,蒋敬,吕方," +
            "郭盛,安道全,皇甫端,王英,扈三娘,鲍旭,樊瑞,孔明,孔亮,项充,李衮,金大坚,马麟,童威,童猛,孟康,侯健,陈达,杨春,郑天寿,陶宗旺,宋清,乐和,龚旺,丁得孙,穆春," +
            "曹正,宋万,杜迁,薛永,施恩,李忠,周通,汤隆,杜兴,邹渊,邹润,朱贵,朱富,蔡福,蔡庆,李立,李云,焦挺,石勇,孙新,顾大嫂,张青,孙二娘,王定六,郁保四,白胜,时迁,段景住";

    //26个地名
    public static final String LOL_NAME="艾欧尼亚、祖安、诺克萨斯、班德尔城、皮尔特沃夫、战争学院、巨神峰、雷瑟守备、裁决之地、黑色玫瑰、暗影岛、钢铁烈阳、水晶之痕、均衡教派、影流、守望之海、征服之海、卡拉曼达、皮城警备、比尔吉沃特、德玛西亚、弗雷尔卓德、无畏先锋、恕瑞玛、扭曲丛林、巨龙之巢";

    /**
     * 随机网名
     * @return
     */
    public static String getUserName(){

        //前缀
        String prefixs[] = LOL_NAME.split("、") ;

        //后缀
        String suffixs[] = SHUIHU_NAME.split(",");
        //随机数生成器
        Random random = new Random();

        String name = prefixs[random.nextInt(prefixs.length)] + suffixs[random.nextInt(suffixs.length)];
        return name ;
    }
}
