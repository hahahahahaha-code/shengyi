package com.shengyi.demo.util;

import com.shengyi.demo.entity.CacheEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author sunhao
 * @description  Java实现本地缓存
 * @date 2020/7/6
 **/
public class LocalCacheTwo {

    private static final Logger log = LoggerFactory.getLogger(LocalCacheTwo.class);

    /**
     *@value默认的缓存容量
     */
    private static final int DEFAULT_CAPACITY = 512;
    /**
     * @value最大容量
     */
    private static final int MAX_CAPACITY = 100000;
    /**
     * @value刷新缓存的频率
     */
    private static final int MONITOR_DURATION = 2;

    /**
     * @value本地持久化保存路径
     */
    private static final String FILE = "D:\\fileDemo\\demo.txt";

    // 启动监控线程
    static {
        new Thread(new TimeoutTimerThread()).start();
    }

    // 内部类方式实现单例
    private static class LocalCacheInstance{
        private static final LocalCacheTwo INSTANCE = new LocalCacheTwo();

    }
    public static LocalCacheTwo getInstance() {
        return LocalCacheInstance.INSTANCE;
    }

    private LocalCacheTwo() {
    }

    /**
     * @value使用默认容量创建一个Map,ConcurrentHashMap线程安全;
     */
    private static Map<String, CacheEntity> cache = new ConcurrentHashMap<>(DEFAULT_CAPACITY);

    /**
     * @see将key-value 保存到本地缓存并制定该缓存的过期时间
     * @param key
     * @param value
     * @param expireTime 过期时间，如果是-1 则表示永不过期
     * @return
     */
    public <T> boolean putValue(String key, T value, int expireTime) {
        return putCloneValue(key, value, expireTime);
    }

    /**
     * 将值通过序列化clone 处理后保存到缓存中，可以解决值引用的问题
     *
     * @param key
     * @param value
     * @param expireTime
     * @return
     */
    private synchronized <T>  boolean putCloneValue(String key, T value, int expireTime) {
        try {
            if (cache.size() >= MAX_CAPACITY) {
                return false;
            }
            // 序列化赋值
            CacheEntity entityClone = clone(new CacheEntity(value, System.nanoTime(), expireTime));
            fileWrite(entityClone);
            cache.put(key, entityClone);
            return true;
        } catch (Exception e) {
            log.error("添加缓存失败：{}", e.getMessage());
        }
        return false;
    }

    /**
     * 序列化 克隆处理
     *
     * @param object
     * @return
     */
    private <E extends Serializable> E clone(E object) {
        E cloneObject = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            oos.close();
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            cloneObject = (E) ois.readObject();
            ois.close();
        } catch (Exception e) {
            log.error("缓存序列化失败：{}", e.getMessage());
        }
        return cloneObject;
    }

    /**
     * 本地持久化
     *
     * @param
     * @return
     */
    public void fileWrite(CacheEntity cacheEntity) {
        try {
            File file = new File(FILE);
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fos);
            objectOutputStream.writeObject(cacheEntity);
            objectOutputStream.close();
        } catch (Exception e) {
            log.error("本地持久化失败：{}", e.getMessage());
        }
    }

    /**
     * @see从本地缓存中获取key对应的值，如果该值不存则则返回null
     * @param key
     * @return
     */
    public Object getValue(String key) {
        Object object = cache.get(key);
        return object == null ? "空值了" : cache.get(key).getValue();
    }

    /**
     * @see移除
     */
    public void remove(String key) {
        cache.remove(key);
    }

    /**
     * @see移除
     */
    public CacheEntity getFileValue() {
        CacheEntity cacheEntity = null;
            try{
                FileInputStream fis = new FileInputStream(FILE);
                ObjectInputStream ois = new ObjectInputStream(fis);
                cacheEntity = (CacheEntity)ois.readObject();
            } catch (Exception e) {
                log.error("本地持久化文件读取失败：{}", e.getMessage());
            }
        return cacheEntity;
    }

    /**
     * @see清空所有
     */
    public void clear() {
        cache.clear();
    }

    /**
     * 过期处理线程
     */
    static class TimeoutTimerThread implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(MONITOR_DURATION);
                    checkTime();
                } catch (Exception e) {
                    log.error("过期缓存清理失败：{}", e.getMessage());
                }
            }
        }

        /**
         * 过期缓存的具体处理方法
         *
         * @throws Exception
         */
        private void checkTime() throws Exception {
            // 开始处理过期
            for (String key : cache.keySet()) {
                CacheEntity tce = cache.get(key);
                long timoutTime = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - tce.getGmtModify());
                // 过期时间 : timoutTime
                if (tce.getExpire() > timoutTime) {
                    continue;
                }
                log.info(" 清除过期缓存 ： " + key);
                //清除过期缓存和删除对应的缓存队列
                cache.remove(key);
            }
        }
    }
}
