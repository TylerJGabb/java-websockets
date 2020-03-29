package com.gabb.sb.spring;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class TestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String string;

    @Override
    public String toString() {
        return "TestEntity{" +
                "id=" + id +
                ", string='" + string + '\'' +
                '}';
    }

    public TestEntity() { }

    public TestEntity(String string) {
        this.string = string;
    }
}
