### 1. Link to your Public GitHub Repository
[https://github.com/Shivam07123/WorkforcemgmtApplication.git](https://github.com/Shivam07123/WorkforcemgmtApplication.git)


### How We'll Evaluate Your Work 📈

We'll be looking at your submission with these criteria in mind:

**✅ Project Setup:**  
The project is structured professionally using standard Spring Boot conventions, with clear package separation (`controller`, `service`, `dto`, `model`, etc.), use of Gradle as the build tool, and Java 17 compatibility.

**✅ Functionality:**  
- Fixed the issue where task reassignment created duplicate tasks by cancelling the old task before reassigning.
- Modified the task fetching logic to exclude cancelled tasks, keeping results clean and relevant.
- Enhanced the daily task view by fetching both current active tasks and older unfinished ones.
- Added full support for task priority (HIGH, MEDIUM, LOW) with endpoints to update and filter tasks based on priority.
- Implemented a full activity log and comment system for each task, with proper timestamps and chronological ordering.

**✅ Code Quality:**  
- Clean, modular, and well-organized codebase.
- Consistent use of DTOs, services, enums, and mappings using MapStruct.
- Logging and null safety practices in place.
- No hardcoded logic — everything driven by clean request/response models.

**✅ Problem Solving:**  
- Efficient use of `Map`, `List`, and filtering logic to simulate in-memory task management in the absence of a database.
- Smart filter design to identify overdue and pending tasks with date comparison.

**✅ Clarity:**  
*To be demonstrated in the video walkthrough.*

