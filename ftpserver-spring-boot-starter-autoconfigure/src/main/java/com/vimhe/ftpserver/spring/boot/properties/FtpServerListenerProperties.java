/*
 * Copyright 2019 Vimhe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vimhe.ftpserver.spring.boot.properties;

import lombok.Data;
import org.apache.ftpserver.ssl.ClientAuth;
import org.apache.mina.filter.firewall.Subnet;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.InetAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * The listener shipped with FtpServer is called "nio-listener" since it is based Java NIO for performance and
 * scalability.
 *
 * @author Vimhe
 */
@Data
@Component
public class FtpServerListenerProperties {

    /**
     * The listener name, if "default" it will override the settings on the default listener.
     * <p>
     * Default value: default
     */
    private String name = "default";

    /**
     * The port on which the listener will accept connections.
     * <p>
     * Default value: 21
     */
    private Integer port = 21;

    /**
     * Server address the listener will bind to.
     * <p>
     * Default value: All available.
     */
    private InetAddress localAddress;

    /**
     * True if the listener should use implicit SSL.
     * <p>
     * Default value: false
     */
    private Boolean implicitSsl = false;

    /**
     * The number of seconds before an inactive client is disconnected. If this value is set to 0, the idle time is
     * disabled (a client can idle forever without getting disconnected by the server). If a lower maximum idle time is
     * configured on a user (e.g. using the PropertiesUserManager idletime configuration), it will override the
     * listener value. Thus, the listener value enforce the upper threshold, but lower values can be provided per user.
     * <p>
     * Default value: 300
     */
    private Duration idleTimeout = Duration.ofSeconds(300);

    /**
     * Required for listeners that should provide FTPS support.
     */
    private Ssl ssl = new Ssl();

    /**
     * This element provides configuration for the data connection.
     */
    private DataConnection dataConnection = new DataConnection();

    /**
     * This element provides a list of black listed IP addresses and networks in CIDR notation.
     */
    private Blacklist blacklist = new Blacklist();

    /**
     * Required for listeners that should provide FTPS support.
     */
    @Data
    public class Ssl {

        /**
         * The SSL protocol to use. Supported values are "SSL" and "TLS"
         * <p>
         * Default value: TLS
         */
        private String protocol = "TLS";

        /**
         * Should client authentication be performed? Supported values are "NEED", "WANT" and "NONE".
         * <p>
         * Default value: NONE
         */
        private ClientAuth clientAuthentication = ClientAuth.NONE;

        /**
         * A space-separated list of cipher suites to enable for this connection. The exact cipher suites that can be
         * used depends on the Java version used, here are the names for Sun's JSSE provider.
         * <p>
         * Default value: All cipher suites are enabled.
         */
        private List<String> enabledCiphersuites = new ArrayList<>(16);

        /**
         * This element is required if the ssl element is provided. It provides configuration for the key store used
         * for finding the private key and server certificate for the FTP server.
         */
        private Keystore keystore = new Keystore();

        /**
         * This element provides configuration for the trust store used for locating trusted certificates.
         */
        private Truststore truststore = new Truststore();

        /**
         * This element is required if the ssl element is provided. It provides configuration for the key store used
         * for finding the private key and server certificate for the FTP server.
         */
        @Data
        public class Keystore {

            /**
             * Path to the key store file.
             */
            private File file = new File("conf/ftpserver.jks");

            /**
             * The password for the key store.
             */
            private String password = "123456";

            /**
             * Password for the key within the key store.
             * <p>
             * Default value: Key store password.
             */
            private String keyPassword = password;

            /**
             * Alias of the key to use within the key store.
             * <p>
             * Default value: Uses first key found.
             */
            private String keyAlias = file.getName();

            /**
             * Key store type.
             * <p>
             * Default value: JRE key store default type, normally JKS.
             */
            private String type = "JKS";

            /**
             * Key store algorithm.
             * <p>
             * Default value: SunX509
             */
            private String algorithm = "SunX509";

        }

        /**
         * This element provides configuration for the trust store used for locating trusted certificates.
         */
        @Data
        public class Truststore {

            /**
             * Path to the trust store file.
             */
            private File file = new File("conf/truststore.jks");

            /**
             * The password for the trust store.
             * <p>
             * Default value: Certificates can be read without password.
             */
            private String password = "123456";

            /**
             * Trust store type.
             * <p>
             * Default value: JRE key store default type, normally JKS.
             */
            private String type = "JKS";

            /**
             * Trust store algorithm.
             * <p>
             * Default value: SunX509
             */
            private String algorithm = "SunX509";

        }

    }

    /**
     * This element provides configuration for the data connection.
     */
    @Data
    public class DataConnection {

        /**
         * Number of seconds before an idle data connection is closed.
         * <p>
         * Default value: 300
         */
        private Duration idleTimeout = Duration.ofSeconds(300);

        /**
         * This element provides configuration for active data connections.
         */
        private Active active = new Active();

        /**
         * This element provides configuration for passive data connections.
         */
        private Passive passive = new Passive();

        /**
         * This element provides configuration for active data connections.
         */
        @Data
        public class Active {

            /**
             * False if active data connections should not be allowed.
             * <p>
             * Default value: true
             */
            private Boolean enabled = true;

            /**
             * The local address the server will use when creating a data connection.
             * <p>
             * Default value: Any available.
             */
            private InetAddress localAddress;

            /**
             * The local port the server will use when creating a data connection.
             * <p>
             * Default value: Any available.
             */
            private Integer localPort;

            /**
             * Should the server check that the IP address for the data connection is the same as for the control
             * socket?
             * <p>
             * Default value: false
             */
            private Boolean ipCheck = false;

        }

        /**
         * This element provides configuration for passive data connections.
         */
        @Data
        public class Passive {

            /**
             * The ports on which the server is allowed to accept passive data connections, see Configure passive ports
             * for details.
             * <p>
             * Default value: Any available port.
             */
            private String ports;

            /**
             * The address on which the server will listen to passive data connections.
             * <p>
             * Default value: The same address as the control socket for the session.
             */
            private InetAddress address;

            /**
             * The address the server will claim to be listening on in the PASV reply. Useful when the server is behind
             * a NAT firewall and the client sees a different address than the server is using.
             */
            private InetAddress externalAddress;

        }

    }

    /**
     * This element provides a list of black listed IP addresses and networks in CIDR notation.
     */
    @Data
    public class Blacklist {

        /**
         * IP addresses
         */
        private List<InetAddress> addresses = new ArrayList<>(16);

        /**
         * Networks addresses
         */
        private List<Subnet> networks = new ArrayList<>(16);

    }

}
