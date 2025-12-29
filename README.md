# EcoWorld – Fire Rescue Math Game

## Project Overview
EcoWorld is an educational 2D game developed using Java and Swing.
The player controls a rescue plane and tries to save buildings and trees from fire
by solving basic math problems.

The game combines simple mathematics with cause-and-effect gameplay
to make learning fun and interactive for primary school students.

---

## Educational Purpose
The main goal of EcoWorld is to teach:
- Basic addition and subtraction
- Logical thinking
- Cause and effect relationships

Correct answers help extinguish fires,
while wrong answers make fires grow and cause destruction.

---

## Gameplay Mechanics
- The player controls a plane using the mouse.
- Fires appear randomly on buildings and trees.
- Each fire displays a math question.
- Water orbs with numbers fall from the sky.
- The player catches a water orb and drops it onto a fire.
- If the number matches the correct answer, the fire is extinguished.
- If the answer is wrong, the fire grows stronger.

The game ends when:
- The player reaches the target score (win), or
- Too many buildings are destroyed (lose).

---

## Scoring and Difficulty
- Each correct answer increases the score.
- Fires grow step by step instead of instantly.
- Buildings collapse only after reaching the highest fire level.
- The game allows mistakes but punishes repeated wrong answers.

This system keeps the game balanced and suitable for children.

---

## Technical Structure

### Main Class
- **Ecoworld**
  - Controls the entire game.
  - Manages the game loop, drawing, input handling, and rules.
  - Uses `JPanel`, `Timer`, `ActionListener`, and mouse listeners.

### Game Objects
- **Plane**
  - Represents the player.
  - Follows mouse movement.
  - Has a hitbox for collision detection.

- **GroundBlock**
  - Represents buildings and trees.
  - Can catch fire, grow fire, or be destroyed.
  - Generates math questions and stores correct answers.

- **WaterOrb**
  - Floating water bubbles with numbers.
  - Used to pick up answers.

- **WaterDrop**
  - Water dropped from the plane.
  - Interacts with fires on the ground.

---

## Game Loop and Logic
- A `Timer` updates the game every 20 milliseconds.
- The `actionPerformed` method:
  - Spawns fires and water orbs
  - Moves all game objects
  - Checks collisions
  - Updates score and game state

Collision detection is handled using:
- `getBounds()` and `intersects()` methods

---

## Challenges and Solutions
One of the main challenges was balancing the game difficulty for children.

Multiple systems work together at the same time:
- Fire growth
- Math questions
- Water spawning
- Scoring system

To solve this:
- Fires grow gradually instead of instantly.
- Buildings collapse only after multiple wrong answers.
- Correct answers immediately reward the player.

This solution made the game simple to understand but still challenging.

---

## Technologies Used
- Java
- Java Swing
- Timer (Game Loop)
- Mouse Events
- Object-Oriented Programming

---

## Future Improvements
- Add sound effects
- Add different difficulty levels
- Add multiplication and division questions
- Add more environments and visual elements

---

## Author
Selenay

