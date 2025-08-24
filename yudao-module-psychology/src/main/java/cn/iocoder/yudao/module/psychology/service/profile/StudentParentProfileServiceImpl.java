package cn.iocoder.yudao.module.psychology.service.profile;

import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.ParentContactVO;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.StudentParentProfileReqVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.profile.ParentContactDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.profile.ParentContactMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-12
 * @Description:学生监护人档案服务层
 * @Version: 1.0
 */
@Service
@Validated
@Slf4j
public class StudentParentProfileServiceImpl implements StudentParentProfileService {

    @Resource
    private ParentContactMapper parentContactMapper;

    @Override
    public void createStudentParentContact(StudentParentProfileReqVO createReqVO) {
        Long studentProfileId = createReqVO.getStudentProfileId();
        List<ParentContactDO> list = new ArrayList<>();
        for (ParentContactVO parentContactVO : createReqVO.getParentList()) {
            ParentContactDO parentContactDO = new ParentContactDO();
            parentContactDO.setStudentProfileId(studentProfileId);
            parentContactDO.setName(parentContactVO.getName());
            parentContactDO.setMobile(parentContactVO.getMobile());
            parentContactDO.setRelation(parentContactVO.getRelation());
            parentContactDO.setRemark(parentContactVO.getRemark());
            list.add(parentContactDO);
        }
        parentContactMapper.insertBatch(list);
    }

    @Override
    public void updateStudentParentContact(StudentParentProfileReqVO createReqVO) {
        Long studentProfileId = createReqVO.getStudentProfileId();
        List<ParentContactVO> list = createReqVO.getParentList();
        for (ParentContactVO parentContactVO : list) {
            ParentContactDO parentContactDO = new ParentContactDO();
            parentContactDO.setId(parentContactVO.getId());
            parentContactDO.setStudentProfileId(studentProfileId);
            parentContactDO.setName(parentContactVO.getName());
            parentContactDO.setMobile(parentContactVO.getMobile());
            parentContactDO.setRelation(parentContactVO.getRelation());
            parentContactDO.setRemark(parentContactVO.getRemark());
            if (parentContactVO.getId() == null) {
                parentContactMapper.insert(parentContactDO);
            } else {
                parentContactMapper.updateById(parentContactDO);
            }
        }
    }

    @Override
    public void deleteStudentParentContact(Long contactId) {
        parentContactMapper.deleteById(contactId);
    }

    @Override
    public void deleteStudentParentContactByStudentProfileId(Long studentProfileId) {
        parentContactMapper.delete(new LambdaQueryWrapperX<ParentContactDO>().eq(ParentContactDO::getStudentProfileId, studentProfileId));
    }

    @Override
    public List<ParentContactDO> selectStudentParentContactByStudentProfileId(Long studentProfileId) {
        List<ParentContactDO> result = parentContactMapper.selectList(new LambdaQueryWrapperX<ParentContactDO>().eq(ParentContactDO::getStudentProfileId, studentProfileId));
        return result;
    }

}
