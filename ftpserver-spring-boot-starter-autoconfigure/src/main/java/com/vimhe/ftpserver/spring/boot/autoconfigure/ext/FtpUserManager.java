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
import org.apache.ftpserver.usermanager.AnonymousAuthentication;
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission;
import org.apache.ftpserver.usermanager.impl.TransferRatePermission;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.springframework.util.unit.DataSize;

import java.util.*;

/**
 * Custom Ftp User Manager.
 *
 * @author Vimhe
 */
public class FtpUserManager implements UserManager {

    private Set<User> userSet = new HashSet<>(2);

    public FtpUserManager(final Map<String, FtpServerConfigurationProperties.User> userSet) {
        userSet.forEach((key, value) -> this.userSet.add(createUser(key, value)));
    }

    private static BaseUser createUser(
        final String username,
        final FtpServerConfigurationProperties.User userProperties) {
        BaseUser user = new BaseUser();

        Optional.of(username).ifPresent(user::setName);
        Optional.ofNullable(userProperties.getUserPassword()).ifPresent(user::setPassword);
        Optional.ofNullable(userProperties.getHomeDirectory()).ifPresent(file ->
            user.setHomeDirectory(file.getAbsolutePath()));
        Optional.ofNullable(userProperties.getEnableFlag()).ifPresent(user::setEnabled);
        Optional.ofNullable(userProperties.getIdleTime()).ifPresent(duration ->
            user.setMaxIdleTime((int) duration.getSeconds()));

        List<Authority> authorities = new ArrayList<>(3);

        // If user has write permission, set the home directory by default
        Optional.ofNullable(userProperties.getWritePermission())
            .filter(aBoolean -> aBoolean)
            .ifPresent(aBoolean -> authorities.add(
                new WritePermission(userProperties.getHomeDirectory().getAbsolutePath()))
            );

        Optional<Integer> maxLoginNumber = Optional.ofNullable(userProperties.getMaxLoginNumber());
        Optional<Integer> maxLoginPerIp = Optional.ofNullable(userProperties.getMaxLoginPerIp());
        if (maxLoginNumber.isPresent() && maxLoginPerIp.isPresent()) {
            authorities.add(
                new ConcurrentLoginPermission(maxLoginNumber.get(), maxLoginPerIp.get())
            );
        }

        Optional<DataSize> downloadRate = Optional.ofNullable(userProperties.getDownloadRate());
        Optional<DataSize> uploadRate = Optional.ofNullable(userProperties.getUploadRate());
        if (downloadRate.isPresent() && uploadRate.isPresent()) {
            authorities.add(
                new TransferRatePermission(
                    (int) downloadRate.get().toBytes(),
                    (int) uploadRate.get().toBytes()
                )
            );
        }

        user.setAuthorities(authorities);

        return user;
    }

    @Override
    public User getUserByName(final String username) {
        return this.userSet.stream()
            .filter(user -> Objects.equals(user.getName(), username))
            .findFirst()
            .orElse(null);
    }

    @Override
    public String[] getAllUserNames() {
        return this.userSet.stream().map(User::getName).distinct().toArray(String[]::new);
    }

    @Override
    public void delete(final String username) throws FtpException {
        throw new FtpException("Unsupported operation, please manually delete in application.properties");
    }

    @Override
    public void save(final User user) throws FtpException {
        throw new FtpException("Unsupported operation, please manually add in application.properties");
    }

    @Override
    public boolean doesExist(final String username) {
        return this.userSet.stream().anyMatch(user -> Objects.equals(user.getName(), username));
    }

    @Override
    public User authenticate(final Authentication authentication) throws AuthenticationFailedException {
        if (authentication instanceof UsernamePasswordAuthentication) {
            UsernamePasswordAuthentication usernamePasswordAuthentication = (UsernamePasswordAuthentication) authentication;

            return Optional.ofNullable(getUserByName(usernamePasswordAuthentication.getUsername()))
                .filter(user -> Objects.equals(user.getPassword(), usernamePasswordAuthentication.getPassword()))
                .orElseThrow(() -> new AuthenticationFailedException("The ftp username or password is incorrect"));
        } else if (authentication instanceof AnonymousAuthentication) {
            // Anonymous has no password
            return getUserByName("anonymous");
        }
        throw new IllegalArgumentException("Authentication not supported by this user manager");
    }

    @Override
    public String getAdminName() {
        // The default admin user named "admin", and case sensitive
        return "admin";
    }

    @Override
    public boolean isAdmin(final String username) {
        return Objects.equals("admin", username);
    }

    public User[] getAllUser() {
        return this.userSet.toArray(new User[0]);
    }

}
