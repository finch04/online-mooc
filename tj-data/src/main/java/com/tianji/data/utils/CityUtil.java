package com.tianji.data.utils;

import cn.hutool.json.JSONUtil;
import com.tianji.common.domain.vo.LogBusinessVO;
import lombok.Data;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * CityParamBuild
 *
 * @author: wgl
 * @describe: 城市参数封装--请求百度地图
 * @date: 2022/12/28 10:10
 */
public class CityUtil {

    // 中国省份
    private static final String[] provinces = {"北京市", "上海市", "天津市", "重庆市", "河北省", "山西省", "辽宁省", "吉林省", "黑龙江省",
            "江苏省", "浙江省", "安徽省", "福建省", "江西省", "山东省", "河南省", "湖北省", "湖南省", "广东省", "海南省", "四川省", "贵州省",
            "云南省", "陕西省", "甘肃省", "青海省", "台湾省", "内蒙古自治区", "广西壮族自治区", "西藏自治区", "宁夏回族自治区", "新疆维吾尔自治区",
            "香港特别行政区", "澳门特别行政区"};

    // 中国各省对应的城市
    private static final String[][] cities = {
            {"北京市"},
            {"上海市"},
            {"天津市"},
            {"重庆市"},
            {"石家庄市", "唐山市", "秦皇岛市", "邯郸市", "邢台市", "保定市", "张家口市", "承德市", "沧州市", "廊坊市", "衡水市"},
            {"太原市", "大同市", "阳泉市", "长治市", "晋城市", "朔州市", "晋中市", "运城市", "忻州市", "临汾市", "吕梁市"},
            {"沈阳市", "大连市", "鞍山市", "抚顺市", "本溪市", "丹东市", "锦州市", "营口市", "阜新市", "辽阳市", "盘锦市", "铁岭市", "朝阳市", "葫芦岛市"},
            {"长春市", "吉林市", "四平市", "辽源市", "通化市", "白山市", "松原市", "白城市", "延边朝鲜族自治州"},
            {"哈尔滨市", "齐齐哈尔市", "鸡西市", "鹤岗市", "双鸭山市", "大庆市", "伊春市", "佳木斯市", "七台河市", "牡丹江市", "黑河市", "绥化市", "大兴安岭地区"},
            {"南京市", "无锡市", "徐州市", "常州市", "苏州市", "南通市", "连云港市", "淮安市", "盐城市", "扬州市", "镇江市", "泰州市", "宿迁市"},
            {"杭州市", "宁波市", "温州市", "嘉兴市", "湖州市", "绍兴市", "金华市", "衢州市", "舟山市", "台州市", "丽水市"},
            {"合肥市", "芜湖市", "蚌埠市", "淮南市", "马鞍山市", "淮北市", "铜陵市", "安庆市", "黄山市", "滁州市", "阜阳市", "宿州市", "六安市", "亳州市", "池州市", "宣城市"},
            {"福州市", "厦门市", "漳州市", "泉州市", "莆田市", "三明市", "南平市", "龙岩市", "宁德市"},
            {"南昌市", "景德镇市", "萍乡市", "九江市", "新余市", "鹰潭市", "赣州市", "吉安市", "宜春市", "抚州市", "上饶市"},
            {"济南市", "青岛市", "淄博市", "枣庄市", "东营市", "烟台市", "潍坊市", "济宁市", "泰安市", "威海市", "日照市", "莱芜市", "临沂市", "德州市", "聊城市", "滨州市", "菏泽市"},
            {"郑州市", "开封市", "洛阳市", "平顶山市", "安阳市", "鹤壁市", "新乡市", "焦作市", "濮阳市", "许昌市", "漯河市", "三门峡市", "南阳市", "商丘市", "信阳市", "周口市", "驻马店市"},
            {"武汉市", "黄石市", "十堰市", "宜昌市", "襄阳市", "鄂州市", "荆门市", "孝感市", "荆州市", "黄冈市", "咸宁市", "随州市", "恩施土家族苗族自治州"},
            {"长沙市", "株洲市", "湘潭市", "衡阳市", "邵阳市", "岳阳市", "常德市", "张家界市", "益阳市", "郴州市", "永州市", "怀化市", "娄底市", "湘西土家族苗族自治州"},
            {"广州市", "韶关市", "深圳市", "珠海市", "汕头市", "佛山市", "江门市", "湛江市", "茂名市", "肇庆市", "惠州市", "梅州市", "汕尾市", "河源市", "阳江市", "清远市", "东莞市", "中山市", "潮州市", "揭阳市", "云浮市"},
            {"海口市", "三亚市", "三沙市", "儋州市"},
            {"成都市", "自贡市", "攀枝花市", "泸州市", "德阳市", "绵阳市", "广元市", "遂宁市", "内江市", "乐山市", "南充市", "眉山市", "宜宾市", "广安市", "达州市", "雅安市", "巴中市", "资阳市", "阿坝藏族羌族自治州", "甘孜藏族自治州", "凉山彝族自治州"},
            {"贵阳市", "六盘水市", "遵义市", "安顺市", "毕节市", "铜仁市", "黔西南布依族苗族自治州", "黔东南苗族侗族自治州", "黔南布依族苗族自治州"},
            {"昆明市", "曲靖市", "玉溪市", "保山市", "昭通市", "丽江市", "普洱市", "临沧市", "楚雄彝族自治州", "红河哈尼族彝族自治州", "文山壮族苗族自治州", "西双版纳傣族自治州", "大理白族自治州", "德宏傣族景颇族自治州", "怒江傈僳族自治州", "迪庆藏族自治州"},
            {"西安市", "铜川市", "宝鸡市", "咸阳市", "渭南市", "延安市", "汉中市", "榆林市", "安康市", "商洛市"},
            {"兰州市", "嘉峪关市", "金昌市", "白银市", "天水市", "武威市", "张掖市", "平凉市", "酒泉市", "庆阳市", "定西市", "陇南市", "临夏回族自治州", "甘南藏族自治州"},
            {"西宁市", "海东市", "海北藏族自治州", "黄南藏族自治州", "海南藏族自治州", "果洛藏族自治州", "玉树藏族自治州", "海西蒙古族藏族自治州"},
            {"台北市", "新北市", "桃园市", "台中市", "台南市", "高雄市", "基隆市", "新竹市", "嘉义市"},
            {"呼和浩特市", "包头市", "乌海市", "赤峰市", "通辽市", "鄂尔多斯市", "呼伦贝尔市", "巴彦淖尔市", "乌兰察布市", "兴安盟", "锡林郭勒盟", "阿拉善盟"},
            {"南宁市", "柳州市", "桂林市", "梧州市", "北海市", "防城港市", "钦州市", "贵港市", "玉林市", "百色市", "贺州市", "河池市", "来宾市", "崇左市"},
            {"拉萨市", "昌都市", "山南市", "日喀则市", "那曲市", "阿里地区", "林芝市"},
            {"银川市", "石嘴山市", "吴忠市", "固原市", "中卫市"},
            {"乌鲁木齐市", "克拉玛依市", "吐鲁番市", "哈密市", "昌吉回族自治州", "博尔塔拉蒙古自治州", "巴音郭楞蒙古自治州", "阿克苏地区", "克孜勒苏柯尔克孜自治州", "喀什地区", "和田地区", "伊犁哈萨克自治州", "塔城地区", "阿勒泰地区"},
            {"香港特别行政区"},
            {"澳门特别行政区"}
    };

    private final static List<String> firstTierCities = Arrays.asList("北京市", "上海市", "广州市", "深圳市","深圳市","武汉市","重庆市","成都市","南京市","长沙市","青岛市","天津市","杭州市","郑州市","西安市","宁波市","无锡市","苏州市","大连市");

    private final static List<String> secondTierCities = Arrays.asList("佛山市", "沈阳市", "无锡市", "济南市", "厦门市", "福州市", "温州市", "哈尔滨市", "石家庄市", "大连市", "南宁市", "泉州市", "金华市", "贵阳市", "常州市", "长春市", "南昌市", "南通市", "嘉兴市", "徐州市", "惠州市", "太原市", "台州市", "绍兴市", "保定市", "中山市", "潍坊市", "临沂市", "珠海市", "烟台市");

    public static boolean isFirstTierCity(String city) {
        return firstTierCities.contains(city);
    }

    public static boolean isSecondTierCity(String city) {
        return secondTierCities.contains(city);
    }

    public static boolean isOtherCity(String city) {
        return !secondTierCities.contains(city)&&!firstTierCities.contains(city);
    }


    public static void handlerCity(LogBusinessVO logBusinessVO) {
        try {
//            生产中需要根据IP获得省份
//            Map params = new LinkedHashMap<String, String>();
//            params.put("ip", logBusinessVO.getHostAddress());
//            params.put("coor", "gcj02");
//            params.put("ak", ak);
//            this.requestGetAK(params);
            CityDTO cityDTO = randomCity();
            logBusinessVO.setProvince(cityDTO.getProvince());
            logBusinessVO.setCity(cityDTO.getCity());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public static String URL = "https://api.map.baidu.com/location/ip?";

//    /**
//     * 默认ak
//     * 选择了ak，使用IP白名单校验：
//     * 根据您选择的AK已为您生成调用代码
//     * 检测到您当前的ak设置了IP白名单校验
//     * 您的IP白名单中的IP非公网IP，请设置为公网IP，否则将请求失败
//     * 请在IP地址为xxxxxxx的计算发起请求，否则将请求失败
//     */
//    public CityDTO requestGetAK(Map<String, String> param) throws Exception {
//        try {
//            StringBuffer queryString = new StringBuffer();
//            queryString.append(URL);
//            for (Map.Entry<?, ?> pair : param.entrySet()) {
//                queryString.append(pair.getKey() + "=");
//                //    第一种方式使用的 jdk 自带的转码方式  第二种方式使用的 spring 的转码方法 两种均可
//                //    queryString.append(URLEncoder.encode((String) pair.getValue(), "UTF-8").replace("+", "%20") + "&");
//                queryString.append(UriUtils.encode((String) pair.getValue(), "UTF-8") + "&");
//            }
//
//            if (queryString.length() > 0) {
//                queryString.deleteCharAt(queryString.length() - 1);
//            }
//
//            java.net.URL url = new URL(queryString.toString());
//            System.out.println(queryString.toString());
//            URLConnection httpConnection = (HttpURLConnection) url.openConnection();
//            httpConnection.connect();
//
//            InputStreamReader isr = new InputStreamReader(httpConnection.getInputStream());
//            BufferedReader reader = new BufferedReader(isr);
//            StringBuffer buffer = new StringBuffer();
//            String line;
//            while ((line = reader.readLine()) != null) {
//                buffer.append(line);
//            }
//            reader.close();
//            isr.close();
//            System.out.println("AK: " + buffer.toString());
//            Map map = JSONUtil.toBean(buffer.toString(), Map.class);
//            Map address_detail = (Map) (((Map) map.get("content")).get("address_detail"));
//            CityDTO cityDTO = new CityDTO();
//            cityDTO.setCity(address_detail.get("city") + "");
//            cityDTO.setProvince(address_detail.get("province") + "");
//            return cityDTO;
//        } catch (Exception e) {
//            //由于内网ip导致无法正确获取ip
//            CityDTO cityDTO = new CityDTO();
//            cityDTO.setCity("北京市");
//            cityDTO.setProvince("北京市");
//            return cityDTO;
//        }
//    }

    private static CityDTO randomCity(){
        // 随机选择一个省
        Random random = new Random();
        int provinceIndex = random.nextInt(provinces.length);
        String selectedProvince = provinces[provinceIndex];

        // 在选定的省中随机选择一个市
        int cityIndex = random.nextInt(cities[provinceIndex].length);
        String selectedCity = cities[provinceIndex][cityIndex];

        // 输出结果
        System.out.println("随机生成的省份：" + selectedProvince);
        System.out.println("随机生成的城市：" + selectedCity);
        CityDTO cityDTO = new CityDTO();
        cityDTO.setCity(selectedCity);
        cityDTO.setProvince(selectedProvince);
        return cityDTO;
    }

    @Data
    static class CityDTO {
        //城市
        private String city;
        //省份
        private String province;
    }

}
