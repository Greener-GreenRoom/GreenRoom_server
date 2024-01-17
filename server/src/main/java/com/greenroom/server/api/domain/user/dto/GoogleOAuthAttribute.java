package com.greenroom.server.api.domain.user.dto;

import com.greenroom.server.api.domain.user.entity.User;
import com.greenroom.server.api.domain.user.enums.Role;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
public class GoogleOAuthAttribute {

    private Map<String,Object> attributes;
    private String nameAttributeKey;
    private String name;
    private String email;

    @Builder
    public GoogleOAuthAttribute(Map<String, Object> attributes, String nameAttributeKey, String name, String email) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.name = name;
        this.email = email;
    }

    public static GoogleOAuthAttribute of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        return ofGoogle(userNameAttributeName, attributes);
    }

    private static GoogleOAuthAttribute ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        return GoogleOAuthAttribute.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    public User toEntity() {
        return User.builder()
                .name(name)
                .email(email)
                .role(Role.GUEST)
                .build();
    }
}
