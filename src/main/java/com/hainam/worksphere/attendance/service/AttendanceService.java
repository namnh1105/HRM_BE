package com.hainam.worksphere.attendance.service;

import com.hainam.worksphere.attendance.domain.Attendance;
import com.hainam.worksphere.attendance.domain.AttendanceStatus;
import com.hainam.worksphere.attendance.dto.request.CheckInRequest;
import com.hainam.worksphere.attendance.dto.request.CheckOutRequest;
import com.hainam.worksphere.attendance.dto.response.AttendanceResponse;
import com.hainam.worksphere.attendance.mapper.AttendanceMapper;
import com.hainam.worksphere.attendance.repository.AttendanceRepository;
import com.hainam.worksphere.employee.domain.Employee;
import com.hainam.worksphere.shared.audit.annotation.AuditAction;
import com.hainam.worksphere.shared.audit.domain.ActionType;
import com.hainam.worksphere.shared.audit.util.AuditContext;
import com.hainam.worksphere.shared.config.CacheConfig;
import com.hainam.worksphere.shared.exception.AttendanceNotFoundException;
import com.hainam.worksphere.shared.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceMapper attendanceMapper;

    @Transactional
    @CacheEvict(value = CacheConfig.ATTENDANCE_CACHE, allEntries = true)
    @AuditAction(type = ActionType.CREATE, entity = "ATTENDANCE")
    public AttendanceResponse checkIn(UUID employeeId, CheckInRequest request, String ipAddress) {
        LocalDate today = LocalDate.now();

        if (attendanceRepository.existsActiveByEmployeeIdAndWorkDate(employeeId, today)) {
            throw new ValidationException("Attendance record already exists for today");
        }

        Attendance attendance = Attendance.builder()
                .employee(Employee.builder().id(employeeId).build())
                .workDate(today)
                .checkInTime(LocalTime.now())
                .checkInIp(ipAddress)
                .checkInLocation(request.getCheckInLocation())
                .note(request.getNote())
                .status(AttendanceStatus.PRESENT)
                .build();

        Attendance saved = attendanceRepository.save(attendance);
        AuditContext.registerCreated(saved);
        return attendanceMapper.toAttendanceResponse(saved);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.ATTENDANCE_CACHE, allEntries = true)
    @AuditAction(type = ActionType.UPDATE, entity = "ATTENDANCE")
    public AttendanceResponse checkOut(UUID employeeId, CheckOutRequest request, String ipAddress) {
        LocalDate today = LocalDate.now();

        Attendance attendance = attendanceRepository.findActiveByEmployeeIdAndWorkDate(employeeId, today)
                .orElseThrow(() -> AttendanceNotFoundException.byId(employeeId.toString()));

        AuditContext.snapshot(attendance);

        LocalTime checkOutTime = LocalTime.now();
        attendance.setCheckOutTime(checkOutTime);
        attendance.setCheckOutIp(ipAddress);
        attendance.setCheckOutLocation(request.getCheckOutLocation());

        if (request.getNote() != null) {
            attendance.setNote(request.getNote());
        }

        // Calculate working hours
        if (attendance.getCheckInTime() != null) {
            Duration duration = Duration.between(attendance.getCheckInTime(), checkOutTime);
            double workingHours = duration.toMinutes() / 60.0;
            attendance.setWorkingHours(Math.round(workingHours * 100.0) / 100.0);
        }

        Attendance saved = attendanceRepository.save(attendance);
        AuditContext.registerUpdated(saved);
        return attendanceMapper.toAttendanceResponse(saved);
    }

    @Cacheable(value = CacheConfig.ATTENDANCE_CACHE, key = "'history:' + #employeeId + ':' + #startDate + ':' + #endDate")
    public List<AttendanceResponse> getAttendanceHistory(UUID employeeId, LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findActiveByEmployeeIdAndWorkDateBetween(employeeId, startDate, endDate)
                .stream()
                .map(attendanceMapper::toAttendanceResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = CacheConfig.ATTENDANCE_CACHE, key = "'today:' + #employeeId")
    public Optional<AttendanceResponse> getTodayAttendance(UUID employeeId) {
        return attendanceRepository.findActiveByEmployeeIdAndWorkDate(employeeId, LocalDate.now())
                .map(attendanceMapper::toAttendanceResponse);
    }
}
