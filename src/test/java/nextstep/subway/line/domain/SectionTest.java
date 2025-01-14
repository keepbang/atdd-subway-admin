package nextstep.subway.line.domain;

import nextstep.subway.exception.DistanceOverException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static nextstep.subway.line.domain.LineTest.LINE_2호선;
import static nextstep.subway.station.domain.StationTest.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

public class SectionTest {

    private Section section;

    @BeforeEach
    void setUp() {
        section = new Section(강남역, 역삼역, LINE_2호선, 10);
    }

    @Test
    @DisplayName("구간에 상행역과 같음")
    void isEqualsUpStationTest_true() {
        assertThat(section.isEqualsUpStation(강남역)).isTrue();
    }

    @Test
    @DisplayName("구간에 상행역과 다름")
    void isEqualsUpStationTest_false() {
        assertThat(section.isEqualsUpStation(양재역)).isFalse();
    }

    @Test
    @DisplayName("구간에 하행역과 같음")
    void isEqualsDownStationTest_true() {
        assertThat(section.isEqualsDownStation(역삼역)).isTrue();
    }

    @Test
    @DisplayName("구간에 하행과 다름")
    void isEqualsDownStationTest_false() {
        assertThat(section.isEqualsDownStation(양재역)).isFalse();
    }

    @Test
    @DisplayName("입력받은 구간이 두 역 사이에 포함되어있다.")
    void isIncludeOneStationTest() {
        Section actual = new Section(강남역, 양재역, LINE_2호선, 10);
        assertThat(section.isIncludeSection(actual)).isTrue();
    }

    @Test
    @DisplayName("입력받은 구간이 종점을 포함하고 있다.")
    void isIncludeOneEndStationTest() {
        Section actual = new Section(양재역, 강남역, LINE_2호선, 10);
        assertThat(section.isIncludeSection(actual)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {
            1, 9
    })
    @DisplayName("하행역이 구간 사이에 있을 경우")
    void downStationBetweenSection(int distance) {
        Section actual = new Section(강남역, 양재역, LINE_2호선, distance);
        section.reArrangeAddSection(actual);
        assertAll(
                () -> assertThat(section.getUpStation()).isEqualTo(양재역),
                () -> assertThat(section.getDistance()).isEqualTo(10 - actual.getDistance())
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {
            1, 9
    })
    @DisplayName("상행역이 구간 사이에 있을 경우")
    void upStationBetweenSection(int distance) {
        Section actual = new Section(양재역, 역삼역, LINE_2호선, distance);
        section.reArrangeAddSection(actual);
        assertAll(
                () -> assertThat(section.getDownStation()).isEqualTo(양재역),
                () -> assertThat(section.getDistance()).isEqualTo(10 - actual.getDistance())
        );
    }

    @Test
    @DisplayName("기존 구간의 길이보다 크거나 같으면 등록 할 수 없다.")
    void distanceOver() {
        Section actual = new Section(강남역, 양재역, LINE_2호선, 10);
        assertThatThrownBy(() -> section.reArrangeAddSection(actual)).isInstanceOf(DistanceOverException.class);
    }

    @Test
    @DisplayName("삭제하는 역이 상행역인 경우(중간역 삭제)")
    void deleteUpStation() {
        Section deleteSection = new Section(역삼역, 양재역, LINE_2호선, 10);
        section.reArrangeDeleteSection(deleteSection, 역삼역);

        assertAll(
                () -> assertThat(section.getDownStation()).isEqualTo(양재역),
                () -> assertThat(section.getDistance()).isEqualTo(20)
        );
    }

    @Test
    @DisplayName("삭제하는 역이 하행역인 경우(중간역 삭제)")
    void deleteDownStation() {
        Section deleteSection = new Section(사당역, 강남역, LINE_2호선, 10);
        section.reArrangeDeleteSection(deleteSection, 강남역);

        assertAll(
                () -> assertThat(section.getUpStation()).isEqualTo(사당역),
                () -> assertThat(section.getDistance()).isEqualTo(20)
        );
    }
}
