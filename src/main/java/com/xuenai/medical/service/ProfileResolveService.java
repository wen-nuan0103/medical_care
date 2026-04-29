package com.xuenai.medical.service;

/**
 * 用户ID与档案ID之间的转换服务
 * <p>
 * 数据库外键设计中，doctor_card_plan.doctor_id 指向 doctor_profile.id，
 * doctor_card_order.patient_id 指向 patient_profile.id。
 * 而登录后 UserContext 中保存的是 sys_user.id。
 * 本服务负责二者之间的转换。
 */
public interface ProfileResolveService {

    /**
     * 根据 sys_user.id 获取 doctor_profile.id
     *
     * @param userId 用户ID
     * @return 医生档案ID
     * @throws com.xuenai.medical.exception.BusinessException 如果该用户不是医生
     */
    Long getDoctorProfileId(Long userId);

    /**
     * 根据 sys_user.id 获取 patient_profile.id
     *
     * @param userId 用户ID
     * @return 患者档案ID
     * @throws com.xuenai.medical.exception.BusinessException 如果该用户不是患者
     */
    Long getPatientProfileId(Long userId);

    /**
     * 根据 doctor_profile.id 获取 sys_user.id
     */
    Long getUserIdByDoctorProfileId(Long doctorProfileId);

    /**
     * 根据 patient_profile.id 获取 sys_user.id
     */
    Long getUserIdByPatientProfileId(Long patientProfileId);
}
