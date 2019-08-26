package cn.ruoshy.myorm.mapper;

import cn.ruoshy.myorm.entity.Store;
import cn.ruoshy.myorm.orm.annotation.Mapper;
import cn.ruoshy.myorm.orm.annotation.Select;

@Mapper
public interface StoreMapper {

    @Select("SELECT * FROM Store WHERE Store_Name=#{name}")
    Store findByName(String name);
}
