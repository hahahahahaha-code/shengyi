package com.shengyi.demo.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @Description:本地缓存
 */
public class LocalCache {

    /**缓存默认失效时间(毫秒)*/
    private static final long DEFAULT_TIMEOUT = 3600*1000;

    /**缓存清除动作执行间隔(秒)*/
    private static final long TASK_TIME = 1;

    /**缓存存储的map*/
    private static final ConcurrentHashMap<String,CacheEntity> cacheMap = new ConcurrentHashMap<>();


    public LocalCache() {
    }

    private static LocalCache cache = null;

    public static LocalCache getInstance() {//单例一下
        if (cache == null) {
            cache = new LocalCache();
            new Thread(new TimeoutTimer()).start();
        }

        return cache;
    }
    //定时器线程-用于检查缓存过期
    static class TimeoutTimer implements Runnable{

        @Override
        public void run(){
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(TASK_TIME);
                    for (String key:cacheMap.keySet()) {
                        CacheEntity entity = cacheMap.get(key);
                        long now = System.currentTimeMillis();
                        if ((now - entity.getTimeStamp()) >= entity.getExpire()) {
                            cacheMap.remove(key);
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**存储单元*/
    static class CacheEntity {

        /**值*/
        private Object value;

        /**过期时间(毫秒)*/
        private long expire;

        /**创建时的时间戳*/
        private long timeStamp;

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public long getExpire() {
            return expire;
        }

        public void setExpire(long expire) {
            this.expire = expire;
        }

        public long getTimeStamp() {
            return timeStamp;
        }

        public void setTimeStamp(long timeStamp) {
            this.timeStamp = timeStamp;
        }
    }

    public static boolean set(String key,Object value,long expire) {

        cacheMap.put(key,setEntity(key,value,expire));

        return true;
    }

    public static boolean set(String key,Object value) {

        cacheMap.put(key,setEntity(key,value,DEFAULT_TIMEOUT));

        return true;
    }

    private static CacheEntity setEntity(String key,Object value,long expire){

        CacheEntity entity = new CacheEntity();
        entity.setValue(value);
        entity.setExpire(expire);
        entity.setTimeStamp(System.currentTimeMillis());

        return entity;
    }

    public static Object get(String key) {
        CacheEntity entity = cacheMap.get(key);

        if (entity == null) {
            return null;
        } else {
            Object value = entity.getValue();
            if (value == null) {
                return null;
            }
            return value;
        }
    }

    public static void remove(String key) {
        cacheMap.remove(key);
    }

}