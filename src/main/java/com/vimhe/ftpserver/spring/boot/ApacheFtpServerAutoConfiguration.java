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

import com.vimhe.ftpserver.spring.boot.bean.NioListener;
import com.vimhe.ftpserver.spring.boot.bean.Server;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.apache.ftpserver.*;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.Listener;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.ssl.SslConfigurationFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Vimhe
 */
@Log
@RequiredArgsConstructor
@Configuration
@ConditionalOnClass(FtpServer.class)
@EnableConfigurationProperties(ApacheFtpServerConfigurationProperties.class)
public class ApacheFtpServerAutoConfiguration {

    private final ApacheFtpServerConfigurationProperties apacheFtpServerConfigurationProperties;

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "ftpserver", value = "use-xml-config", havingValue = "false", matchIfMissing = true)
    public FtpServer javaBasedConfigFtpServer() {
        FtpServerFactory serverFactory = new FtpServerFactory();

        serverFactory.setConnectionConfig(this.createConnectionConfig());

        List<NioListener> listeners = apacheFtpServerConfigurationProperties.getServer().getListeners();
        if (CollectionUtils.isEmpty(listeners)) {
            return serverFactory.createServer();
        }

        for (NioListener listener : listeners) {
            serverFactory.addListener(listener.getName(), this.createListener(listener));
        }

        serverFactory.setUserManager(this.createUserManager());

        return serverFactory.createServer();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "ftpserver", value = "use-xml-config", havingValue = "true")
    public FtpServer xmlBasedConfigFtpServer() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("conf/config.xml");
        FtpServer ftpServer;

        if (context.containsBean("server")) {
            ftpServer = (FtpServer) context.getBean("server");
        } else {
            String[] beanNames = context.getBeanNamesForType(FtpServer.class);
            if (beanNames.length == 1) {
                ftpServer = (FtpServer) context.getBean(beanNames[0]);
            } else if (beanNames.length > 1) {
                ftpServer = (FtpServer) context.getBean(beanNames[0]);
            } else {
                throw new IllegalArgumentException("XML configuration does not contain a server configuration");
            }
        }

        return ftpServer;
    }

    private ConnectionConfig createConnectionConfig() {
        ConnectionConfigFactory connConfFactory = new ConnectionConfigFactory();

        final Server server = apacheFtpServerConfigurationProperties.getServer();
        if (server == null) {
            return connConfFactory.createConnectionConfig();
        }

        connConfFactory.setMaxThreads(server.getMaxThreads());
        connConfFactory.setMaxLogins(server.getMaxLogins());
        connConfFactory.setMaxAnonymousLogins(server.getMaxAnonLogins());
        connConfFactory.setAnonymousLoginEnabled(server.getAnonEnabled());
        connConfFactory.setMaxLoginFailures(server.getMaxLoginfailures());
        connConfFactory.setLoginFailureDelay((Long.valueOf(server.getLoginFailureDelay().getSeconds() * 1000)).intValue());
        return connConfFactory.createConnectionConfig();
    }

    private Map<String, Listener> createListeners() {
        Map<String, Listener> listenerMap = new HashMap<>();

        List<NioListener> listeners = apacheFtpServerConfigurationProperties.getServer().getListeners();
        for (NioListener listener : listeners) {
            listenerMap.put(listener.getName(), this.createListener(listener));
        }
        return listenerMap;
    }

    private UserManager createUserManager() {
        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        userManagerFactory.setFile(new File("conf/users.properties"));

        return userManagerFactory.createUserManager();
    }

    private Listener createListener(NioListener nioListener) {
        final NioListener.Ssl ssl = nioListener.getSsl();

        ListenerFactory listenerFactory = new ListenerFactory();
        listenerFactory.setPort(nioListener.getPort());
        if (nioListener.getLocalAddress() != null) {
            listenerFactory.setServerAddress(nioListener.getLocalAddress().getHostAddress());
        }
        listenerFactory.setImplicitSsl(nioListener.getImplicitSsl());
        listenerFactory.setIdleTimeout(Long.valueOf(nioListener.getIdleTimeout().getSeconds()).intValue());

        if (ssl != null) {
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
        }

        listenerFactory.setDataConnectionConfiguration(this.createDataConnectionConfiguration(nioListener));

        return listenerFactory.createListener();
    }

    private DataConnectionConfiguration createDataConnectionConfiguration(NioListener nioListener) {
        final NioListener.DataConnection dataConnection = nioListener.getDataConnection();
        final NioListener.DataConnection.Active active = dataConnection.getActive();
        final NioListener.DataConnection.Passive passive = dataConnection.getPassive();

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
        dataConnConfFactory.setIdleTime(Long.valueOf(dataConnection.getIdleTimeout().getSeconds()).intValue());

        return dataConnConfFactory.createDataConnectionConfiguration();
    }

}
