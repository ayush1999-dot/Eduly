package com.gdsd.TutorService.service.impl;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.gdsd.TutorService.config.AzureBlob.AzureBlobStorageConfig;
import com.gdsd.TutorService.dto.Student.*;
import com.gdsd.TutorService.exception.GenericException;
import com.gdsd.TutorService.model.*;
import com.gdsd.TutorService.repository.*;
import com.gdsd.TutorService.service.interf.StudentService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class StudentServiceImpl implements StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentContentRepository studentContentRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private AzureBlobStorageConfig azureBlobStorageConfig;
    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private TutorRepository tutorRepository;
    @Autowired
    private  TutorContentRepository tutorContentRepository;


    @Override
    public Integer getStudentIdFromEmail(String studentEmail) {
        Student student = studentRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new GenericException("Student with studentEmail: "
                        + studentEmail + " not found", HttpStatus.NOT_FOUND));

        return student.getStudentId();
    }

    @Override
    public String getStudentNameFromId(Integer studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() ->
                        new GenericException("Student with id: " + studentId
                                + " not found", HttpStatus.NOT_FOUND));
        return student.getFirstName() + " " + student.getLastName();
    }

    public String getStudentProfileImageFromId(Integer studentId) {
        Optional<StudentContent> content = studentContentRepository.findByStudentIdAndContentType(studentId, "profile_image");

        if(content.isPresent()) {
            return content.get().getContentLink();
        } else {
            return "";
        }
    }

    @Override
    public StudentResponceDto getStudentById(Integer studentId) {
        Student student = studentRepository.findById(studentId).orElseThrow(() -> new RuntimeException("Student with id not found "+ studentId+"not found"));
        return modelMapper.map(student, StudentResponceDto.class);
    }

    // Fetch student profile by email
    public Student getStudentByEmail(String email) {
        return studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student not found with email: " + email));
    }

    @Override
    public StudentProfileRespDto updateStudentProfileImage(StudentProfileImageRequestDto requestDto, Integer studentId) {
            String contentType = "profile_image";
            String fileName = getExistingFileName(studentId, contentType);
            try {

                // if already exists then delete from blob storage
                if (fileName != null) {
                    BlobClient blobClient = azureBlobStorageConfig.blobContainerClient().getBlobClient(fileName);
                    blobClient.deleteIfExists();
                }

                // generate a new file name
                fileName = generateFileName(contentType, studentId);

                BlobClient blobClient = azureBlobStorageConfig.blobContainerClient().getBlobClient(fileName);
                BlobHttpHeaders headers = new BlobHttpHeaders().setContentType(requestDto.getFile().getContentType());
                blobClient.upload(requestDto.getFile().getInputStream(), requestDto.getFile().getSize(), true);
                blobClient.setHttpHeaders(headers);


                URI blobUri = URI.create(blobClient.getBlobUrl());
                String contentLink = blobUri.toString();

                StudentContent studentContent = studentContentRepository.findByStudentIdAndContentType(studentId,"profile_image").orElse(new StudentContent());

                studentContent.setStudentId(studentId);
                studentContent.setContentType("profile_image");
                studentContent.setStatus("PENDING_APPROVAL");
                studentContent.setContentLink(contentLink);
                studentContentRepository.save(studentContent);

                StudentProfileRespDto respDto = new StudentProfileRespDto();
                respDto.setStatus("PENDING_APPROVAL");
                respDto.setProfileImgLink(contentLink);
                return respDto;

            } catch (IOException e) {
                throw new GenericException("Failed to upload content: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }

        }

    private String generateFileName(String contentType, Integer studentId) {
        return "student_" + contentType + "_" + studentId  + UUID.randomUUID();
    }

    private String getExistingFileName(Integer studentId, String contentType) {
        Optional<StudentContent> existingContent = studentContentRepository.findByStudentIdAndContentType(studentId, contentType);

        if (existingContent.isPresent()) {
            String contentLink = existingContent.get().getContentLink();
            String[] contentLinkParts = contentLink.split("/");
            return contentLinkParts[contentLinkParts.length - 1];
        } else {
            return null;
        }
    }

    @Override
    public Boolean updateStudentProfile(Integer studentId, StudentProfileUpdateRequestDto studentProfileUpdateRequest) {
        Student student = studentRepository.findById(studentId).orElse(null);
        if (student != null) {
            student.setFirstName(studentProfileUpdateRequest.getFirstName());
            student.setLastName(studentProfileUpdateRequest.getLastName());
            student.setEmail(studentProfileUpdateRequest.getEmail());
            studentRepository.save(student);
            return true;
        }
        return false;
    }

    @Override
    public Session bookSession(Integer sessionId, Integer studentId) {
        Session session = sessionRepository.findById(sessionId).orElse(null);
        if (session != null) {
            session.setStudentId(studentId);
            session.setStatus("BOOKED");
            return sessionRepository.save(session);
        }
        return null;
    }

    @Override
    public SessionCancellationResponseDto cancelSession(Integer sessionId, Integer studentId) {
        Session session = sessionRepository.findById(sessionId).orElse(null);
        if (session != null && session.getStudentId().equals(studentId)) {
            session.setStudentId(null);
            session.setStatus("FREE");
            sessionRepository.save(session);

            Tutor tutor = tutorRepository.findById(session.getTutorId()).orElse(null);
            String tutorName = tutor != null ? tutor.getFirstName() + " " + tutor.getLastName() : "Unknown Tutor";
            String formattedDate = session.getDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            String formattedTime = session.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"));

            SessionCancellationResponseDto responseDto = new SessionCancellationResponseDto();
            responseDto.setMessage("Session with " + tutorName + " on " + formattedDate + " " + formattedTime + " cancelled successfully");
            return responseDto;
        }
        return null;
    }

    // Delete student profile by email
    @Transactional
    public void deleteStudentByEmail(String email) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student not found with email: " + email));
        // Manually delete related reviews
        // BAD WAY IT WILL CREATE GHOST REVIEWS
        // FUNCTION SHOULD SOFT-DELETE AND MARK PROFILE AS INACTIVE
        // TO DO THIS MODEL AND DB NEEDS TO CHANGE AND ADD [ boolean isDeleted ]
        // PENDING APPROVAL FROM @AASHAY
        reviewRepository.deleteByStudentId(student.getStudentId());
        studentRepository.delete(student);
    }

    public StudentProfileDto getStudentProfile(String email) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student not found with email: " + email));

        // Fetch the profile image content
        Optional<StudentContent> contentOpt = studentContentRepository.findByStudentIdAndContentType(student.getStudentId(), "profile_image");
        StudentContent content = contentOpt.orElse(new StudentContent());

        // Map to DTO
        StudentProfileDto dto = new StudentProfileDto();
        dto.setId(student.getStudentId());
        dto.setFirstName(student.getFirstName());
        dto.setLastName(student.getLastName());
        dto.setEmail(student.getEmail());

        // Use the status from StudentContent (if available, otherwise default to "PENDING_APPROVAL")
        String profileStatus = content.getStatus() != null ? content.getStatus() : "PENDING_APPROVAL";
        StudentProfileDto.ProfileImgLink profileImgLink = new StudentProfileDto.ProfileImgLink(
                content.getContentLink(),
                profileStatus
        );
        dto.setProfileImgLink(profileImgLink);

        return dto;
    }
    @Override
    public List<UpcomingAppointmentDto> getUpcomingAppointments(Integer studentId) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // Fetch sessions for the student that are booked and from today onwards
        List<Session> sessions = sessionRepository.findByStudentIdAndDateAfterOrDateEqualsAndStatus(studentId, today, "BOOKED");

        // Prepare list to hold upcoming appointments
        List<UpcomingAppointmentDto> upcomingAppointments = new ArrayList<>();

        // Iterate over sessions to populate appointment details
        for (Session session : sessions) {
            LocalDate sessionDate = session.getDate();
            LocalTime sessionTime = session.getStartTime();

            // Filter appointments from today onwards
            if (sessionDate.isAfter(today) || (sessionDate.equals(today) && sessionTime.isAfter(now))) {
                UpcomingAppointmentDto appointmentDto = new UpcomingAppointmentDto();
                appointmentDto.setDate(sessionDate.toString());
                appointmentDto.setSessionId(session.getSessionId());
                appointmentDto.setFrom(sessionTime.toString());
                appointmentDto.setTo(session.getEndTime().toString());
                appointmentDto.setStatus(session.getStatus());

                // Fetch tutor details
                Integer tutorId = session.getTutorId();
                Tutor tutor = tutorRepository.findById(tutorId).orElse(null);
                if (tutor != null) {
                    UpcomingAppointmentDto.TutorDetailDto tutorDetailDto = new UpcomingAppointmentDto.TutorDetailDto();
                    tutorDetailDto.setId(tutorId);
                    tutorDetailDto.setName(tutor.getFirstName() + " " + tutor.getLastName());

                    // Fetch profile image link from TutorContent if available
                    String profileImgLink = tutorContentRepository.findProfileImageLinkByTutorId(tutorId);
                    tutorDetailDto.setProfileImgLink(profileImgLink);

                    String bbblink = tutorRepository.findBbbLinkByTutorId(tutorId);
                    tutorDetailDto.setBbbLink(bbblink);

                    appointmentDto.setTutorDetail(tutorDetailDto);
                }

                // Add appointment to the list
                upcomingAppointments.add(appointmentDto);
            }
        }

        return upcomingAppointments;
    }
}
