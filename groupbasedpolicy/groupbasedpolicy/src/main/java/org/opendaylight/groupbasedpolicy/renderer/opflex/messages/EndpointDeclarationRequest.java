/*
 * Copyright (C) 2014 Cisco Systems, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Authors : Thomas Bachman
 */
package org.opendaylight.groupbasedpolicy.renderer.opflex.messages;

import java.util.List;

import org.opendaylight.groupbasedpolicy.jsonrpc.RpcMessage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
@JsonDeserialize
public class EndpointDeclarationRequest extends RpcMessage {

    public static final String DECLARATION_MESSAGE = "endpoint_declaration";

    static public class Params {
        private String subject;
        private String context;
        private String policy_name;
        private String location;
        private List<String> identifier;
        private List<String> data;
        private String status;
        private int prr;
        public String getSubject() {
            return subject;
        }
        public void setSubject(String subject) {
            this.subject = subject;
        }
        public String getContext() {
            return context;
        }
        public void setContext(String context) {
            this.context = context;
        }
        public String getPolicy_name() {
            return policy_name;
        }
        public void setPolicy_name(String policy_name) {
            this.policy_name = policy_name;
        }
        public String getLocation() {
            return location;
        }
        public void setLocation(String location) {
            this.location = location;
        }
        public List<String> getIdentifier() {
            return identifier;
        }
        public void setIdentifier(List<String> identifier) {
            this.identifier = identifier;
        }
        public List<String> getData() {
            return data;
        }
        public void setData(List<String> data) {
            this.data = data;
        }
        public String getStatus() {
            return status;
        }
        public void setStatus(String status) {
            this.status = status;
        }
        public int getPrr() {
            return prr;
        }
        public void setPrr(int prr) {
            this.prr = prr;
        }
    }
    private String id;
    private String method;
    private List<Params> params;

    @JsonIgnore
    private String name;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public void setMethod(String method) {
        this.method = method;
    }

    public List<Params> getParams() {
        return this.params;
    }

    public void setParams(List<Params> params) {
        this.params = params;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public EndpointDeclarationRequest(String name) {
        this.name = name;
    }

    public EndpointDeclarationRequest() {
        this.name = DECLARATION_MESSAGE;
        this.method = DECLARATION_MESSAGE;
    }
    
    /**
     * Minimal check on validity of message
     * @return true if message has passed validity check
     */
    @JsonIgnore
    @Override
    public boolean valid() {
        if (params == null)
            return false;
        if (params.get(0) == null)
            return false;
        if (params.get(0).getSubject() == null)
            return false;
        if (params.get(0).getContext() == null)
            return false;
        if (params.get(0).getPolicy_name() == null)
            return false;
        if (params.get(0).getStatus() == null)
            return false;
        if (params.get(0).getLocation() == null)
            return false;
        if (params.get(0).getIdentifier() == null)
            return false;
        if (params.get(0).getIdentifier().size() <= 0)
            return false;
        
        return true;
    }
}
