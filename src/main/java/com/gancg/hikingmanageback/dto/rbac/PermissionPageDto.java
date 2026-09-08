package com.gancg.hikingmanageback.dto.rbac;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class PermissionPageDto {
    List<PermissionDto> records;
    long total;
    int pageNum;
    int pageSize;
}
