package com.as.CiCdDemo.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrlShortnerRequest {
    String url;
}
