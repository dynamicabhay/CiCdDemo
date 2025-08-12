package com.as.CiCdDemo.dto;

import com.as.CiCdDemo.model.Role;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.*;

import java.util.Set;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    String username;
    String password;
    Set<Role> roles;
}
