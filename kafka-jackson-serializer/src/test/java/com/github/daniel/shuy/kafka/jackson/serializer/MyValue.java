package com.github.daniel.shuy.kafka.jackson.serializer;

import java.util.Objects;

public class MyValue {
    private String name;
    private int age;

    public MyValue() {
    }

    public MyValue(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.name);
        hash = 37 * hash + this.age;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MyValue other = (MyValue) obj;
        if (this.age != other.age) {
            return false;
        }
        return Objects.equals(this.name, other.name);
    }
}
