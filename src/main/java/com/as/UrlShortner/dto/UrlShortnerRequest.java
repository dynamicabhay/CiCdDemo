package com.as.UrlShortner.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrlShortnerRequest {
    String url;
    String alias;
}
