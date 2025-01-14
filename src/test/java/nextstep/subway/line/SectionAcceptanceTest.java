package nextstep.subway.line;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.line.dto.SectionRequest;
import nextstep.subway.station.dto.StationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.List;

import static nextstep.subway.line.LineAcceptanceTest.지하철_노선_등록되어_있음;
import static nextstep.subway.station.StationAcceptanceTest.지하철_역_등록되어_있음;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class SectionAcceptanceTest extends AcceptanceTest {
    private StationResponse 강남역;
    private StationResponse 역삼역;
    private StationResponse 삼성역;
    private StationResponse 사당역;
    private String LineLocation;

    public static ExtractableResponse<Response> 지하철_구간_등록_요청(SectionRequest request, String location) {
        return RestAssured
                .given().log().all()
                .body(request)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post(location + "/sections")
                .then().log().all().extract();
    }

    public static SectionRequest 구간_요청_파라미터_생성(Long upStationId, Long downStationId, int distance) {
        return new SectionRequest(upStationId, downStationId, distance);
    }

    @BeforeEach
    void beforeEach() {
        강남역 = 지하철_역_등록되어_있음("강남역").as(StationResponse.class);
        역삼역 = 지하철_역_등록되어_있음("역삼역").as(StationResponse.class);
        삼성역 = 지하철_역_등록되어_있음("삼성역").as(StationResponse.class);
        사당역 = 지하철_역_등록되어_있음("사당역").as(StationResponse.class);

        LineRequest 수인분당선 = new LineRequest("수인분당선", "yellow", 강남역.getId(), 역삼역.getId(), 10);
        LineLocation = 지하철_노선_등록되어_있음(수인분당선).header("Location");
    }

    @DisplayName("지하철 노선에 구간을 추가한다.")
    @Test
    void addSection() {
        // given
        SectionRequest request = 구간_요청_파라미터_생성(강남역.getId(), 삼성역.getId(), 3);

        // when
        ExtractableResponse<Response> response = 지하철_구간_등록_요청(request, LineLocation);

        // then
        지하철_구간_등록됨(response);
        지하철_구간_목록_수정됨(response, Arrays.asList(강남역, 삼성역, 역삼역), 10);
    }

    @DisplayName("하행역이 두 역 사이에 등록 될 경우")
    @Test
    void addSectionBetweenDownStation() {
        // given
        SectionRequest request = 구간_요청_파라미터_생성(강남역.getId(), 삼성역.getId(), 9);

        // when
        ExtractableResponse<Response> response = 지하철_구간_등록_요청(request, LineLocation);

        // then
        지하철_구간_등록됨(response);
        지하철_구간_목록_수정됨(response, Arrays.asList(강남역, 삼성역, 역삼역), 10);
    }

    @DisplayName("상행역이 두 역 사이에 등록 될 경우")
    @Test
    void addSectionBetweenUpStation() {
        // given
        SectionRequest request = 구간_요청_파라미터_생성(삼성역.getId(), 역삼역.getId(), 1);

        // when
        ExtractableResponse<Response> response = 지하철_구간_등록_요청(request, LineLocation);

        // then
        지하철_구간_등록됨(response);
        지하철_구간_목록_수정됨(response, Arrays.asList(강남역, 삼성역, 역삼역), 10);
    }

    @DisplayName("상행 종점이 새로 등록될 경우")
    @Test
    void addSectionFirstUpStation() {
        // given
        SectionRequest request = 구간_요청_파라미터_생성(삼성역.getId(), 강남역.getId(), 10);

        // when
        ExtractableResponse<Response> response = 지하철_구간_등록_요청(request, LineLocation);

        // then
        지하철_구간_등록됨(response);
        지하철_구간_목록_수정됨(response, Arrays.asList(삼성역, 강남역, 역삼역), 20);
    }

    @DisplayName("하행 종점이 새로 등록될 경우")
    @Test
    void addSectionLastDownStation() {
        // given
        SectionRequest request = 구간_요청_파라미터_생성(역삼역.getId(), 삼성역.getId(), 10);

        // when
        ExtractableResponse<Response> response = 지하철_구간_등록_요청(request, LineLocation);

        // then
        지하철_구간_등록됨(response);
        지하철_구간_목록_수정됨(response, Arrays.asList(강남역, 역삼역, 삼성역), 20);
    }

    @DisplayName("역 사이에 새로운 역을 등록할 때 기존 역 사이 길이보다 작아야 한다.")
    @Test
    void addSectionBetweenStationLongDistance() {
        // given
        SectionRequest request = 구간_요청_파라미터_생성(강남역.getId(), 삼성역.getId(), 10);

        // when
        ExtractableResponse<Response> response = 지하철_구간_등록_요청(request, LineLocation);

        // then
        지하철_구간_등록_실패됨(response);
    }

    @DisplayName("상행역과 하행역 모두가 구간에 등록되어 있으면 추가할 수 없다.")
    @Test
    void alreadyAddStation() {
        // given
        SectionRequest request = 구간_요청_파라미터_생성(강남역.getId(), 역삼역.getId(), 5);

        // when
        ExtractableResponse<Response> response = 지하철_구간_등록_요청(request, LineLocation);

        // then
        지하철_구간_등록_실패됨(response);
    }

    @DisplayName("상행역과 하행역 둘 중 하나가 구간에 포함되어있지 않으면 추가 할 수 없다.")
    @Test
    void includeOneStation() {
        // given
        SectionRequest request = 구간_요청_파라미터_생성(사당역.getId(), 삼성역.getId(), 5);

        // when
        ExtractableResponse<Response> response = 지하철_구간_등록_요청(request, LineLocation);

        // then
        지하철_구간_등록_실패됨(response);
    }

    @DisplayName("존재하지 않는 노선에 구간을 추가할 수 없다.")
    @Test
    void notFoundLineInsert() {
        // given
        SectionRequest request = 구간_요청_파라미터_생성(강남역.getId(), 삼성역.getId(), 5);

        // when
        ExtractableResponse<Response> response = 존재하지_않는_노선에_구간_추가_요청(request);

        // then
        지하철_구간_등록_실패됨(response);
    }

    private ExtractableResponse<Response> 존재하지_않는_노선에_구간_추가_요청(SectionRequest request) {
        return 지하철_구간_등록_요청(request, "lines/3");
    }

    private void 지하철_구간_목록_수정됨(ExtractableResponse<Response> response, List<StationResponse> excepted, int totalDistance) {
        LineResponse actual = response.as(LineResponse.class);
        assertAll(
                () -> assertThat(actual.getStations()).hasSize(3),
                () -> assertThat(actual.getStations()).containsExactlyElementsOf(excepted),
                () -> assertThat(actual.getTotalDistance()).isEqualTo(totalDistance)
        );
    }

    private void 지하철_구간_등록됨(ExtractableResponse<Response> response) {
        요청_결과_검증(response, HttpStatus.OK);
    }

    private void 지하철_구간_등록_실패됨(ExtractableResponse<Response> response) {
        요청_결과_검증(response, HttpStatus.BAD_REQUEST);
    }
}
