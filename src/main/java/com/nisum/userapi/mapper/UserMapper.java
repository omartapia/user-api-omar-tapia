package com.nisum.userapi.mapper;

import com.nisum.userapi.dto.PhoneRequest;
import com.nisum.userapi.dto.UserPatchRequest;
import com.nisum.userapi.dto.UserRequest;
import com.nisum.userapi.dto.UserResponse;
import com.nisum.userapi.domain.Phone;
import com.nisum.userapi.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {


 @Mapping(target = "id", ignore = true)
 @Mapping(target = "userId", ignore = true)
 Phone toPhoneEntity(PhoneRequest request);

 List<Phone> toListPhoneEntity(List<PhoneRequest> request);


 @Mapping(target = "id", ignore = true)
 @Mapping(target = "created", ignore = true)
 @Mapping(target = "modified", ignore = true)
 @Mapping(target = "lastLogin", ignore = true)
 @Mapping(target = "token", ignore = true)
 @Mapping(target = "active", ignore = true)
 User toEntity(UserRequest request);

 @Mapping(target = "id", ignore = true)
 @Mapping(target = "created", ignore = true)
 @Mapping(target = "modified", ignore = true)
 @Mapping(target = "lastLogin", ignore = true)
 @Mapping(target = "token", ignore = true)
 @Mapping(target = "active", ignore = true)
 User toEntity(UserPatchRequest request);

 @Mapping(target = "isactive", source = "active")
 UserResponse toResponse(User user);

 default OffsetDateTime map(LocalDateTime value) {
  return value == null ? null : value.atZone(ZoneId.systemDefault()).toOffsetDateTime();
 }
}
