
package org.example.mall_tiny01.dto;

import lombok.Data;

@Data
public class UpdatePasswordDTO {
    private String username;
    private String oldPassword;
    private String newPassword;
}
