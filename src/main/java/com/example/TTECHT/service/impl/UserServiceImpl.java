package com.example.TTECHT.service.impl;

import com.example.TTECHT.constant.PredefinedRole;
import com.example.TTECHT.dto.repsonse.UserResponse;
import com.example.TTECHT.dto.request.UpdatePasswordRequest;
import com.example.TTECHT.dto.request.UserCreationRequest;
import com.example.TTECHT.dto.request.UserUpdateRequest;
import com.example.TTECHT.entity.user.Role;
import com.example.TTECHT.entity.user.User;
import com.example.TTECHT.exception.AppException;
import com.example.TTECHT.exception.ErrorCode;
import com.example.TTECHT.mapper.UserMapper;
import com.example.TTECHT.repository.user.RoleRepository;
import com.example.TTECHT.repository.user.UserRepository;
import com.example.TTECHT.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserServiceImpl implements UserService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    public UserResponse createUser(UserCreationRequest request) {

        userRepository.findByUsername(request.getUsername()).ifPresent(user -> {
            log.error("User with username {} already exists", request.getUsername());
            throw new AppException(ErrorCode.USERNAME_EXISTED);
        });

        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            log.error("User with email {} already exists", request.getEmail());
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        });


        userRepository.findByPhoneNumber(request.getPhoneNumber()).ifPresent(user -> {
            log.error("User with phone number {} already exists", request.getPhoneNumber());
            throw new AppException(ErrorCode.PHONE_NUMBER_EXISTED);
        });


        User user = userMapper.toUser(request);
        log.info("User with username {} has been created", user.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        HashSet<Role> roles = new HashSet<>();
        roleRepository.findByName(PredefinedRole.USER_ROLE).ifPresent(roles::add);

        user.setRoles(roles);
        System.out.println(user);

        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException exception) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        return userMapper.toUserResponse(user);
    }

    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        User user = userRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userMapper.toUserResponse(user);
    }

    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        try {
            log.info("Updating user with ID: {}", userId);
            log.info("Update request: {}", request.toString());


            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

            // 2. Update basic fields via mapper
            userMapper.updateUser(user, request);

            // 3. Handle password update (only if provided)
            if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
                user.setPassword(passwordEncoder.encode(request.getPassword()));
            }

            // 4. Handle roles update (only if provided)
            if (request.getRoles() != null && !request.getRoles().isEmpty()) {
                var roles = roleRepository.findByNameIn(request.getRoles());

                // Check if all requested roles were found
                if (roles.size() != request.getRoles().size()) {
                    log.warn("Some roles not found. Requested: {}, Found: {}",
                            request.getRoles(), roles.stream().map(Role::getName).toList());
                }

                user.setRoles(new HashSet<>(roles));
            }

            // 5. Save and return
            User savedUser = userRepository.save(user);
            return userMapper.toUserResponse(savedUser);

        } catch (NumberFormatException e) {
            log.error("Invalid user ID format: {}", userId);
            throw new AppException(ErrorCode.INVALID_USER_ID);
        } catch (Exception e) {
            log.error("Error updating user with ID: {}", userId, e);
            throw new AppException(ErrorCode.UPDATE_USER_FAILED);
        }
    }

    public void updatePassword(String userId, UpdatePasswordRequest request) {
        
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }

        if (request.getNewPassword() == null || request.getNewPassword().isBlank() || request.getConfirmNewPassword() == null || request.getConfirmNewPassword().isBlank()) {
            throw new AppException(ErrorCode.PASSWORD_CANNOT_BE_BLANK);
        }

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new AppException(ErrorCode.PASSWORDS_NOT_MATCH);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }


    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void inActivateUser(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        user.setActive(false);
        userRepository.save(user);
        log.info("User with ID {} has been deactivated", userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getUsers() {
        log.info("In method get Users");
        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUser(String id) {
        return userMapper.toUserResponse(
                userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }
}
