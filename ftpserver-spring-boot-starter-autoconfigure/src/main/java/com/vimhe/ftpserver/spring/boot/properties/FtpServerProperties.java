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
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Server configuration Properties.
 *
 * @author Vimhe
 */
@Data
@Component
@ConfigurationProperties(prefix = "ftpserver.server")
public class FtpServerProperties {

    /**
     * A unique identifier for this server.
     */
    private String id = "myFtpServer";

    /**
     * The maximum number of threads used in the thread pool for handling client connections
     * <p>
     * Default value: maxLogins, or get by Runtime.getRuntime().availableProcessors() if neither value is set.
     */
    private Integer maxThreads = Runtime.getRuntime().availableProcessors();

    /**
     * The maximum number of simultaneous users.
     * <p>
     * Default value: 16
     */
    private Integer maxLogins = 16;

    /**
     * The maximum number of simultaneous anonymous users.
     * <p>
     * Default value: 16
     */
    private Integer maxAnonLogins = 16;

    /**
     * Are anonymous logins enabled?.
     * <p>
     * Default value: true
     */
    private Boolean anonEnabled = true;

    /**
     * The number of failed login attempts before the connection is closed.
     * <p>
     * Default value: 3
     */
    private Integer maxLoginfailures = 3;

    /**
     * The number of milliseconds that the connection is delayed after a failed login attempt. Used to limit to
     * possibility of brute force guessing passwords.
     * <p>
     * Default value: 500
     */
    private Duration loginFailureDelay = Duration.ofMillis(500);

    /**
     * The listener shipped with FtpServer is called "nio-listener" since it is based Java NIO for performance and
     * scalability.
     */
    private List<FtpServerListenerProperties> listeners = new ArrayList<>(1);

}
