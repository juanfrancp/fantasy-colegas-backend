package com.fantasycolegas.fantasy_colegas_backend.dto.request;

import com.fantasycolegas.fantasy_colegas_backend.model.enums.AppRole;
import lombok.Data;

@Data
public class UserRoleUpdateDto {
    private AppRole newRole;
}