package com.tianji.message.utils;

import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.utils.JsonUtils;
import com.tianji.message.domain.po.Sensitive;
import com.tianji.message.service.ISensitiveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.tianji.message.constants.RedisConstants.*;

/**
 * 敏感词检测工具类，统一使用 StringRedisTemplate
 */
@Slf4j
public class SensitiveWordDetector {

    // 统一使用 StringRedisTemplate
    private static StringRedisTemplate stringRedisTemplate;



    // 敏感词字典（内存缓存），使用JSON字符串存储
    private static String dictionaryJson = "";
    private static Map<String, Object> dictionaryMap = new ConcurrentHashMap<>();

    // 敏感词列表（内存缓存）
    private static List<String> sensitiveWordsList = new ArrayList<>();



    // 敏感词服务实例（需外部初始化）
    private static ISensitiveService sensitiveService;

    /**
     * 初始化工具类，需在应用启动时调用
     */
    public static void init(StringRedisTemplate redisTpl, ISensitiveService service) {
        stringRedisTemplate = redisTpl;
        sensitiveService = service;
        loadSensitiveWordsFromRedis();
    }

    /**
     * 检测文本是否包含敏感词，并打印敏感词及其位置
     */
    public static boolean containsSensitiveWord(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        // 从内存获取字典，不存在则从Redis加载
        if (dictionaryMap.isEmpty()) {
            loadSensitiveWordsFromRedis();
        }

        int len = text.length();
        List<SensitiveWordInfo> sensitiveWordInfos = new ArrayList<>();

        for (int i = 0; i < len; i++) {
            int wordLength = checkWord(text, i);
            if (wordLength > 0) {
                // 记录敏感词及其位置
                String sensitiveWord = text.substring(i, i + wordLength);
                SensitiveWordInfo info = new SensitiveWordInfo(
                        sensitiveWord,
                        i,
                        i + wordLength - 1
                );
                sensitiveWordInfos.add(info);

                // 跳过已匹配的字符
                i += wordLength - 1;
            }
        }

        // 打印敏感词信息
        printSensitiveWordInfo(sensitiveWordInfos, text);

        return !sensitiveWordInfos.isEmpty();
    }

    /**
     * 搜索文本中某个位置开始的敏感词，返回匹配长度
     */
    private static int checkWord(String text, int beginIndex) {
        if (dictionaryMap.isEmpty()) {
            return 0;
        }

        boolean isEnd = false;
        int wordLength = 0;
        Map<String, Object> curMap = dictionaryMap;
        int len = text.length();

        for (int i = beginIndex; i < len; i++) {
            String key = String.valueOf(text.charAt(i));
            curMap = (Map<String, Object>) curMap.get(key);
            if (curMap == null) {
                break;
            } else {
                wordLength++;
                if ("1".equals(curMap.get("isEnd"))) {
                    isEnd = true;
                }
            }
        }
        return isEnd ? wordLength : 0;
    }

    /**
     * 敏感词信息类，用于存储敏感词及其位置
     */
    private static class SensitiveWordInfo {
        private String word;        // 敏感词
        private int startPosition;  // 起始位置
        private int endPosition;    // 结束位置

        public SensitiveWordInfo(String word, int startPosition, int endPosition) {
            this.word = word;
            this.startPosition = startPosition;
            this.endPosition = endPosition;
        }

        @Override
        public String toString() {
            return "敏感词: '" + word + "', 位置: [" + startPosition + ", " + endPosition + "]";
        }
    }

    /**
     * 打印敏感词信息
     */
    private static void printSensitiveWordInfo(List<SensitiveWordInfo> infos, String text) {
        if (infos.isEmpty()) {
            System.out.println("文本中未检测到敏感词");
            return;
        }

        System.out.println("检测到 " + infos.size() + " 个敏感词:");
        for (SensitiveWordInfo info : infos) {
            System.out.println(info);
        }

        // 打印带标记的文本
        StringBuilder markedText = new StringBuilder(text);
        for (SensitiveWordInfo info : infos) {
            markedText.replace(info.startPosition, info.endPosition + 1,
                    "[" + info.word + "]");
        }
        System.out.println("标记文本: " + markedText);
    }

    /**
     * 从Redis加载敏感词库
     */
    @SuppressWarnings("unchecked")
    private static void loadSensitiveWordsFromRedis() {
        try {
            // 从Redis获取字典JSON
            dictionaryJson = stringRedisTemplate.opsForValue().get(SENSITIVE_DICTIONARY_KEY);

            if (StringUtils.hasText(dictionaryJson)) {
                // 将JSON字符串转换为Map
                dictionaryMap = JsonUtils.toBean(dictionaryJson, Map.class);
                return;
            }

            // Redis中没有字典，从数据库加载并写入Redis
            loadSensitiveWordsFromDatabase();
        } catch (Exception e) {
            throw new RuntimeException("加载敏感词库失败", e);
        }
    }

    /**
     * 从数据库加载敏感词并写入Redis
     */
    private static void loadSensitiveWordsFromDatabase() {
        try {
            // 从数据库获取敏感词列表
            List<Sensitive> sensitives = sensitiveService.list();
            if (CollectionUtils.isEmpty(sensitives)) {
                return;
            }

            sensitiveWordsList = sensitives.stream()
                    .map(Sensitive::getSensitives)
                    .collect(Collectors.toList());

            // 生成敏感词字典
            Map<String, Object> map = new HashMap<>(sensitiveWordsList.size());
            for (String word : sensitiveWordsList) {
                Map<String, Object> curMap = map;
                int len = word.length();
                for (int i = 0; i < len; i++) {
                    String key = String.valueOf(word.charAt(i));
                    Map<String, Object> wordMap = (Map<String, Object>) curMap.get(key);
                    if (wordMap == null) {
                        wordMap = new HashMap<>(2);
                        wordMap.put("isEnd", "0");
                        curMap.put(key, wordMap);
                    }
                    curMap = wordMap;
                    if (i == len - 1) {
                        curMap.put("isEnd", "1");
                    }
                }
            }

            // 将字典转换为JSON字符串
            dictionaryJson = JsonUtils.toJsonStr(map);

            // 写入Redis
            stringRedisTemplate.opsForValue().set(
                    SENSITIVE_DICTIONARY_KEY,
                    dictionaryJson,
                    SENSITIVE_CACHE_TTL,
                    TimeUnit.SECONDS
            );
            stringRedisTemplate.opsForValue().set(
                    SENSITIVE_WORDS_KEY,
                    String.join(",", sensitiveWordsList),
                    SENSITIVE_CACHE_TTL,
                    TimeUnit.SECONDS
            );

            // 更新内存字典
            dictionaryMap = map;
        } catch (Exception e) {
            throw new RuntimeException("加载敏感词库失败", e);
        }
    }

    /**
     * 刷新Redis中的敏感词库
     */
    public static void refreshSensitiveWords() {
        loadSensitiveWordsFromDatabase();
    }

    /**
     * 添加敏感词
     */
    public static boolean addSensitiveWord(Sensitive sensitive) {
        try {
            // 检查是否存在重复
            Integer count = sensitiveService.lambdaQuery()
                    .eq(Sensitive::getSensitives, sensitive.getSensitives())
                    .count();
            if (count > 0) {
                throw new BadRequestException("存在同名违禁词");
            }

            boolean result = sensitiveService.save(sensitive);
            if (result) {
                refreshSensitiveWords();
            }
            return result;
        } catch (Exception e) {
            log.info("添加敏感词失败:{}",e);
            throw new BadRequestException("添加敏感词失败");
        }
    }

    /**
     * 更新敏感词
     */
    public static boolean updateSensitiveWord(Sensitive sensitive) {
        try {
            // 检查是否存在重复
            Sensitive one = sensitiveService.lambdaQuery()
                    .eq(Sensitive::getSensitives, sensitive.getSensitives())
                    .one();
            if (one != null && !one.getId().equals(sensitive.getId())) {
                throw new BadRequestException("存在同名违禁词");
            }

            boolean result = sensitiveService.updateById(sensitive);
            if (result) {
                refreshSensitiveWords();
            }
            return result;
        } catch (Exception e) {
            throw new BadRequestException("更新敏感词失败");
        }
    }

    /**
     * 删除敏感词
     */
    public static boolean deleteSensitiveWord(Long id) {
        try {
            Sensitive sensitive = sensitiveService.getById(id);
            if (sensitive == null) {
                throw new BadRequestException("不存在该条数据");
            }

            boolean result = sensitiveService.removeById(id);
            if (result) {
                refreshSensitiveWords();
            }
            return result;
        } catch (Exception e) {
            throw new BadRequestException("删除敏感词失败");
        }
    }


}