package com.xuenai.medical.controller;

import com.xuenai.medical.auth.RequireRole;
import com.xuenai.medical.common.BaseResponse;
import com.xuenai.medical.common.ResultUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequireRole("ADMIN")
public class AdminDashboardController {

    private final JdbcTemplate jdbcTemplate;

    public AdminDashboardController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/phase-one")
    public BaseResponse<Map<String, Long>> phaseOneOverview() {
        Map<String, Long> data = new LinkedHashMap<>();
        data.put("users", count("sys_user"));
        data.put("doctors", count("doctor_profile"));
        data.put("pharmacists", count("pharmacist_profile"));
        data.put("drugs", count("drug"));
        return ResultUtils.success(data);
    }

    private Long count(String tableName) {
        return jdbcTemplate.queryForObject("select count(1) from " + tableName + " where deleted = 0", Long.class);
    }
}

