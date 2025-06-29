package com.as.CiCdDemo;

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
