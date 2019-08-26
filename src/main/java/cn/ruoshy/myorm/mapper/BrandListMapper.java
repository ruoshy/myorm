package cn.ruoshy.myorm.mapper;

import cn.ruoshy.myorm.entity.BrandList;
import cn.ruoshy.myorm.orm.annotation.Mapper;
import cn.ruoshy.myorm.orm.annotation.Select;

@Mapper
public interface BrandListMapper {

    @Select("SELECT * FROM BrandList WHERE id=#{id}")
    BrandList findById(Integer id);
}
