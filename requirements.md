# Software Requirements

## Vision

### Project Vision: 
The vision of this app is to create a fun interactive mulitplayer game. A user will be able to play for a team of either bounty hunters or outlaws and recieve a notification when the enemy is near!

### Pain Points:
This app is fun-for-all, providing entertainment and bonding to large diverse groups of people. The only requirement is that you have an android device.

### Why It Matters:
This app will keep you on your toes! At any point during your mundane, monotonous day you could get a notification that an enemy is near! Providing you with a quick jolt of adrenaline and fun.

## Scope (In/Out)
### IN - 
- This app will allow users to create an account and join an active game.
- Users locations will be tracked and stored in the database.
- A user will recieve a notification when a user of the opposite team is near.
- A user can press a button when the notification is triggered.
- A user can see how many members of either team are active.


### OUT - 
- This app will not give users directions.
- This app will not act as a tracking or location sharing service.

### Minimum Viable Product
What will your MVP functionality be?
- A user can log in.
- A user is assignmed to a team.
- A user will recieve feedback when an enemy is near.


What are your stretch goals?
- A user will get a notification when a member of the opposite team is near.
- A user can press a button to eliminate the enemy team member.
- Create game sessions.
- User team is tied to the game session not the user.
- Add sound effects when a user is eliminated.
- Custom animation.
- Allow team members to resuscitate recenlty eliminated team members.
- Add a sheriff character that when eliminated broadcasts the location of the player who eliminated them.



## Stretch
What stretch goals are you going to aim for?
- A user will get a notification when a member of the opposite team is near.
- A user can press a button to eliminate the enemy team member.
- Create game sessions.
- User team is randomly re-assigned every new game session.
- Add sound effects when you win!


## Functional Requirements
- A user can create an account.
- A user is automatically assigned to a team.
- A user will recieve a notification when a member of the opposite team comes within a certain distance.
- Both users can press a button to try and eliminate the other and recieve meaningful feedback when they are sucessful/unsuccessful.


### Data Flow
When a user first opens the app they will be prompted to create an account or sign in. Upon creating an account they will be assigned to either the outlaw or the bounty hunter team. The app will then run quietly in the background until an enemy from the opposite team is near. When an emeny is 'sighted' the user will recieve a notification that a member of the opposite team is near, and be prompted to push a button to eliminate them. If they push the button first they will see feedback indicating their victory! Else, they will recieve feedback of their devestating defeat.


## Non-Functional Requirements
Security - Users are able to create an account which stores their unique data. We will do this by utilizing Firebase Authentication. Upon account creation the user will be assigned to a team.

Usability - A user will be able to log in, they will recieve a notification when an enemy is near and they will be able to respond by pressing a button. If the enemy presses the button first they will recieve meaningful feedback informing them.

