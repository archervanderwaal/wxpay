package me.stormma.demo;

import me.stormma.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created on 2017/5/26.
 *
 * @author stormma
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class XDTest {

    @Autowired
    private Task task;

    @Test
    public void test() {
        int i = 0;
        while (true) {
            task.uploadImg();
            i++;
            System.out.println(i);
        }
    }
}
