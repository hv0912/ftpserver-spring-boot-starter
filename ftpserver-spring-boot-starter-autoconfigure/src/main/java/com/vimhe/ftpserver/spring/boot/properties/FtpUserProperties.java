package com.vimhe.ftpserver.spring.boot.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import java.io.File;
import java.time.Duration;

/**
 * @author V
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
