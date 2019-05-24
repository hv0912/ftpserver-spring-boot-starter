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

package com.vimhe.ftpserver.spring.boot;

import com.vimhe.ftpserver.spring.boot.ext.FtpUserManager;
import com.vimhe.ftpserver.spring.boot.properties.FtpServerListenerProperties;
import com.vimhe.ftpserver.spring.boot.properties.FtpServerProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.apache.ftpserver.*;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.Listener;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.ssl.SslConfigurationFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Apache FtpServer AutoConfiguration
 *
 * @author Vimhe
 */
@Log
@RequiredArgsConstructor
@Configuration
@ConditionalOnClass(FtpServer.class)
@EnableConfigurationProperties(FtpServerProperties.class)
public class FtpServerAutoConfiguration {

    private final FtpServerConfigurationProperties configurationProperties;

    @Bean
    @ConditionalOnMissingBean
    public FtpServer ftpServer() {
        FtpServerFactory serverFactory = new FtpServerFactory();

        serverFactory.setConnectionConfig(this.createConnectionConfig());

        List<FtpServerListenerProperties> listeners = configurationProperties.getServer().getListeners();
        if (CollectionUtils.isEmpty(listeners)) {
            serverFactory.addListener("default", new ListenerFactory().createListener());
        } else {
            for (FtpServerListenerProperties listener : listeners) {
                serverFactory.addListener(listener.getName(), this.createListener(listener));
            }
        }

        serverFactory.setUserManager(this.ftpUserManager());

        return serverFactory.createServer();
    }

    private ConnectionConfig createConnectionConfig() {
        FtpServerProperties serverProperties = configurationProperties.getServer();
        ConnectionConfigFactory connConfFactory = new ConnectionConfigFactory();
        connConfFactory.setMaxThreads(serverProperties.getMaxThreads());
        connConfFactory.setMaxLogins(serverProperties.getMaxLogins());
        connConfFactory.setMaxAnonymousLogins(serverProperties.getMaxAnonLogins());
        connConfFactory.setAnonymousLoginEnabled(serverProperties.getAnonEnabled());
        connConfFactory.setMaxLoginFailures(serverProperties.getMaxLoginfailures());
        connConfFactory.setLoginFailureDelay((int) serverProperties.getLoginFailureDelay().toMillis());
        return connConfFactory.createConnectionConfig();
    }

    private Listener createListener(FtpServerListenerProperties ftpServerListenerProperties) {
        ListenerFactory listenerFactory = new ListenerFactory();
        listenerFactory.setPort(ftpServerListenerProperties.getPort());
        if (ftpServerListenerProperties.getLocalAddress() != null) {
            listenerFactory.setServerAddress(ftpServerListenerProperties.getLocalAddress().getHostAddress());
        }
        listenerFactory.setImplicitSsl(ftpServerListenerProperties.getImplicitSsl());
        listenerFactory.setIdleTimeout((int) ftpServerListenerProperties.getIdleTimeout().getSeconds());

        final FtpServerListenerProperties.Ssl ssl = ftpServerListenerProperties.getSsl();
        SslConfigurationFactory sslConfFactory = new SslConfigurationFactory();
        sslConfFactory.setSslProtocol(ssl.getProtocol());
        sslConfFactory.setClientAuthentication(ssl.getClientAuthentication().name());
        sslConfFactory.setEnabledCipherSuites(ssl.getEnabledCiphersuites().toArray(new String[0]));
        sslConfFactory.setKeystoreFile(ssl.getKeystore().getFile());
        sslConfFactory.setKeystorePassword(ssl.getKeystore().getPassword());
        sslConfFactory.setKeyPassword(ssl.getKeystore().getKeyPassword());
        sslConfFactory.setKeyAlias(ssl.getKeystore().getKeyAlias());
        sslConfFactory.setKeystoreType(ssl.getKeystore().getType());
        sslConfFactory.setKeystoreAlgorithm(ssl.getKeystore().getAlgorithm());
        sslConfFactory.setTruststoreFile(ssl.getTruststore().getFile());
        sslConfFactory.setTruststorePassword(ssl.getTruststore().getPassword());
        sslConfFactory.setTruststoreType(ssl.getTruststore().getType());
        sslConfFactory.setTruststoreAlgorithm(ssl.getTruststore().getAlgorithm());
        listenerFactory.setSslConfiguration(sslConfFactory.createSslConfiguration());

        listenerFactory.setDataConnectionConfiguration(this.createDataConnectionConfiguration(ftpServerListenerProperties));

        return listenerFactory.createListener();
    }

    private DataConnectionConfiguration createDataConnectionConfiguration(FtpServerListenerProperties ftpServerListenerProperties) {
        final FtpServerListenerProperties.DataConnection dataConnection = ftpServerListenerProperties.getDataConnection();
        final FtpServerListenerProperties.DataConnection.Active active = dataConnection.getActive();
        final FtpServerListenerProperties.DataConnection.Passive passive = dataConnection.getPassive();

        final DataConnectionConfigurationFactory dataConnConfFactory = new DataConnectionConfigurationFactory();

        dataConnConfFactory.setActiveEnabled(active.getEnabled());
        if (active.getLocalAddress() != null) {
            dataConnConfFactory.setActiveLocalAddress(active.getLocalAddress().getHostAddress());
        }
        if (active.getLocalPort() != null) {
            dataConnConfFactory.setActiveLocalPort(active.getLocalPort());
        }
        dataConnConfFactory.setActiveIpCheck(active.getIpCheck());
        if (passive.getPorts() != null) {
            dataConnConfFactory.setPassivePorts(passive.getPorts());
        }
        if (passive.getAddress() != null) {
            dataConnConfFactory.setPassiveAddress(passive.getAddress().getHostAddress());
        }
        if (passive.getExternalAddress() != null) {
            dataConnConfFactory.setPassiveExternalAddress(passive.getExternalAddress().getHostAddress());
        }
        dataConnConfFactory.setIdleTime((int) dataConnection.getIdleTimeout().getSeconds());

        return dataConnConfFactory.createDataConnectionConfiguration();
    }

    @Bean
    public UserManager ftpUserManager() {
        return new FtpUserManager(this.configurationProperties);
    }

}
