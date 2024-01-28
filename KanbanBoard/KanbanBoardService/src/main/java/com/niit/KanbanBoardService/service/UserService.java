package com.niit.KanbanBoardService.service;


import com.niit.KanbanBoardService.domian.*;
import com.niit.KanbanBoardService.exception.*;


public interface UserService {

    User saveUser(User user) throws UserAlreadyExistsException;

    Board createNewBoard(Board board, String emailId) throws InvalidCredentialsException, BoardAlreadyExistsException;

    Stage addStagesIntoBoard(String boardName, Stage stage, String emailId) throws InvalidCredentialsException, StageAlreadyExistException, BoardNotFoundException;

    Member addMembersInBoard(String boardName, Member member, String emailId) throws InvalidCredentialsException, BoardNotFoundException, MemberAlreadyExistException;

    Task createNewTask(String boardName, String stageName, Task task, String emailId) throws InvalidCredentialsException, BoardNotFoundException, StageNotFoundException, TaskAlreadyExistException;

    User moveTaskToAnotherStage(String emailId, String boardName, String fromStage, String toStage, String taskId) throws InvalidCredentialsException, BoardNotFoundException, TaskNotFoundException, StageNotFoundException;

    Board updateBoard(String emailId, String boardName, Board newBoardName) throws InvalidCredentialsException, BoardNotFoundException;
   

    boolean deleteBoard(String emailId,String boardName) throws InvalidCredentialsException, BoardNotFoundException;

    Task updateTask(String emailId, String boardName ,String stageName, Task newTaskData) throws InvalidCredentialsException, TaskNotFoundException, BoardNotFoundException, StageNotFoundException;

    boolean deleteTask(String emailId, String boardName , String stageName , String taskId) throws TaskNotFoundException, InvalidCredentialsException, BoardNotFoundException, StageNotFoundException;

    User updateUser(String emailId ,User user) throws InvalidCredentialsException;

    Task addMemberToTask(String emailId,String boardName, String stageName , String taskId, Member member) throws Exception;
    Member addTaskToMemeber(String emailId, String memberEmailId, String taskId, String boardName) throws Exception;


    // final method to display all
    User getCompleteDataOfUser(String emailId);

}
