package cn.ruoshy.myorm.controller;

import cn.ruoshy.myorm.mapper.BrandListMapper;
import cn.ruoshy.myorm.mapper.StoreMapper;
import com.alibaba.fastjson.JSON;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


@RestController
public class DemoController {

    @Resource
    private StoreMapper storeMapper;

    @RequestMapping("/store")
    public String getStore() {
        return JSON.toJSONString(storeMapper.findByName("Apple Store 官方旗舰店"));
    }


    @Resource
    private BrandListMapper brandListMapper;
    @RequestMapping("/brands")
    public String getBrandList() {
        return JSON.toJSONString(brandListMapper.findById(2));
    }
}
