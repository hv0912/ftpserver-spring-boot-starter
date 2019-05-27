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

package com.vimhe.ftpserver.spring.boot.autoconfigure.support;

import lombok.RequiredArgsConstructor;
import org.apache.ftpserver.FtpServer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * FtpServer InitializationBean.
 *
 * @author Vimhe
 */
@RequiredArgsConstructor
@Component
public class FtpServerInitializationBean implements InitializingBean, DisposableBean {

    private final FtpServer ftpServer;

    @Override
    public void destroy() {
        if (!ftpServer.isStopped()) {
            ftpServer.stop();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ftpServer.start();
    }

}
