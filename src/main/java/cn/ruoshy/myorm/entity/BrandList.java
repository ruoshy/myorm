package cn.ruoshy.myorm.entity;

public class BrandList {
    private Integer id;
    private Integer store_Id;
    private String brand_Img;
    private String brand_Name;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getStore_Id() {
        return store_Id;
    }

    public void setStore_Id(Integer store_Id) {
        this.store_Id = store_Id;
    }

    public String getBrand_Img() {
        return brand_Img;
    }

    public void setBrand_Img(String brand_Img) {
        this.brand_Img = brand_Img;
    }

    public String getBrand_Name() {
        return brand_Name;
    }

    public void setBrand_Name(String brand_Name) {
        this.brand_Name = brand_Name;
    }
}
