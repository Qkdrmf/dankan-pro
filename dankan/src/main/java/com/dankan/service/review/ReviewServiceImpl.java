package com.dankan.service.review;

import com.dankan.domain.*;
import com.dankan.dto.response.review.*;
import com.dankan.dto.request.review.ReviewRequestDto;
import com.dankan.exception.image.ImageNotFoundException;
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
import java.util.*;

@Slf4j
public class ReviewServiceImpl implements ReviewService {
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final RoomRepository roomRepository;
    private final DateLogRepository dateLogRepository;
    private final ImageRepository imageRepository;


    public ReviewServiceImpl(UserRepository userRepository
            ,ReviewRepository reviewRepository
            ,RoomRepository roomRepository
            ,DateLogRepository dateLogRepository
            ,ImageRepository imageRepository) {
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.roomRepository = roomRepository;
        this.dateLogRepository = dateLogRepository;
        this.imageRepository = imageRepository;
    }

    @Override
    @Transactional
    public ReviewResponseDto addReview(ReviewRequestDto reviewRequestDto) {
        Long userId = JwtUtil.getMemberId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserIdNotFoundException(userId.toString()));

        DateLog dateLog = DateLog.of(userId);
        dateLogRepository.save(dateLog);

        RoomReview roomReview = RoomReview.of(reviewRequestDto,user, dateLog.getId());
        reviewRepository.save(roomReview);

        return ReviewResponseDto.of(user,roomReview);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        Long userId = JwtUtil.getMemberId();

        RoomReview roomReview = reviewRepository.findByUserIdAndReviewId(userId,reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        roomReview.setDeletedAt(LocalDate.now());

        reviewRepository.save(roomReview);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDto> findRecentReview(Integer pages) {
        List<ReviewResponseDto> responseDtoList = new ArrayList<>();
        Sort sort = Sort.by(Sort.Direction.DESC,"createdAt");
        Pageable pageable =  PageRequest.of(pages,10,sort);
        Slice<RoomReview> roomReviewList = reviewRepository.findActiveReview(pageable);

        for (RoomReview roomReview : roomReviewList) {
            String imgUrls = "";
            User user = userRepository.findById(roomReview.getUserId())
                    .orElseThrow(() -> new UserIdNotFoundException(roomReview.getUserId().toString()));

            if (roomReview.getImageId()!=null) {
                Image image = imageRepository.findById(roomReview.getImageId())
                        .orElseThrow(() -> new ImageNotFoundException(roomReview.getImageId()));
                imgUrls = image.getImageUrl();
            }

            ReviewResponseDto responseDto = ReviewResponseDto.of(user,roomReview,imgUrls);
            responseDtoList.add(responseDto);
        }

        return responseDtoList;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewSearchResponse> findReviewByAddress(String address) {
        List<RoomReview> roomReviewList = reviewRepository.findByAddressSearch(address);

        HashMap<String,List<RoomReview>> reviewHashMap = getRoomViewListHash(roomReviewList);

        return getReviewSearchResponseList(reviewHashMap);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewSearchResponse> findReviewByStar() {
        List<RoomReview> roomReviewList = reviewRepository.findAll();

        HashMap<String,List<RoomReview>> reviewHashMap = getRoomViewListHash(roomReviewList);

        return getReviewSearchResponseList(reviewHashMap);
    }

    // 같은 주소끼리 리뷰들을 묶어준다.
    private HashMap<String,List<RoomReview>> getRoomViewListHash(List<RoomReview> roomReviewList) {
        HashMap<String,List<RoomReview>> reviewHashMap = new HashMap<>();

        for (RoomReview roomReview : roomReviewList) {
            String roomAddress = roomReview.getAddress();
            if (reviewHashMap.containsKey(roomAddress)) {
                reviewHashMap.get(roomAddress).add(roomReview);
            } else {
                reviewHashMap.put(roomAddress, new ArrayList<>(Arrays.asList(roomReview)));
            }
        }

        return reviewHashMap;
    }

    //같은 주소끼리 묶인 리뷰 평균 총별점 + 별점별 정렬
    private List<ReviewSearchResponse> getReviewSearchResponseList(HashMap<String,List<RoomReview>> reviewHashMap) {
        List<ReviewSearchResponse> responseDtoList = new ArrayList<>();

        for (Map.Entry<String, List<RoomReview>> hashMap : reviewHashMap.entrySet()) {
            String reviewAddress = hashMap.getValue().get(0).getAddress();
            String imgUrl = findRoomMainImageByAddress(reviewAddress);

            ReviewSearchResponse reviewSearchResponse = ReviewSearchResponse.of(hashMap.getValue(),imgUrl);
            responseDtoList.add(reviewSearchResponse);
        }

        responseDtoList.sort( //별점 순 조회
                Comparator.comparing(ReviewSearchResponse::getAvgTotalRate).reversed()
        );

        return responseDtoList;
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewDetailResponseDto findReviewDetail(String address) {
        List<RoomReview> reviewList = reviewRepository.findByAddress(address);
        String imgUrl = findRoomMainImageByAddress(address);

        return ReviewDetailResponseDto.of(reviewList,imgUrl);
    }

    private String findRoomMainImageByAddress(String address) {
        String imgUrl = null;

        if (roomRepository.findByAddress(address,1L).isPresent()) {
            Room room = roomRepository.findByAddress(address,1L)
                    .orElseThrow(() -> new RoomNotFoundException(address));

            Image image = imageRepository.findMainImage(room.getRoomId(),0L)
                    .orElseThrow(() -> new ImageNotFoundException(room.getRoomId()));

            imgUrl = image.getImageUrl();
        }

        return imgUrl;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OtherReviewResponseDto> findOtherReview(String address, Integer pages) {
        List<OtherReviewResponseDto> responseDtoList = new ArrayList<>();
        Sort sort = Sort.by(Sort.Direction.DESC,"createdAt");
        Pageable pageable = PageRequest.of(pages,10,sort);
        List<RoomReview> roomReviewList = reviewRepository.findByAddress(address,pageable);

        for (RoomReview roomReview : roomReviewList) {
            Long userId = roomReview.getUserId();
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserIdNotFoundException(userId.toString()));

            List<Image> imageList = imageRepository.findByIdAndImageType(roomReview.getReviewId(),3L);
            OtherReviewResponseDto otherReviewResponseDto = OtherReviewResponseDto.of(user,roomReview,imageList);
            responseDtoList.add(otherReviewResponseDto);
        }

        return responseDtoList;
    }
}
