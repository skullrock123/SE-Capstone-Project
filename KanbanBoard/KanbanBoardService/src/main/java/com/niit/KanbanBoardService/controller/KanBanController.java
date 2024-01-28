package com.niit.KanbanBoardService.controller;

import com.niit.KanbanBoardService.domian.*;
import com.niit.KanbanBoardService.exception.BoardNotFoundException;
import com.niit.KanbanBoardService.exception.InvalidCredentialsException;
import com.niit.KanbanBoardService.exception.UserAlreadyExistsException;
import com.niit.KanbanBoardService.service.UserService;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.cors.CorsConfiguration;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "http://localhost:4200/")
public class KanBanController {
    private UserService userService;
    private ResponseEntity responseEntity;
    @Autowired
    public KanBanController(UserService userService) {
        this.userService = userService;
    }


    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody User user){
        try {
            responseEntity = new ResponseEntity<>(userService.saveUser(user), HttpStatus.OK);
        }catch (UserAlreadyExistsException e){
            responseEntity = new ResponseEntity<>(e.getMessage(),HttpStatus.CONFLICT);
        }catch (Exception e){
            responseEntity = new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return responseEntity;
    }



    @PostMapping("/user/saveBoard")
    public ResponseEntity<?> saveNewBoard(@RequestBody Board board , HttpServletRequest request)  {
        String emailId = getUserIdFromClaims(request);
        System.out.println("emailId :: "+emailId);

        try {
            responseEntity = new ResponseEntity<>(userService.createNewBoard(board,emailId), HttpStatus.OK);
        }catch (InvalidCredentialsException e){
            responseEntity = new ResponseEntity<>(e.getMessage(),HttpStatus.UNAUTHORIZED);
        }catch (Exception e){
            responseEntity = new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return responseEntity;
    }

    @PostMapping("/user/saveStages/{boardName}")
    public ResponseEntity<?> saveNewStages(@PathVariable String boardName,@RequestBody Stage stage , HttpServletRequest request)  {
        String emailId = getUserIdFromClaims(request);
        System.out.println("emailId :: "+emailId);
        try {
            responseEntity = new ResponseEntity<>(userService.addStagesIntoBoard(boardName,stage,emailId), HttpStatus.OK);
        }catch (InvalidCredentialsException e){
            responseEntity = new ResponseEntity<>(e.getMessage(),HttpStatus.UNAUTHORIZED);
        }catch (Exception e){
            responseEntity = new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return responseEntity;
    }


    @PostMapping("/user/saveMembers/{boardName}")
    public ResponseEntity<?> saveMembers(@PathVariable String boardName, @RequestBody Member member, HttpServletRequest request)  {
        String emailId = getUserIdFromClaims(request);
        System.out.println("emailId :: "+emailId);
        try {
            responseEntity = new ResponseEntity<>(userService.addMembersInBoard(boardName,member,emailId), HttpStatus.OK);
        }catch (InvalidCredentialsException e){
            responseEntity = new ResponseEntity<>(e.getMessage(),HttpStatus.UNAUTHORIZED);
        }catch (Exception e){
            responseEntity = new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return responseEntity;
    }

    @PostMapping("/user/saveTask/{boardName}/{stageName}")
    public ResponseEntity<?> saveTaskToStage(@PathVariable String boardName, @PathVariable String stageName, @RequestBody Task task, HttpServletRequest request)  {
        String emailId = getUserIdFromClaims(request);
        System.out.println("emailId :: "+emailId);
        try {
            responseEntity = new ResponseEntity<>(userService.createNewTask(boardName,stageName,task,emailId), HttpStatus.OK);
        }catch (InvalidCredentialsException e){
            responseEntity = new ResponseEntity<>(e.getMessage(),HttpStatus.UNAUTHORIZED);
        }catch (Exception e){
            responseEntity = new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return responseEntity;
    }

    @PostMapping("/user/transferTask/{boardName}/{fromStage}/{toStage}/{taskId}")
    public ResponseEntity<?> transferTaskFromOneToAnother(@PathVariable String boardName, @PathVariable String fromStage, @PathVariable String toStage , @PathVariable String taskId, HttpServletRequest request)  {
        String emailId = getUserIdFromClaims(request);
        System.out.println("emailId :: "+emailId);
        try {
            responseEntity = new ResponseEntity<>(userService.moveTaskToAnotherStage(emailId,boardName,fromStage,toStage,taskId), HttpStatus.OK);
        }catch (InvalidCredentialsException e){
            responseEntity = new ResponseEntity<>(e.getMessage(),HttpStatus.UNAUTHORIZED);
        }catch (Exception e){
            responseEntity = new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return responseEntity;
    }



    @PutMapping("/user/updateBoard/{boardName}")
    public ResponseEntity<?>updateBoardName(@PathVariable String boardName,@RequestBody Board newBoardName,HttpServletRequest request) throws InvalidCredentialsException, BoardNotFoundException {
        String emailId = getUserIdFromClaims(request);
        System.out.println("emailId :: "+emailId);
        try {
            responseEntity = new ResponseEntity<>(userService.updateBoard(emailId,boardName,newBoardName), HttpStatus.OK);
        } catch (Exception e)
        {
            responseEntity = new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return responseEntity;
  
    }
  
  
    @DeleteMapping("/user/deleteBoard/{boardName}")
    public ResponseEntity<?> deleteBoard(@PathVariable String boardName, HttpServletRequest request)  {
        Claims claims = (Claims) request.getAttribute("claims");
        String emailId = claims.getSubject();
        System.out.println("emailId :: "+emailId);
        try {
            responseEntity = new ResponseEntity<>(userService.deleteBoard(emailId,boardName), HttpStatus.OK);
        }catch (InvalidCredentialsException e){
            responseEntity = new ResponseEntity<>(e.getMessage(),HttpStatus.UNAUTHORIZED);
        }catch (Exception e){
            responseEntity = new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return responseEntity;
    }




    @PutMapping("/user/updateTask/{boardName}/{stageName}")
    public ResponseEntity<?> updateTask(@PathVariable String boardName,@PathVariable String stageName , @RequestBody Task newTaskData , HttpServletRequest request)  {
        String emailId = getUserIdFromClaims(request);
        System.out.println("emailId :: "+emailId);
        try {
            responseEntity = new ResponseEntity<>(userService.updateTask(emailId,boardName,stageName,newTaskData), HttpStatus.OK);
        }catch (InvalidCredentialsException e){
            responseEntity = new ResponseEntity<>(e.getMessage(),HttpStatus.UNAUTHORIZED);
        }catch (Exception e){
            responseEntity = new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return responseEntity;
    }


    @DeleteMapping("/user/deleteTask/{boardName}/{stageName}/{taskId}")
    public ResponseEntity<?> deleteTask(@PathVariable String boardName,@PathVariable String stageName , @PathVariable String taskId , HttpServletRequest request)  {
        String emailId = getUserIdFromClaims(request);
        System.out.println("emailId :: "+emailId);
        try {
            responseEntity = new ResponseEntity<>(userService.deleteTask(emailId,boardName,stageName,taskId), HttpStatus.OK);
        } catch (Exception e){
            responseEntity = new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return responseEntity;
    }

    @GetMapping("/user/getAllUserDetails")
    public ResponseEntity<?> getAllUserData(HttpServletRequest request){
        String emailId = getUserIdFromClaims(request);
        System.out.println("emailId :: "+emailId);
        responseEntity = new ResponseEntity<>(userService.getCompleteDataOfUser(emailId),HttpStatus.OK);
        return  responseEntity;
    }


    @PutMapping("/user/updateUser")
    public ResponseEntity<?> updateUser(@RequestBody User updateUser ,HttpServletRequest request)  {
        String emailId = getUserIdFromClaims(request);
        System.out.println("emailId :: "+emailId);
        try {
            responseEntity = new ResponseEntity<>(userService.updateUser(emailId,updateUser), HttpStatus.OK);
        } catch (Exception e){
            responseEntity = new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return responseEntity;
    }

    @PostMapping("/user/addMemberInTask/{boardName}/{stageName}/{taskId}")
    public ResponseEntity<?> addMemberIntoTask(@PathVariable String boardName,@PathVariable String stageName , @PathVariable String taskId ,Member member ,HttpServletRequest request)  {
        String emailId = getUserIdFromClaims(request);
        System.out.println("emailId :: "+emailId);
        try {
            responseEntity = new ResponseEntity<>(userService.addMemberToTask(emailId,boardName,stageName,taskId,member), HttpStatus.OK);
        } catch (Exception e){
            responseEntity = new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return responseEntity;
    }

    @PostMapping("/user/addTaskToMember/{boardName}/{memberEmailId}/{taskId}")
    public ResponseEntity<?> addTaskToMember(@PathVariable String boardName,@PathVariable String memberEmailId , @PathVariable String taskId ,HttpServletRequest request)  {
        String emailId = getUserIdFromClaims(request);
        System.out.println("emailId :: "+emailId);
        try {
            responseEntity = new ResponseEntity<>(userService.addTaskToMemeber(emailId,memberEmailId,taskId,boardName), HttpStatus.OK);
        } catch (Exception e){
            responseEntity = new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return responseEntity;
    }



    // getting emailId after decrypting the token

    private String getUserIdFromClaims(HttpServletRequest request){
        Claims claims = (Claims) request.getAttribute("claims");
        System.out.println("Customer EmailID from claims :: " + claims.getSubject());
        return claims.getSubject();
    }

}
