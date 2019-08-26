package cn.ruoshy.myorm.entity;

public class Store {
    private Integer store_Id;
    private String store_Name;
    private String city;
    private String head_Portrait;
    private String internal_Classification;
    private String store_Array_Img;

    public Integer getStore_Id() {
        return store_Id;
    }

    public void setStore_Id(Integer store_Id) {
        this.store_Id = store_Id;
    }

    public String getStore_Name() {
        return store_Name;
    }

    public void setStore_Name(String store_Name) {
        this.store_Name = store_Name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getHead_Portrait() {
        return head_Portrait;
    }

    public void setHead_Portrait(String head_Portrait) {
        this.head_Portrait = head_Portrait;
    }

    public String getInternal_Classification() {
        return internal_Classification;
    }

    public void setInternal_Classification(String internal_Classification) {
        this.internal_Classification = internal_Classification;
    }

    public String getStore_Array_Img() {
        return store_Array_Img;
    }

    public void setStore_Array_Img(String store_Array_Img) {
        this.store_Array_Img = store_Array_Img;
    }
}
