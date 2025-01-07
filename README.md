The **BucketList App** is a goal-tracking application that helps users capture and store their dreams, aspirations, and goals. It allows users to document details, reflections, and track progress toward their ambitions.

## Key Features 
- **DreamListFragment**: Displays a scrollable list summarizing all dreams.
- **DreamDetailFragment**: Allows users to view and update details of a selected dream.
- **Dream States**:
  - **Deferred**: Dreams that are on hold and not actively pursued.
  - **Fulfilled**: Dreams that are completed or achieved.
- **Reflections**: Enables recording intermediate progress or notes for each dream.
- **Single Activity Architecture**: Hosts two fragments (list and detail views), one at a time, forming a basic list-detail interface.

## Key Learnings
- **Dynamic UI Implementation**: Managing user interfaces with activities, fragments, intents, linear layouts, and RecyclerViews.
- **State Management**: Using ViewModels to preserve data during device rotations and configuration changes.
- **Data Storage Fundamentals**: Preparing for local SQLite database integration using Room.
- **Fragment Communication**: Building the foundation for fragment navigation and effective data transfer between fragments.
- **Concurrency and Responsiveness**: Understanding the basics of Kotlin Coroutines for asynchronous task management.
- **Interactive Features**: Exploring swipe gestures, implicit intents, and image capture (planned for future parts).
- **Dialogs**: Learning to use dialog boxes for interactive user interfaces and message display.
