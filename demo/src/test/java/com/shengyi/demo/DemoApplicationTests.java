package com.shengyi.demo;

import com.shengyi.demo.entity.CacheEntity;
import com.shengyi.demo.util.LocalCache;
import com.shengyi.demo.util.LocalCacheTwo;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

@SpringBootTest
class DemoApplicationTests {

    @Test
    void localCache() {
        String value = "测试咯";
        LocalCacheTwo localCache =  LocalCacheTwo.getInstance();
        localCache.putValue("12",value,100000);
        localCache.remove("12");
        Object object = localCache.getValue("12");
        System.out.println(object);
    }

    @Test
    void getFileValue() {
        LocalCacheTwo localCache =  LocalCacheTwo.getInstance();
        CacheEntity cacheEntity  = localCache.getFileValue();
        System.out.println(cacheEntity.getValue());
    }

}
