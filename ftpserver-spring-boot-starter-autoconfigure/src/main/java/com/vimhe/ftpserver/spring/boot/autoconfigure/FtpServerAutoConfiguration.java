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

package com.vimhe.ftpserver.spring.boot.autoconfigure;

import com.vimhe.ftpserver.spring.boot.autoconfigure.ext.FtpUserManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.apache.ftpserver.*;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.Listener;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.ssl.SslConfiguration;
import org.apache.ftpserver.ssl.SslConfigurationFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * FtpServer AutoConfiguration.
 *
 * @author Vimhe
 */
@Log
@RequiredArgsConstructor
@Configuration
@ConditionalOnClass(FtpServer.class)
@EnableConfigurationProperties(FtpServerConfigurationProperties.class)
public class FtpServerAutoConfiguration {

    private final FtpServerConfigurationProperties configurationProperties;

    @Bean
    @ConditionalOnMissingBean
    public FtpServer ftpServer() {
        FtpServerFactory serverFactory = new FtpServerFactory();
        serverFactory.setConnectionConfig(this.createConnectionConfig(this.configurationProperties.getServer()));
        serverFactory.setListeners(this.createListenerConfig(this.configurationProperties.getServer().getListener()));
        serverFactory.setUserManager(this.createUserManager());
        return serverFactory.createServer();
    }

    @Bean
    public UserManager createUserManager() {
        final Map<String, FtpServerConfigurationProperties.User> properties = this.configurationProperties.getUser();

        // If no one user is config, set a anonymous user with default configuration
        if (CollectionUtils.isEmpty(properties)) {
            FtpServerConfigurationProperties.User user = new FtpServerConfigurationProperties.User();
            properties.put("anonymous", user);
        }

        return new FtpUserManager(properties);
    }

    private ConnectionConfig createConnectionConfig(final FtpServerConfigurationProperties.Server properties) {
        ConnectionConfigFactory factory = new ConnectionConfigFactory();

        Optional.ofNullable(properties.getMaxThreads()).ifPresent(factory::setMaxThreads);
        Optional.ofNullable(properties.getMaxLogins()).ifPresent(factory::setMaxLogins);
        Optional.ofNullable(properties.getMaxAnonLogins()).ifPresent(factory::setMaxAnonymousLogins);
        Optional.ofNullable(properties.getAnonEnabled()).ifPresent(factory::setAnonymousLoginEnabled);
        Optional.ofNullable(properties.getMaxLoginFailures()).ifPresent(factory::setMaxLoginFailures);
        Optional.ofNullable(properties.getLoginFailureDelay()).ifPresent(duration ->
            factory.setLoginFailureDelay((int) duration.toMillis()));

        return factory.createConnectionConfig();
    }

    private Map<String, Listener> createListenerConfig(
        final Map<String, FtpServerConfigurationProperties.Listener> properties
    ) {
        // The apache ftp server need at least one listener, default name is "default"
        if (CollectionUtils.isEmpty(properties)) {
            FtpServerConfigurationProperties.Listener property = new FtpServerConfigurationProperties.Listener();
            property.setPort(21);
            properties.put("default", property);
        }

        Map<String, Listener> listenerMap = new HashMap<>(1);
        properties.forEach((key, value) -> listenerMap.put(key, this.createListener(value)));
        return listenerMap;
    }

    private Listener createListener(final FtpServerConfigurationProperties.Listener properties) {
        final ListenerFactory listenerFactory = new ListenerFactory();

        Optional.ofNullable(properties.getPort()).ifPresent(listenerFactory::setPort);
        Optional.ofNullable(properties.getLocalAddress()).ifPresent(inetAddress ->
            listenerFactory.setServerAddress(inetAddress.getHostAddress()));
        Optional.ofNullable(properties.getImplicitSsl()).ifPresent(listenerFactory::setImplicitSsl);
        Optional.ofNullable(properties.getIdleTimeout()).ifPresent(duration ->
            listenerFactory.setIdleTimeout((int) duration.getSeconds()));

        Optional.ofNullable(properties.getSsl().getKeystore().getFile())
            .ifPresent(file -> listenerFactory.setSslConfiguration(this.createSslConfig(properties.getSsl())));

        listenerFactory.setDataConnectionConfiguration(this.createDataConnectionConfig(properties.getDataConnection()));

        return listenerFactory.createListener();
    }

    private SslConfiguration createSslConfig(final FtpServerConfigurationProperties.Ssl properties) {
        final SslConfigurationFactory factory = new SslConfigurationFactory();

        Optional.ofNullable(properties.getProtocol()).ifPresent(factory::setSslProtocol);
        Optional.ofNullable(properties.getClientAuthentication()).ifPresent(clientAuth ->
            factory.setClientAuthentication(clientAuth.name()));
        Optional.ofNullable(properties.getEnabledCipherSuites()).ifPresent(strings ->
            factory.setEnabledCipherSuites(strings.toArray(new String[0])));
        Optional.ofNullable(properties.getKeystore().getFile()).ifPresent(factory::setKeystoreFile);
        Optional.ofNullable(properties.getKeystore().getPassword()).ifPresent(factory::setKeystorePassword);
        Optional.ofNullable(properties.getKeystore().getKeyPassword()).ifPresent(factory::setKeyPassword);
        Optional.ofNullable(properties.getKeystore().getKeyAlias()).ifPresent(factory::setKeyAlias);
        Optional.ofNullable(properties.getKeystore().getType()).ifPresent(factory::setKeystoreType);
        Optional.ofNullable(properties.getKeystore().getAlgorithm()).ifPresent(factory::setKeystoreAlgorithm);
        Optional.ofNullable(properties.getTruststore().getFile()).ifPresent(factory::setTruststoreFile);
        Optional.ofNullable(properties.getTruststore().getPassword()).ifPresent(factory::setTruststorePassword);
        Optional.ofNullable(properties.getTruststore().getType()).ifPresent(factory::setTruststoreType);
        Optional.ofNullable(properties.getTruststore().getAlgorithm()).ifPresent(factory::setTruststoreAlgorithm);

        return factory.createSslConfiguration();
    }

    private DataConnectionConfiguration createDataConnectionConfig(
        final FtpServerConfigurationProperties.DataConnection properties
    ) {
        final DataConnectionConfigurationFactory factory = new DataConnectionConfigurationFactory();

        Optional.ofNullable(properties.getIdleTimeout()).ifPresent(duration ->
            factory.setIdleTime((int) duration.getSeconds()));
        Optional.ofNullable(properties.getActive().getEnabled()).ifPresent(factory::setActiveEnabled);
        Optional.ofNullable(properties.getActive().getLocalAddress()).ifPresent(inetAddress ->
            factory.setActiveLocalAddress(inetAddress.getHostAddress()));
        Optional.ofNullable(properties.getActive().getLocalPort()).ifPresent(factory::setActiveLocalPort);
        Optional.ofNullable(properties.getActive().getIpCheck()).ifPresent(factory::setActiveIpCheck);
        Optional.ofNullable(properties.getPassive().getPorts()).ifPresent(factory::setPassivePorts);
        Optional.ofNullable(properties.getPassive().getAddress()).ifPresent(inetAddress ->
            factory.setPassiveAddress(inetAddress.getHostAddress()));
        Optional.ofNullable(properties.getPassive().getExternalAddress()).ifPresent(inetAddress ->
            factory.setPassiveExternalAddress(inetAddress.getHostAddress()));

        return factory.createDataConnectionConfiguration();
    }

}
