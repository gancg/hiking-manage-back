package com.gancg.hikingmanageback.model;

import lombok.Data;

@Data
public class RbacUserRow {
    private Long id;
    private String username;
    private String displayName;
    private String email;
    private String mobile;
    private String status;
    private String roleCodesCsv;
}
