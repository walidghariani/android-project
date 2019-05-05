package com.example.processmaker.model;

public class Workflow {
    private String pro_uid;
    private String tas_uid;
    private String name;
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Workflow{" +
                "pro_uid='" + pro_uid + '\'' +
                ", tas_uid='" + tas_uid + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    public String getPro_uid() {
        return pro_uid;
    }

    public String getTas_uid() {
        return tas_uid;
    }

    public void setPro_uid(String pro_uid) {
        this.pro_uid = pro_uid;
    }

    public void setTas_uid(String tas_uid) {
        this.tas_uid = tas_uid;
    }

    public Workflow(String pro_uid, String tas_uid, String name, int id) {
        this.pro_uid = pro_uid;
        this.tas_uid = tas_uid;
        this.name = name;
        this.id = id;
    }

}
