package com.niit.KanbanBoardService.service;


import com.niit.KanbanBoardService.domian.*;
import com.niit.KanbanBoardService.exception.*;
import com.niit.KanbanBoardService.proxy.UserAuthProxy;
import com.niit.KanbanBoardService.repository.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserAuthProxy userAuthProxy;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private StageRepository stageRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private MemberRepository memberRepository;

    @Override
    public User saveUser(User user) throws UserAlreadyExistsException {
        // add new user
        if (userRepository.findById(user.getEmailId()).isPresent()) {
            throw new UserAlreadyExistsException();
        }
        User savedUser = userRepository.save(user);
        if (!(savedUser.getEmailId().isEmpty())) {

            ResponseEntity response = userAuthProxy.saveUser(savedUser);
            System.out.println(response.getBody());
        }
        return savedUser;
    }



    @Override
    public Board createNewBoard(Board board, String emailId) throws InvalidCredentialsException, BoardAlreadyExistsException {
        //access current login user
        // create new board
        User user = userRepository.findById(emailId).get();
        if (user == null) {
            throw new InvalidCredentialsException();
        }
        List<Board> userBoard;
        if (user.getMyBoards() != null) {
            userBoard = user.getMyBoards();
        } else {
            userBoard = new ArrayList<>();
        }
        boolean boardIsPresent = false;
        for (Board b : userBoard) {
            if (b.getBoardName().equals(board.getBoardName())) {
                boardIsPresent = true;
            }
        }
        if (boardIsPresent) {
            throw new BoardAlreadyExistsException();
        }
        userBoard.add(board);
        user.setMyBoards(userBoard);
        userRepository.save(user);
        return boardRepository.save(board);
    }

    @Override
    public Stage addStagesIntoBoard(String boardName, Stage stage, String emailId) throws InvalidCredentialsException, StageAlreadyExistException, BoardNotFoundException {
        // access current login user
        // access the board of that user
        // add stages into it
        User user = userRepository.findById(emailId).get();
        if (user == null) {
            throw new InvalidCredentialsException();
        }
        List<Board> userBoard = user.getMyBoards();
        if (userBoard == null) {
            throw new BoardNotFoundException();
        }

        boolean stageIsPresent = false;
        for (Board ub : userBoard) {
            if (ub.getBoardName().equals(boardName)) {
                List<Stage> userBoardStages;
                if (ub.getMyStages() != null) {
                    userBoardStages = ub.getMyStages();
                    for (Stage ubs : userBoardStages) {
                        if (ubs.getStageName().equals(stage.getStageName())) {
                            stageIsPresent = true;
                        }
                    }
                    if (stageIsPresent) {
                        throw new StageAlreadyExistException();
                    }
                } else {
                    userBoardStages = new ArrayList<>();
                }

                userBoardStages.add(stage);
                ub.setMyStages(userBoardStages);
                boardRepository.save(ub);
            }

        }
        user.setMyBoards(userBoard);
        userRepository.save(user);


        return stageRepository.save(stage);
    }

    @Override
    public Member addMembersInBoard(String boardName, Member member, String emailId) throws InvalidCredentialsException, BoardNotFoundException, MemberAlreadyExistException {
        // access current login user
        // access the board of that user
        // add members into it
        User user = userRepository.findById(emailId).get();
        if (user == null) {
            throw new InvalidCredentialsException();
        }
        List<Board> userBoard = user.getMyBoards();
        if (userBoard == null) {
            throw new BoardNotFoundException();
        }
        for (Board ub : userBoard) {
            if (ub.getBoardName().equals(boardName)) {
                List<Member> boardMembers;
                boolean memberIsPresent = false;
                if (ub.getBoardMembers() != null) {
                    boardMembers = ub.getBoardMembers();
                    for (Member bm : boardMembers) {
                        if (bm.getMemberEmailId().equals(member.getMemberEmailId())) {
                            memberIsPresent = true;
                        }
                    }
                    if (memberIsPresent) {
                        throw new MemberAlreadyExistException();
                    }
                } else {
                    boardMembers = new ArrayList<>();
                }
                boardMembers.add(member);
                ub.setBoardMembers(boardMembers);
                boardRepository.save(ub);
            }

        }

        user.setMyBoards(userBoard);
        userRepository.save(user);
        return memberRepository.save(member);
    }

    @Override
    public Task createNewTask(String boardName, String stageName, Task task, String emailId) throws InvalidCredentialsException, BoardNotFoundException, StageNotFoundException, TaskAlreadyExistException {
        // access current login user
        // access the board of that user
        // for the first time :
        // access first stage using stage name and add the task into it

        User user = userRepository.findById(emailId).get();
        if (user == null) {
            throw new InvalidCredentialsException();
        }
        List<Board> userBoard = user.getMyBoards();
        if (userBoard == null) {
            throw new BoardNotFoundException();
        }
        Board targetBoard = null;
        for (Board board : userBoard) {
            if (board.getBoardName().equals(boardName)) {
                targetBoard = board;
                break;
            }
        }
        if (targetBoard == null) {
            throw new BoardNotFoundException();
        }
        List<Stage> stageList = targetBoard.getMyStages();
        if (stageList == null) {
            throw new StageNotFoundException();
        }
        Stage targetStage = null;
        for (Stage stage : stageList) {
            if (stage.getStageName().equals(stageName)) {
                targetStage = stage;
                break;
            }
        }
        List<Task> taskList = targetStage.getMyTasks();
        if (taskList == null) {
            taskList = new ArrayList<>();

        }
        taskList.add(task);
        targetStage.setMyTasks(taskList);
        targetBoard.setMyStages(stageList);
        boardRepository.save(targetBoard);
        userRepository.save(user);
        stageRepository.saveAll(stageList);
        return taskRepository.save(task);

    }

    @Override
    public User moveTaskToAnotherStage(String emailId, String boardName, String fromStage, String toStage, String taskId) throws InvalidCredentialsException, BoardNotFoundException, TaskNotFoundException, StageNotFoundException {


        User user = userRepository.findById(emailId).get();
        if (user == null) {
            throw new InvalidCredentialsException();
        }

        List<Board> userBoards = user.getMyBoards();
        if (userBoards == null || userBoards.isEmpty()) {
            throw new BoardNotFoundException();
        }

        Board targetBoard = null;
        for (Board board : userBoards) {
            if (board.getBoardName().equals(boardName)) {
                targetBoard = board;
                break;
            }
        }
        if (targetBoard == null) {
            throw new BoardNotFoundException();
        }

        List<Stage> targetBoardStages = targetBoard.getMyStages();
        if (targetBoardStages == null) {
            targetBoardStages = new ArrayList<>();
        }

        Stage currentStage = null;
        for (Stage stage : targetBoardStages) {
            if (stage.getStageName().equals(fromStage)) {
                currentStage = stage;
                break;
            }
        }
        if (currentStage == null) {
            throw new StageNotFoundException();
        }

        Stage targetStage = null;
        for (Stage stage : targetBoardStages) {
            if (stage.getStageName().equals(toStage)) {
                targetStage = stage;
                break;
            }
        }
        if (targetStage == null) {
            throw new StageNotFoundException();
        }

        Task taskToMove = null;
        List<Task> currentStageTasks = currentStage.getMyTasks();
        if (currentStageTasks != null) {
            for (Task task : currentStageTasks) {
                if (task.getTaskId().equals(taskId)) {
                    taskToMove = task;
                    break;
                }
            }
        }
        if (taskToMove == null) {
            throw new TaskNotFoundException();
        }

        List<Task> targetStageTasks = targetStage.getMyTasks();
        if (targetStageTasks == null) {
            targetStageTasks = new ArrayList<>();
        }
        targetStageTasks.add(taskToMove);
        targetStage.setMyTasks(targetStageTasks);

        List<Task> updatedCurrentStageTasks = new ArrayList<>();
        if (currentStageTasks != null) {
            for (Task task : currentStageTasks) {
                if (!task.getTaskId().equals(taskId)) {
                    updatedCurrentStageTasks.add(task);
                }
            }
        }
        currentStage.setMyTasks(updatedCurrentStageTasks);

        List<Stage> updatedStages = new ArrayList<>();
        for (Stage stage : targetBoard.getMyStages()) {
            if (!stage.getStageName().equals(fromStage) && !stage.getStageName().equals(toStage)) {
                updatedStages.add(stage);
            }
        }
        updatedStages.add(currentStage);
        updatedStages.add(targetStage);

        targetBoard.setMyStages(updatedStages);

        boardRepository.save(targetBoard);
        stageRepository.saveAll(updatedStages);

        return userRepository.save(user);

    }


    @Override
    public Board updateBoard(String emailId, String boardName, Board newBoardName) throws InvalidCredentialsException, BoardNotFoundException {

        // Retrieve the user from the userRepository
        System.out.println("Updating board: emailId=" + emailId + ", boardName=" + boardName + ", newBoardName=" + newBoardName.getBoardName());

        // Retrieve the user from the userRepository
        User user = userRepository.findById(emailId).get();

        // Check if the user exists
        if (user == null) {
            throw new InvalidCredentialsException();
        }

        // Retrieve the list of boards for the user
        List<Board> boardList = user.getMyBoards();

        System.out.println(boardList);

        // Check if the boardList is null (assuming it can be null)
        if (boardList == null) {
            throw new BoardNotFoundException();
        }

        Board updatedBoard = null;
        // Iterate through the user's boards to find the board to update
        for (Board board : boardList) {
            if (board.getBoardName().equals(boardName)) {
                System.out.println("Updating board name from '" + boardName + "' to '" + newBoardName.getBoardName() + "'");
                board.setBoardName(newBoardName.getBoardName());
                updatedBoard = board;
                break;
            }
        }

        System.out.println("updatedBoard :: " + updatedBoard);
        //  Set the updated list of boards to the user
        user.setMyBoards(boardList);

        // Save the changes to the user in the userRepository
        userRepository.save(user);

        // Check if an update was performed
        if (updatedBoard != null) {
            // Save the updated board in the boardRepository
            boardRepository.deleteById(boardName);
            Board savedBoard = boardRepository.save(updatedBoard);
            return savedBoard;
        } else {
            // If no board was updated, throw a BoardNotFoundException
            throw new BoardNotFoundException();
        }
    }

    @Override
    public boolean deleteBoard(String emailId, String boardName) throws InvalidCredentialsException, BoardNotFoundException {

        // Check for valid user
        User user = userRepository.findById(emailId).get();
        if (user == null) {
            throw new InvalidCredentialsException();
        }
        List<Board> userBoards = user.getMyBoards();
        if (userBoards == null) {
            throw new BoardNotFoundException();
        }

        boolean boardDeleted = false;
        Board targetBoard = null;

        Iterator<Board> iterator = userBoards.iterator();
        while (iterator.hasNext()) {
            Board board = iterator.next();
            if (board.getBoardName().equals(boardName)) {
                targetBoard = board;
                iterator.remove();
                boardDeleted = true;
                break;
            }
        }

        if (targetBoard == null) {
            throw new BoardNotFoundException();
        }

        // Save the changes
        boardRepository.delete(targetBoard);
        userRepository.save(user);
        return boardDeleted;
    }

    @Override
    public Task updateTask(String emailId, String boardName, String stageName, Task newTaskData) throws InvalidCredentialsException, TaskNotFoundException, BoardNotFoundException, StageNotFoundException {

        User user = userRepository.findById(emailId).get();
        if (user == null) {
            throw new InvalidCredentialsException();
        }
        for (Board board : user.getMyBoards()) {
            if (board == null) {
                throw new BoardNotFoundException();
            }
            if (board.getBoardName().equals(boardName)) {
                for (Stage stage : board.getMyStages()) {
                    if (stage == null) {
                        throw new StageNotFoundException();
                    }
                    if (stage.getStageName().equals(stageName)) {

                        for (Task task : stage.getMyTasks()) {
                            if (task.getTaskId().equals(newTaskData.getTaskId())) {
                                // Update task data
                                if (newTaskData.getTaskName() != null) {
                                    task.setTaskName(newTaskData.getTaskName());
                                }
                                if (newTaskData.getPriority() != null) {
                                    task.setPriority(newTaskData.getPriority());
                                }
//                             

                                //task.setDescription(newTaskData.getDescription());


                                // Add other fields as needed

                                userRepository.save(user);
                                boardRepository.save(board);
                                stageRepository.save(stage);

                                return task;
                            }
                        }
                    }
                }
            }
        }

        throw new TaskNotFoundException();
    }

    @Override
    public boolean deleteTask(String emailId, String boardName, String stageName, String taskId) throws TaskNotFoundException, InvalidCredentialsException, BoardNotFoundException, StageNotFoundException {
        Optional<User> userOptional = userRepository.findById(emailId);
        if (userOptional.isEmpty()) {
            throw new InvalidCredentialsException();
        }
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            for (Board board : user.getMyBoards()) {
                if (board == null) {
                    throw new BoardNotFoundException();
                }
                if (board.getBoardName().equals(boardName)) {
                    for (Stage stage : board.getMyStages()) {
                        if (stage == null) {
                            throw new StageNotFoundException();
                        }
                        if (stage.getStageName().equals(stageName)) {
                            List<Task> tasks = stage.getMyTasks();
                            Iterator<Task> iterator = tasks.iterator();

                            while (iterator.hasNext()) {
                                Task task = iterator.next();
                                if (task.getTaskId().equals(taskId)) {
                                    iterator.remove();

                                    // Save changes to the database
                                    userRepository.save(user);
                                    boardRepository.save(board);
                                    stageRepository.save(stage);

                                    // Delete the task from TaskRepository
                                    taskRepository.deleteById(taskId);

                                    return true; // Task deleted successfully
                                }
                            }
                        }
                    }
                }
            }
        }
        throw new TaskNotFoundException();
        // Task not found or deletion failed

    }


    @Override
    public User updateUser(String emailId ,User user) throws InvalidCredentialsException {

        User existingUser = userRepository.findById(emailId).get();

        if (existingUser == null) {
            throw new InvalidCredentialsException();
        }
        // Copy the non-null properties from updatedUserData to existingUser
        BeanUtils.copyProperties(user, existingUser, "emailId","password","myBoards");

        // Save the updated user
        return userRepository.save(existingUser);
    }



    @Override
    public Task addMemberToTask(String emailId, String boardName, String stageName, String taskId,Member member) throws Exception {
//        User user = userRepository.findById(emailId).get();
//        if (user == null){
//            throw new InvalidCredentialsException();
//        }
//        List<Board> userBoard = user.getMyBoards();
//        if (userBoard==null){
//            throw new BoardNotFoundException();
//        }
//        for (Board b:userBoard){
//            if (b.getBoardName().equals(boardName)){
//                List<Stage> userStages = b.getMyStages();
//                if (userStages==null){
//                    throw new StageNotFoundException();
//                }
//                for (Stage s : userStages){
//                    if (s.getStageName().equals(stageName)){
//                        List<Task> userTask = s.getMyTasks();
//                        if (userTask==null){
//                            throw new TaskNotFoundException();
//                        }
//                        for (Task t : userTask){
//                            if (t.getTaskId().equals(taskId)){
//                                if (t.getMyMembers().getMemberEmailId().equals(member.getMemberEmailId())){
//                                    throw new MemberAlreadyExistException();
//                                }
//                                t.setMyMembers(member);
//                                userRepository.save(user);
//                                boardRepository.save(b);
//                                stageRepository.save(s);
//                                taskRepository.save(t);
//                                return t;
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        throw new Exception();
        return null;
    }
    @Override
    public Member addTaskToMemeber(String emailId, String memberEmailId, String taskId, String boardName) throws Exception{
        User user = userRepository.findById(emailId).get();
        if (user == null){
            throw new InvalidCredentialsException();
        }
        List<Board> userBoard = user.getMyBoards();
        if (userBoard==null){
            throw new BoardNotFoundException();
        }

        for (Board board: userBoard){
            List<Member> myMembers= board.getBoardMembers();
            Iterator<Member> iterator = myMembers.iterator();
            while (iterator.hasNext()){
                Member targetMember = iterator.next();
                if (targetMember.getMemberEmailId().equals(memberEmailId)){

                    if (targetMember.getMyTask() == null){
                        targetMember.setMyTask(new ArrayList<>());
                    }
                    Task task = taskRepository.findById(taskId).get();
                    if (task == null){
                        throw new TaskNotFoundException();
                    }
                    if (targetMember.getMyTask().size() <=2){
                        targetMember.getMyTask().add(task);
                    }else {
                        throw new TaskLimitExceedException();
                    }


                    memberRepository.save(targetMember);
                }
            }
            board.setBoardMembers(myMembers);
            boardRepository.save(board);
        }
        user.setMyBoards(userBoard);
        userRepository.save(user);
        return memberRepository.findById(memberEmailId).get();
    }


    @Override
    public User getCompleteDataOfUser(String emailId) {
        return userRepository.findById(emailId).get();
    }


}
