# cs346-proj

## Goal
This is an app that allows teachers to create iClicker quizzes and allows students to answer questions to the open quizzes.


## Quick-Start Instructions
Clone this repository using the HTTPS link.
Open the app on IntelliJ and then use the play button to run the application.

## Team Members
Vipasha Gupta
Prasanna Thallapalli
Tracy Dong
Sineha Manivannan
Saikrishna Tadepalli

## Tasks completed for Sprint 1
iClicker App:
- Created the User, Quiz, Question and Lecture models
- User model supports both students and teacher types
- Created APIs for 
   CRUD operations for Quizzes, questions
   User signup, sign in and authentication 
   Getting the answers and the responses for each question in a given quiz

## Tasks completed for Sprint 2
- Created the selection model
- Created the frontend interface for student view (navigation between screens)
- Created the API responses for respective API calls to display data (sign up, log in, view classes, view quizzes, view questions)
- Created additional backend APIs (supporting user fields, extending model to support class sections)

## Tasks completed for Sprint 3
- Refactored backend APIs completely to allow for integrability and seamless design
- Added remaining backend APIs for the functionality of the app to be close to ready for use in practical settings
- Added ability to see grades/stats visually, so that students are able to effectively understand their results, better impacting their educational journey
- Manually tested backend and created a testing infrastructure with tests for various possible request bodies of an API
- Tested all checking within APIs
- Simplified existing tests to easily be changed based on the database values
- Created a testing database for sandbox testing
- Completed the frontend interface for student view (navigation between screens)
- Completed majority of remaining frontend APIs
- Created additional backend APIs, such as the ability to get responses from questions and quizzes, and tabulate scores
- Modified backend APIs based on broken tests
- Developed quiz UI
- Added class section join code UI for professors
- Seperated student and professor UI
- Add radiobutton for student/teacher when creating a new account
- Delete question and delete quiz abilties introduced
- Closed quizzes are greyed out
- Search functionaility for classes and quizzes UI
- Created quiz UI
- Change quiz state for teachers is now possible (only teachers)

## Tasks completed for Sprint 4
- Completed thorough unit testing of the backend (utilized mocking)
- Established a name for our app (WatClicker)
- Created a logo, brand banner and integrated to the frontend
- Modified UI style (cursor change on hover, new buttons, main page + class page changes, animation to log in)
- Explored websockets for real-time updating from the server
- Implemented polling (calling APIs at regular intervals) for real-time updating
- Added error checking on the frontend (error texts, pop ups)
- Main frontend UI file was restructured into several files for readability
- Editing a class, quiz, question were completed
- Completed API for editing a student's selection
- Fixed bugs and reamining features to be implemented from previous sprint

## Tasks completed for Final submission
- Added grades API 
- Fixed bugs
- Hosted backend
- Added descriptive error and success messages for API
- Fixed API for edit question such that existing student responses are not removed when answer changes

## Project Documents
Project proposal: https://git.uwaterloo.ca/stadepal/cs346-proj/-/wikis/Project-Proposal

Requirements: https://git.uwaterloo.ca/stadepal/cs346-proj/-/wikis/Requirements

Design: https://git.uwaterloo.ca/stadepal/cs346-proj/-/wikis/Design

Discussion: https://git.uwaterloo.ca/stadepal/cs346-proj/-/wikis/Discussion

## Software Releases
Release for sprint 1: https://git.uwaterloo.ca/stadepal/cs346-proj/-/releases/Sprint1

Release for Sprint 2: https://git.uwaterloo.ca/stadepal/cs346-proj/-/releases/Sprint2

Release for Sprint 3: https://git.uwaterloo.ca/stadepal/cs346-proj/-/releases/Sprint3

Relase for Sprint 4: https://git.uwaterloo.ca/stadepal/cs346-proj/-/releases/Sprint4

Release for Final submission: https://git.uwaterloo.ca/stadepal/cs346-proj/-/releases/finalSubmission

## Link to frontend repository
https://git.uwaterloo.ca/v56gupta1/cs346-frontend

## Link to QA spreadsheet
https://docs.google.com/spreadsheets/d/1ASiCsnciWGrWTTPVmqpHGknBot4UQsgAq8k23vmPzrk/edit?usp=sharing

## License
For open source projects, say how it is licensed.
