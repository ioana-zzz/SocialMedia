package org.example.service;

import org.example.domain.Friendship;
import org.example.domain.Message;
import org.example.domain.Utilizator;
import org.example.domain.validators.FriendshipValidator;
import org.example.domain.validators.MessageValidator;
import org.example.domain.validators.UtilizatorValidator;
import org.example.paging.Page;
import org.example.paging.Pageable;
import org.example.repository.database.FriendshipDBRepository;
import org.example.repository.database.MessageDBRepository;
import org.example.repository.database.UtilizatorDBRepository;

import org.example.domain.validators.ValidationException;
import org.example.utils.events.FriendshipChangeEvent;
import org.example.utils.observable.*;
import org.example.utils.observable.Observable;
import org.example.utils.observable.Observer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Service  {
    private final UtilizatorDBRepository userRepo = new UtilizatorDBRepository(new UtilizatorValidator(), "jdbc:postgresql://localhost:5432/social_network","postgres","ioana");
    private final FriendshipDBRepository friendshipRepo = new FriendshipDBRepository(new FriendshipValidator(), "jdbc:postgresql://localhost:5432/social_network","postgres","ioana");

    private Utilizator crtUser;
    private static Service instance = null;
    private static final BiPredicate<String, Friendship> isFriend = (id, fr) -> fr.getId().contains(id) && fr.getStatus().equals("accepted");
    private static final BiPredicate<String, Friendship> isPending = (id, fr) -> fr.getId2().equals(id) && fr.getStatus().equals("pending");

    private final MessageDBRepository messageRepo = new MessageDBRepository(new MessageValidator(), userRepo,"jdbc:postgresql://localhost:5432/social_network","postgres","ioana");

    private Service() {

    }

    public static Service getInstance(){

        if(instance== null)
            instance = new Service();
        return instance;
    }

    public void createAccount(String id, String firstName, String lastName, String password) throws ValidationException, ServiceException {
        Optional<Utilizator> existingUser = userRepo.findOne(id);
        if (existingUser.isPresent()) {
            throw new ServiceException("User with ID already exists. Choose another username");
        }
        var passd = Base64.getEncoder().encodeToString(password.getBytes());
        Utilizator user = new Utilizator(id, firstName, lastName,passd ,LocalDateTime.now(),null);
        userRepo.save(user);
    }

    public void login(String id, String password) throws ServiceException {
        Optional<Utilizator> user = userRepo.findOne(id);
        if (user.isEmpty()) {
            throw new ServiceException("User not found.");
        }
        var usrpassd = new String(Base64.getDecoder().decode(user.get().getPassword()));

        if (!usrpassd.equals(password)) {
            throw new ServiceException("Invalid password.");
        }

       crtUser = user.get();
    }

//    public void logout() {
//
//        if(crtUser != null)
//            crtUser = null;
//        else
//            throw new ServiceException("No user logged in.");
//    }

    public void addFriend(String friendId) throws ServiceException {
        if (crtUser == null) {
            throw new ServiceException("No user logged in.");
        }
        /*Optional<Utilizator> friend = userRepo.findOne(friendId);
        if (friend.isEmpty()) {
            throw new ServiceException("Friend not found.");
        }*/
        Utilizator friend = userRepo.findOne(friendId).orElseThrow(() -> new ServiceException("User not found."));

        String friendshipId = crtUser.getId() + "+" + friend.getId();
        if (friendshipRepo.findOne(friendshipId).isPresent()) {
            throw new ServiceException("Friendship already exists.");
        }

        friendshipId = friend.getId() + "+" + crtUser.getId();
        if (friendshipRepo.findOne(friendshipId).isPresent()) {
            throw new ServiceException("Friendship already exists.");
        }

        Friendship fr = new Friendship(crtUser.getId(), friend.getId(), "pending", LocalDateTime.now());
        try {
            friendshipRepo.save(fr);
        }catch (ValidationException e){
            throw new ServiceException(e.getMessage());
        }
    }

    public void acceptFriend(String friendId) throws ServiceException {
        if (crtUser == null) {
            throw new ServiceException("No user logged in.");
        }
        String friendshipId = friendId + "+" + crtUser.getId();
        Optional<Friendship> fr = friendshipRepo.findOne(friendshipId);
        if (fr.isEmpty()) {
            throw new ServiceException("Friendship not found or invalid.");
        }
        fr.get().setStatus("accepted");
        fr.get().setFriendsFrom(LocalDateTime.now());
        friendshipRepo.update(fr.get());


    }

    public void rejectFriend( String friendId) throws ServiceException {
        if (crtUser == null) {
            throw new ServiceException("No user logged in.");
        }
        String friendshipId = friendId + "+" + crtUser.getId();
        Optional<Friendship> fr = friendshipRepo.findOne(friendshipId);
        if (fr.isEmpty()) {
            throw new ServiceException("Friendship not found or invalid.");
        }
        friendshipRepo.delete(friendshipId);
    }

    public void deleteAccount() throws ServiceException {
        if (crtUser == null) {
            throw new ServiceException("No user logged in.");
        }
        userRepo.delete(crtUser.getId());

        crtUser = null;
    }

    public void deleteFriend(String friendId) throws ServiceException {
        if (crtUser == null) {
            throw new ServiceException("No user logged in.");
        }
        String friendshipId = crtUser.getId() + "+" + friendId;
        String friendshipId2 = friendId + "+" + crtUser.getId();
        Optional<Friendship> fr = friendshipRepo.findOne(friendshipId);
        Optional<Friendship> fr2 = friendshipRepo.findOne(friendshipId2);
        if (fr.isEmpty() && fr2.isEmpty()) {
            throw new ServiceException("Friendship not found.");
        }
        if (fr.isPresent()) {
            friendshipRepo.delete(friendshipId);
        }
        else  {
            friendshipRepo.delete(friendshipId2);
        }

    }

    public List<Utilizator> getFriends() {
        if (crtUser == null) {
            throw new ServiceException("No user logged in.");
        }


        Iterable<Friendship> friendships = friendshipRepo.findAll();
        List<Utilizator> friends = new ArrayList<>();
        friendships.forEach(fr -> {
            if(isFriend.test(crtUser.getId(), fr))
            {
                Optional<Utilizator> friend = fr.getId1().equals(crtUser.getId()) ? userRepo.findOne(fr.getId2()) : userRepo.findOne(fr.getId1());
                friends.add(friend.get());
            }

        });

        return friends;
    }


    public List<Utilizator> getFriendRequests() {
        if (crtUser == null) {
            throw new ServiceException("No user logged in.");
        }

        Iterable<Friendship> friendships = friendshipRepo.findAll();
        List<Utilizator> friends = new ArrayList<>();
        friendships.forEach(fr -> {
            if (isPending.test(crtUser.getId(), fr)) {
                Optional<Utilizator> friend = userRepo.findOne(fr.getId1());
                friends.add(friend.get());
            }
        });

        return friends;
    }

    public Utilizator getCrtUser() {
        return crtUser;
    }


    public List<Set<Utilizator>> findConnectedComponents() {
        Map<String, Set<String>> adjacencyList = buildAdjacencyList();
        Set<String> visited = new HashSet<>();
        List<Set<Utilizator>> connectedComponents = new ArrayList<>();

        adjacencyList.keySet().stream().filter(userId -> !visited.contains(userId)).forEach(userId -> {
            Set<Utilizator> component = new HashSet<>();
            dfs(userId, visited, adjacencyList, component);
            connectedComponents.add(component);
        });

        return connectedComponents;
    }

    private Map<String, Set<String>> buildAdjacencyList() {
        Map<String, Set<String>> adjacencyList = new HashMap<>();
        List<Friendship> friendships = new ArrayList<>();
        friendshipRepo.findAll().forEach(fr -> {
            if (fr.getStatus().equals("accepted"))
                friendships.add(fr);
        });

        friendships.forEach(fr -> {
            adjacencyList.putIfAbsent(fr.getId1(), new HashSet<>());
            adjacencyList.putIfAbsent(fr.getId2(), new HashSet<>());
            adjacencyList.get(fr.getId1()).add(fr.getId2());
            adjacencyList.get(fr.getId2()).add(fr.getId1());
        });

        return adjacencyList;
    }

    private void dfs(String userId, Set<String> visited, Map<String, Set<String>> adjacencyList, Set<Utilizator> component) {
        visited.add(userId);
        Optional<Utilizator> user = userRepo.findOne(userId);
        component.add(user.get());

        adjacencyList.get(userId).stream().filter(neighbor -> !visited.contains(neighbor)).forEach(neighbor -> dfs(neighbor, visited, adjacencyList, component));
    }

    public Set<Utilizator> findComponentWithLongestPath() {
        List<Set<Utilizator>> connectedComponents = findConnectedComponents();

        AtomicInteger maxLength = new AtomicInteger(0);
        AtomicReference<Set<Utilizator>> componentWithLongestPath = new AtomicReference<>(new HashSet<>());

        connectedComponents.forEach(component -> {
            int longestPathLength = findLongestPathInComponent(component);
            if (longestPathLength > maxLength.get()) {
                maxLength.set(longestPathLength);
                componentWithLongestPath.set(component);
            }
        });

        return componentWithLongestPath.get();
    }

    private int findLongestPathInComponent(Set<Utilizator> component) {
        AtomicInteger maxLength = new AtomicInteger(0);

        component.forEach(user -> {
            Set<String> visited = new HashSet<>();
            maxLength.set(Math.max(maxLength.get(), dfsLongestPath(user.getId(), visited)));
        });
        return maxLength.get();
    }

    private int dfsLongestPath(String userId, Set<String> visited) {
        visited.add(userId);
        AtomicInteger maxLength = new AtomicInteger(0);

        buildAdjacencyList().get(userId).forEach(neighbor -> {
            if (!visited.contains(neighbor)) {
                maxLength.set(Math.max(maxLength.get(), 1 + dfsLongestPath(neighbor, visited)));
            }
        });

        visited.remove(userId);
        return maxLength.get();
    }


    public Optional<Utilizator> searchUserByName(String name) {
        List<Utilizator> users = new ArrayList<>();
        userRepo.findAll().forEach(users::add);
        for (Utilizator user : users) {
            if ((user.getFirstName().strip() + " " + user.getLastName().strip()).equals(name)) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }


    public List<Utilizator> getAllUsers() {
        List<Utilizator> users = new ArrayList<>();
        userRepo.findAll().forEach(users::add);
        return users;
    }


    public Optional<Utilizator> getUserById(String id) {
        return userRepo.findOne(id);}


    public List<String> getFriendsInfo() {
        if (crtUser == null) {
            throw new ServiceException("No user logged in.");
        }

        List<String> friendsInfo = new ArrayList<>();
        for (Friendship friendship : friendshipRepo.findAll()) {
            if (isFriend.test(crtUser.getId(), friendship)) {
                Utilizator friend = friendship.getId1().equals(crtUser.getId())
                        ? userRepo.findOne(friendship.getId2()).orElseThrow()
                        : userRepo.findOne(friendship.getId1()).orElseThrow();
                String friendInfo = friend.getFirstName() + " " + friend.getLastName() +
                        " - Friends since: " + friendship.getFriendsFrom().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                friendsInfo.add(friendInfo);
            }
        }
        return friendsInfo;
    }

    public List<String> getFriendRequestsInfo() {
        if (crtUser == null) {
            throw new ServiceException("No user logged in.");
        }

        List<String> friendRequestsInfo = new ArrayList<>();
        for (Friendship friendship : friendshipRepo.findAll()) {
            if (isPending.test(crtUser.getId(), friendship)) {
                Utilizator requester = userRepo.findOne(friendship.getId1()).orElseThrow();
                String requestInfo = requester.getFirstName() + " " + requester.getLastName() +
                        " - Request sent on: " + friendship.getFriendsFrom().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                friendRequestsInfo.add(requestInfo);
            }
        }
        return friendRequestsInfo;
    }


    public long countFriendRequestsSinceLastSeen() {
        if (crtUser == null) {
            throw new ServiceException("No user logged in.");
        }

        LocalDateTime lastSeen = crtUser.getLastSeen();
        return friendshipRepo.findAll().stream()
                .filter(friendship -> friendship.getId2().equals(crtUser.getId()) && friendship.getFriendsFrom().isAfter(lastSeen))
                .count();
    }



    public void sendMessage(String from, List<String> to, String messageContent) throws ServiceException {

        Utilizator user = userRepo.findOne(from).orElseThrow(() -> new ServiceException("Sender not found."));
        for (String recipient : to) {
            if (!userRepo.findOne(recipient).isPresent()) {
                throw new ServiceException("Recipient " + recipient + " not found.");
            }
        }

        Message message = new Message(
                user,
                to.stream().map(userRepo::findOne).map(Optional::get).collect(Collectors.toList()),
                messageContent,
                LocalDateTime.now()
        );

        messageRepo.save(message);


    }


    public List<Message> getConversation(String user1, String user2) {
        return messageRepo.findAll().stream()
                .filter(msg -> msg.getFrom().getId().equals(user1) && msg.getTo().get(0).getId().equals(user2) ||
                        msg.getFrom().getId().equals(user2) && msg.getTo().get(0).getId().equals(user1))
                .sorted(Comparator.comparing(Message::getDate))
                .collect(Collectors.toList());
    }

    public Map<String, List<Message>> getAllConversations(String userId) {
        return messageRepo.findAll().stream()
                .filter(msg -> msg.getFrom().getId().equals(userId) || msg.getTo().get(0).getId().contains(userId))
                .collect(Collectors.groupingBy(msg -> {
                    if (msg.getFrom().getId().equals(userId)) {
                        return msg.getTo().get(0).getId();
                    } else {
                        return msg.getFrom().getId();
                    }
                }));
    }



    public Optional<Utilizator> findOne(String id) {
        return userRepo.findOne(id);
    }

    public void setLastSeen(){
        if(crtUser != null){
            userRepo.setLastSeen(LocalDateTime.now(),crtUser);
        }
    }


    public Page<Friendship> findAllFriendsOnPage(Pageable pageable) {
        return friendshipRepo.findAllFriendshipsOnPage(getCrtUser().getId(),pageable );
    }

    public Page<Friendship> findAllFriendRequestsOnPage(Pageable pageable) {
        return friendshipRepo.findAllFriendRequestsOnPage( getCrtUser().getId(),pageable );
    }

   public String friendshipToString(Friendship fr){
        String id = fr.getId1().equals(crtUser.getId()) ? fr.getId2() : fr.getId1();
        Utilizator friend = userRepo.findOne(id).orElseThrow();
        return friend.getFirstName() + " " + friend.getLastName() + " - Friends since: " + fr.getFriendsFrom().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

   }

   public String friendRequestToString(Friendship fr)
   {
         Utilizator requester = userRepo.findOne(fr.getId1()).orElseThrow();
         return requester.getFirstName() + " " + requester.getLastName() + " - Request sent on: " + fr.getFriendsFrom().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
   }

}




