package com.neobank.users.datafetcher;

import com.neobank.users.model.User;
import com.neobank.users.service.UsersService;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsEntityFetcher;
import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * EntityFetcher para Apollo Federation
 * Permite que otros subgrafos resuelvan referencias a User usando solo el ID
 */
@DgsComponent
@RequiredArgsConstructor
public class UserEntityFetcher {
    
    private final UsersService usersService;
    
    @DgsEntityFetcher(name = "User")
    public User resolveUser(Map<String, Object> values) {
        String id = (String) values.get("id");
        return usersService.getUserById(id);
    }
}
