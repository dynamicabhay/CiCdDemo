package com.as.UrlShortner.dto;

import com.as.UrlShortner.model.Role;
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
