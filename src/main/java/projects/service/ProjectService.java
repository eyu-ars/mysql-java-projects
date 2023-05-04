package projects.service;

import projects.dao.ProjectDao;
import projects.entity.Project;

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

}
