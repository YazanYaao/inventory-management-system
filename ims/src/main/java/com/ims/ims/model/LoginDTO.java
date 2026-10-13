

package com.ims.ims.model;

import jakarta.validation.constraints.NotBlank;

public class LoginDTO {
    @NotBlank(message = "اسم المستخدم مطلوب")
    private String username;

    @NotBlank(message = "كلمة المرور مطلوبة")
    private String password;

    // Constructors
    public LoginDTO() {}

    

    public LoginDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }
    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}