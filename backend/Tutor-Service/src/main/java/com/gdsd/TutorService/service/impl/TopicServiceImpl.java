package com.gdsd.TutorService.service.impl;

import com.gdsd.TutorService.dto.Topic.TopicRequestDto;
import com.gdsd.TutorService.dto.Topic.TopicResponseDto;
import com.gdsd.TutorService.exception.ResourceNotFoundException;
import com.gdsd.TutorService.model.Topic;
import com.gdsd.TutorService.model.Tutor;
import com.gdsd.TutorService.repository.TopicRepository;
import com.gdsd.TutorService.repository.TutorRepository;
import com.gdsd.TutorService.service.interf.TopicService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TopicServiceImpl implements TopicService {

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private TutorRepository tutorRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public String addTopic(TopicRequestDto topicRequestDto) {
        Topic topic = modelMapper.map(topicRequestDto, Topic.class);
        Tutor tutor = tutorRepository.findById(topic.getTutorId()).
                orElseThrow(() -> new ResourceNotFoundException("Tutor", "tutorId", topic.getTutorId()));

        topic.setTopicId(null);
        topicRepository.save(topic);
        return "Successfully created Topic with id: " + topic.getTopicId();
    }

    @Override
    public TopicResponseDto getTopicById(Integer topicId) {
        Topic topic = topicRepository.findById(topicId).
                orElseThrow(() -> new ResourceNotFoundException("Topic", "topicId", topicId));
        TopicResponseDto topicResponseDto = modelMapper.map(topic, TopicResponseDto.class);

        return topicResponseDto;
    }

    @Override
    public String removeTopicById(Integer topicId) {
        Topic topic = topicRepository.findById(topicId).
                orElseThrow(() -> new RuntimeException("Tutor with given id doesn't exist"));

        topicRepository.delete(topic);
        return "Successfully deleted Topic with id: " + topic.getTopicId();
    }


}
