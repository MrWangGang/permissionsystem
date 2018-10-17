package com.lamb.permissionsystem.service.impl;

import com.lamb.permissionsystem.common.exception.ProcessException;
import com.lamb.permissionsystem.repository.dao.operation.UserTokenTMOperation;
import com.lamb.permissionsystem.repository.dao.repository.FoundationQueryRepository;
import com.lamb.permissionsystem.repository.dao.repository.SystemDORepository;
import com.lamb.permissionsystem.repository.dao.repository.SystemServiceDORepository;
import com.lamb.permissionsystem.repository.dao.repository.UserDORepository;
import com.lamb.permissionsystem.repository.entity.domain.ServiceDO;
import com.lamb.permissionsystem.repository.entity.domain.UserDO;
import com.lamb.permissionsystem.repository.entity.template.UserTokenTM;
import com.lamb.permissionsystem.service.IdentityVerificationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

import static com.lamb.permissionsystem.common.enums.FoundationPropertyEnum.*;
import static com.lamb.permissionsystem.common.enums.ProcessExceptionEnum.*;
import static com.lamb.permissionsystem.repository.enums.RedisTMKeyEnum.USER_TOKEN;


/**
 * @description: 身份验证
 * @author: Mr.WangGang
 * @create: 2018-10-17 下午 12:31
 **/
@Service
@Transactional
public class IdentityVerificationServiceImpl implements IdentityVerificationService {
    @Resource
    private UserDORepository userDORepository;

    @Resource
    private SystemDORepository systemDORepository;

    @Resource
    private SystemServiceDORepository systemServiceDORepository;


    @Resource
    private UserTokenTMOperation userTokenTMOperation;

    @Resource
    private FoundationQueryRepository foundationQueryRepository;


    @Override
    public void validate(String serviceCode,String accessToken) {
        if(StringUtils.isBlank(serviceCode)){
            throw new ProcessException(EI00000001);
        }

        ServiceDO serviceDO = foundationQueryRepository.findByServiceCode(serviceCode).orElseThrow(()->new ProcessException(EB00000003));

        Byte serviceStrategy = serviceDO.getServiceStrategy();

        if(T_SERVICE_ANY.getValue().equals(serviceStrategy)){
            //任何人都可以访问
            return;
        }else if(T_SERVICE_AUTC.getValue().equals(serviceStrategy)){
            //需要认证
            UserDO userDO = autc(accessToken);
            legitimateRequestUSS(userDO,serviceDO);
            return;
        }else if(T_SERVICE_AUTZ.getValue().equals(serviceStrategy)){
            //需要授权
            UserDO userDO = autc(accessToken);
            legitimateRequestUSS(userDO,serviceDO);
            autz(userDO,serviceDO);
            return;
        }else{
            throw new ProcessException(EB00000004);
        }
    }

    //认证
    private UserDO autc(String accessToken){
        if(StringUtils.isBlank(accessToken)){
            throw new ProcessException(EI00000000);
        }
        UserTokenTM userTokenTM = Optional.ofNullable(userTokenTMOperation.getObject(USER_TOKEN.getKey()+accessToken)).orElseThrow(()->new ProcessException(EB00000000));
        UserDO userDO = userDORepository.findById(userTokenTM.getUserId()).orElseThrow(()->new ProcessException(EB00000000));
        return userDO;
    }

    //授权
    private void autz(UserDO userDO,ServiceDO serviceDO){
        List<ServiceDO> services =  foundationQueryRepository.findServiceByUserId(userDO.getUserId()).orElseThrow(()->new ProcessException(EB00000005));
        if (services.stream().noneMatch(e -> serviceDO.getServiceId().compareTo(e.getServiceId()) == 0)){
            throw new ProcessException(EB00000005);
        }
    }
    //合法的用户系统服务之间关系
    private void legitimateRequestUSS(UserDO userDO,ServiceDO serviceDO){
        systemDORepository.findById(userDO.getSystemId()).orElseThrow(()->new ProcessException(EB00000001));
        systemServiceDORepository.findByServiceId(serviceDO.getServiceId()).orElseThrow(()->new ProcessException(EB00000002));
    }
}