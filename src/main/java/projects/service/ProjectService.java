package projects.service;

import java.util.List;
import projects.dao.ProjectDao;
import projects.entity.Project;
import projects.exception.DbException;

public class ProjectService {

  ProjectDao projectDao = new ProjectDao();
  
  /**
   * A method calls the DAO class to insert a project row.
   * @param project The {@link Project} object.
   * @return The project object with the newly generated primary key value. 
   */
  public Project addProject(Project project) {
    // Calling insertProject method from project DAO for inserting a project 
    return projectDao.insertProject(project);
  }

  /**
   * Calls the project DAO to retrieve all projects.
   * @return A list of project records
   */
  public List<Project> fetchAllProjects() {
    return projectDao.fetchAllProjects();
  }

  /**
   * Calls the project DAO to get a single project details, including materials, steps, and
   * categories. If the project ID is invalid, it throws an exception.
   * @param projectId The project ID
   * @return project object if it is successful
   */
  public Project fetchProjectById(Integer projectId) {
    return projectDao.fetchProjectById(projectId).
        orElseThrow(() -> new DbException("Project with project ID=" + projectId + " does not exist."));
  }

}
