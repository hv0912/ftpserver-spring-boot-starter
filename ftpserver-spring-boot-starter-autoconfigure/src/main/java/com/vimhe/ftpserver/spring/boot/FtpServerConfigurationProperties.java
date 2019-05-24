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

import com.vimhe.ftpserver.spring.boot.properties.FtpServerProperties;
import com.vimhe.ftpserver.spring.boot.properties.FtpUserProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * FtpServer Configuration Properties.
 *
 * @author Vimhe
 */
@Data
@Component
@ConfigurationProperties(prefix = "ftpserver")
public class FtpServerConfigurationProperties {

    /**
     * Server configuration.
     */
    private FtpServerProperties server = new FtpServerProperties();

    /**
     * User configuration.
     */
    private List<FtpUserProperties> user = new ArrayList<>(1);

}
