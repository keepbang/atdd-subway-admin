package nextstep.subway.line.application;

import nextstep.subway.line.domain.Line;
import nextstep.subway.line.domain.LineRepository;
import nextstep.subway.line.domain.Section;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.line.dto.LineResponses;
import nextstep.subway.line.dto.SectionRequest;
import nextstep.subway.station.domain.Station;
import nextstep.subway.station.domain.StationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LineService {
    private final LineRepository lineRepository;
    private final StationRepository stationRepository;

    public LineService(
            final LineRepository lineRepository
            , final StationRepository stationRepository) {
        this.lineRepository = lineRepository;
        this.stationRepository = stationRepository;
    }

    public LineResponse save(final LineRequest request) {
        Line persistLine = lineRepository.save(request.toLine());
        Section section = createSection(request, persistLine);
        persistLine.addSection(section);
        return LineResponse.of(persistLine);
    }

    public LineResponse addSection(final Long id, final SectionRequest section) {
        Line persistLine = lineRepository.findByIdElseThrow(id);
        persistLine.addSection(createSection(section, persistLine));
        return LineResponse.of(persistLine);
    }

    private Section createSection(final SectionRequest request, final Line line) {
        Station upStation = stationRepository.findByIdElseThrow(request.getUpStationId());
        Station downStation = stationRepository.findByIdElseThrow(request.getDownStationId());
        return new Section(upStation, downStation, line, request.getDistance());
    }

    @Transactional(readOnly = true)
    public LineResponses findAll() {
        return LineResponses.of(lineRepository.findAll());
    }

    @Transactional(readOnly = true)
    public LineResponse findById(Long id) {
        return LineResponse.of(lineRepository.findById(id)
                .orElse(new Line()));
    }

    public void update(final Long id, final LineRequest updatedLine) {
        Line line = lineRepository.findByIdElseThrow(id);
        line.update(Line.of(updatedLine));
    }

    public void delete(final Long id) {
        lineRepository.findByIdElseThrow(id);
        lineRepository.deleteById(id);
    }

    public void removeSectionByStationId(final Long id, final Long stationId) {
        Line persistLine = lineRepository.findByIdElseThrow(id);
        Station deleteStation = stationRepository.findByIdElseThrow(stationId);
        persistLine.remove(deleteStation);
    }
}
