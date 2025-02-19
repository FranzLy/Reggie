package com.franz.reggie;

import com.franz.reggie.dto.DishDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.*;
import redis.clients.jedis.Jedis;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SpringBootTest
class ReggieApplicationTests {
    @Autowired
    private RedisTemplate redisTemplate;


    @Test
    void contextLoads() {
    }

    @Test
    void testDemo() {
        String fileName = "0a3b3288-3446-4420-bbff-f263d0c02d8e.jpg";
        System.out.println("后缀名： " + fileName.substring(fileName.lastIndexOf(".")));
    }

    @Test
    void testJedis() {
        //获取连接
        Jedis jedis = new Jedis("127.0.0.1", 6379);

        //验证
//        jedis.auth("123456");

        //执行操作
        //string
        System.out.println("-----STRING----");
        String age = jedis.get("age");
        System.out.println(age);
        jedis.set("age", "18");
        System.out.println(jedis.get("age"));

        //list
        System.out.println("-----LIST----");
        jedis.lpush("name", "liyu", "zhangsan");
        System.out.println(jedis.lrange("name", 0, -1));

        //hash
        System.out.println("-----HASH----");
        jedis.hset("student", "name", "zhangsan");
        System.out.println(jedis.hget("student", "name"));

        //set
        System.out.println("-----SET----");
        jedis.sadd("teacher", "zhangsan", "liyu");
        System.out.println(jedis.smembers("teacher"));

        //zset
        System.out.println("-----ZSET----");
        jedis.zadd("school", 10, "qizhong");
        jedis.zadd("school", 20, "yizhong");
        System.out.println(jedis.zrange("school", 0, -1));

        //keys *
        System.out.println("-----KEYS----");
        System.out.println(jedis.keys("*"));

        //关闭
        jedis.close();
    }

    @Test
    void testRedisTemplate() {
        testRedisString();

        testRedisList();

        testRedisHash();

        testRedisSetOperations();

        testRedisZSetOperations();

    }

    @Test
    void testRedisString() {
        //string
        redisTemplate.opsForValue().set("city", "xuzhou");
        String city = (String) redisTemplate.opsForValue().get("city");
        System.out.println(city);

        redisTemplate.opsForValue().set("city2","xuzhou",10, TimeUnit.SECONDS);

        Boolean flag = redisTemplate.opsForValue().setIfAbsent("city2", "xuzhou", 10, TimeUnit.SECONDS);
        System.out.println(flag);
    }

    @Test
    void testRedisList() {
        //list
        ListOperations listOperations = redisTemplate.opsForList();

        listOperations.leftPush("name", "zhangsan", "liyu");

        String name = (String) listOperations.getFirst("name");
        System.out.println(name);

        System.out.println((List<String>) listOperations.range("name", 0, -1));

        System.out.println(listOperations.rightPush("name", "taotao meng"));

        //从队头出队
        int size = listOperations.size("name").intValue();
        for (int i = 0; i < size; i++) {
            String s = (String) listOperations.leftPop("name");
            System.out.println(s);
        }
    }

    @Test
    void testRedisHash() {
        HashOperations hashOperations = redisTemplate.opsForHash();

        hashOperations.put("user:1001", "name", "zhangsan");
        hashOperations.put("user:1001", "age", 25);

        String name = (String) hashOperations.get("user:1001", "name");
        System.out.println(name); // 输出: zhangsan


        //batchly
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", "lisi");
        userMap.put("age", 30);
        userMap.put("city", "Beijing");

        hashOperations.putAll("user:1002", userMap);

        //
        Map<String, Object> user = hashOperations.entries("user:1002");
        System.out.println(user);

        boolean exists = hashOperations.hasKey("user:1001", "age");
        System.out.println(exists); // 输出: true

        hashOperations.delete("user:1001", "age");

        hashOperations.increment("user:1001", "age", 2); // age +2
        hashOperations.increment("user:1001", "age", -1); // age -1

        Set<String> keys = hashOperations.keys("user:1002");
        System.out.println(keys); // 输出: [name, age, city]

        List<Object> values = hashOperations.values("user:1002");
        System.out.println(values); // 输出: [lisi, 30, Beijing]

        long size = hashOperations.size("user:1002");
        System.out.println(size); // 输出: 3

        //
        redisTemplate.delete("user:1001");
        Set<String> hashKeys = hashOperations.keys("user:1002");
        hashOperations.delete("user:1002", hashKeys.toArray());

    }

    @Test
    public void testRedisSetOperations() {
        SetOperations<String, Object> setOperations = redisTemplate.opsForSet();
        String setKey1 = "fruits";
        String setKey2 = "tropicalFruits";

        // 添加元素到 Set
        setOperations.add(setKey1, "apple", "banana", "cherry");
        setOperations.add(setKey2, "banana", "mango", "pineapple");

        // 获取 Set 中的所有元素
        Set<Object> fruits = setOperations.members(setKey1);
        System.out.println("Fruits Set: " + fruits);

        // 判断是否包含某个元素
        boolean exists = setOperations.isMember(setKey1, "banana");
        System.out.println("Is banana in fruits set? " + exists);

        // 获取 Set 的大小
        long size = setOperations.size(setKey1);
        System.out.println("Fruits Set Size: " + size);

        // 计算交集、并集、差集
        Set<Object> intersection = setOperations.intersect(setKey1, setKey2);
        System.out.println("Intersection of Fruits and Tropical Fruits: " + intersection);

        Set<Object> union = setOperations.union(setKey1, setKey2);
        System.out.println("Union of Fruits and Tropical Fruits: " + union);

        Set<Object> difference = setOperations.difference(setKey1, setKey2);
        System.out.println("Difference (Fruits - Tropical Fruits): " + difference);

        // 随机弹出一个元素
        Object randomElement = setOperations.pop(setKey1);
        System.out.println("Randomly popped element: " + randomElement);

        // 移除元素
        setOperations.remove(setKey1, "apple");
        System.out.println("Fruits Set after removal: " + setOperations.members(setKey1));

        // 清空 Set
        redisTemplate.delete(setKey1);
        System.out.println("Fruits Set after deletion: " + setOperations.members(setKey1));
    }

    @Test
    /**
     * 测试 Redis ZSet（有序集合）操作
     */
    public void testRedisZSetOperations() {
        ZSetOperations<String, Object> zSetOperations = redisTemplate.opsForZSet();
        String zSetKey = "leaderboard";

        // 添加元素到 ZSet
        zSetOperations.add(zSetKey, "Alice", 100);
        zSetOperations.add(zSetKey, "Bob", 200);
        zSetOperations.add(zSetKey, "Charlie", 150);
        zSetOperations.add(zSetKey, "Dave", 180);

        // 获取 ZSet 中的所有元素（按分数升序排列）
        Set<ZSetOperations.TypedTuple<Object>> allScores = zSetOperations.rangeWithScores(zSetKey, 0, -1);
        System.out.println("Leaderboard (Ascending Order): " + allScores);

        // 获取 ZSet 中的所有元素（按分数降序排列）
        Set<ZSetOperations.TypedTuple<Object>> allScoresDesc = zSetOperations.reverseRangeWithScores(zSetKey, 0, -1);
        System.out.println("Leaderboard (Descending Order): " + allScoresDesc);

        // 获取某个元素的分数
        Double aliceScore = zSetOperations.score(zSetKey, "Alice");
        System.out.println("Alice's Score: " + aliceScore);

        // 获取某个元素的排名（升序）
        Long bobRank = zSetOperations.rank(zSetKey, "Bob");
        System.out.println("Bob's Rank (Ascending Order): " + bobRank);

        // 获取某个元素的排名（降序）
        Long bobRankDesc = zSetOperations.reverseRank(zSetKey, "Bob");
        System.out.println("Bob's Rank (Descending Order): " + bobRankDesc);

        // 递增元素的分数
        zSetOperations.incrementScore(zSetKey, "Alice", 50);
        System.out.println("Alice's new Score: " + zSetOperations.score(zSetKey, "Alice"));

        // 获取指定分数区间的元素
        Set<Object> scoreRange = zSetOperations.rangeByScore(zSetKey, 120, 190);
        System.out.println("Players with scores between 120 and 190: " + scoreRange);

        // 删除指定元素
        zSetOperations.remove(zSetKey, "Charlie");
        System.out.println("Leaderboard after removing Charlie: " + zSetOperations.rangeWithScores(zSetKey, 0, -1));

        // 清空整个 ZSet
        redisTemplate.delete(zSetKey);
        System.out.println("Leaderboard after deletion: " + zSetOperations.rangeWithScores(zSetKey, 0, -1));
    }

    @Test
    void testDishDtoSetOperations() {
//        DishDto dish = null;
//        redisTemplate.opsForValue().set("dish:1", dish, 5, TimeUnit.MINUTES);

        LocalDateTime now = LocalDateTime.now();
        redisTemplate.opsForValue().set("dish:2", now, 5, TimeUnit.MINUTES);

//        DishDto storedDish = (DishDto) redisTemplate.opsForValue().get("dish:1");
//        System.out.println("Stored Dish: " + storedDish);

    }

}
