package com.acme.audit.api;
public interface UserContextResolver { int order(); UserContext resolve(Object request); }
