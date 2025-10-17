package org.spacelab.housingutilitiessystemadmin.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.spacelab.housingutilitiessystemadmin.entity.User;
import org.spacelab.housingutilitiessystemadmin.entity.location.City;
import org.spacelab.housingutilitiessystemadmin.entity.location.Status;
import org.spacelab.housingutilitiessystemadmin.entity.location.Street;
import org.spacelab.housingutilitiessystemadmin.exception.OperationException;
import org.spacelab.housingutilitiessystemadmin.mappers.UserMapper;
import org.spacelab.housingutilitiessystemadmin.models.filters.user.UserRequestTable;
import org.spacelab.housingutilitiessystemadmin.models.user.UserRequest;
import org.spacelab.housingutilitiessystemadmin.models.user.UserResponse;
import org.spacelab.housingutilitiessystemadmin.models.user.UserResponseTable;
import org.spacelab.housingutilitiessystemadmin.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final FileService fileService;

    private final CityService cityService;
    private final StreetService streetService;

    public Optional<User> findById(ObjectId id) {
        return userRepository.findById(id);
    }
    public User save(User user) {
        return userRepository.save(user);
    }
    public List<User> findAll() {
        return userRepository.findAll();
    }
    public void deleteById(ObjectId id) {
        userRepository.deleteById(id);
    }
    public List<User> saveAll(List<User> users) {
        return userRepository.saveAll(users);
    }
    public void deleteAll() {
        userRepository.deleteAll();
    }


    public UserResponse getUserById(ObjectId id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new OperationException("получении пользователя",
                        "Пользователь с ID " + id + " не найден", HttpStatus.NOT_FOUND));
        log.info("Пользователь с ID {} успешно получен", id);
        return userMapper.mapUserToResponse(user);
    }

    public UserResponse createUser(UserRequest userRequest) {
        Optional<User> existingUser = userRepository.findByEmail(userRequest.getEmail());
        if (existingUser.isPresent()) {
            throw new OperationException("создании пользователя",
                    "Пользователь с email " + userRequest.getEmail() + " уже существует", HttpStatus.CONFLICT);
        }

        User user = userMapper.toEntity(userRequest);
        
        if (userRequest.getCityId() != null) {
            City city = cityService.findById(userRequest.getCityId())
                    .orElseThrow(() -> new OperationException("получении города",
                            "Город с ID " + userRequest.getCityId() + " не найден", HttpStatus.NOT_FOUND));
            user.setCity(city);
        }
        
        if (userRequest.getAddressId() != null) {
            Street street = streetService.findById(userRequest.getAddressId())
                    .orElseThrow(() -> new OperationException("получении улицы",
                            "Улица с ID " + userRequest.getAddressId() + " не найдена", HttpStatus.NOT_FOUND));
            user.setStreet(street);
        }
        
        if (userRequest.getStatus() != null) {
            try {
                user.setStatus(Status.valueOf(userRequest.getStatus()));
            } catch (IllegalArgumentException e) {
                throw new OperationException("создании пользователя",
                        "Неверный статус: " + userRequest.getStatus(), HttpStatus.BAD_REQUEST);
            }
        }
        
        if (userRequest.getPhotoFile() != null && !userRequest.getPhotoFile().isEmpty()) {
            try {
                String photoPath = fileService.uploadFile(userRequest.getPhotoFile());
                user.setPhoto(photoPath);
                log.info("Фото для пользователя успешно загружено: {}", photoPath);
            } catch (Exception e) {
                log.error("Ошибка при загрузке фото пользователя", e);
                throw new OperationException("создании пользователя",
                        "Ошибка при загрузке фото: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        
        User savedUser = userRepository.save(user);
        log.info("Пользователь с ID {} успешно создан", savedUser.getId());
        return userMapper.mapUserToResponse(savedUser);
    }

    public UserResponse updateUser(ObjectId id, UserRequest userRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new OperationException("обновлении пользователя",
                        "Пользователь с ID " + id + " не найден"));

        userMapper.partialUpdate(userRequest, user);
        
        if (userRequest.getCityId() != null) {
            City city = cityService.findById(userRequest.getCityId())
                    .orElseThrow(() -> new OperationException("получении города",
                            "Город с ID " + userRequest.getCityId() + " не найден", HttpStatus.NOT_FOUND));
            user.setCity(city);
        }
        
        if (userRequest.getAddressId() != null) {
            Street street = streetService.findById(userRequest.getAddressId())
                    .orElseThrow(() -> new OperationException("получении улицы",
                            "Улица с ID " + userRequest.getAddressId() + " не найдена", HttpStatus.NOT_FOUND));
            user.setStreet(street);
        }
        
        if (userRequest.getStatus() != null) {
            try {
                user.setStatus(Status.valueOf(userRequest.getStatus()));
            } catch (IllegalArgumentException e) {
                throw new OperationException("обновлении пользователя",
                        "Неверный статус: " + userRequest.getStatus(), HttpStatus.BAD_REQUEST);
            }
        }
        
        if (userRequest.getPhotoFile() != null && !userRequest.getPhotoFile().isEmpty()) {
            try {
                if (user.getPhoto() != null && !user.getPhoto().isEmpty()) {
                    fileService.deleteFile(user.getPhoto());
                }
                
                String photoPath = fileService.uploadFile(userRequest.getPhotoFile());
                user.setPhoto(photoPath);
                log.info("Фото для пользователя с ID {} успешно обновлено: {}", id, photoPath);
            } catch (Exception e) {
                log.error("Ошибка при обновлении фото пользователя с ID {}", id, e);
                throw new OperationException("обновлении пользователя",
                        "Ошибка при загрузке фото: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        
        User updatedUser = userRepository.save(user);
        log.info("Пользователь с ID {} успешно обновлен", updatedUser.getId());
        return userMapper.mapUserToResponse(updatedUser);
    }

    public boolean deleteUser(ObjectId id) {
        if (!userRepository.existsById(id)) {
            throw new OperationException("удалении пользователя",
                    "Пользователь с ID " + id + " не найден");
        }
        userRepository.deleteById(id);
        log.info("Пользователь с ID {} успешно удален", id);
        return true;
    }

    public Page<UserResponseTable> getUsersTable(UserRequestTable userRequestTable) {
        Page<User> users = getUser(userRequestTable);
        Page<UserResponseTable> userResponses = userMapper.toResponseTablePage(users);
        log.info("Получена страница пользователей: страница {}, размер {}", userRequestTable.getPage(), userRequestTable.getSize());
        return userResponses;
    }

    public Page<User> getUser(UserRequestTable userRequestTable) {
        return userRepository.findUsersWithFilters(userRequestTable);
    }


    @Async
    public CompletableFuture<UserResponse> findByIdAsync(ObjectId id) {
        return CompletableFuture.completedFuture(findById(id)).thenApply(userOpt -> {
            if (userOpt.isEmpty()) {
                throw new OperationException("получении пользователя",
                        "Пользователь с ID " + id + " не найден", HttpStatus.NOT_FOUND);
            }
            return userMapper.mapUserToResponse(userOpt.get());
        });
    }

    @Async
    public CompletableFuture<Optional<User>> findByEmailAsync(String email) {
        return CompletableFuture.completedFuture(userRepository.findByEmail(email));
    }

    @Async
    public CompletableFuture<UserResponse> createUserAsync(@Valid UserRequest userRequest) {
        return CompletableFuture.completedFuture(createUser(userRequest));
    }

    @Async
    public CompletableFuture<UserResponse> updateUserAsync(@Valid UserRequest userRequest) {
        return CompletableFuture.completedFuture(updateUser(userRequest.getId(), userRequest));
    }

    @Async
    public CompletableFuture<Boolean> deleteByIdAsync(ObjectId id) {
        Boolean deleted = deleteUser(id);
        return CompletableFuture.completedFuture(deleted);
    }

    @Async
    public CompletableFuture<Page<UserResponseTable>> getPageableUsersAsync(
            int page, int size, String lastName, String firstName, String middleName,
            String phone, String email, String status) {

        UserRequestTable userRequestTable = new UserRequestTable();
        userRequestTable.setPage(page);
        userRequestTable.setSize(size);

        String fullNameSearch = "";
        if (lastName != null && !lastName.trim().isEmpty()) {
            fullNameSearch += lastName.trim() + " ";
        }
        if (firstName != null && !firstName.trim().isEmpty()) {
            fullNameSearch += firstName.trim() + " ";
        }
        if (middleName != null && !middleName.trim().isEmpty()) {
            fullNameSearch += middleName.trim();
        }
        if (!fullNameSearch.trim().isEmpty()) {
            userRequestTable.setFullName(fullNameSearch.trim());
        }

        if (phone != null && !phone.trim().isEmpty()) {
            userRequestTable.setPhoneNumber(phone.trim());
        }



        if (status != null && !status.trim().isEmpty()) {
            try {
                userRequestTable.setStatus(Status.valueOf(status.trim()));
            } catch (IllegalArgumentException e) {
            }
        }

        return CompletableFuture.completedFuture(getUsersTable(userRequestTable));
    }
}
