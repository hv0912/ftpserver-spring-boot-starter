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

package com.vimhe.ftpserver.spring.boot.autoconfigure.ext;

import com.vimhe.ftpserver.spring.boot.autoconfigure.FtpServerConfigurationProperties;
import org.apache.ftpserver.ftplet.*;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission;
import org.apache.ftpserver.usermanager.impl.TransferRatePermission;
import org.apache.ftpserver.usermanager.impl.WritePermission;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Custom Ftp User Manager.
 *
 * @author Vimhe
 */
public class FtpUserManager implements UserManager {

    private List<User> users;

    public FtpUserManager(FtpServerConfigurationProperties configurationProperties) {
        users = configurationProperties.getUser().stream()
            .map(userProperties -> {
                BaseUser user = new BaseUser();
                user.setName(userProperties.getUsername());
                user.setPassword(userProperties.getUserPassword());
                user.setHomeDirectory(userProperties.getHomeDirectory().getName());
                user.setEnabled(userProperties.getEnableFlag());
                user.setMaxIdleTime((int) userProperties.getIdleTime().getSeconds());

                Authority writePermission = new WritePermission(userProperties.getHomeDirectory().getName());
                Authority concurrentLoginPermission = new ConcurrentLoginPermission(
                    userProperties.getMaxLoginNumber(),
                    userProperties.getMaxLoginPerIp()
                );
                Authority transferRatePermission = new TransferRatePermission(
                    (int) userProperties.getDownloadRate().toBytes(),
                    (int) userProperties.getUploadRate().toBytes()
                );
                List<Authority> authorities = new ArrayList<>(3);
                authorities.add(writePermission);
                authorities.add(concurrentLoginPermission);
                authorities.add(transferRatePermission);
                user.setAuthorities(authorities);

                return user;
            })
            .collect(Collectors.toList());
    }

    @Override
    public User getUserByName(String username) throws FtpException {


        for (User user : users) {
            if (Objects.equals(user.getName(), username)) {
                return user;
            }
        }
        return null;
    }

    @Override
    public String[] getAllUserNames() throws FtpException {
        return users.stream().map(User::getName).toArray(String[]::new);
    }

    @Override
    public void delete(String username) throws FtpException {
        throw new FtpException("Unsupported operations, please manually delete in application.properties");
    }

    @Override
    public void save(User user) throws FtpException {
        throw new FtpException("Unsupported operations, please manually add in application.properties");
    }

    @Override
    public boolean doesExist(String username) throws FtpException {
        return users.stream().map(User::getName).anyMatch(s -> Objects.equals(s, username));
    }

    @Override
    public User authenticate(Authentication authentication) throws AuthenticationFailedException {
        return null;
    }

    @Override
    public String getAdminName() throws FtpException {
        return "admin";
    }

    @Override
    public boolean isAdmin(String username) throws FtpException {
        return Objects.equals("admin", username);
    }

}