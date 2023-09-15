package com.dankan.service.review;


import com.dankan.dto.response.review.*;
import com.dankan.dto.request.review.ReviewRequestDto;

import java.util.List;

public interface ReviewService {
    ReviewResponseDto addReview(ReviewRequestDto reviewRequestDto);

    List<OtherReviewResponseDto> findOtherReview(String address, Integer pages);
    ReviewDetailResponseDto findReviewDetail(String address);

    List<ReviewResponseDto> findRecentReview(Integer pages);
    List<ReviewSearchResponse> findReviewByStar();
    List<ReviewSearchResponse> findReviewByAddress(String address);

    void deleteReview(Long reviewId);
}
