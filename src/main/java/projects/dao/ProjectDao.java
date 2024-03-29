package projects.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import projects.entity.Category;
import projects.entity.Material;
import projects.entity.Project;
import projects.entity.Step;
import projects.exception.DbException;
import provided.util.DaoBase;

/**
 * This class uses JDBC to perform CRUD operations on the project table.
 * @author Admin
 *
 */
@SuppressWarnings("unused")
public class ProjectDao extends DaoBase {

  private static final String CATEGORY_TABLE = "category";
  private static final String MATERIAL_TABLE = "material";
  private static final String PROJECT_TABLE = "project";
  private static final String PROJECT_CATEGORY_TABLE = "project_category";
  private static final String STEP_TABLE = "step";
  
  /**
   * A method that inserts a project into database.
   * @param project Project object
   * @return inserted project with the primary key.
   * @throws DbException Thrown if an error occurs inserting the row.
   */
  public Project insertProject(Project project) {
    
    // @formatter:off
    String sql = ""
        + "INSERT INTO " + PROJECT_TABLE + " "
        + "(project_name, estimated_hours, actual_hours, difficulty, notes) "
        + "VALUES "
        + "(?, ?, ?, ?, ?)";
    // @formatter:on
    
    try(Connection conn = DbConnection.getConnection()){
      // start transaction
      startTransaction(conn);
      
      try(PreparedStatement stmt = conn.prepareStatement(sql)){
        setParameter(stmt, 1, project.getProjectName(), String.class);
        setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
        setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
        setParameter(stmt, 4, project.getDifficulty(), Integer.class);
        setParameter(stmt, 5, project.getNotes(), String.class);
        
        stmt.executeUpdate();
        
        Integer projectId = getLastInsertId(conn, PROJECT_TABLE);
        // Commit transaction
        commitTransaction(conn);
        
        project.setProjectId(projectId);
        
        return project;
        
      }catch(SQLException e) {
        // Roll back transaction if SQL exception happens
         rollbackTransaction(conn);
         throw new DbException(e);
      }
      
    }catch(SQLException e) {
      throw new DbException(e);
    }
  }

  
  /**
   * A method that fetches all projects from database.
   * @return List of all projects
   */
  public List<Project> fetchAllProjects() {
    String sql = "SELECT * FROM " + PROJECT_TABLE + " ORDER BY project_name";

    try (Connection conn = DbConnection.getConnection()) {
      startTransaction(conn);
      
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        try(ResultSet rs = stmt.executeQuery()){
          List<Project> projects = new LinkedList<>();
          
          while(rs.next()) {
            projects.add(extract(rs, Project.class));  
          }
          
          return projects;
        }

      } catch (Exception e) {
        rollbackTransaction(conn);
        throw new DbException(e);
      }

    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  
  /**
   * A method that fetches a single project from database by a given project ID.
   * @param projectId Integer
   * @return Optional<Project> 
   * @throws DbException
   */
  public Optional<Project> fetchProjectById(Integer projectId) {
    String sql = "SELECT * FROM " + PROJECT_TABLE + " WHERE project_id = ?";
    
    try(Connection conn = DbConnection.getConnection()){
      startTransaction(conn);
      
      try{
        Project project = null;
        
        try(PreparedStatement stmt = conn.prepareStatement(sql)){
          setParameter(stmt, 1, projectId, Integer.class);
          
          try(ResultSet rs = stmt.executeQuery()){
            if(rs.next()) {
              project = extract(rs, Project.class);
            }
          }
        }
        
        if(Objects.nonNull(project)) {
          project.getMaterials().addAll(fetchMaterialsForProject(conn, projectId));
          project.getSteps().addAll(fetchStepsForProject(conn, projectId));
          project.getCategories().addAll(fetchCategoriesForProject(conn, projectId));
        }
        
        commitTransaction(conn);
        return Optional.ofNullable(project);
        
      }catch (Exception e) {
        rollbackTransaction(conn);
        throw new DbException(e);
      }
    }catch(SQLException e) {
      throw new DbException(e);
    }
  }

  
  /**
   * This method fetches list of categories that associated with a single project by a given project ID.
   * @param conn Connection
   * @param projectId Integer
   * @return List of categories
   * @throws SQLException
   */
  private List<Category> fetchCategoriesForProject(Connection conn, Integer projectId) throws SQLException {
    // @formatter:off
    String sql = ""
        + "SELECT c.* " 
        + "FROM " + CATEGORY_TABLE + " c "
        + "JOIN " + PROJECT_CATEGORY_TABLE + " pc USING (category_id) "
        + "WHERE project_id = ?";
    // @formatter:on
    
    try(PreparedStatement stmt = conn.prepareStatement(sql)){
      setParameter(stmt, 1, projectId, Integer.class);
      
      try(ResultSet rs = stmt.executeQuery()){
        List<Category> categories = new LinkedList<>();
        
        while(rs.next()) {
          categories.add(extract(rs, Category.class));
        }
        
        return categories;
      }
    }
  }

  
  /**
   * This method fetches list of steps that associated with a single project by a given project ID.
   * @param conn Connection
   * @param projectId Integer
   * @return List of steps
   * @throws SQLException
   */
  private List<Step> fetchStepsForProject(Connection conn, Integer projectId) throws SQLException {
    String sql = "SELECT * FROM " + STEP_TABLE + " WHERE project_id = ?";
    
    try(PreparedStatement stmt = conn.prepareStatement(sql)){
      setParameter(stmt, 1, projectId, Integer.class);
      
      try(ResultSet rs = stmt.executeQuery()){
        List<Step> steps = new LinkedList<>();
        
        while(rs.next()) {
          steps.add(extract(rs, Step.class));
        }
        
        return steps;
      }
    }  
  }

  
  /**
   * This method fetches list of materials that associated with a single project by a given project ID.
   * @param conn Connection
   * @param projectId Integer
   * @return List of materials
   * @throws SQLException
   */
  private List<Material> fetchMaterialsForProject(Connection conn, Integer projectId) throws SQLException {
    String sql = "SELECT * FROM " + MATERIAL_TABLE + " WHERE project_id = ?";
    
    try(PreparedStatement stmt = conn.prepareStatement(sql)){
      setParameter(stmt, 1, projectId, Integer.class);
      
      try(ResultSet rs = stmt.executeQuery()){
        List<Material> materials = new LinkedList<>();
        
        while(rs.next()) {
          materials.add(extract(rs, Material.class));
        }
        
        return materials;
      }
    }  
  }

 
  /**
   * A method that updates a project from database.
   * @param project Project object
   * @return true if the project is updated, false otherwise.
   * @throws DbException Thrown if an error occurs updating the row.
   */
  public boolean modifyProjectDetails(Project project) {
    // @formatter:off
    String sql = ""
        + "UPDATE " + PROJECT_TABLE + " SET "
        + "project_name = ?, estimated_hours = ?, actual_hours = ?, difficulty = ?, notes = ? "
        + "WHERE project_id = ?";
    // @formatter:on
    
    try(Connection conn = DbConnection.getConnection()){
      // start transaction
      startTransaction(conn);
      
      try(PreparedStatement stmt = conn.prepareStatement(sql)){
        setParameter(stmt, 1, project.getProjectName(), String.class);
        setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
        setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
        setParameter(stmt, 4, project.getDifficulty(), Integer.class);
        setParameter(stmt, 5, project.getNotes(), String.class);
        setParameter(stmt, 6, project.getProjectId(), Integer.class);
        
        boolean updated = stmt.executeUpdate() == 1;

        // Commit transaction
        commitTransaction(conn);
        
        return updated;
        
      }catch(SQLException e) {
        // Roll back transaction if SQL exception happens
         rollbackTransaction(conn);
         throw new DbException(e);
      }
      
    }catch(SQLException e) {
      throw new DbException(e);
    }
  }


  /**
   * A method that deletes a project from database by a given project ID.
   * @param projectId Integer
   * @return true if the project is deleted, false otherwise.
   * @throws DbException Thrown if an error occurs deleting the row.
   */
  public boolean deleteProject(Integer projectId) {
    String sql = ""
        + "DELETE FROM " + PROJECT_TABLE + " WHERE project_id = ?";
    
    try(Connection conn = DbConnection.getConnection()){
      // start transaction
      startTransaction(conn);
      
      try(PreparedStatement stmt = conn.prepareStatement(sql)){
        setParameter(stmt, 1, projectId, Integer.class);
        
        boolean deleted = stmt.executeUpdate() == 1;

        // Commit transaction
        commitTransaction(conn);
        
        return deleted;
        
      }catch(SQLException e) {
        // Roll back transaction if SQL exception happens
         rollbackTransaction(conn);
         throw new DbException(e);
      }
      
    }catch(SQLException e) {
      throw new DbException(e);
    }
  }

}
