package org.example.kaoyanplatform;

import org.example.kaoyanplatform.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class KaoYanPlatformApplicationTests {

    @Autowired
    private UserMapper userMapper;

    @Test
    void contextLoads() {
        // 查询所有用户
        userMapper.selectList(null).forEach(System.out::println);
    }

}
