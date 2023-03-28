package snust.sbsp.crew.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import snust.sbsp.common.exception.CustomCommonException;
import snust.sbsp.common.exception.ErrorCode;
import snust.sbsp.company.domain.Company;
import snust.sbsp.company.dto.res.base.CompanyDto;
import snust.sbsp.company.service.CompanyService;
import snust.sbsp.crew.domain.Crew;
import snust.sbsp.crew.domain.type.Role;
import snust.sbsp.crew.dto.res.CrewRes;
import snust.sbsp.crew.repository.CrewRepository;
import snust.sbsp.crew.specification.CrewSpecification;
import snust.sbsp.project.service.ProjectService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CrewService {

  private final CompanyService companyService;

  private final ProjectService projectService;

  private final CrewRepository crewRepository;

  @Transactional(readOnly = true)
  public Crew readCrew(String crewEmail) {

    return crewRepository.findByEmail(crewEmail)
      .orElseThrow(() -> new CustomCommonException(ErrorCode.CREW_NOT_FOUND));
  }

  @Transactional(readOnly = true)
  public CrewRes readCrew(Long crewId) {
    Crew crew = crewRepository.findById(crewId)
      .orElseThrow(() -> new CustomCommonException(ErrorCode.CREW_NOT_FOUND));

    return CrewRes.builder()
      .crew(crew)
      .company(new CompanyDto(crew.getCompany()))
      .projectList(projectService.readProjectList(crew))
      .build();
  }

  public List<CrewRes> readCrewList(
    Long companyId,
    Boolean isPending,
    Role role,
    String name
  ) {
    Specification<Crew> specification = ((root, query, criteriaBuilder) -> null);
    if (name != null)
      specification = specification.and(CrewSpecification.equalName(name));
    if (role != null)
      specification = specification.and(CrewSpecification.equalRole(role));
    if (isPending != null)
      specification = specification.and(CrewSpecification.equalIsPending(isPending));
    if (companyId != null) {
      Company company = companyService.findById(companyId);
      specification = specification.and(CrewSpecification.equalCompany(company));
    }

    List<Crew> crewList = crewRepository.findAll(specification);

    return crewList
      .stream()
      .map(crew ->
        CrewRes
          .builder()
          .crew(crew)
          .company(new CompanyDto(crew.getCompany()))
          .build()
      ).collect(Collectors.toList());
  }
}
