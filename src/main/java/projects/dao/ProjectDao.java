package projects.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import projects.entity.Project;
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

}
