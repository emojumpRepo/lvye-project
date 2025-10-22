package cn.iocoder.yudao.module.system.controller.admin.user.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户带角色信息响应 VO
 *
 * @author MinGoo
 */
@Schema(description = "管理后台 - 用户带角色信息 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserWithRoleInfoRespVO {

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "用户昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋道")
    private String nickname;

    @Schema(description = "用户角色信息列表")
    private List<RoleInfo> roleInfo;

    /**
     * 角色信息
     */
    @Schema(description = "角色信息")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleInfo {

        @Schema(description = "角色编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Long roleId;

        @Schema(description = "角色标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "admin")
        private String roleCode;
    }
}
