package com.bookhub.profileservice.mappers;

import com.bookhub.profileservice.dtos.requests.PersonCreateRequestDto;
import com.bookhub.profileservice.dtos.requests.PersonUpdateRequestDto;
import com.bookhub.profileservice.dtos.responses.PersonResponseDto;
import com.bookhub.profileservice.models.Person;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PersonMapper {
    Person toPerson(PersonCreateRequestDto dto);
    PersonResponseDto toDto(Person person);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updatePerson(@MappingTarget Person person, PersonUpdateRequestDto dto);
}
