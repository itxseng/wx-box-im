package com.bx.implatform.dto;

import com.bx.implatform.enums.RegisterMode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@Schema(description = "用户注册DTO")
public class RegisterDTO {

    @Schema(description = "注册方式, username:用户名注册, phone:手机注册,email: 邮箱注册")
    private String mode = RegisterMode.USER_NAME.getCode() ;

    @Length(max = 20, message = "昵称长度不能大于20")
    @NotEmpty(message = "用户名不可为空")
    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "手机号码")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Length(min = 5, max = 20, message = "密码长度必须在5-20个字符之间")
    @NotEmpty(message = "用户密码不可为空")
    @Schema(description = "用户密码")
    private String password;

    @Length(max = 20, message = "昵称长度不能大于20")
    @NotEmpty(message = "用户昵称不可为空")
    @Schema(description = "用户昵称")
    private String nickName;

    @Schema(description = "验证码")
    private String code;

}
