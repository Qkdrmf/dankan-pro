package com.dankan.service.review;

import com.dankan.domain.*;
import com.dankan.dto.request.image.ImageRequestDto;
import com.dankan.dto.request.review.ReviewDetailRequestDto;
import com.dankan.dto.response.image.ImageResponseDto;
import com.dankan.dto.response.review.ReviewDetailResponseDto;
import com.dankan.dto.response.review.ReviewImageResponseDto;
import com.dankan.dto.response.review.ReviewRateResponseDto;
import com.dankan.dto.response.review.ReviewResponseDto;
import com.dankan.dto.request.review.ReviewRequestDto;
import com.dankan.exception.image.ImageNotFoundException;
import com.dankan.exception.options.OptionNotFoundException;
import com.dankan.exception.review.ReviewNotFoundException;
import com.dankan.exception.room.RoomNotFoundException;
import com.dankan.exception.user.UserIdNotFoundException;
import com.dankan.repository.*;
import com.dankan.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ReviewServiceImpl implements ReviewService {
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final RoomRepository roomRepository;
    private final DateLogRepository dateLogRepository;
    private final OptionsRepository optionsRepository;
    private final ImageRepository imageRepository;


    public ReviewServiceImpl(UserRepository userRepository
            ,ReviewRepository reviewRepository
            ,RoomRepository roomRepository
            ,DateLogRepository dateLogRepository
            ,OptionsRepository optionsRepository
            ,ImageRepository imageRepository) {
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.roomRepository = roomRepository;
        this.dateLogRepository = dateLogRepository;
        this.optionsRepository = optionsRepository;
        this.imageRepository = imageRepository;
    }

    @Override
    @Transactional
    public ReviewResponseDto addReview(ReviewRequestDto reviewRequestDto) {
        Long userId = JwtUtil.getMemberId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserIdNotFoundException(userId.toString()));

        Room room = roomRepository.findFirstByRoomAddress_Address(reviewRequestDto.getAddress())
                .orElseThrow(() -> new RoomNotFoundException(reviewRequestDto.getAddress()));

        Options option = optionsRepository.findByRoomIdAndCodeKey(room.getRoomId(),"RoomType")
                .orElseThrow(() -> new OptionNotFoundException("RoomType"));

        List<Options> optionsList = Options.of(room.getRoomId(),reviewRequestDto);
        optionsRepository.saveAll(optionsList);

        DateLog dateLog = DateLog.builder()
                .userId(userId)
                .createdAt(LocalDate.now())
                .lastUserId(userId)
                .updatedAt(LocalDate.now())
                .build();
        dateLogRepository.save(dateLog);

        RoomReview roomReview = RoomReview.of(reviewRequestDto,user,room.getRoomId(), dateLog.getId());
        reviewRepository.save(roomReview);

        return ReviewResponseDto.of(user,roomReview,null,option);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        Long userId = JwtUtil.getMemberId();

        RoomReview roomReview = reviewRepository.findByUserIdAndReviewId(userId,reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));
        reviewRepository.delete(roomReview);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDto> findRecentReview(Integer pages) {
        List<ReviewResponseDto> responseDtoList = new ArrayList<>();
        Sort sort = Sort.by(Sort.Direction.DESC,"updatedAt");
        Pageable pageable =  PageRequest.of(pages,5,sort);
        Slice<RoomReview> roomReviewList = reviewRepository.findAll(pageable);

        for (RoomReview roomReview : roomReviewList) {
            Room room  = roomRepository.findById(roomReview.getRoomId())
                  .orElseThrow(() -> new RoomNotFoundException(roomReview.getRoomId().toString()));

            User user = userRepository.findById(room.getUserId())
                    .orElseThrow(() -> new UserIdNotFoundException(room.getUserId().toString()));

            Options option = optionsRepository.findByRoomIdAndCodeKey(room.getRoomId(),"RoomType")
                    .orElseThrow(() -> new OptionNotFoundException("RoomType"));

            Image image = imageRepository.findMainImage(room.getRoomId(),0L)
                    .orElseThrow(() -> new ImageNotFoundException(room.getRoomId()));

            ReviewResponseDto responseDto = ReviewResponseDto.of(user,roomReview,image.getImageUrl(),option);
            responseDtoList.add(responseDto);
        }

        return responseDtoList;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDto> findReviewByStar(Integer pages) {
        List<ReviewResponseDto> responseDtoList = new ArrayList<>();
        Sort sort = Sort.by(Sort.Direction.DESC,"totalRate");
        Pageable pageable = PageRequest.of(pages,5,sort);
        Slice<RoomReview> roomReviewList = reviewRepository.findAll(pageable);

        for (RoomReview roomReview : roomReviewList) {
            Room room = roomRepository.findById(roomReview.getRoomId())
                    .orElseThrow(() -> new RoomNotFoundException(roomReview.getRoomId()));

            Options option = optionsRepository.findByRoomIdAndCodeKey(room.getRoomId(),"RoomType")
                    .orElseThrow(() -> new OptionNotFoundException("RoomType"));

            Image image = imageRepository.findMainImage(room.getRoomId(),0L)
                    .orElseThrow(() -> new ImageNotFoundException(room.getRoomId()));

            ReviewResponseDto responseDto = ReviewResponseDto.of(roomReview,image.getImageUrl(),option);
            responseDtoList.add(responseDto);
        }

        return responseDtoList;
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewRateResponseDto findReviewRate(String address) {
        Room room = roomRepository.findFirstByRoomAddress_Address(address)
                .orElseThrow(() -> new RoomNotFoundException(address));
        List<String> codeKeys = new ArrayList<>(List.of("AccessRate,CleanRate,HostRate,FacilityRate,NoiseRate"));
        Options option = optionsRepository.findByRoomIdAndCodeKey(room.getRoomId(),"RoomType")
                .orElseThrow(() -> new OptionNotFoundException("RoomType"));
        List<Options> optionsList = optionsRepository.findRateOptions(room.getRoomId(),codeKeys);

        // 하나의 도로명 주소에는 여러 방이 있을 수 있습니다. 어떤 이미지를 대표로 가져올지 고려해봐야 합니다.
        Image image = imageRepository.findMainImage(room.getRoomId(),0L)
                .orElseThrow(() -> new ImageNotFoundException(room.getRoomId()));

        List<RoomReview> reviewList = reviewRepository.findByAddress(address);
        return ReviewRateResponseDto.of(room,reviewList, image.getImageUrl(), option,optionsList);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDetailResponseDto> findReviewDetail(ReviewDetailRequestDto reviewDetailRequestDto) {
        List<ReviewDetailResponseDto> responseDtoList = new ArrayList<>();
        Sort sort = Sort.by(Sort.Direction.DESC,"updatedAt");
        Pageable pageable = PageRequest.of(reviewDetailRequestDto.getPages(),5,sort);
        List<RoomReview> roomReviewList = reviewRepository.findByAddress(reviewDetailRequestDto.getAddress(),pageable);

        for (RoomReview roomReview : roomReviewList) {
            Long userId = roomReview.getUserId();
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserIdNotFoundException(userId.toString()));

            List<Image> imageList = imageRepository.findByIdAndImageType(roomReview.getReviewId(),3L);
            ReviewDetailResponseDto reviewDetailResponseDto = ReviewDetailResponseDto.of(user,roomReview,imageList);
            responseDtoList.add(reviewDetailResponseDto);
        }

        return responseDtoList;
    }
}