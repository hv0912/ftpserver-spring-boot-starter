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

package com.vimhe.ftpserver.spring.boot.autoconfigure.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import java.io.File;
import java.time.Duration;

/**
 * User configuration Properties.
 *
 * @author Vimhe
 */
@Data
@Component
@ConfigurationProperties(prefix = "ftpserver.user")
public class FtpUserProperties {

    public static final String USER_HOME_PATH = System.getProperty("user.home");

    private String username = "anonymous";

    private String userPassword;

    private File homeDirectory = new File(File.separator + USER_HOME_PATH + "anonymous" + File.separator);

    private Boolean enableFlag = true;

    private Boolean writePermission = false;

    private Integer maxLoginNumber = 0;

    private Integer maxLoginPerIp = 0;

    private Duration idleTime = Duration.ofSeconds(300);

    private DataSize uploadRate = DataSize.ofMegabytes(1);

    private DataSize downloadRate = DataSize.ofMegabytes(1);

}
