package com.as.CiCdDemo.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class User {
    Long id;
    String name;
    int age;
    String city;
}
