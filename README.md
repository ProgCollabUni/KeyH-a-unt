# README
This project is made for a course at the University of Basel. We are coding a game called Key H(a)unted.
The game is a top down labyrinth in a dungeon. The map layout is grid-based. The ideal amount of players would be 4. There are 4 hunters and one ghost. The hunters goal is to escape the haunted house. The ghost wants to catch the hunters.
The players only see their room. 
The game is round based. Each round is 15 seconds long. In those 15 seconds, every player can enter one action. The player can either, enter the next room, open a chest or do nothing. If the timer runs out and the player did not make a move, the game assumes that the player wanted to wait. The chests are random and contain at least 1 key. Any one of the explorers can open the escape door at the center of the dungeon. If the door has been opened, the players can escape. 
The ghost can not open any chest. The ghost can go through walls, which means they do not have to have a door to go to the any adjacent room in any direction.

## Currently

Currently, the game is still under development. There will be weekly updates on the game and the documentation will also be updated regularly, due to it being necessary to pass the course. We use a custom gradle file for stylecheck. The game logic and GUI chat is done. The docs folder contains all the documentation, uptil milestone 3, of the project. The contents will also be reguraly updated. The instruction manual can also be found in the documentation.
