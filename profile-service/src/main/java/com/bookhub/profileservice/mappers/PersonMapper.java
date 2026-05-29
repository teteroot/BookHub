package com.bookhub.profileservice.mappers;

import com.bookhub.profileservice.dtos.requests.PersonCreateRequestDto;
import com.bookhub.profileservice.dtos.requests.PersonUpdateRequestDto;
import com.bookhub.profileservice.dtos.responses.PersonResponseDto;
import com.bookhub.profileservice.models.Person;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PersonMapper {
    Person toPerson(PersonCreateRequestDto dto);
    Person toPerson(PersonUpdateRequestDto dto);
    PersonResponseDto toDto(Person person);
}
