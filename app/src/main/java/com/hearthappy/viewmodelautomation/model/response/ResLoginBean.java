package com.hearthappy.viewmodelautomation.model.response;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created Date 2019-12-06.
 *
 * @author ChenRui
 * ClassDescription：响应登录信息
 */
public class ResLoginBean implements Parcelable {


    /**
     * token : {"is_domain":false,"methods":["password"],"roles":[{"id":"9190430dd2cd464e8843ebab427b3480","name":"reader"},{"id":"59a4cad7b56f46019e184c9dd4b68b0d","name":"member"}],"expires_at":"2019-12-06T07:33:16.000000Z","project":{"domain":{"id":"default","name":"Default"},"id":"25f4a74802bf4a70ac2c1502449ee7f8","name":"admin"},"catalog":[{"endpoints":[{"region_id":"RegionOne","url":"http://192.168.50.101/veaudit","region":"RegionOne","interface":"public","id":"f1b3a895e57c438899bcaa1da6528e40"}],"type":"veaudit","id":"329f4f29e8074be380fbfeef71fa54f3","name":"veaudit"},{"endpoints":[{"region_id":"RegionOne","url":"http://192.168.50.101:9696","region":"RegionOne","interface":"public","id":"a3f46e3878da4d70a4fb913a2f30a50e"}],"type":"network","id":"337c0f6336e84113a431b4c11fe2b710","name":"neutron"},{"endpoints":[{"region_id":"RegionOne","url":"http://192.168.50.101/volume/v1/25f4a74802bf4a70ac2c1502449ee7f8","region":"RegionOne","interface":"admin","id":"06f44047b8ae4510abbf9d1534b28b47"},{"region_id":"RegionOne","url":"http://192.168.50.101/volume/v1/25f4a74802bf4a70ac2c1502449ee7f8","region":"RegionOne","interface":"public","id":"995725d2ffe0471f90e1e1a8e8914760"},{"region_id":"RegionOne","url":"http://192.168.50.101/volume/v1/25f4a74802bf4a70ac2c1502449ee7f8","region":"RegionOne","interface":"internal","id":"be45ddcc6bdd48d4a7d49305036a24f8"}],"type":"volume","id":"5254bcfc54314fd89317cc048bb625a6","name":"cinder"},{"endpoints":[{"region_id":"RegionOne","url":"http://192.168.50.101/vediscover","region":"RegionOne","interface":"public","id":"d2cc74d4edaa4795af93a6042f6f71d3"}],"type":"vediscover","id":"675bf50a6b184b409bb261eccc3df5df","name":"vediscover"},{"endpoints":[{"region_id":"RegionOne","url":"http://192.168.50.101/volume/v3/25f4a74802bf4a70ac2c1502449ee7f8","region":"RegionOne","interface":"admin","id":"06e449b7d4334e808e589bfc0b4f93cf"},{"region_id":"RegionOne","url":"http://192.168.50.101/volume/v3/25f4a74802bf4a70ac2c1502449ee7f8","region":"RegionOne","interface":"public","id":"0eb81e6ea31c453a9061ad355cd37bf6"},{"region_id":"RegionOne","url":"http://192.168.50.101/volume/v3/25f4a74802bf4a70ac2c1502449ee7f8","region":"RegionOne","interface":"internal","id":"f0f00b11b96c4059b3a7f534a746194e"}],"type":"volumev3","id":"6c1997df6ce14c839a12805431fb81a2","name":"cinderv3"},{"endpoints":[{"region_id":"RegionOne","url":"http://192.168.50.101:8980","region":"RegionOne","interface":"public","id":"a0d0856b924e4e98b7c613e4f5a39f6d"}],"type":"idvserver","id":"77a8d20d5145401dbaee62308016853b","name":"idvserver"},{"endpoints":[{"region_id":"RegionOne","url":"http://192.168.50.101/placement","region":"RegionOne","interface":"public","id":"b6cca0c595cc4b95a82a429edc2e3cf1"}],"type":"placement","id":"77de14f624dd43bdbfb65578b20f5ec5","name":"placement"},{"endpoints":[{"region_id":"RegionOne","url":"http://192.168.50.101/volume/v2/25f4a74802bf4a70ac2c1502449ee7f8","region":"RegionOne","interface":"public","id":"2d72553bcd20494bb5ee364ffc34006a"},{"region_id":"RegionOne","url":"http://192.168.50.101/volume/v2/25f4a74802bf4a70ac2c1502449ee7f8","region":"RegionOne","interface":"admin","id":"6efbe98c6d1f40188b942d536bf83e5a"},{"region_id":"RegionOne","url":"http://192.168.50.101/volume/v2/25f4a74802bf4a70ac2c1502449ee7f8","region":"RegionOne","interface":"internal","id":"924d4c76821a4c9685e5c137c99e6e6f"}],"type":"volumev2","id":"9723edebadce4004ac3cea5d2e2ec222","name":"cinderv2"},{"endpoints":[{"region_id":"RegionOne","url":"http://192.168.50.101/compute/v2.1/","region":"RegionOne","interface":"public","id":"81e8216168f04491adef711ff1584cf7"}],"type":"compute","id":"a5bc15e5838046a08c33179ef0837d29","name":"nova"},{"endpoints":[{"region_id":"RegionOne","url":"http://192.168.50.177:8080","region":"RegionOne","interface":"public","id":"2bef0204a1ca4f95a332fee48fac897a"}],"type":"ceph","id":"b15e28aac5894bcd8673308eb68afd4d","name":"ceph"},{"endpoints":[{"region_id":"RegionOne","url":"http://192.168.50.101:38082","region":"RegionOne","interface":"public","id":"520d89d587564f0d8455eeb73d26acb7"}],"type":"ngdserver","id":"bc6c20c953ac459996f2661e25279e49","name":"ngdserver"},{"endpoints":[{"region_id":"RegionOne","url":"http://192.168.50.122:8993","region":"RegionOne","interface":"public","id":"793a084a83e0452b98058d5a378e7476"}],"type":"termserver","id":"cce2ac4fe7724ec784433013e5d35dbc","name":"termserver"},{"endpoints":[{"region_id":"RegionOne","url":"http://192.168.50.101/image","region":"RegionOne","interface":"public","id":"813bc79c498f41499156c7e0f7eaa92c"}],"type":"image","id":"dd672b7afb8d4675914e4b660f66cd4d","name":"glance"},{"endpoints":[{"region_id":"RegionOne","url":"http://192.168.50.101/identity","region":"RegionOne","interface":"public","id":"325d3e305699427994375e8684f41294"},{"region_id":"RegionOne","url":"http://192.168.50.101/identity","region":"RegionOne","interface":"admin","id":"9867a6687bd7481182ad6380a7d6c3d4"},{"region_id":"RegionOne","url":"http://192.168.50.101/identity","region":"RegionOne","interface":"internal","id":"bd7bbe7f12a546f99d8f396786df2507"}],"type":"identity","id":"e0c87dac95c5439fbb6a701a9ae955e4","name":"keystone"}],"user":{"password_expires_at":null,"domain":{"id":"default","name":"Default"},"id":"c7d3c7cb41f1484ebd952b6aeb947d07","name":"chenrui"},"audit_ids":["XuiAT9GiTI6QFZsjNGhuJg"],"issued_at":"2019-12-06T06:33:16.000000Z"}
     */

    private TokenBean token;

    private ResLoginBean(Parcel in) {
    }

    public static final Creator<ResLoginBean> CREATOR = new Creator<ResLoginBean>() {
        @Override
        public ResLoginBean createFromParcel(Parcel in) {
            return new ResLoginBean(in);
        }

        @Override
        public ResLoginBean[] newArray(int size) {
            return new ResLoginBean[size];
        }
    };

    public TokenBean getToken() {
        return token;
    }

    public void setToken(TokenBean token) {
        this.token = token;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    public static class TokenBean implements Parcelable {
        /**
         * is_domain : false
         * methods : ["password"]
         * roles : [{"id":"9190430dd2cd464e8843ebab427b3480","name":"reader"},{"id":"59a4cad7b56f46019e184c9dd4b68b0d","name":"member"}]
         * expires_at : 2019-12-06T07:33:16.000000Z
         * project : {"domain":{"id":"default","name":"Default"},"id":"25f4a74802bf4a70ac2c1502449ee7f8","name":"admin"}
         * catalog : [{"endpoints":[{"region_id":"RegionOne","url":"http://192.168.50.101/veaudit","region":"RegionOne","interface":"public","id":"f1b3a895e57c438899bcaa1da6528e40"}],"type":"veaudit","id":"329f4f29e8074be380fbfeef71fa54f3","name":"veaudit"},{"endpoints":[{"region_id":"RegionOne","url":"http://192.168.50.101:9696","region":"RegionOne","interface":"public","id":"a3f46e3878da4d70a4fb913a2f30a50e"}],"type":"network","id":"337c0f6336e84113a431b4c11fe2b710","name":"neutron"},{"endpoints":[{"region_id":"RegionOne","url":"http://192.168.50.101/volume/v1/25f4a74802bf4a70ac2c1502449ee7f8","region":"RegionOne","interface":"admin","id":"06f44047b8ae4510abbf9d1534b28b47"},{"region_id":"RegionOne","url":"http://192.168.50.101/volume/v1/25f4a74802bf4a70ac2c1502449ee7f8","region":"RegionOne","interface":"public","id":"995725d2ffe0471f90e1e1a8e8914760"},{"region_id":"RegionOne","url":"http://192.168.50.101/volume/v1/25f4a74802bf4a70ac2c1502449ee7f8","region":"RegionOne","interface":"internal","id":"be45ddcc6bdd48d4a7d49305036a24f8"}],"type":"volume","id":"5254bcfc54314fd89317cc048bb625a6","name":"cinder"},{"endpoints":[{"region_id":"RegionOne","url":"http://192.168.50.101/vediscover","region":"RegionOne","interface":"public","id":"d2cc74d4edaa4795af93a6042f6f71d3"}],"type":"vediscover","id":"675bf50a6b184b409bb261eccc3df5df","name":"vediscover"},{"endpoints":[{"region_id":"RegionOne","url":"http://192.168.50.101/volume/v3/25f4a74802bf4a70ac2c1502449ee7f8","region":"RegionOne","interface":"admin","id":"06e449b7d4334e808e589bfc0b4f93cf"},{"region_id":"RegionOne","url":"http://192.168.50.101/volume/v3/25f4a74802bf4a70ac2c1502449ee7f8","region":"RegionOne","interface":"public","id":"0eb81e6ea31c453a9061ad355cd37bf6"},{"region_id":"RegionOne","url":"http://192.168.50.101/volume/v3/25f4a74802bf4a70ac2c1502449ee7f8","region":"RegionOne","interface":"internal","id":"f0f00b11b96c4059b3a7f534a746194e"}],"type":"volumev3","id":"6c1997df6ce14c839a12805431fb81a2","name":"cinderv3"},{"endpoints":[{"region_id":"RegionOne","url":"http://192.168.50.101:8980","region":"RegionOne","interface":"public","id":"a0d0856b924e4e98b7c613e4f5a39f6d"}],"type":"idvserver","id":"77a8d20d5145401dbaee62308016853b","name":"idvserver"},{"endpoints":[{"region_id":"RegionOne","url":"http://192.168.50.101/placement","region":"RegionOne","interface":"public","id":"b6cca0c595cc4b95a82a429edc2e3cf1"}],"type":"placement","id":"77de14f624dd43bdbfb65578b20f5ec5","name":"placement"},{"endpoints":[{"region_id":"RegionOne","url":"http://192.168.50.101/volume/v2/25f4a74802bf4a70ac2c1502449ee7f8","region":"RegionOne","interface":"public","id":"2d72553bcd20494bb5ee364ffc34006a"},{"region_id":"RegionOne","url":"http://192.168.50.101/volume/v2/25f4a74802bf4a70ac2c1502449ee7f8","region":"RegionOne","interface":"admin","id":"6efbe98c6d1f40188b942d536bf83e5a"},{"region_id":"RegionOne","url":"http://192.168.50.101/volume/v2/25f4a74802bf4a70ac2c1502449ee7f8","region":"RegionOne","interface":"internal","id":"924d4c76821a4c9685e5c137c99e6e6f"}],"type":"volumev2","id":"9723edebadce4004ac3cea5d2e2ec222","name":"cinderv2"},{"endpoints":[{"region_id":"RegionOne","url":"http://192.168.50.101/compute/v2.1/","region":"RegionOne","interface":"public","id":"81e8216168f04491adef711ff1584cf7"}],"type":"compute","id":"a5bc15e5838046a08c33179ef0837d29","name":"nova"},{"endpoints":[{"region_id":"RegionOne","url":"http://192.168.50.177:8080","region":"RegionOne","interface":"public","id":"2bef0204a1ca4f95a332fee48fac897a"}],"type":"ceph","id":"b15e28aac5894bcd8673308eb68afd4d","name":"ceph"},{"endpoints":[{"region_id":"RegionOne","url":"http://192.168.50.101:38082","region":"RegionOne","interface":"public","id":"520d89d587564f0d8455eeb73d26acb7"}],"type":"ngdserver","id":"bc6c20c953ac459996f2661e25279e49","name":"ngdserver"},{"endpoints":[{"region_id":"RegionOne","url":"http://192.168.50.122:8993","region":"RegionOne","interface":"public","id":"793a084a83e0452b98058d5a378e7476"}],"type":"termserver","id":"cce2ac4fe7724ec784433013e5d35dbc","name":"termserver"},{"endpoints":[{"region_id":"RegionOne","url":"http://192.168.50.101/image","region":"RegionOne","interface":"public","id":"813bc79c498f41499156c7e0f7eaa92c"}],"type":"image","id":"dd672b7afb8d4675914e4b660f66cd4d","name":"glance"},{"endpoints":[{"region_id":"RegionOne","url":"http://192.168.50.101/identity","region":"RegionOne","interface":"public","id":"325d3e305699427994375e8684f41294"},{"region_id":"RegionOne","url":"http://192.168.50.101/identity","region":"RegionOne","interface":"admin","id":"9867a6687bd7481182ad6380a7d6c3d4"},{"region_id":"RegionOne","url":"http://192.168.50.101/identity","region":"RegionOne","interface":"internal","id":"bd7bbe7f12a546f99d8f396786df2507"}],"type":"identity","id":"e0c87dac95c5439fbb6a701a9ae955e4","name":"keystone"}]
         * user : {"password_expires_at":null,"domain":{"id":"default","name":"Default"},"id":"c7d3c7cb41f1484ebd952b6aeb947d07","name":"chenrui"}
         * audit_ids : ["XuiAT9GiTI6QFZsjNGhuJg"]
         * issued_at : 2019-12-06T06:33:16.000000Z
         */

        private boolean is_domain;
        private String expires_at;
        private ProjectBean project;
        private UserBean user;
        private String issued_at;
        private List<String> methods;
        private List<RolesBean> roles;
        private List<CatalogBean> catalog;
        private List<String> audit_ids;

        protected TokenBean(Parcel in) {
            is_domain = in.readByte() != 0;
            expires_at = in.readString();
            issued_at = in.readString();
            methods = in.createStringArrayList();
            audit_ids = in.createStringArrayList();
        }

        public static final Creator<TokenBean> CREATOR = new Creator<TokenBean>() {
            @Override
            public TokenBean createFromParcel(Parcel in) {
                return new TokenBean(in);
            }

            @Override
            public TokenBean[] newArray(int size) {
                return new TokenBean[size];
            }
        };

        public boolean isIs_domain() {
            return is_domain;
        }

        public void setIs_domain(boolean is_domain) {
            this.is_domain = is_domain;
        }

        public String getExpires_at() {
            return expires_at;
        }

        public void setExpires_at(String expires_at) {
            this.expires_at = expires_at;
        }

        public ProjectBean getProject() {
            return project;
        }

        public void setProject(ProjectBean project) {
            this.project = project;
        }

        public UserBean getUser() {
            return user;
        }

        public void setUser(UserBean user) {
            this.user = user;
        }

        public String getIssued_at() {
            return issued_at;
        }

        public void setIssued_at(String issued_at) {
            this.issued_at = issued_at;
        }

        public List<String> getMethods() {
            return methods;
        }

        public void setMethods(List<String> methods) {
            this.methods = methods;
        }

        public List<RolesBean> getRoles() {
            return roles;
        }

        public void setRoles(List<RolesBean> roles) {
            this.roles = roles;
        }

        public List<CatalogBean> getCatalog() {
            return catalog;
        }

        public void setCatalog(List<CatalogBean> catalog) {
            this.catalog = catalog;
        }

        public List<String> getAudit_ids() {
            return audit_ids;
        }

        public void setAudit_ids(List<String> audit_ids) {
            this.audit_ids = audit_ids;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeByte((byte) (is_domain ? 1 : 0));
            dest.writeString(expires_at);
            dest.writeString(issued_at);
            dest.writeStringList(methods);
            dest.writeStringList(audit_ids);
        }

        public static class ProjectBean implements Parcelable {
            /**
             * domain : {"id":"default","name":"Default"}
             * id : 25f4a74802bf4a70ac2c1502449ee7f8
             * name : admin
             */

            private DomainBean domain;
            private String id;
            private String name;

            protected ProjectBean(Parcel in) {
                id = in.readString();
                name = in.readString();
            }

            public static final Creator<ProjectBean> CREATOR = new Creator<ProjectBean>() {
                @Override
                public ProjectBean createFromParcel(Parcel in) {
                    return new ProjectBean(in);
                }

                @Override
                public ProjectBean[] newArray(int size) {
                    return new ProjectBean[size];
                }
            };

            public DomainBean getDomain() {
                return domain;
            }

            public void setDomain(DomainBean domain) {
                this.domain = domain;
            }

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(id);
                dest.writeString(name);
            }

            public static class DomainBean implements Parcelable {
                /**
                 * id : default
                 * name : Default
                 */

                private String id;
                private String name;

                protected DomainBean(Parcel in) {
                    id = in.readString();
                    name = in.readString();
                }

                public static final Creator<DomainBean> CREATOR = new Creator<DomainBean>() {
                    @Override
                    public DomainBean createFromParcel(Parcel in) {
                        return new DomainBean(in);
                    }

                    @Override
                    public DomainBean[] newArray(int size) {
                        return new DomainBean[size];
                    }
                };

                public String getId() {
                    return id;
                }

                public void setId(String id) {
                    this.id = id;
                }

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                @Override
                public int describeContents() {
                    return 0;
                }

                @Override
                public void writeToParcel(Parcel dest, int flags) {
                    dest.writeString(id);
                    dest.writeString(name);
                }
            }
        }

        public static class UserBean implements Parcelable {
            /**
             * password_expires_at : null
             * domain : {"id":"default","name":"Default"}
             * id : c7d3c7cb41f1484ebd952b6aeb947d07
             * name : chenrui
             */

            private Object password_expires_at;
            private DomainBeanX domain;
            private String id;
            private String name;

            protected UserBean(Parcel in) {
                id = in.readString();
                name = in.readString();
            }

            public static final Creator<UserBean> CREATOR = new Creator<UserBean>() {
                @Override
                public UserBean createFromParcel(Parcel in) {
                    return new UserBean(in);
                }

                @Override
                public UserBean[] newArray(int size) {
                    return new UserBean[size];
                }
            };

            public Object getPassword_expires_at() {
                return password_expires_at;
            }

            public void setPassword_expires_at(Object password_expires_at) {
                this.password_expires_at = password_expires_at;
            }

            public DomainBeanX getDomain() {
                return domain;
            }

            public void setDomain(DomainBeanX domain) {
                this.domain = domain;
            }

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(id);
                dest.writeString(name);
            }

            public static class DomainBeanX implements Parcelable {
                /**
                 * id : default
                 * name : Default
                 */

                private String id;
                private String name;

                protected DomainBeanX(Parcel in) {
                    id = in.readString();
                    name = in.readString();
                }

                public static final Creator<DomainBeanX> CREATOR = new Creator<DomainBeanX>() {
                    @Override
                    public DomainBeanX createFromParcel(Parcel in) {
                        return new DomainBeanX(in);
                    }

                    @Override
                    public DomainBeanX[] newArray(int size) {
                        return new DomainBeanX[size];
                    }
                };

                public String getId() {
                    return id;
                }

                public void setId(String id) {
                    this.id = id;
                }

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                @Override
                public int describeContents() {
                    return 0;
                }

                @Override
                public void writeToParcel(Parcel dest, int flags) {
                    dest.writeString(id);
                    dest.writeString(name);
                }
            }
        }

        public static class RolesBean {
            /**
             * id : 9190430dd2cd464e8843ebab427b3480
             * name : reader
             */

            private String id;
            private String name;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        }

        public static class CatalogBean {
            /**
             * endpoints : [{"region_id":"RegionOne","url":"http://192.168.50.101/veaudit","region":"RegionOne","interface":"public","id":"f1b3a895e57c438899bcaa1da6528e40"}]
             * type : veaudit
             * id : 329f4f29e8074be380fbfeef71fa54f3
             * name : veaudit
             */

            private String type;
            private String id;
            private String name;
            private List<EndpointsBean> endpoints;

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public List<EndpointsBean> getEndpoints() {
                return endpoints;
            }

            public void setEndpoints(List<EndpointsBean> endpoints) {
                this.endpoints = endpoints;
            }

            public static class EndpointsBean {
                /**
                 * region_id : RegionOne
                 * url : http://192.168.50.101/veaudit
                 * region : RegionOne
                 * interface : public
                 * id : f1b3a895e57c438899bcaa1da6528e40
                 */

                private String region_id;
                private String url;
                private String region;
                @SerializedName("interface")
                private String interfaceX;
                private String id;

                public String getRegion_id() {
                    return region_id;
                }

                public void setRegion_id(String region_id) {
                    this.region_id = region_id;
                }

                public String getUrl() {
                    return url;
                }

                public void setUrl(String url) {
                    this.url = url;
                }

                public String getRegion() {
                    return region;
                }

                public void setRegion(String region) {
                    this.region = region;
                }

                public String getInterfaceX() {
                    return interfaceX;
                }

                public void setInterfaceX(String interfaceX) {
                    this.interfaceX = interfaceX;
                }

                public String getId() {
                    return id;
                }

                public void setId(String id) {
                    this.id = id;
                }
            }
        }
    }
}
