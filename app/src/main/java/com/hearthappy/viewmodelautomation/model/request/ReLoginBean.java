package com.hearthappy.viewmodelautomation.model.request;

import com.hearthappy.annotations.Body;
import com.hearthappy.annotations.Request;
import com.hearthappy.annotations.RequestType;

import java.util.List;

/**
 * Created Date 2019-12-06.
 *
 * @author ChenRui
 * ClassDescription：
 */

@Request(type = RequestType.POST, urlString = "/identity/v3/auth/tokens/")
@Body
public class ReLoginBean {


    /**
     * auth : {"identity":{"methods":["password"],"password":{"user":{"domain":{"name":"default"},"name":"chenrui","password":"cr123456"}}},"scope":{"project":{"domain":{"name":"default"},"name":"admin"}}}
     */

    private AuthBean auth;

    public AuthBean getAuth() {
        return auth;
    }

    public void setAuth(AuthBean auth) {
        this.auth = auth;
    }

    public static class AuthBean {
        /**
         * identity : {"methods":["password"],"password":{"user":{"domain":{"name":"default"},"name":"chenrui","password":"cr123456"}}}
         * scope : {"project":{"domain":{"name":"default"},"name":"admin"}}
         */

        private IdentityBean identity;
        private ScopeBean scope;

        public IdentityBean getIdentity() {
            return identity;
        }

        public void setIdentity(IdentityBean identity) {
            this.identity = identity;
        }

        public ScopeBean getScope() {
            return scope;
        }

        public void setScope(ScopeBean scope) {
            this.scope = scope;
        }

        public static class IdentityBean {
            /**
             * methods : ["password"]
             * password : {"user":{"domain":{"name":"default"},"name":"chenrui","password":"cr123456"}}
             */

            private PasswordBean password;
            private List<String> methods;

            public PasswordBean getPassword() {
                return password;
            }

            public void setPassword(PasswordBean password) {
                this.password = password;
            }

            public List<String> getMethods() {
                return methods;
            }

            public void setMethods(List<String> methods) {
                this.methods = methods;
            }

            public static class PasswordBean {
                /**
                 * user : {"domain":{"name":"default"},"name":"chenrui","password":"cr123456"}
                 */

                private UserBean user;

                public UserBean getUser() {
                    return user;
                }

                public void setUser(UserBean user) {
                    this.user = user;
                }

                public static class UserBean {
                    /**
                     * domain : {"name":"default"}
                     * name : chenrui
                     * password : cr123456
                     */

                    private DomainBean domain;
                    private String name;
                    private String password;

                    public DomainBean getDomain() {
                        return domain;
                    }

                    public void setDomain(DomainBean domain) {
                        this.domain = domain;
                    }

                    public String getName() {
                        return name;
                    }

                    public void setName(String name) {
                        this.name = name;
                    }

                    public String getPassword() {
                        return password;
                    }

                    public void setPassword(String password) {
                        this.password = password;
                    }

                    public static class DomainBean {
                        /**
                         * name : default
                         */

                        private String name;

                        public String getName() {
                            return name;
                        }

                        public void setName(String name) {
                            this.name = name;
                        }
                    }
                }
            }
        }

        public static class ScopeBean {
            /**
             * project : {"domain":{"name":"default"},"name":"admin"}
             */

            private ProjectBean project;

            public ProjectBean getProject() {
                return project;
            }

            public void setProject(ProjectBean project) {
                this.project = project;
            }

            public static class ProjectBean {
                /**
                 * domain : {"name":"default"}
                 * name : admin
                 */

                private DomainBeanX domain;
                private String name;

                public DomainBeanX getDomain() {
                    return domain;
                }

                public void setDomain(DomainBeanX domain) {
                    this.domain = domain;
                }

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                public static class DomainBeanX {
                    /**
                     * name : default
                     */

                    private String name;

                    public String getName() {
                        return name;
                    }

                    public void setName(String name) {
                        this.name = name;
                    }
                }
            }
        }
    }
}
