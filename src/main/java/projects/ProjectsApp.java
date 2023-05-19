package projects;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import projects.entity.Project;
import projects.exception.DbException;
import projects.service.ProjectService;

public class ProjectsApp {

  //@formatter:off
  private List<String> operations = List.of(
      "1) Add aproject", 
      "2) List projects",
      "3) Select a project",
      "4) Update project details",
      "5) Delete a project"
      );
  //@formatter:on

  // Creating Scanner object
  private Scanner scanner = new Scanner(System.in);

  // Creating project service object
  ProjectService projectService = new ProjectService();
  
  // Creating instance variable type project 
  private Project currentProject;

  
  /**
   * The starting point of project application.
   * @param args Unused
   */
  public static void main(String[] args) {
    // Calling processUserSelections method
    new ProjectsApp().processUserSelections();
  }
  

  /**
   * A method prints the operations, used to process user selection and perform requested operation. If you enter  
   * invalid selection, it prints out error message. It repeats until the user request to terminate the application.
   */
  private void processUserSelections() {
    boolean done = false;

    while (!done) {
      try {
        int selection = getUserSelection();

        switch (selection) {

          case -1:
            done = exitMenu();
            break;

          case 1:
            createProject();
            break;
            
          case 2:
            listProjects();
            break;
            
          case 3:
            selectProject();
            break;
            
          case 4:
            updateProjectDetails();
            break;
            
          case 5:
            deleteProject();
            break;
               
          default:
            System.out.println("\n" + selection + " is not a valid selection. Try again.");
            break;
        }

      } catch (Exception e) {
        System.out.println("\nError: " + e + " Try again.");
      }
    }
  }

  
  /**
   * A method prints list of projects on the console, get user project ID selection, 
   * and delete project detail based on the user selection.
   */
  private void deleteProject() {
    // Calling listProjects method for printing projects on the console
    listProjects();
    
    Integer projectId = getIntInput("Enter the ID of the project to delete");
    
    projectService.deleteProject(projectId);
    System.out.println("Project " + projectId + " was deleted successfully.");
    
    // Set currentProject
    if(Objects.nonNull(currentProject) && currentProject.getProjectId().equals(projectId)) {
      currentProject = null;
    }
    
  }


  /**
   * A method collects project inputs from user and modified the record.
   */
  private void updateProjectDetails() {
    // Checking if the project is null
    if(Objects.isNull(currentProject)) {
      System.out.println("\nPlease select a project.");
      return;
    }

    /*
     * Collecting project inputs for modification 
     */
    String projectName = getStringInput("Enter the project name [" + currentProject.getProjectName() + "]");
    BigDecimal estimatedHours = getDecimalInput("Enter the estimated hours [" + currentProject.getEstimatedHours() + "]");
    BigDecimal actualHours = getDecimalInput("Enter the actual hours [" + currentProject.getActualHours() + "]");
    Integer difficulty = getIntInput("Enter the project difficulty (1-5) [" + currentProject.getDifficulty() + "]");
    
    // Calling checkDifficultyInputIsValid for validation
    checkDifficultyInputIsValid(difficulty);

    String notes = getStringInput("Enter the project notes [" + currentProject.getNotes() + "]");

    // Creating project object type of Project
    Project project = new Project();

    /*
     * Accessing project setters
     */
    project.setProjectName(Objects.isNull(projectName) ? currentProject.getProjectName() : projectName);
    project.setEstimatedHours(Objects.isNull(estimatedHours) ? currentProject.getEstimatedHours() : estimatedHours);
    project.setActualHours(Objects.isNull(actualHours) ? currentProject.getActualHours() : actualHours);
    project.setDifficulty(Objects.isNull(difficulty) ? currentProject.getDifficulty() : difficulty);
    project.setNotes(Objects.isNull(projectName) ? currentProject.getNotes() : notes);
    project.setProjectId(currentProject.getProjectId());

    // Calling modifyProjectDetails method from project service to update a project
    projectService.modifyProjectDetails(project);
    currentProject = projectService.fetchProjectById(currentProject.getProjectId());   
  }


  /**
   * A method prints list of projects on the console, get user selection, 
   * and fetch project detail based on the user selection.
   */
  private void selectProject() {
    listProjects();
    
    Integer projectId = getIntInput("Enter a project ID to select a project");
    
    currentProject = null;
    currentProject = projectService.fetchProjectById(projectId);
  }


  /**
   * A method prints list of projects on the console.
   */
  private void listProjects() {
    List<Project> projects = projectService.fetchAllProjects();
    
    System.out.println("\nProjects:");
    
    projects.forEach(project -> System.out.println(
        "  " + project.getProjectId() + ": " + project.getProjectName()));
    
  }


  /**
   * Accepting and validating user inputs for a project row then call the project service to create the row.
   */
  private void createProject() {
    /*
     * Collecting project inputs
     */
    String projectName = getStringInput("Enter the project name");
    BigDecimal estimatedHours = getDecimalInput("Enter the estimated hours");
    BigDecimal actualHours = getDecimalInput("Enter the actual hours");
    Integer difficulty = getIntInput("Enter the project difficulty (1-5)");
    
    // Calling checkDifficultyInputIsValid for validation
    checkDifficultyInputIsValid(difficulty);

    String notes = getStringInput("Enter the project notes");

    // Creating project object type of Project
    Project project = new Project();

    /*
     * Accessing project setters
     */
    project.setProjectName(projectName);
    project.setEstimatedHours(estimatedHours);
    project.setActualHours(actualHours);
    project.setDifficulty(difficulty);
    project.setNotes(notes);

    // Calling addProject method from project service to add a project
    Project dbProject = projectService.addProject(project);
    System.out.println("You have successfully created project: " + dbProject);

  }

  
  /**
   * A method that accepts difficulty and checks difficulty input is in a valid range(1-5). If not throws DBexception.
   * @param prompt Integer
   * @throws DbException if the input is not a valid range(1-5).
   */
  private void checkDifficultyInputIsValid(Integer prompt) {

    // Checking the difficulty is null. If it is null assigning -1 otherwise prompt
    int difficulty = Objects.isNull(prompt) ? -1 : prompt;

    // Checking the difficulty is in valid range (1-5)
    if (difficulty < 0 || difficulty > 5) {
      throw new DbException(difficulty + " is not a valid difficulty input.");
    }

  }

  
  /**
   * A method print out exiting menu message and returns true for terminating the application.
   * @return true
   */
  private boolean exitMenu() {
    System.out.println("Exiting the menu.");
    return true;
  }

  
  /**
   * A method prints available menu selections. It then gets user's menu selection from the console and 
   * returns user selection number otherwise -1.
   * 
   * @return The menu selection as an int or -1.
   */
  private int getUserSelection() {
    // Calling printOperation method
    printOperations();

    // Calling getIntInput method and assign to local input variable
    Integer input = getIntInput("Enter a menu selection");

    // Checking user selection input is null. If it is null returning -1 otherwise input.
    return Objects.isNull(input) ? -1 : input;
  }

  /**
   * A method that prints a prompt on the console, takes a given input and returns its correspondent integer value.
   * @param prompt The prompt to print.
   * @return Null If the input is null otherwise Integer.
   * @throws DbException if the input is not parse to integer.
   */
  private Integer getIntInput(String prompt) {
    // Calling getStringInput method and assign to local input variable
    String input = getStringInput(prompt);

    // Checking the input is null
    if (Objects.isNull(input))
      return null;

    try {
      return Integer.parseInt(input);

    } catch (NumberFormatException e) {
      throw new DbException(input + " is not a valid number. Try again.");
    }

  }


  /**
   * A method that prints a prompt on the console, takes a given input and returns its correspondent BigDecimal value.
   * @param prompt The prompt to print.
   * @return Null If the input is null otherwise BigDecimal.
   * @throws DbException if the input is not a valid decimal number.
   */
  private BigDecimal getDecimalInput(String prompt) {
    // Calling getStringInput method and assign to local input variable
    String input = getStringInput(prompt);

    // Checking the input is null
    if (Objects.isNull(input))
      return null;

    try {
      return new BigDecimal(input).setScale(2);

    } catch (NumberFormatException e) {
      throw new DbException(input + " is not a valid decimal number.");
    }
  }

  /**
   * Print a prompt on the console and gets the user's input from the console. If the user 
   * enters nothing, {@code null} is returned. Otherwise, the trimmed input is returned.
   * @param prompt The prompt to print.
   * @return The user's input or {@code null}.
   */
  private String getStringInput(String prompt) {
    System.out.print(prompt + ": ");

    // Calling nextLine method from the scanner object and assign to local input variable
    String input = scanner.nextLine();

    return input.isBlank() ? null : input.trim();
  }

  /**
   * A method prints list of operation on the console and current project if it is not null.
   */
  private void printOperations() {
    System.out.println("\nThese are the available selections. Press the Enter key to quite:");
    // Printing out every element from operation list
    operations.forEach(line -> System.out.println("  " + line));
    
    if(Objects.isNull(currentProject)) {
      System.out.println("\nYou are not working with a project.");
    }else {
      System.out.println("\nYou are working with a project: " + currentProject);
    }
  }

}
