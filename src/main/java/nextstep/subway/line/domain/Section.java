package nextstep.subway.line.domain;

import nextstep.subway.common.BaseEntity;
import nextstep.subway.exception.DistanceOverException;
import nextstep.subway.station.domain.Station;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "section")
public class Section extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "up_station_id")
    private Station upStation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "down_station_id")
    private Station downStation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "line_id")
    private Line line;

    @Column(name = "distance")
    private int distance;

    public Section() {
    }

    public Section(Station upStation, Station downStation, Line line, int distance) {
        this.upStation = upStation;
        this.downStation = downStation;
        this.line = line;
        this.distance = distance;
    }

    public Line getLine() {
        return line;
    }

    public Station getUpStation() {
        return upStation;
    }

    public Station getDownStation() {
        return downStation;
    }

    public int getDistance() {
        return distance;
    }

    public boolean isIncludeSection(final Section nonPersistSection) {
        return isEqualsUpStation(nonPersistSection.upStation)
                || isEqualsDownStation(nonPersistSection.downStation);
    }

    public boolean hasStation(final Station station) {
        return isEqualsUpStation(station)
                || isEqualsDownStation(station);
    }

    public boolean isEqualsUpStation(final Station station) {
        return upStation.equals(station);
    }

    public boolean isEqualsDownStation(final Station station) {
        return downStation.equals(station);
    }

    public void reArrangeDeleteSection(final Section deleteSection, final Station deleteStation) {
        if (deleteSection.isEqualsUpStation(deleteStation)) {
            downStation = deleteSection.downStation;
        }

        if (deleteSection.isEqualsDownStation(deleteStation)) {
            upStation = deleteSection.upStation;
        }

        distance += deleteSection.distance;
    }

    public void reArrangeAddSection(final Section nonPersistSection) {
        if (distance <= nonPersistSection.distance) {
            throw new DistanceOverException();
        }

        upStationToDownStation(nonPersistSection);
        downStationToUpStation(nonPersistSection);

        distance -= nonPersistSection.distance;
    }

    private void downStationToUpStation(Section nonPersistSection) {
        if (isEqualsDownStation(nonPersistSection.downStation)) {
            downStation = nonPersistSection.upStation;
        }
    }

    private void upStationToDownStation(Section nonPersistSection) {
        if (isEqualsUpStation(nonPersistSection.upStation)) {
            upStation = nonPersistSection.downStation;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Section)) return false;
        Section section = (Section) o;
        return distance == section.distance
                && Objects.equals(id, section.id)
                && Objects.equals(upStation, section.upStation)
                && Objects.equals(downStation, section.downStation)
                && Objects.equals(line, section.line);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, upStation, downStation, distance);
    }
}
