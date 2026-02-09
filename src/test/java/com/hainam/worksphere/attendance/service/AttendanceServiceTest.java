package com.hainam.worksphere.attendance.service;

import com.hainam.worksphere.BaseUnitTest;
import com.hainam.worksphere.TestFixtures;
import com.hainam.worksphere.attendance.domain.Attendance;
import com.hainam.worksphere.attendance.domain.AttendanceStatus;
import com.hainam.worksphere.attendance.dto.request.CheckInRequest;
import com.hainam.worksphere.attendance.dto.request.CheckOutRequest;
import com.hainam.worksphere.attendance.dto.response.AttendanceResponse;
import com.hainam.worksphere.attendance.mapper.AttendanceMapper;
import com.hainam.worksphere.attendance.repository.AttendanceRepository;
import com.hainam.worksphere.employee.domain.Employee;
import com.hainam.worksphere.shared.exception.AttendanceNotFoundException;
import com.hainam.worksphere.shared.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("AttendanceService Tests")
class AttendanceServiceTest extends BaseUnitTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private AttendanceMapper attendanceMapper;

    @InjectMocks
    private AttendanceService attendanceService;

    private Employee testEmployee;
    private Attendance testAttendance;
    private AttendanceResponse testAttendanceResponse;
    private UUID employeeId;

    @BeforeEach
    void setUp() {
        testEmployee = TestFixtures.createTestEmployee();
        employeeId = testEmployee.getId();
        testAttendance = TestFixtures.createTestAttendance();
        testAttendance.setEmployee(testEmployee);

        testAttendanceResponse = AttendanceResponse.builder()
                .id(UUID.randomUUID())
                .employeeId(employeeId)
                .employeeName(testEmployee.getFullName())
                .workDate(LocalDate.now())
                .checkInTime(LocalTime.of(8, 0))
                .status(AttendanceStatus.PRESENT)
                .workingHours(8.0)
                .build();
    }

    @Test
    @DisplayName("Should check in successfully")
    void shouldCheckInSuccessfully() {
        // Given
        CheckInRequest request = CheckInRequest.builder()
                .checkInLocation("Office")
                .note("On time")
                .build();
        String ipAddress = TEST_IP_ADDRESS;

        when(attendanceRepository.existsActiveByEmployeeIdAndWorkDate(eq(employeeId), any(LocalDate.class)))
                .thenReturn(false);
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);
        when(attendanceMapper.toAttendanceResponse(testAttendance)).thenReturn(testAttendanceResponse);

        // When
        AttendanceResponse result = attendanceService.checkIn(employeeId, request, ipAddress);

        // Then
        assertAll(
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result.getEmployeeId()).isEqualTo(employeeId),
            () -> assertThat(result.getStatus()).isEqualTo(AttendanceStatus.PRESENT),
            () -> verify(attendanceRepository).existsActiveByEmployeeIdAndWorkDate(eq(employeeId), any(LocalDate.class)),
            () -> verify(attendanceRepository).save(any(Attendance.class)),
            () -> verify(attendanceMapper).toAttendanceResponse(testAttendance)
        );
    }

    @Test
    @DisplayName("Should throw ValidationException when already checked in")
    void shouldThrowWhenAlreadyCheckedIn() {
        // Given
        CheckInRequest request = CheckInRequest.builder()
                .checkInLocation("Office")
                .build();
        String ipAddress = TEST_IP_ADDRESS;

        when(attendanceRepository.existsActiveByEmployeeIdAndWorkDate(eq(employeeId), any(LocalDate.class)))
                .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> attendanceService.checkIn(employeeId, request, ipAddress))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("already exists for today");

        verify(attendanceRepository).existsActiveByEmployeeIdAndWorkDate(eq(employeeId), any(LocalDate.class));
        verify(attendanceRepository, never()).save(any(Attendance.class));
        verifyNoInteractions(attendanceMapper);
    }

    @Test
    @DisplayName("Should check out successfully")
    void shouldCheckOutSuccessfully() {
        // Given
        CheckOutRequest request = CheckOutRequest.builder()
                .checkOutLocation("Office")
                .note("Done for the day")
                .build();
        String ipAddress = TEST_IP_ADDRESS;

        Attendance checkedInAttendance = Attendance.builder()
                .id(UUID.randomUUID())
                .employee(testEmployee)
                .workDate(LocalDate.now())
                .checkInTime(LocalTime.of(8, 0))
                .status(AttendanceStatus.PRESENT)
                .isDeleted(false)
                .build();

        when(attendanceRepository.findActiveByEmployeeIdAndWorkDate(eq(employeeId), any(LocalDate.class)))
                .thenReturn(Optional.of(checkedInAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(checkedInAttendance);
        when(attendanceMapper.toAttendanceResponse(checkedInAttendance)).thenReturn(testAttendanceResponse);

        // When
        AttendanceResponse result = attendanceService.checkOut(employeeId, request, ipAddress);

        // Then
        assertAll(
            () -> assertThat(result).isNotNull(),
            () -> assertThat(checkedInAttendance.getCheckOutTime()).isNotNull(),
            () -> assertThat(checkedInAttendance.getCheckOutIp()).isEqualTo(ipAddress),
            () -> assertThat(checkedInAttendance.getCheckOutLocation()).isEqualTo("Office"),
            () -> assertThat(checkedInAttendance.getWorkingHours()).isNotNull(),
            () -> verify(attendanceRepository).findActiveByEmployeeIdAndWorkDate(eq(employeeId), any(LocalDate.class)),
            () -> verify(attendanceRepository).save(any(Attendance.class)),
            () -> verify(attendanceMapper).toAttendanceResponse(checkedInAttendance)
        );
    }

    @Test
    @DisplayName("Should throw AttendanceNotFoundException when no check-in to check out")
    void shouldThrowWhenNoCheckInToCheckOut() {
        // Given
        CheckOutRequest request = CheckOutRequest.builder()
                .checkOutLocation("Office")
                .build();
        String ipAddress = TEST_IP_ADDRESS;

        when(attendanceRepository.findActiveByEmployeeIdAndWorkDate(eq(employeeId), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> attendanceService.checkOut(employeeId, request, ipAddress))
                .isInstanceOf(AttendanceNotFoundException.class);

        verify(attendanceRepository).findActiveByEmployeeIdAndWorkDate(eq(employeeId), any(LocalDate.class));
        verify(attendanceRepository, never()).save(any(Attendance.class));
        verifyNoInteractions(attendanceMapper);
    }

    @Test
    @DisplayName("Should get today attendance successfully")
    void shouldGetTodayAttendanceSuccessfully() {
        // Given
        when(attendanceRepository.findActiveByEmployeeIdAndWorkDate(eq(employeeId), any(LocalDate.class)))
                .thenReturn(Optional.of(testAttendance));
        when(attendanceMapper.toAttendanceResponse(testAttendance)).thenReturn(testAttendanceResponse);

        // When
        Optional<AttendanceResponse> result = attendanceService.getTodayAttendance(employeeId);

        // Then
        assertAll(
            () -> assertThat(result).isPresent(),
            () -> assertThat(result.get().getEmployeeId()).isEqualTo(employeeId),
            () -> verify(attendanceRepository).findActiveByEmployeeIdAndWorkDate(eq(employeeId), any(LocalDate.class)),
            () -> verify(attendanceMapper).toAttendanceResponse(testAttendance)
        );
    }

    @Test
    @DisplayName("Should get attendance history successfully")
    void shouldGetAttendanceHistorySuccessfully() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        Attendance attendance1 = TestFixtures.createTestAttendance();
        attendance1.setEmployee(testEmployee);
        Attendance attendance2 = TestFixtures.createTestAttendance();
        attendance2.setEmployee(testEmployee);

        List<Attendance> attendances = Arrays.asList(attendance1, attendance2);

        when(attendanceRepository.findActiveByEmployeeIdAndWorkDateBetween(employeeId, startDate, endDate))
                .thenReturn(attendances);
        when(attendanceMapper.toAttendanceResponse(any(Attendance.class))).thenReturn(testAttendanceResponse);

        // When
        List<AttendanceResponse> result = attendanceService.getAttendanceHistory(employeeId, startDate, endDate);

        // Then
        assertAll(
            () -> assertThat(result).hasSize(2),
            () -> verify(attendanceRepository).findActiveByEmployeeIdAndWorkDateBetween(employeeId, startDate, endDate),
            () -> verify(attendanceMapper, times(2)).toAttendanceResponse(any(Attendance.class))
        );
    }
}
