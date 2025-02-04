package com.gdsd.TutorService.service.impl;

import com.gdsd.TutorService.exception.ReviewNotAllowedException;
import com.gdsd.TutorService.exception.StudentBannedException;
import com.gdsd.TutorService.exception.StudentLockedException;
import com.gdsd.TutorService.model.Review;
import com.gdsd.TutorService.model.Student;
import com.gdsd.TutorService.model.Tutor;
import com.gdsd.TutorService.repository.ReviewRepository;
import com.gdsd.TutorService.repository.StudentRepository;
import com.gdsd.TutorService.repository.TutorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewServiceImpl {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private StudentRepository studentRepository;


    @Autowired
    private TutorRepository tutorRepository;

    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    public Optional<Review> getReviewById(Integer reviewId) {
        return reviewRepository.findById(reviewId);
    }

    public Optional<List<Review>> getReviewsByTutorId(Integer tutorId) {
        return reviewRepository.findByTutorId(tutorId); // Directly return the repository result
    }

    public Optional<List<Review>> getReviewsByStudentId(Integer studentId) {
        return reviewRepository.findByStudentId(studentId); // Directly return the repository result
    }

    private void incrementTutorReviewCount(Integer tutorId) {
        Optional<Tutor> tutorOptional = tutorRepository.findById(tutorId);
        if (tutorOptional.isPresent()) {
            Tutor tutor = tutorOptional.get();
            tutor.setNumberOfRatings(tutor.getNumberOfRatings() + 1);
            tutorRepository.save(tutor);
        }
    }

    private void decrementTutorReviewCount(Integer tutorId) {
        Optional<Tutor> tutorOptional = tutorRepository.findById(tutorId);
        if (tutorOptional.isPresent()) {
            Tutor tutor = tutorOptional.get();
            int currentReviewCount = tutor.getNumberOfRatings();
            if (currentReviewCount > 0) {
                tutor.setNumberOfRatings(currentReviewCount - 1);
                tutorRepository.save(tutor);
            }
        }
    }

    public void createReview(Review review) {
        // Check if the student is banned or locked
        Optional<Student> studentOptional = studentRepository.findById(review.getStudentId());
        if (studentOptional.isPresent()) {
            Student student = studentOptional.get();
            if (student.getBanned()) {
                throw new StudentBannedException(review.getStudentId());
            }
            if (student.isLocked()) {
                throw new StudentLockedException(review.getStudentId());
            }
        } else {
            throw new ReviewNotAllowedException("Student not found. Cannot post a review.");
        }
        Review savedReview = reviewRepository.save(review);
        incrementTutorReviewCount(review.getTutorId());
        updateTutorAverageRating(review.getTutorId());
    }

    public Review updateReview(Integer reviewId, Review reviewDetails) {
        Optional<Review> reviewOptional = reviewRepository.findById(reviewId);
        if (reviewOptional.isPresent()) {
            Review review = reviewOptional.get();
            // Check if the student is allowed to update the review
            Optional<Student> studentOptional = studentRepository.findById(review.getStudentId());
            if (studentOptional.isPresent()) {
                Student student = studentOptional.get();
                if (student.getBanned()) {
                    throw new StudentBannedException(review.getStudentId());
                }
                if (student.isLocked()) {
                    throw new StudentLockedException(review.getStudentId());
                }
            } else {
                throw new ReviewNotAllowedException("Student not found. Cannot update the review.");
            }
            review.setRating(reviewDetails.getRating());  // Use instance method
            review.setText(reviewDetails.getText());
            review.setTutorId(reviewDetails.getTutorId());
            review.setStudentId(reviewDetails.getStudentId());
            Review updatedReview = reviewRepository.save(review);
            updateTutorAverageRating(review.getTutorId());
            return updatedReview;
        } else {
            throw new RuntimeException("Review not found with id " + reviewId);
        }
    }

    public void deleteReview(Integer reviewId) {
        Optional<Review> reviewOptional = reviewRepository.findById(reviewId);
        Review review = reviewOptional.get();
        if (reviewOptional.isPresent()) {
            // Check if the student is allowed to delete the review
            Optional<Student> studentOptional = studentRepository.findById(review.getStudentId());
            if (studentOptional.isPresent()) {
                Student student = studentOptional.get();
                if (student.getBanned()) {
                    throw new StudentBannedException(review.getStudentId());
                }
                if (student.isLocked()) {
                    throw new StudentLockedException(review.getStudentId());
                }
            } else {
                throw new ReviewNotAllowedException("Student not found. Cannot delete the review.");
            }
            Integer tutorId = reviewOptional.get().getTutorId();
            reviewRepository.deleteById(reviewId);
            decrementTutorReviewCount(tutorId);
            updateTutorAverageRating(tutorId);
        }
        else {
            throw new RuntimeException("Review not found with id " + reviewId);
        }
    }

    private void updateTutorAverageRating(Integer tutorId) {
        Optional<List<Review>> reviewsOptional = reviewRepository.findByTutorId(tutorId);
        if (reviewsOptional.isPresent() && !reviewsOptional.get().isEmpty()) {  // Ensure the optional is present and list is not empty
            List<Review> reviews = reviewsOptional.get();
            double averageRating = reviews.stream()
                    .mapToDouble(Review::getRating)
                    .average()
                    .orElse(0.0);
            Optional<Tutor> tutorOptional = tutorRepository.findById(tutorId);
            if (tutorOptional.isPresent()) {
                Tutor tutor = tutorOptional.get();
                tutor.setAverageRating(averageRating);
                tutorRepository.save(tutor);
            }
        }
    }
}
