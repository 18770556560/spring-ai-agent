package com.cc.springaiagent.utils;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class MyJsonReaderTest {

    @Autowired MyJsonReader myJsonReader;
    @Test
    void loadBasicJsonDocuments() {
        myJsonReader.loadBasicJsonDocuments().forEach(System.out::println);
    }

    @Test
    void loadJsonWithSpecificFields() {
        myJsonReader.loadJsonWithSpecificFields().forEach(System.out::println);
    }

    @Test
    void loadJsonWithPointer() {
        myJsonReader.loadJsonWithPointer().forEach(System.out::println);
    }
}