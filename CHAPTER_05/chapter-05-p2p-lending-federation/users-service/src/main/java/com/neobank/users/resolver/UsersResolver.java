package com.neobank.users.resolver;

import com.neobank.users.model.User;
import com.neobank.users.service.UsersService;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@DgsComponent
@RequiredArgsConstructor
public class UsersResolver {
    
    private final UsersService usersService;
    
    @DgsQuery
    public User user(@InputArgument String id) {
        return usersService.getUserById(id);
    }
    
    @DgsQuery
    public List<User> users() {
        return usersService.getAllUsers();
    }
    
    @DgsQuery
    public List<User> verifiedLenders() {
        return usersService.getVerifiedLenders();
    }
    
    @DgsQuery
    public List<User> verifiedBorrowers() {
        return usersService.getVerifiedBorrowers();
    }
    
    @DgsMutation
    public Map<String, Object> createUser(@InputArgument Map<String, Object> input) {
        try {
            String email = input.get("email").toString();
            String fullName = input.get("fullName").toString();
            User.UserType userType = User.UserType.valueOf(input.get("userType").toString());
            
            User user = usersService.createUser(email, fullName, userType);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User created successfully");
            response.put("user", user);
            return response;
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            response.put("user", null);
            return response;
        }
    }
}
