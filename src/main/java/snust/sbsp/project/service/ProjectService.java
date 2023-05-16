package snust.sbsp.project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import snust.sbsp.common.exception.CustomCommonException;
import snust.sbsp.common.exception.ErrorCode;
import snust.sbsp.company.domain.Company;
import snust.sbsp.company.repository.CompanyRepository;
import snust.sbsp.crew.domain.Crew;
import snust.sbsp.crew.domain.type.Role;
import snust.sbsp.crew.service.CrewService;
import snust.sbsp.project.domain.Project;
import snust.sbsp.project.domain.type.CtrType;
import snust.sbsp.project.domain.type.DetailCtrType;
import snust.sbsp.project.dto.req.ProjectReq;
import snust.sbsp.project.dto.res.base.ProjectDto;
import snust.sbsp.project.repository.ProjectRepository;
import snust.sbsp.project.specification.ProjectSpecification;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

  private final ProjectRepository projectRepository;

  private final CompanyRepository companyRepository;

  private final CrewService crewService;

  @Transactional
  public void createProject(
    ProjectReq projectReq,
    Long crewId
  ) {
    Crew crew = crewService.readCrewById(crewId);

    Role role = crew.getRole();
    if (!role.equals(Role.COMPANY_ADMIN) && !role.equals(Role.SERVICE_ADMIN))
      throw new CustomCommonException(ErrorCode.FORBIDDEN);

    Company company = role.equals(Role.COMPANY_ADMIN)
      ? crew.getCompany()
      : companyRepository.findById(projectReq.getCompanyId())
      .orElseThrow(() -> new CustomCommonException(ErrorCode.COMPANY_NOT_FOUND));

    Project project = Project.builder()
      .name(projectReq.getName())
      .startDate(projectReq.getStartDate())
      .endDate(projectReq.getEndDate())
      .ctrType(CtrType.from(projectReq.getCtrType()))
      .detailCtrType(DetailCtrType.from(projectReq.getDetailCtrType()))
      .thumbnailUrl(projectReq.getThumbnailUrl())
      .floorUrl(projectReq.getFloorPlanUrl())
      .company(company)
      .build();

    projectRepository.save(project);
  }

  @Transactional(readOnly = true)
  public List<ProjectDto> readExceptMyProjectList(
    Long currentCrewId,
    Long companyId,
    String name,
    String ctrClass,
    String detailCtrClass
  ) {
    Specification<Project> specification = ((root, query, criteriaBuilder) -> null);
    if (name != null)
      specification = specification.and(ProjectSpecification.equalName(name));
    if (ctrClass != null)
      specification = specification.and(ProjectSpecification.equalCtrClass(CtrType.from(ctrClass)));
    if (detailCtrClass != null)
      specification = specification.and(ProjectSpecification.equalDetailCtrClass(DetailCtrType.from(detailCtrClass)));
    if (companyId != null) {
      specification = specification.and(ProjectSpecification.equalCompanyId(companyId));
    }

    List<ProjectDto> allProjectList = projectRepository.findAll(specification)
      .stream()
      .map((ProjectDto::new))
      .collect(Collectors.toList());

    List<ProjectDto> myProjectList = readMyProjectList(currentCrewId);
    System.out.println(allProjectList);
    System.out.println(myProjectList);
    allProjectList.removeAll(myProjectList);
    System.out.println(allProjectList);
    return allProjectList;
  }

  @Transactional(readOnly = true)
  public List<ProjectDto> readMyProjectList(Long crewId) {
    Crew foundedCrew = crewService.readCrewById(crewId);
    System.out.println(foundedCrew);

    return foundedCrew.getParticipantList()
      .stream()
      .map((participant -> new ProjectDto(participant.getProject())))
      .collect(Collectors.toList());
  }
}
