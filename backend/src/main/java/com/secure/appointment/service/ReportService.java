package com.secure.appointment.service;

import com.secure.appointment.entity.Appointment;
import com.secure.appointment.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ReportService {

    private final AppointmentRepository appointmentRepository;

    public ReportService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public ByteArrayInputStream generateAppointmentReport(Long slotId) throws IOException {
        List<Appointment> appointments = appointmentRepository.findAllBySlotIdAndStatus(slotId, com.secure.appointment.entity.AppointmentStatus.BOOKED);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Appointments");

            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Customer Name", "Customer Email", "Date", "Status"};
            
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            int rowIdx = 1;
            for (Appointment appt : appointments) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(appt.getId());
                row.createCell(1).setCellValue(appt.getCustomer().getName());
                row.createCell(2).setCellValue(appt.getCustomer().getEmail());
                row.createCell(3).setCellValue(appt.getSlot().getStartTime().toString());
                row.createCell(4).setCellValue(appt.getStatus().toString());
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}
