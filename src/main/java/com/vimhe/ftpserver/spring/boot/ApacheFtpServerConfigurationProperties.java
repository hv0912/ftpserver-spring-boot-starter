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

import com.vimhe.ftpserver.spring.boot.bean.Server;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Ftp Server configuration properties.
 *
 * @author Vimhe
 */
@Data
@Component
@ConfigurationProperties(prefix = "ftpserver")
public class ApacheFtpServerConfigurationProperties {

    /**
     * Whether to enable xml-based configuration.
     * <p>
     * If true, use xml-based configuration, otherwise use the configuration in application.properties, There are all
     * default configuration in application.properties, no additional configuration can be used.
     */
    public Boolean useXmlConfig = false;

    /**
     * Server configuration.
     */
    private Server server = new Server();

}
