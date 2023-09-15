package com.dankan.dto.response.review;

import com.dankan.domain.RoomReview;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDetailResponseDto {
    private String address;
    private Double avgTotalRate;
    private String imgUrl;
    private Long reviewCount;

    private Double avgCleanRate;
    private Double avgNoiseRate;
    private Double avgAccessRate;
    private Double avgHostRate;
    private Double avgFacilityRate;

    public static ReviewDetailResponseDto of(List<RoomReview> roomReviewList,String imgUrl) {
        Long reviewCount = (long) roomReviewList.size();

        Double avgTotalRate = 0.0;
        Double avgCleanRate = 0.0;
        Double avgNoiseRate = 0.0;
        Double avgAccessRate = 0.0;
        Double avgHostRate = 0.0;
        Double avgFacilityRate = 0.0;

        for (RoomReview roomReview : roomReviewList) {
            avgTotalRate += roomReview.getTotalRate();
            avgCleanRate += roomReview.getCleanRate();
            avgNoiseRate += roomReview.getNoiseRate();
            avgAccessRate += roomReview.getAccessRate();
            avgHostRate += roomReview.getHostRate();
            avgFacilityRate += roomReview.getFacilityRate();
        }

        if (reviewCount > 0) {
            avgTotalRate = avgTotalRate / (double) reviewCount;
            avgCleanRate = avgCleanRate / (double) reviewCount;
            avgNoiseRate = avgNoiseRate / (double) reviewCount;
            avgAccessRate = avgAccessRate / (double) reviewCount;
            avgHostRate = avgHostRate / (double) reviewCount;
            avgFacilityRate = avgFacilityRate / (double) reviewCount;
        }

        return ReviewDetailResponseDto.builder()
                .address(roomReviewList.get(0).getAddress())
                .avgTotalRate(Math.round(avgTotalRate*10)/10.0)
                .avgCleanRate(Math.round(avgCleanRate*10)/10.0)
                .avgNoiseRate(Math.round(avgNoiseRate*10)/10.0)
                .avgAccessRate(Math.round(avgAccessRate*10)/10.0)
                .avgHostRate(Math.round(avgHostRate*10)/10.0)
                .avgFacilityRate(Math.round(avgFacilityRate*10)/10.0)
                .reviewCount((long) roomReviewList.size())
                .imgUrl(imgUrl)
                .build();
    }
}
