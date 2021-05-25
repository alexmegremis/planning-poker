package com.alexmegremis.planningpoker;

import org.springframework.boot.actuate.info.GitInfoContributor;
import org.springframework.boot.info.GitProperties;
import org.springframework.stereotype.Component;

@Component
public class CustomGitInfoContributor extends GitInfoContributor {

    public CustomGitInfoContributor(final GitProperties properties) {
        super(properties);
    }
}
