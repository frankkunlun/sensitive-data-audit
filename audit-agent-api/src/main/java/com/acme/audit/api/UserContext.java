package com.acme.audit.api;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public final class UserContext {
    public static final UserContext UNKNOWN = new UserContext("UNKNOWN", null, null, Collections.<String>emptySet(), false);
    private final String userId, userName, organizationId; private final Set<String> roleCodes; private final boolean authenticated;
    public UserContext(String userId, String userName, String organizationId, Set<String> roleCodes, boolean authenticated) {
        this.userId=userId; this.userName=userName; this.organizationId=organizationId;
        this.roleCodes=Collections.unmodifiableSet(new LinkedHashSet<String>(roleCodes)); this.authenticated=authenticated;
    }
    public String getUserId(){return userId;} public String getUserName(){return userName;} public String getOrganizationId(){return organizationId;}
    public Set<String> getRoleCodes(){return roleCodes;} public boolean isAuthenticated(){return authenticated;}
}
